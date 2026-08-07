//
// Copyright 2020 Electronic Arts Inc.
//
// TiberianDawn.DLL and RedAlert.dll and corresponding source code is free
// software: you can redistribute it and/or modify it under the terms of
// the GNU General Public License as published by the Free Software Foundation,
// either version 3 of the License, or (at your option) any later version.

// TiberianDawn.DLL and RedAlert.dll and corresponding source code is distributed
// in the hope that it will be useful, but with permitted additional restrictions
// under Section 7 of the GPL. See the GNU General Public License in LICENSE.TXT
// distributed with this program. You should have received a copy of the
// GNU General Public License along with permitted additional restrictions
// with this program. If not, see https://github.com/electronicarts/CnC_Remastered_Collection

#include "macros.h"
#include "wwkeyboard_sdl2.h"
#include "video.h"
#include "sdl_keymap.h"
#include "settings.h"
#include <cmath>
#include <SDL.h>
#ifdef __ANDROID__
#include "debugstring.h"
#endif

void Focus_Loss();
void Focus_Restore();
void Process_Network();

WWKeyboardClassSDL2::~WWKeyboardClassSDL2()
{
}

#ifdef __ANDROID__
/*
** Resolve a still-unclassified finger into PAN once it has been held long
** enough. This must run per frame, not only on motion: a finger held perfectly
** still generates no FINGERMOTION events, so a motion-driven check alone would
** never arm panning.
*/
void WWKeyboardClassSDL2::Update_Touch_Gesture()
{
    if (TouchMode != TOUCH_PENDING) {
        return;
    }

    if (SDL_GetTicks() - TouchStartMs >= TOUCH_PAN_HOLD_MS) {
        TouchMode = TOUCH_PAN;
        DBG_INFO("TOUCH: armed PAN (held %ums)", (unsigned)(SDL_GetTicks() - TouchStartMs));
    }
}

/*
** Report how long the scroll request has been continuously active. If the view
** stops moving while this keeps climbing, the platform layer is doing its job
** and the limit is inside the engine (scenario bounds or shroud), not here.
*/
void WWKeyboardClassSDL2::Log_Pan_State()
{
    if (TouchMode != TOUCH_PAN) {
        PanFrameCount = 0;
        return;
    }

    ++PanFrameCount;

    if ((PanFrameCount % 30) == 0) {
        DBG_INFO("TOUCH: PAN active frame %u  scrollActive=%d dir=%u",
                 (unsigned)PanFrameCount,
                 (int)AnalogScrollActive,
                 (unsigned)ScrollDirection);
    }
}

