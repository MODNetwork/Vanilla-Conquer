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
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        // Re-apply on every focus gain. The bars come back after a swipe, a
        // notification, or returning from the recents switcher, and without
        // this they would stay visible for the rest of the session.
        if (hasFocus) {
            applyImmersiveMode();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyImmersiveMode();
    }
}
