package dev.pricharda.vc95;

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
public class VC95Activity extends SDLActivity {

    @Override
    protected String[] getLibraries() {
        return new String[] {
            "SDL2",
            "vanillatd"
        };
    }
}
