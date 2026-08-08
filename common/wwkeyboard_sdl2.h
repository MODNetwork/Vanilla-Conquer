#pragma once
#include "wwkeyboard.h"

class WWKeyboardClassSDL2 : public WWKeyboardClass
{
public:
    virtual ~WWKeyboardClassSDL2();

    virtual void Fill_Buffer_From_System(void);
    virtual bool Is_Gamepad_Active();
    virtual void Open_Controller();
    virtual void Close_Controller();
    virtual bool Is_Analog_Scroll_Active();
    virtual unsigned char Get_Scroll_Direction();
    virtual KeyASCIIType To_ASCII(unsigned short key);

private:
    void Handle_Controller_Axis_Event(const SDL_ControllerAxisEvent& motion);
    void Handle_Controller_Button_Event(const SDL_ControllerButtonEvent& button);
    void Process_Controller_Axis_Motion();

    // used to convert user-friendly pointer speed values into more useable ones
    static constexpr float CONTROLLER_SPEED_MOD = 2000000.0f;
    // bigger value correndsponds to faster pointer movement speed with bigger stick axis values
    static constexpr float CONTROLLER_AXIS_SPEEDUP = 1.03f;
    // speedup value while the trigger is pressed
    static constexpr int CONTROLLER_TRIGGER_SPEEDUP = 2;

    enum
    {
        CONTROLLER_L_DEADZONE = 4000,
        CONTROLLER_R_DEADZONE = 6000,
        CONTROLLER_TRIGGER_R_DEADZONE = 3000,
        /*
        ** D-35: the left trigger drives a HELD modifier key rather than a
        ** one-shot, so it needs a press point and a separate, lower release
        ** point. With a single threshold a trigger resting against it would
        ** chatter Ctrl down and up many times a second.
        */
        CONTROLLER_TRIGGER_L_PRESS = 16000,
        CONTROLLER_TRIGGER_L_RELEASE = 8000
    };

    SDL_GameController* GameController = nullptr;
    int16_t ControllerLeftXAxis = 0;
    int16_t ControllerLeftYAxis = 0;
    int16_t ControllerRightXAxis = 0;
    int16_t ControllerRightYAxis = 0;
    uint32_t LastControllerTime = 0;
    float ControllerSpeedBoost = 1;
    // D-35: latched state of the left trigger, read as Ctrl.
    bool LeftTriggerHeld = false;
    /*
    ** D-38: which navigation the player last used outside gameplay. The D-pad
    ** drives a keyboard highlight and the stick drives a cursor; these are
    ** separate systems and A has to resolve to one of them. Rather than guess,
    ** it follows whichever the player touched last. Stick or a screen tap means
    ** A clicks; D-pad means A confirms the highlight.
    */
    bool LastNavWasDpad = false;
    bool AnalogScrollActive = false;
    ScrollDirType ScrollDirection = SDIR_NONE;

#ifdef __ANDROID__
    /*
    ** Touch gesture classification.
    **
    ** Tiberian Dawn scrolls the map only when the cursor sits on the literal
    ** edge pixel of the play area. That is a one-pixel target in 640x400 game
    ** coordinates and is not reachable with a finger, so drag-to-edge panning
    ** is unusable on a phone regardless of implementation quality.
    **
    ** Instead: hold still briefly, then slide, to pan. The delay is what
    ** separates a pan from a drag-box - both are "finger down then move", and
    ** only elapsed-time-before-movement distinguishes them.
    **
    ** A click is therefore NOT dispatched on finger-down. It is withheld until
    ** the gesture resolves, which costs a few milliseconds of latency on taps
    ** and is imperceptible.
    */
    enum TouchState
    {
        TOUCH_IDLE = 0,  // no finger down
        TOUCH_PENDING,   // down, not yet classified
        TOUCH_DRAG,      // moved early -> click-drag (select, marquee, orders)
        TOUCH_PAN,       // held still, then moved -> map scroll
        TOUCH_TWOFINGER  // second finger landed -> ESC on lift
    };

    // Movement in window pixels below which a finger counts as "still".
    static constexpr int TOUCH_SLOP_PX = 24;
    // Hold this long without exceeding the slop to arm panning.
    static constexpr uint32_t TOUCH_PAN_HOLD_MS = 220;
    // Minimum slide from the anchor before panning actually starts.
    static constexpr int TOUCH_PAN_DEADZONE_PX = 20;

    TouchState TouchMode = TOUCH_IDLE;
    SDL_FingerID TouchFinger = 0;
    int TouchOriginX = 0; // window px, where the finger first landed
    int TouchOriginY = 0;
    uint32_t TouchStartMs = 0;

    /*
    ** Two-finger tap -> ESC.
    **
    ** ESC is not a movie-skip key, it is three things at once (verified in
    ** the engine source): it opens the in-game options dialog
    ** (conquer.cpp:709, Options.KeyOption1), it breaks out of a playing movie
    ** (conquer.cpp:2949), and it is Cancel in roughly thirty dialogs. One
    ** gesture therefore covers all three.
    **
    ** Two fingers rather than a double-tap: the engine deliberately degrades
    ** double-clicks into two ordinary single clicks
    ** ("Fake this into being just a rapid click of the left button twice",
    ** wwkeyboard.cpp:576), so Tiberian Dawn has no double-click semantics to
    ** collide with - but swallowing a double-tap WOULD break rapid tapping on
    ** the sidebar to queue builds. A two-finger tap has no conflict at all and
    ** needs no per-region exceptions.
    */
    int TouchFingerCount = 0;  // fingers currently down, clamped at zero
    bool TwoFingerFired = false;

    /*
    ** Long-press -> right-click.
    **
    ** DisplayClass::Mouse_Right_Press (display.cpp) is a five-way cancel chain,
    ** and every branch of it was unreachable on touch until this existed:
    **   PendingObjectPtr -> cancel building placement
    **   IsRepairMode     -> exit repair mode
    **   IsSellMode       -> exit sell mode
    **   IsTargettingMode -> exit ion cannon / airstrike targeting
    **   otherwise        -> Unselect_All()
    **
    ** Tapping empty ground cannot serve as "deselect": with units selected that
    ** is the move order, and every tile of the tactical map is a valid
    ** destination, so there is no inert area to tap.
    **
    ** Hold-still-then-lift was already a complete no-op - the gesture arms
    ** TOUCH_PAN, but with no slide the scroll never engages and FINGERUP emits
    ** nothing. So it is free, it collides with nothing, and long-press as a
    ** secondary action is the standard Android idiom.
    **
    ** PanEngaged distinguishes "held and panned" from "held and did not pan".
    ** Without it, ending a genuine map scroll would fire a spurious cancel.
    */
    bool PanEngaged = false;

    void Handle_Finger_Event(const SDL_TouchFingerEvent& finger, uint32_t type);
    void Update_Touch_Gesture();
#endif
};