void WWKeyboardClassSDL2::Handle_Finger_Event(const SDL_TouchFingerEvent& finger, uint32_t type)
{
    int out_w = 0, out_h = 0;
    Get_Video_Output_Size(out_w, out_h);

    const int win_x = (int)(finger.x * (float)out_w);
    const int win_y = (int)(finger.y * (float)out_h);

    if (type == SDL_FINGERDOWN) {
        ++TouchFingerCount;

        /*
        ** A second finger landing while the first is still unclassified is a
        ** two-finger tap: the gesture bound to ESC. It is claimed here, before
        ** the first finger has resolved into a tap, so no stray click is ever
        ** dispatched underneath it.
        **
        ** Deliberately NOT claimed once the first finger has become a DRAG or
        ** a PAN - interrupting a marquee selection or a map scroll because a
        ** palm brushed the screen would be worse than doing nothing.
        */
        if (TouchMode == TOUCH_PENDING && TouchFingerCount == 2) {
            TouchMode = TOUCH_TWOFINGER;
            TwoFingerFired = false;
            DBG_INFO("TOUCH: -> TWOFINGER");
            return;
        }

        /*
        ** Only the first finger drives the cursor. Ignoring later fingers
        ** stops a stray palm or second thumb from yanking the cursor away
        ** mid-gesture.
        */
        if (TouchMode != TOUCH_IDLE) {
            return;
        }

        TouchFinger = finger.fingerId;
        TouchOriginX = win_x;
        TouchOriginY = win_y;
        TouchStartMs = SDL_GetTicks();
        TouchMode = TOUCH_PENDING;
        PanEngaged = false;

        // Cursor follows immediately so the player sees where they are aiming,
        // but no click is dispatched until the gesture is classified.
        Set_Video_Mouse_Absolute(win_x, win_y);
        return;
    }

    /*
    ** Two-finger handling sits BEFORE the single-finger id filter on purpose:
    ** the second finger's id is deliberately not TouchFinger, so the filter
    ** below would discard its lift and the gesture would never complete.
    */
    if (TouchMode == TOUCH_TWOFINGER) {
        if (type == SDL_FINGERUP) {
            if (TouchFingerCount > 0) {
                --TouchFingerCount;
            }

            // Fire once, on the first lift. Which finger leaves first is not
            // something the player controls or thinks about.
            if (!TwoFingerFired) {
                Put_Key_Message(SDL_SCANCODE_ESCAPE, false);
                Put_Key_Message(SDL_SCANCODE_ESCAPE, true);
                TwoFingerFired = true;
                DBG_INFO("TOUCH: TWOFINGER -> ESC");
            }

            // Stay in this state until the screen is actually clear, so the
            // trailing finger cannot be misread as the start of a new gesture.
            if (TouchFingerCount <= 0) {
                TouchFingerCount = 0;
                TouchMode = TOUCH_IDLE;
                TouchFinger = 0;
            }
        }

        // Motion during a two-finger tap moves nothing and scrolls nothing.
        return;
    }

    if (finger.fingerId != TouchFinger) {
        // Keep the count honest even for fingers this layer ignores, or the
        // next two-finger tap would be counted against a stale total.
        if (type == SDL_FINGERUP && TouchFingerCount > 0) {
            --TouchFingerCount;
        }
        return;
    }

    const int dx = win_x - TouchOriginX;
    const int dy = win_y - TouchOriginY;
    const int moved_sq = dx * dx + dy * dy;

    if (type == SDL_FINGERMOTION) {
        switch (TouchMode) {
        case TOUCH_PENDING:
            if (moved_sq > TOUCH_SLOP_PX * TOUCH_SLOP_PX) {
                /*
                ** Moved before the hold elapsed: this is a click-drag. Replay
                ** the press at the ORIGIN so marquee selection boxes start
                ** where the finger landed, not where it has already reached.
                */
                int gx, gy;
                TouchMode = TOUCH_DRAG;
                Set_Video_Mouse_Absolute(TouchOriginX, TouchOriginY);
                Get_Video_Mouse_Game(gx, gy);
                Put_Mouse_Message(VK_LBUTTON, gx, gy, false);
                DBG_INFO("TOUCH: -> DRAG from %d,%d", gx, gy);

                Set_Video_Mouse_Absolute(win_x, win_y);
            }
            break;

        case TOUCH_DRAG:
            Set_Video_Mouse_Absolute(win_x, win_y);
            break;

        case TOUCH_PAN:
            /*
            ** Drive the engine's existing analog-scroll hook (built for
            ** gamepad sticks, consumed in ScrollClass::AI). No game logic is
            ** touched: the engine already asks the platform layer whether to
            ** scroll and in which direction.
            **
            ** Direction is the slide vector from the anchor. Sliding right
            ** scrolls the view right, so the map moves under a stationary
            ** finger in the natural direction of travel.
            */
            if (moved_sq > TOUCH_PAN_DEADZONE_PX * TOUCH_PAN_DEADZONE_PX) {
                AnalogScrollActive = true;

                // Latched, not cleared when the finger returns inside the
                // deadzone: once this hold has scrolled the map it is a pan for
                // the rest of its life, and lifting it must not fire a cancel.
                PanEngaged = true;

                /*
                ** TouchPanInvert flips to a grab-the-map feel: sliding right
                ** drags the map right, so the view travels left. Purely
                ** preference; set in CONQUER.INI, no rebuild.
                */
                const int pdx = Settings.Video.TouchPanInvert ? -dx : dx;
                const int pdy = Settings.Video.TouchPanInvert ? -dy : dy;

                if (abs(pdx) > 2 * abs(pdy)) {
                    ScrollDirection = (pdx > 0) ? SDIR_E : SDIR_W;
                } else if (abs(pdy) > 2 * abs(pdx)) {
                    ScrollDirection = (pdy > 0) ? SDIR_S : SDIR_N;
                } else if (pdx > 0) {
                    ScrollDirection = (pdy > 0) ? SDIR_SE : SDIR_NE;
                } else {
                    ScrollDirection = (pdy > 0) ? SDIR_SW : SDIR_NW;
                }
            } else {
                AnalogScrollActive = false;
            }
            break;

        default:
            break;
        }
        return;
    }

    if (type == SDL_FINGERUP) {
        int gx, gy;

        switch (TouchMode) {
        case TOUCH_PENDING:
            // Lifted before classifying and without travelling: a plain tap.
            Set_Video_Mouse_Absolute(win_x, win_y);
            Get_Video_Mouse_Game(gx, gy);
            Put_Mouse_Message(VK_LBUTTON, gx, gy, false);
            Put_Mouse_Message(VK_LBUTTON, gx, gy, true);
            DBG_INFO("TOUCH: TAP at %d,%d", gx, gy);
            break;

        case TOUCH_DRAG:
            Set_Video_Mouse_Absolute(win_x, win_y);
            Get_Video_Mouse_Game(gx, gy);
            Put_Mouse_Message(VK_LBUTTON, gx, gy, true);
            DBG_INFO("TOUCH: DRAG end at %d,%d", gx, gy);
            break;

        case TOUCH_PAN:
            AnalogScrollActive = false;
            ScrollDirection = SDIR_NONE;

            if (PanEngaged) {
                DBG_INFO("TOUCH: PAN end");
            } else {
                /*
                ** Held past the pan threshold but never slid: a long press.
                ** Emits a right-click, which DisplayClass::Mouse_Right_Press
                ** turns into the appropriate cancel - building placement,
                ** repair mode, sell mode, targeting mode, or deselect - by its
                ** own priority chain. The platform layer picks none of that; it
                ** only delivers the button the engine already knows how to
                ** interpret.
                **
                ** Dispatched at the ORIGIN rather than the lift point. They are
                ** within the deadzone of each other by definition, and the
                ** origin is where the player actually aimed.
                */
                Set_Video_Mouse_Absolute(TouchOriginX, TouchOriginY);
                Get_Video_Mouse_Game(gx, gy);
                Put_Mouse_Message(VK_RBUTTON, gx, gy, false);
                Put_Mouse_Message(VK_RBUTTON, gx, gy, true);
                DBG_INFO("TOUCH: LONG-PRESS -> RIGHT-CLICK at %d,%d", gx, gy);
            }
            break;

        default:
            break;
        }

        if (TouchFingerCount > 0) {
            --TouchFingerCount;
        }

        TouchMode = TOUCH_IDLE;
        TouchFinger = 0;
    }
}
#endif

