/*
 * Gate 2 skeleton for the Android port.
 *
 * Purpose: prove the full pipe end to end before any engine code is involved.
 * SDLActivity launches, a window and renderer are created, the screen is
 * cleared to a solid colour, and the app survives home/resume.
 *
 * Evidence tag is VCTD per CLAUDE.md. Capture with:
 *     adb logcat -s VCTD SDL -d > gate2_evidence.txt
 *
 * This file is replaced by the engine entry point at Phase 3.
 */

#include <SDL.h>
#include <android/log.h>

#define VCTD_TAG "VCTD"
#define VCLOG(...) __android_log_print(ANDROID_LOG_INFO, VCTD_TAG, __VA_ARGS__)
#define VCERR(...) __android_log_print(ANDROID_LOG_ERROR, VCTD_TAG, __VA_ARGS__)

int main(int argc, char *argv[])
{
    (void)argc;
    (void)argv;

    VCLOG("gate2: entry reached, SDL_Init starting");

    if (SDL_Init(SDL_INIT_VIDEO | SDL_INIT_EVENTS) != 0) {
        VCERR("gate2: SDL_Init FAILED: %s", SDL_GetError());
        return 1;
    }

    SDL_version linked;
    SDL_GetVersion(&linked);
    VCLOG("gate2: SDL linked version %d.%d.%d", linked.major, linked.minor, linked.patch);

    /* Writable runtime path. Phase 3 redirects config/saves/cache here. */
    const char *internal = SDL_AndroidGetInternalStoragePath();
    VCLOG("gate2: internal storage path = %s", internal ? internal : "(null)");

    SDL_Window *window = SDL_CreateWindow(
        "vc95",
        SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED,
        0, 0,
        SDL_WINDOW_FULLSCREEN | SDL_WINDOW_RESIZABLE);

    if (!window) {
        VCERR("gate2: SDL_CreateWindow FAILED: %s", SDL_GetError());
        SDL_Quit();
        return 1;
    }

    SDL_Renderer *renderer = SDL_CreateRenderer(window, -1, SDL_RENDERER_ACCELERATED);
    if (!renderer) {
        VCERR("gate2: SDL_CreateRenderer FAILED: %s", SDL_GetError());
        SDL_DestroyWindow(window);
        SDL_Quit();
        return 1;
    }

    int w = 0, h = 0;
    SDL_GetRendererOutputSize(renderer, &w, &h);
    VCLOG("gate2: renderer output size %dx%d", w, h);

    int running = 1;
    int frames = 0;

    while (running) {
        SDL_Event e;
        while (SDL_PollEvent(&e)) {
            switch (e.type) {
            case SDL_QUIT:
                VCLOG("gate2: SDL_QUIT received");
                running = 0;
                break;

            /* Lifecycle: these fire on home/resume. Gate 2 requires survival. */
            case SDL_APP_WILLENTERBACKGROUND:
                VCLOG("gate2: lifecycle WILLENTERBACKGROUND");
                break;
            case SDL_APP_DIDENTERBACKGROUND:
                VCLOG("gate2: lifecycle DIDENTERBACKGROUND");
                break;
            case SDL_APP_WILLENTERFOREGROUND:
                VCLOG("gate2: lifecycle WILLENTERFOREGROUND");
                break;
            case SDL_APP_DIDENTERFOREGROUND:
                VCLOG("gate2: lifecycle DIDENTERFOREGROUND");
                break;

            case SDL_WINDOWEVENT:
                /* Fold/unfold and rotation land here. See D-14: the target
                 * device is a foldable, so geometry must be re-read rather
                 * than cached. Phase 5 touch mapping depends on this. */
                if (e.window.event == SDL_WINDOWEVENT_SIZE_CHANGED) {
                    SDL_GetRendererOutputSize(renderer, &w, &h);
                    VCLOG("gate2: SIZE_CHANGED -> renderer output now %dx%d", w, h);
                }
                break;

            case SDL_FINGERDOWN:
                VCLOG("gate2: touch at normalised %.3f,%.3f", e.tfinger.x, e.tfinger.y);
                break;

            default:
                break;
            }
        }

        /* Solid colour clear. Distinctive on purpose so a PASS is unambiguous
         * and cannot be confused with a black screen failure. */
        SDL_SetRenderDrawColor(renderer, 0x1E, 0x6F, 0x50, 0xFF);
        SDL_RenderClear(renderer);
        SDL_RenderPresent(renderer);

        if (frames == 0) {
            VCLOG("gate2: first frame presented");
        }
        if (++frames % 300 == 0) {
            VCLOG("gate2: alive, %d frames presented", frames);
        }

        SDL_Delay(16);
    }

    VCLOG("gate2: shutting down cleanly after %d frames", frames);
    SDL_DestroyRenderer(renderer);
    SDL_DestroyWindow(window);
    SDL_Quit();
    return 0;
}
