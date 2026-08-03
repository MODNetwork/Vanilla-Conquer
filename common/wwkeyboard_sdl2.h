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
        CONTROLLER_TRIGGER_R_DEADZONE = 3000
    };

    SDL_GameController* GameController = nullptr;
    int16_t ControllerLeftXAxis = 0;
    int16_t ControllerLeftYAxis = 0;
    int16_t ControllerRightXAxis = 0;
    int16_t ControllerRightYAxis = 0;
    uint32_t LastControllerTime = 0;
    float ControllerSpeedBoost = 1;
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
        TOUCH_IDLE = 0, // no finger down
        TOUCH_PENDING,  // down, not yet classified
        TOUCH_DRAG,     // moved early -> click-drag (select, marquee, orders)
        TOUCH_PAN       // held still, then moved -> map scroll
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

    uint32_t PanFrameCount = 0;

    void Handle_Finger_Event(const SDL_TouchFingerEvent& finger, uint32_t type);
    void Update_Touch_Gesture();
    void Log_Pan_State();
#endif
};