void WWKeyboardClassSDL2::Fill_Buffer_From_System(void)
{
#ifdef __ANDROID__
    Update_Touch_Gesture();
    Log_Pan_State();
#endif
#ifdef NETWORKING
    Process_Network();
#endif
    SDL_Event event;

    while (!Is_Buffer_Full() && SDL_PollEvent(&event)) {
        unsigned short key;
        switch (event.type) {
        case SDL_QUIT:
            exit(0);
            break;
        case SDL_KEYDOWN:
#ifdef __ANDROID__
            // Instrumentation for the D-31 command bar audit. Every button on
            // that bar arrives here as an ordinary SDL_KEYDOWN, so this is the
            // one place that can distinguish "the key never arrived" from "the
            // key arrived and the engine ignored it" - two failures that look
            // identical from the player's side.
            DBG_INFO("KEY: scancode=%d sym=%d mod=0x%04X",
                     (int)event.key.keysym.scancode,
                     (int)event.key.keysym.sym,
                     (unsigned)event.key.keysym.mod);
#endif
            Put_Key_Message(event.key.keysym.scancode, false);
            break;
        case SDL_KEYUP:
            if (event.key.keysym.scancode == SDL_SCANCODE_RETURN && Down(VK_MENU)) {
                Toggle_Video_Fullscreen();
            } else {
                Put_Key_Message(event.key.keysym.scancode, true);
            }
            break;
#ifdef __ANDROID__
        /*
        ** Touch, handled at the source rather than via synthesised mouse
        ** events. SDL_TouchFingerEvent carries normalised 0..1 coordinates
        ** relative to the window, plus a fingerId - both required by the
        ** Phase 5 gestures (drag-box, long-press, two-finger pan).
        **
        ** The cursor is placed on FINGERDOWN before the click is dispatched,
        ** and tracked on FINGERMOTION so dragging follows the finger.
        */
        case SDL_FINGERDOWN:
        case SDL_FINGERMOTION:
        case SDL_FINGERUP:
            Handle_Finger_Event(event.tfinger, event.type);
            break;
#endif
        case SDL_MOUSEMOTION:
#ifdef __ANDROID__
            /*
            ** Touch is absolute. SDL reports it as mouse motion with a
            ** synthesised xrel/yrel, and feeding that to the relative
            ** accumulator moves the cursor BY the distance to the finger
            ** rather than TO it - so it drifts further away on every tap.
            ** Real pointing devices (a mouse over USB/BT) keep relative.
            */
            DBG_INFO("TOUCHDBG motion which=%u (TOUCH=%u) x=%d y=%d xrel=%d yrel=%d",
                     (unsigned)event.motion.which,
                     (unsigned)SDL_TOUCH_MOUSEID,
                     event.motion.x,
                     event.motion.y,
                     event.motion.xrel,
                     event.motion.yrel);

            if (event.motion.which == SDL_TOUCH_MOUSEID) {
                Set_Video_Mouse_Absolute(event.motion.x, event.motion.y);
                break;
            }
#endif
            Move_Video_Mouse(static_cast<float>(event.motion.xrel), static_cast<float>(event.motion.yrel));
            break;
        case SDL_MOUSEBUTTONDOWN:
        case SDL_MOUSEBUTTONUP: {
            int x, y;

#ifdef __ANDROID__
            /*
            ** A tap can arrive as a button event without any preceding motion,
            ** so anchor the cursor to the touch point before the click is
            ** dispatched. Otherwise the press registers wherever the cursor
            ** happened to be left.
            */
            if (event.button.which == SDL_TOUCH_MOUSEID) {
                Set_Video_Mouse_Absolute(event.button.x, event.button.y);
            }
#endif

            switch (event.button.button) {
            case SDL_BUTTON_LEFT:
            default:
                key = VK_LBUTTON;
                break;
            case SDL_BUTTON_RIGHT:
                key = VK_RBUTTON;
                break;
            case SDL_BUTTON_MIDDLE:
                key = VK_MBUTTON;
                break;
            }

#ifdef __ANDROID__
            /*
            ** For touch, Set_Video_Mouse_Absolute above already placed the
            ** cursor in game coordinates via render_dst. Report that position
            ** directly. The scale-divide branch below is wrong here because it
            ** ignores render_dst's letterbox offset, so in native/boxed mode
            ** every click would be displaced by the width of the black bar.
            */
            if (event.button.which == SDL_TOUCH_MOUSEID) {
                Get_Video_Mouse_Game(x, y);
            } else
#endif
            if (Settings.Mouse.RawInput || Is_Gamepad_Active()) {
                Get_Video_Mouse(x, y);
            } else {
                float scale_x = 1.0f, scale_y = 1.0f;
                Get_Video_Scale(scale_x, scale_y);
                x = event.button.x / scale_x;
                y = event.button.y / scale_y;
            }

            Put_Mouse_Message(key, x, y, event.type == SDL_MOUSEBUTTONDOWN ? false : true);
        } break;
        case SDL_WINDOWEVENT:
            switch (event.window.event) {
            case SDL_WINDOWEVENT_EXPOSED:
            case SDL_WINDOWEVENT_RESTORED:
            case SDL_WINDOWEVENT_FOCUS_GAINED:
                Focus_Restore();
                break;
            case SDL_WINDOWEVENT_HIDDEN:
            case SDL_WINDOWEVENT_MINIMIZED:
            case SDL_WINDOWEVENT_FOCUS_LOST:
                Focus_Loss();
                break;
#ifdef __ANDROID__
            /*
            ** Display geometry is not stable on Android. It changes during
            ** startup as system insets settle, on rotation, and on this target
            ** device every time the fold opens or closes (D-14). Anything that
            ** cached a size at init is wrong from here on, so recompute the
            ** video scaling whenever the surface changes.
            **
            ** NOTE: this is a window-event SUBTYPE, tested against
            ** event.window.event. The reference port tests it against
            ** event.type, where it can never match, and additionally compiles
            ** the whole SDL_WINDOWEVENT case out on Android - which is why its
            ** window never resizes when the device is unfolded.
            */
            case SDL_WINDOWEVENT_SIZE_CHANGED:
            case SDL_WINDOWEVENT_RESIZED:
                Update_Video_Scaling();
                break;
#endif
            }
            break;
        case SDL_MOUSEWHEEL:
            if (event.wheel.y > 0) { // scroll up
                Put_Key_Message(VK_MOUSEWHEEL_UP, false);
            } else if (event.wheel.y < 0) { // scroll down
                Put_Key_Message(VK_MOUSEWHEEL_DOWN, false);
            }
            break;
        case SDL_CONTROLLERDEVICEREMOVED:
            if (GameController != nullptr) {
                const SDL_GameController* removedController = SDL_GameControllerFromInstanceID(event.jdevice.which);
                if (removedController == GameController) {
                    SDL_GameControllerClose(GameController);
                    GameController = nullptr;
                }
            }
            break;
        case SDL_CONTROLLERDEVICEADDED:
            if (GameController == nullptr) {
                GameController = SDL_GameControllerOpen(event.jdevice.which);
                // D-35: this is the hot-plug path - a pad connected after the
                // game was already running. Logged so a mid-session connect is
                // distinguishable from a startup one.
                DBG_INFO("CONTROLLER: hot-plugged '%s'.",
                         GameController && SDL_GameControllerName(GameController)
                             ? SDL_GameControllerName(GameController)
                             : "unnamed");
            }
            break;
        case SDL_CONTROLLERAXISMOTION:
            Handle_Controller_Axis_Event(event.caxis);
            break;
        case SDL_CONTROLLERBUTTONDOWN:
        case SDL_CONTROLLERBUTTONUP:
            Handle_Controller_Button_Event(event.cbutton);
            break;
        }
    }
    if (Is_Gamepad_Active()) {
        Process_Controller_Axis_Motion();
    }
}

