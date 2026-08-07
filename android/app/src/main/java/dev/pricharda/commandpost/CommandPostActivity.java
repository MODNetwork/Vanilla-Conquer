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

    /**
     * Static handle for the JNI callback below. The engine has no reference to
     * the activity instance, and only one game activity exists at a time.
     */
    private static CommandPostActivity sInstance = null;

    /**
     * D-32. Called from common/video_sdl2.cpp when the engine enters or leaves
     * gameplay, so the command bar can hide itself over menus, movies, score
     * and debrief screens.
     *
     * Arrives on the SDL thread, so the view work is posted to the UI thread -
     * touching a View off the main thread is undefined behaviour and would
     * eventually crash rather than fail visibly.
     */
    public static void nativeSetInGame(final boolean inGame) {
        final CommandPostActivity self = sInstance;
        if (self == null) {
            return;
        }

        self.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (self.commandBar != null) {
                    self.commandBar.setInGame(inGame);
                }
            }
        });
    }

    /**
     * D-41. Called from common/video_sdl2.cpp when D-pad left is pressed on a
     * controller, to open or close OUR command bar - the overlay built in this
     * project, not the game's own build sidebar. The two are separate things
     * on separate buttons.
     *
     * Same threading rule as nativeSetInGame: this arrives on the SDL thread
     * and every View touch is posted to the UI thread.
     */
    public static void nativeToggleCommandBar() {
        final CommandPostActivity self = sInstance;
        if (self == null) {
            return;
        }

        self.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (self.commandBar != null) {
                    self.commandBar.togglePanel();
                }
            }
        });
    }

    /**
     * Which engine this activity was asked to load.
     *
     * Whitelisted rather than trusted: an unrecognised value would otherwise
     * reach System.loadLibrary() and crash with a confusing UnsatisfiedLinkError
     * instead of falling back cleanly.
     */
    private String selectedEngine() {
        final Intent intent = getIntent();
        if (intent != null) {
            final String requested = intent.getStringExtra(EXTRA_ENGINE);
            if ("vanillatd".equals(requested) || "vanillara".equals(requested)) {
                return requested;
            }
        }
        return DEFAULT_ENGINE;
    }

    @Override
    protected String[] getLibraries() {
        // getLibraries() is called by SDLActivity during onCreate, before
        // super.onCreate() completes, so getIntent() is already available.
        return new String[] {
            "SDL2",
            selectedEngine()
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
        sInstance = this;

        if (commandBar == null) {
            final android.view.View layout = getContentView();
            if (layout instanceof android.view.ViewGroup) {
                // D-33: the bar needs to know which engine is loaded. FORM and
                // QUEUE exist in Red Alert but not Tiberian Dawn, and are
                // dimmed rather than removed.
                commandBar = CommandBarView.attach(
                    this, (android.view.ViewGroup) layout, selectedEngine());
            }
        }
    }

    @Override
    protected void onDestroy() {
        // Drop the static reference so a JNI callback arriving during teardown
        // cannot touch a dead activity.
        if (sInstance == this) {
            sInstance = null;
        }
        super.onDestroy();
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
