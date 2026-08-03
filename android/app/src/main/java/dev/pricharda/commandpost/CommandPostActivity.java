package dev.pricharda.commandpost;

import android.os.Build;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;

import org.libsdl.app.SDLActivity;

/**
 * Android entry activity.
 *
 * SDLActivity's default getLibraries() returns {"SDL2", "main"}. The engine
 * builds as libvanillatd.so (tiberiandawn/CMakeLists.txt sets OUTPUT_NAME
 * vanillatd), so the load list is overridden here rather than renaming the
 * engine target - the target name is upstream's and stays untouched.
 *
 * D-13: the app is designed to host two titles. When Red Alert is enabled
 * post-Gate-7 this is the single place that selects which engine library
 * loads; nothing else in the platform layer assumes one game.
 */
public class CommandPostActivity extends SDLActivity {

    @Override
    protected String[] getLibraries() {
        return new String[] {
            "SDL2",
            "vanillatd"
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