bool WWKeyboardClassSDL2::Is_Gamepad_Active()
{
    return GameController != nullptr;
}

void WWKeyboardClassSDL2::Open_Controller()
{
    const int count = SDL_NumJoysticks();
    DBG_INFO("CONTROLLER: %d joystick device(s) present at startup.", count);

    for (int i = 0; i < count; ++i) {
        /*
        ** D-35: the decisive line for diagnosing a dead pad. Android can report
        ** a device as a GAMEPAD while SDL still refuses it, because SDL only
        ** raises SDL_CONTROLLERBUTTON events for devices it holds a
        ** game-controller MAPPING for. A pad with no mapping is silent no
        ** matter how correct the button code is, and the fix in that case is a
        ** mapping string - not anything in Handle_Controller_Button_Event.
        ** Logging which of the two happened turns an hour of guessing into one
        ** glance.
        */
        if (SDL_IsGameController(i)) {
            const char* name = SDL_GameControllerNameForIndex(i);
            GameController = SDL_GameControllerOpen(i);
            DBG_INFO("CONTROLLER: opened '%s' as a game controller.", name ? name : "unnamed");
        } else {
            const char* name = SDL_JoystickNameForIndex(i);
            DBG_WARN("CONTROLLER: '%s' has no SDL mapping - its buttons cannot reach the game.",
                     name ? name : "unnamed");
        }
    }
}

