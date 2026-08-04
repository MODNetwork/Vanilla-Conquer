package dev.pricharda.commandpost;

import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import org.libsdl.app.SDLActivity;

/**
 * Android entry activity - hosts whichever engine the launcher selected.
 *
 * SDLActivity's default getLibraries() returns {"SDL2", "main"}. The engines
 * build as libvanillatd.so and libvanillara.so (each CMakeLists sets
 * OUTPUT_NAME), so the load list is overridden here rather than renaming the
 * engine targets - those names are upstream's and stay untouched.
 *
 * D-13/D-25: this is the single place that selects which engine library loads.
 * Nothing else in the platform layer assumes one game: the touch layer, audio,
 * paths, aspect handling and soft keyboard all live in common/ and are shared.
 *
 * IMPORTANT - why the title cannot be changed after launch. SDL loads the
 * engine with System.loadLibrary() and the engine then runs its own main loop
 * for the life of the process. A native library cannot be unloaded and
 * replaced, so switching titles requires a fresh process. LauncherActivity
 * therefore starts this activity with the chosen title, and returning to the
 * launcher kills the process rather than trying to swap engines in place.
 */
public class CommandPostActivity extends SDLActivity {

    /** Intent extra naming the engine library to load, without the lib prefix. */
    public static final String EXTRA_ENGINE = "dev.pricharda.commandpost.ENGINE";

    /** Used when the activity is started directly, e.g. by adb am start. */
    private static final String DEFAULT_ENGINE = "vanillatd";

    /** D-31 on-screen command bar. Null until onStart has attached it. */
    private CommandBarView commandBar = null;

    @Override
    protected String[] getLibraries() {
        // getLibraries() is called by SDLActivity during onCreate, before
        // super.onCreate() completes, so getIntent() is already available.
        String engine = DEFAULT_ENGINE;

        final Intent intent = getIntent();
        if (intent != null) {
            final String requested = intent.getStringExtra(EXTRA_ENGINE);

            // Whitelist rather than trust the extra. An unrecognised value would
            // otherwise reach System.loadLibrary() and crash with a confusing
            // UnsatisfiedLinkError instead of a clear fallback.
            if ("vanillatd".equals(requested) || "vanillara".equals(requested)) {
                engine = requested;
            }
        }

        return new String[] {
            "SDL2",
            engine
        };
    }

    /**
     * Hide the system status and navigation bars.
     *
     * The Theme.NoTitleBar.Fullscreen theme alone is not sufficient on
     * Android 11+: the status bar still draws over the top of the surface,
     * covering Tiberian Dawn's own top bar - which is where the in-game
     * options live, making them unreachable.
     *
     * Also relevant on a foldable: the display cutout must be allowed to be
     * drawn into, otherwise the system letterboxes around it and the play
     * area shrinks.
     */
    private void applyImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            final WindowInsetsController controller = getWindow().getInsetsController();

            if (controller != null) {
                controller.hide(WindowInsets.Type.systemBars());
                controller.setSystemBarsBehavior(
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // D-31: attach the on-screen command bar to SDL's own layout.
        //
        // Deferred to onStart rather than onCreate: SDLActivity builds mLayout
        // during its onCreate, and getContentView() returns null before that
        // has run. Attaching too early would silently do nothing.
        // getContentView() is declared as View, not ViewGroup, so the type is
        // checked rather than cast blindly - if SDL ever changes what it
        // returns, this degrades to "no command bar" instead of a crash.
        if (commandBar == null) {
            final android.view.View layout = getContentView();
            if (layout instanceof android.view.ViewGroup) {
                commandBar = CommandBarView.attach(this, (android.view.ViewGroup) layout);
            }
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Re-apply on every focus gain. The bars come back after a swipe, a
        // notification, or returning from the recents switcher, and without
        // this they would stay visible for the rest of the session.
        if (hasFocus) {
            applyImmersiveMode();
        } else if (commandBar != null) {
            // Drop any latched modifier on focus loss. A Shift left held
            // across a pause is still held on resume as far as the engine is
            // concerned, and the player would have no way to work out why
            // their clicks had stopped behaving normally.
            commandBar.releaseAllModifiers();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyImmersiveMode();
    }

    /**
     * Return to the Command Post title picker when the engine exits, instead of
     * dropping the user onto the home screen.
     *
     * D-26. This is the seam rather than a chosen convenience: when SDL_main
     * returns, SDLActivity's own SDLMain.run() calls mSingleton.finish()
     * (SDLActivity.java:1894). Overriding finish() therefore catches the
     * engine's exit by EVERY route - the main menu's own Exit item, the
     * engine's Prog_End() path, or an SDL_QUIT - without touching game logic or
     * duplicating a hook into both tiberiandawn/ and redalert/.
     *
     * Why the process is killed rather than reused. The engine library is
     * loaded with System.loadLibrary() and cannot be unloaded. Reusing this
     * process to launch the other title would mean loading libvanillara.so into
     * a process that already holds libvanillatd.so - two engines exporting
     * SDL_main, two sets of engine globals, and SDLActivity's static singleton
     * state left over from the previous run. LauncherActivity runs in its own
     * process (see AndroidManifest), so killing this one leaves the picker
     * untouched and guarantees the next title starts clean.
     */
    @Override
    public void finish() {
        // Guard against re-entry: finish() can be called more than once during
        // teardown, and launching the picker twice would stack it.
        if (!isFinishing()) {
            final Intent picker = new Intent(this, LauncherActivity.class);
            picker.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(picker);
        }

        super.finish();

        // Tear down this process so the loaded engine library goes with it.
        // Done after super.finish() and after the picker has been started, so
        // the picker - which lives in a different process - is unaffected.
        android.os.Process.killProcess(android.os.Process.myPid());
    }
}