void WWKeyboardClassSDL2::Close_Controller()
{
    if (SDL_GameControllerGetAttached(GameController)) {
        SDL_GameControllerClose(GameController);
        GameController = nullptr;
    }
}

void WWKeyboardClassSDL2::Process_Controller_Axis_Motion()
{
    const uint32_t currentTime = SDL_GetTicks();
    const float deltaTime = currentTime - LastControllerTime;
    LastControllerTime = currentTime;

    if (ControllerLeftXAxis != 0 || ControllerLeftYAxis != 0) {
        const int16_t xSign = (ControllerLeftXAxis > 0) - (ControllerLeftXAxis < 0);
        const int16_t ySign = (ControllerLeftYAxis > 0) - (ControllerLeftYAxis < 0);

        float movX = std::pow(std::abs(ControllerLeftXAxis), CONTROLLER_AXIS_SPEEDUP) * xSign * deltaTime
                     * Settings.Mouse.ControllerPointerSpeed / CONTROLLER_SPEED_MOD * ControllerSpeedBoost;
        float movY = std::pow(std::abs(ControllerLeftYAxis), CONTROLLER_AXIS_SPEEDUP) * ySign * deltaTime
                     * Settings.Mouse.ControllerPointerSpeed / CONTROLLER_SPEED_MOD * ControllerSpeedBoost;

        Move_Video_Mouse(movX, movY);
    }
}

void WWKeyboardClassSDL2::Handle_Controller_Axis_Event(const SDL_ControllerAxisEvent& motion)
{
    AnalogScrollActive = false;
    ScrollDirType directionX = SDIR_NONE;
    ScrollDirType directionY = SDIR_NONE;

    if (motion.axis == SDL_CONTROLLER_AXIS_LEFTX) {
        if (std::abs(motion.value) > CONTROLLER_L_DEADZONE)
            ControllerLeftXAxis = motion.value;
        else
            ControllerLeftXAxis = 0;
    } else if (motion.axis == SDL_CONTROLLER_AXIS_LEFTY) {
        if (std::abs(motion.value) > CONTROLLER_L_DEADZONE)
            ControllerLeftYAxis = motion.value;
        else
            ControllerLeftYAxis = 0;
    } else if (motion.axis == SDL_CONTROLLER_AXIS_RIGHTX) {
        if (std::abs(motion.value) > CONTROLLER_R_DEADZONE)
            ControllerRightXAxis = motion.value;
        else
            ControllerRightXAxis = 0;
    } else if (motion.axis == SDL_CONTROLLER_AXIS_RIGHTY) {
        if (std::abs(motion.value) > CONTROLLER_R_DEADZONE)
            ControllerRightYAxis = motion.value;
        else
            ControllerRightYAxis = 0;
    } else if (motion.axis == SDL_CONTROLLER_AXIS_TRIGGERRIGHT) {
        if (std::abs(motion.value) > CONTROLLER_TRIGGER_R_DEADZONE)
            ControllerSpeedBoost = 1 + (static_cast<float>(motion.value) / 32767) * CONTROLLER_TRIGGER_SPEEDUP;
        else
            ControllerSpeedBoost = 1;
    } else if (motion.axis == SDL_CONTROLLER_AXIS_TRIGGERLEFT) {
        /*
        ** D-35: left trigger is Ctrl - force fire, and the key that CREATES a
        ** team. It has to be HELD, not tapped: conquer.cpp reads the modifier
        ** through Keyboard->Down(), which is only true while the key sits in
        ** the down state, so a press/release pair around the real action is
        ** the only thing the engine will accept.
        **
        ** Hysteresis, not a plain threshold - see the header. Only edges are
        ** sent, so the key queue is not flooded while the trigger is held.
        */
        const bool held = LeftTriggerHeld ? (motion.value > CONTROLLER_TRIGGER_L_RELEASE)
                                          : (motion.value > CONTROLLER_TRIGGER_L_PRESS);
        if (held != LeftTriggerHeld) {
            LeftTriggerHeld = held;
            Put_Key_Message(SDL_SCANCODE_LCTRL, !held);
            DBG_INFO("CONTROLLER: Ctrl %s", held ? "down" : "up");
        }
    }

    if (ControllerRightXAxis != 0) {
        AnalogScrollActive = true;
        directionX = ControllerRightXAxis > 0 ? SDIR_E : SDIR_W;
    }
    if (ControllerRightYAxis != 0) {
        AnalogScrollActive = true;
        directionY = ControllerRightYAxis > 0 ? SDIR_S : SDIR_N;
    }

    if (directionX == SDIR_E && directionY == SDIR_N) {
        ScrollDirection = SDIR_NE;
    } else if (directionX == SDIR_E && directionY == SDIR_S) {
        ScrollDirection = SDIR_SE;
    } else if (directionX == SDIR_W && directionY == SDIR_N) {
        ScrollDirection = SDIR_NW;
    } else if (directionX == SDIR_W && directionY == SDIR_S) {
        ScrollDirection = SDIR_SW;
    } else if (directionX == SDIR_E) {
        ScrollDirection = SDIR_E;
    } else if (directionX == SDIR_W) {
        ScrollDirection = SDIR_W;
    } else if (directionY == SDIR_S) {
        ScrollDirection = SDIR_S;
    } else if (directionY == SDIR_N) {
        ScrollDirection = SDIR_N;
    }
}

/***********************************************************************************************
 * D-35: controller button map, ruled by Michael for the Nacon MGX Pro.
 *
 * THIS DOES NOT REPLACE TOUCH. Touch arrives on SDL_FINGER* events, controller
 * on SDL_CONTROLLER*; neither path knows the other exists and both stay live.
 * They also share one cursor - hwcursor - so a tap places it and the left stick
 * nudges it from wherever the tap left it. Plug in, it works; unplug, touch is
 * untouched. Is_Gamepad_Active() is the whole of the "detect and adapt".
 *
 * WHERE THIS DEPARTS FROM MICHAEL'S LAYOUT, and why. Two commands he assigned
 * over had no other route on a controller, so they moved to inputs that were
 * sitting unused rather than being dropped:
 *
 *   B stays RIGHT-CLICK, at his explicit approval. display.cpp:3345 makes it a
 *   five-way cancel - deselect, exit repair, exit sell, cancel placement,
 *   cancel targeting. Nothing else reaches any of it. "Base" moved to L3.
 *
 *   Ctrl and Alt came off LB/RB - which he wanted for prev/next - onto the left
 *   trigger and R3. Ctrl is not optional: conquer.cpp Handle_Team makes it the
 *   key that CREATES a team, so without it a controller could not build one at
 *   all. Alt force-moves and recalls.
 *
 *   BACK is sidebar-scroll-UP. This is the one addition beyond his spec, and it
 *   is flagged for his ruling: he asked for D-pad down to "open sidebar", but
 *   the sidebar in both titles is permanently on screen and UP/DOWN only scroll
 *   the build list. A scroll-down with no scroll-up is half a control.
 *
 * Scancodes are the ones already proven by the on-screen command bar, not
 * re-derived. Both routes end at Put_Key_Message, and wwkeyboard.h shows the
 * engine's KN_ codes ARE the SDL scancodes, so no translation can go wrong.
 *=============================================================================*/
void WWKeyboardClassSDL2::Handle_Controller_Button_Event(const SDL_ControllerButtonEvent& button)
{
    bool keyboardPress = false;
    bool mousePress = false;
    unsigned short key;
    SDL_Scancode scancode;

    // D-36: gameplay or not. Drives the D-pad only; every other button means
    // the same thing everywhere.
    const bool in_game = Get_Video_Cursor_Clip();

    switch (button.button) {
    case SDL_CONTROLLER_BUTTON_A:
        mousePress = true;
        key = VK_LBUTTON;
        break;
    case SDL_CONTROLLER_BUTTON_B:
        mousePress = true;
        key = VK_RBUTTON;
        break;
    case SDL_CONTROLLER_BUTTON_X:
        keyboardPress = true;
        scancode = SDL_SCANCODE_X; // scatter
        break;
    case SDL_CONTROLLER_BUTTON_Y:
        keyboardPress = true;
        scancode = SDL_SCANCODE_G; // guard
        break;
    case SDL_CONTROLLER_BUTTON_START:
        keyboardPress = true;
        scancode = SDL_SCANCODE_ESCAPE;
        break;
    case SDL_CONTROLLER_BUTTON_BACK:
        keyboardPress = true;
        scancode = SDL_SCANCODE_E; // select everything on screen
        break;
    case SDL_CONTROLLER_BUTTON_LEFTSHOULDER:
        keyboardPress = true;
        scancode = SDL_SCANCODE_B; // previous unit
        break;
    case SDL_CONTROLLER_BUTTON_RIGHTSHOULDER:
        keyboardPress = true;
        scancode = SDL_SCANCODE_N; // next unit
        break;
    case SDL_CONTROLLER_BUTTON_LEFTSTICK:
        keyboardPress = true;
        scancode = SDL_SCANCODE_H; // centre view on base
        break;
    case SDL_CONTROLLER_BUTTON_RIGHTSTICK:
        keyboardPress = true;
        scancode = SDL_SCANCODE_LALT; // force move, and recall team
        break;
    /*
    ** D-36: the D-pad is the one control whose meaning depends on where you
    ** are. Outside a mission it drives the engine's own menu navigation, which
    ** already exists and is fully keyboard-driven - menus.cpp:254/259/290 map
    ** KN_UP, KN_DOWN and KN_RETURN. Nothing had to be written for that; the
    ** arrows simply were not being sent. In a mission the arrows would be
    ** wasted, so it carries sidebar scroll and the first two team slots.
    **
    ** in_game comes from Get_Video_Cursor_Clip - the same signal D-32 already
    ** uses to hide the on-screen bar outside gameplay, and which Michael has
    ** already passed on that behaviour.
    */
    case SDL_CONTROLLER_BUTTON_DPAD_UP:
        keyboardPress = true;
        scancode = in_game ? SDL_SCANCODE_UP    // sidebar scroll up
                           : SDL_SCANCODE_UP;   // menu: move selection up
        break;
    case SDL_CONTROLLER_BUTTON_DPAD_DOWN:
        keyboardPress = true;
        scancode = in_game ? SDL_SCANCODE_DOWN  // sidebar scroll down
                           : SDL_SCANCODE_DOWN; // menu: move selection down
        break;
    case SDL_CONTROLLER_BUTTON_DPAD_LEFT:
        keyboardPress = true;
        scancode = in_game ? SDL_SCANCODE_1     // team 1
                           : SDL_SCANCODE_LEFT; // menu: difficulty, sliders
        break;
    case SDL_CONTROLLER_BUTTON_DPAD_RIGHT:
        keyboardPress = true;
        scancode = in_game ? SDL_SCANCODE_2      // team 2
                           : SDL_SCANCODE_RIGHT; // menu: difficulty, sliders
        break;
    default:
        break;
    }

    /*
    ** D-36: log every press. This is what turns "that button did nothing" into
    ** a one-glance answer, and it distinguishes the three things that look
    ** identical from the outside: the button never arrived (no line at all),
    ** it arrived and sent the wrong key (line shows the wrong scancode), or it
    ** sent the right key and the engine ignored it (line is correct).
    ** Presses only - a release line for every press would double the noise and
    ** tell us nothing extra.
    */
    if (button.state == SDL_PRESSED) {
        DBG_INFO("CONTROLLER: button %d -> %s %d (in_game=%s)",
                 button.button,
                 keyboardPress ? "scancode" : (mousePress ? "mousebtn" : "UNMAPPED"),
                 keyboardPress ? (int)scancode : (mousePress ? (int)key : -1),
                 in_game ? "yes" : "no");
    }

    if (keyboardPress) {
        Put_Key_Message(scancode, button.state == SDL_RELEASED);
    } else if (mousePress) {
        int x, y;
        Get_Video_Mouse(x, y);
        Put_Mouse_Message(key, x, y, button.state == SDL_RELEASED);
    }
}

bool WWKeyboardClassSDL2::Is_Analog_Scroll_Active()
{
    return AnalogScrollActive;
}

unsigned char WWKeyboardClassSDL2::Get_Scroll_Direction()
{
    return ScrollDirection;
}

KeyASCIIType WWKeyboardClassSDL2::To_ASCII(unsigned short key)
{
    if (key & WWKEY_RLS_BIT) {
        return KA_NONE;
    }

    key &= 0xFF; // drop all mods

    if (key > ARRAY_SIZE(sdl_keymap) / 2 - 1) {
        return KA_NONE;
    }

    if (SDL_GetModState() & KMOD_SHIFT) {
        return sdl_keymap[key + ARRAY_SIZE(sdl_keymap) / 2];
    } else {
        return sdl_keymap[key];
    }
}

WWKeyboardClass* CreateWWKeyboardClass(void)
{
    return new WWKeyboardClassSDL2;
}
