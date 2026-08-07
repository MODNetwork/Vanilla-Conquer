#include "wwstd.h"
#include "settings.h"
#include "ini.h"
#include "miscasm.h"

SettingsClass Settings;

SettingsClass::SettingsClass()
{
    /*
    ** Mouse settings
    */
    Mouse.RawInput = true;
    Mouse.Sensitivity = 100;
#ifdef __ANDROID__
    /*
    ** D-35: a phone controller is a plug-in accessory, so the gamepad subsystem
    ** has to be up before one ever appears. This flag gates
    ** SDL_Init(SDL_INIT_GAMECONTROLLER) in video_sdl2.cpp - left false, no
    ** controller event of any kind is delivered, so nothing else in the
    ** controller path can work.
    **
    ** Safe to force on, verified rather than assumed: the only other thing this
    ** flag touches is SDL_SetRelativeMouseMode, and that call sits inside
    ** "if (Settings.Video.Windowed)" (video_sdl2.cpp:581). Android is never
    ** windowed - video_sdl2.cpp:271 takes the whole screen unconditionally - so
    ** the branch that would change mouse mode is unreachable here. The touch
    ** layer is untouched by this.
    */
    Mouse.ControllerEnabled = true;
#else
    Mouse.ControllerEnabled = false;
#endif
    Mouse.ControllerPointerSpeed = 10;
    Options.MouseWheelScrolling = true;

    /*
    ** Video settings
    */
    Video.WindowWidth = 640;
    Video.WindowHeight = 400;
    Video.Windowed = false;
    Video.Width = 0;
    Video.Height = 0;
    Video.Boxing = true;
#ifdef __ANDROID__
    // D-16: fill the panel by default on Android. "auto" resolves against the
    // live renderer output, so one binary fits every device and refits on fold.
    // Override with BoxingAspectRatio=16:10 in CONQUER.INI for true native
    // proportions - no rebuild required.
    Video.BoxingAspectRatio = "auto";
#else
    Video.BoxingAspectRatio = "16:10";
#endif
#ifdef __ANDROID__
    // Michael's ruling after live A/B on device: grab-the-map is the natural
    // feel, matching how every other touch surface behaves. Override with
    // TouchPanInvert=false in CONQUER.INI, no rebuild required.
    Video.TouchPanInvert = true;
#else
    Video.TouchPanInvert = false;
#endif
#ifdef __ANDROID__
    /*
    ** D-30. 60 on Android, against the desktop default of 120.
    **
    ** Measured, not assumed. A five-minute play session on a Pixel 9 Pro Fold
    ** held a locked 120fps - average frame time 8.32ms against a 120Hz budget
    ** of 8.33ms, worst frame 8.59ms, not a single missed vsync. Thermals were
    ** flat throughout: skin 37.06C start to finish, no throttling.
    **
    ** The problem is what it costs. The process sat at 90-137% CPU - over a
    ** full core - and battery fell 71% to 69% in five minutes, roughly 24% an
    ** hour. For a 1995 game rendering 320x200 sprites that is a lot of power
    ** spent on frames nobody asked for.
    **
    ** The engine's simulation runs on its own fixed tick and is unaffected by
    ** this: the render limiter only governs how often the same game state is
    ** redrawn. Halving it should roughly halve the render cost with no change
    ** to how the game plays or feels.
    **
    ** Overridable with FrameLimit in CONQUER.INI, so the 120 case can be
    ** re-measured any time without a rebuild.
    */
    Video.FrameLimit = 60;
#else
    Video.FrameLimit = 120;
#endif
    Video.InterpolationMode = 2;
    Video.HardwareCursor = false;
    Video.DOSMode = false;
    Video.Scaler = "nearest";
    Video.Driver = "default";
    Video.PixelFormat = "default";
}

void SettingsClass::Load(INIClass& ini)
{
    char buf[128];

    /*
    ** Mouse settings
    */
    Mouse.RawInput = ini.Get_Bool("Mouse", "RawInput", Mouse.RawInput);
    Mouse.Sensitivity = ini.Get_Int("Mouse", "Sensitivity", Mouse.Sensitivity);
    Mouse.ControllerEnabled = ini.Get_Bool("Mouse", "ControllerEnabled", Mouse.ControllerEnabled);
    Mouse.ControllerPointerSpeed = ini.Get_Int("Mouse", "ControllerPointerSpeed", Mouse.ControllerPointerSpeed);
#ifdef __ANDROID__
    /*
    ** D-35: hold this on regardless of what the INI says, and do it AFTER the
    ** read so a stale file cannot win.
    **
    ** Changing only the constructor default was not enough and the failure was
    ** silent. Every install that ran before this change already has
    ** "ControllerEnabled=no" written into its conquer.ini / redalert.ini, put
    ** there by Save() from the old default. Get_Bool then hands that stale "no"
    ** straight back, Open_Controller is never called, SDL_INIT_GAMECONTROLLER
    ** never runs, and not one controller event is delivered - while the pad
    ** sits there paired and reported by Android, looking fine. A fresh install
    ** would have worked and an upgrade would not.
    **
    ** Editing the file on one device would have fixed one device. This fixes
    ** every device, including ones not yet installed.
    **
    ** Nothing is given up by forcing it. With no pad attached the gamepad
    ** subsystem costs an init call and Is_Gamepad_Active() stays false, so the
    ** touch path behaves exactly as it did before.
    */
    Mouse.ControllerEnabled = true;

    /*
    ** D-36: cursor speed, forced for the same reason and in the same place.
    ** ControllerPointerSpeed=10 is already persisted in every existing
    ** conquer.ini and redalert.ini, so raising the constructor default alone
    ** would have changed nothing on any device that had run the game before -
    ** the exact F-16 trap, avoided rather than repeated.
    **
    ** Process_Controller_Axis_Motion multiplies stick deflection by this value
    ** linearly, so 18 is 1.8x the stock cursor speed. One number, easy to
    ** retune on Michael's word.
    */
    Mouse.ControllerPointerSpeed = 18;
#endif
    /*
    ** Compatibility with CNCNet configuration for this feature
    */
    Options.MouseWheelScrolling = ini.Get_Bool("Options", "MouseWheelScrolling", Options.MouseWheelScrolling);
    Options.MouseWheelScrolling = ini.Get_Bool("Mouse", "MouseWheelScrolling", Options.MouseWheelScrolling);

    /*
    ** Video settings
    */
    Video.WindowWidth = ini.Get_Int("Video", "WindowWidth", Video.WindowWidth);
    Video.WindowHeight = ini.Get_Int("Video", "WindowHeight", Video.WindowHeight);
    Video.Windowed = ini.Get_Bool("Video", "Windowed", Video.Windowed);
    Video.Boxing = ini.Get_Bool("Video", "Boxing", Video.Boxing);
    Video.BoxingAspectRatio = ini.Get_String("Video", "BoxingAspectRatio", Video.BoxingAspectRatio);
    Video.TouchPanInvert = ini.Get_Bool("Video", "TouchPanInvert", Video.TouchPanInvert);
    Video.Width = ini.Get_Int("Video", "Width", Video.Width);
    Video.Height = ini.Get_Int("Video", "Height", Video.Height);
    Video.FrameLimit = ini.Get_Int("Video", "FrameLimit", Video.FrameLimit);
    Video.HardwareCursor = ini.Get_Bool("Video", "HardwareCursor", Video.HardwareCursor);
    Video.DOSMode = ini.Get_Bool("Video", "DOSMode", Video.DOSMode);
    Video.Scaler = ini.Get_String("Video", "Scaler", Video.Scaler);
    Video.Driver = ini.Get_String("Video", "Driver", Video.Driver);
    Video.PixelFormat = ini.Get_String("Video", "PixelFormat", Video.PixelFormat);

    /*
    ** VQA and WSA interpolation mode 0 = scanlines, 1 = vertical doubling, 2 = linear
    */
    Video.InterpolationMode = Bound(ini.Get_Int("Video", "InterpolationMode", Video.InterpolationMode), 0, 2);

    /*
    ** Boxing and raw input require software cursor.
    */
    if (Video.Boxing || Mouse.RawInput || Mouse.ControllerEnabled) {
        Video.HardwareCursor = false;
    }

    ini.Get_String("Video", "ButtonStyle", "Default", buf, sizeof(buf));
    if (!stricmp(buf, "Gold")) {
        Video.ButtonStyle = 1;
    } else if (!stricmp(buf, "Classic") || !stricmp(buf, "DOS")) {
        Video.ButtonStyle = 0;
    } else {
        Video.ButtonStyle = -1;
    }
}

void SettingsClass::Save(INIClass& ini)
{
    /*
    ** Mouse settings
    */
    ini.Put_Bool("Mouse", "RawInput", Mouse.RawInput);
    ini.Put_Int("Mouse", "Sensitivity", Mouse.Sensitivity);
    ini.Put_Bool("Mouse", "ControllerEnabled", Mouse.ControllerEnabled);
    ini.Put_Int("Mouse", "ControllerPointerSpeed", Mouse.ControllerPointerSpeed);
    ini.Put_Bool("Mouse", "MouseWheelScrolling", Options.MouseWheelScrolling);

    /*
    ** Video settings
    */
    ini.Put_Int("Video", "WindowWidth", Video.WindowWidth);
    ini.Put_Int("Video", "WindowHeight", Video.WindowHeight);
    ini.Put_Bool("Video", "Windowed", Video.Windowed);
    ini.Put_Bool("Video", "Boxing", Video.Boxing);
    ini.Put_String("Video", "BoxingAspectRatio", Video.BoxingAspectRatio);
    ini.Put_Int("Video", "Width", Video.Width);
    ini.Put_Int("Video", "Height", Video.Height);
    ini.Put_Int("Video", "FrameLimit", Video.FrameLimit);
    ini.Put_Bool("Video", "HardwareCursor", Video.HardwareCursor);
    ini.Put_Bool("Video", "DOSMode", Video.DOSMode);
    ini.Put_String("Video", "Scaler", Video.Scaler);
    ini.Put_String("Video", "Driver", Video.Driver);
    ini.Put_String("Video", "PixelFormat", Video.PixelFormat);

    /*
    ** VQA and WSA interpolation mode 0 = scanlines, 1 = vertical doubling, 2 = linear
    */
    ini.Put_Int("Video", "InterpolationMode", Video.InterpolationMode);

    ini.Put_String(
        "Video", "ButtonStyle", Video.ButtonStyle == -1 ? "Default" : (Video.ButtonStyle == 1 ? "Gold" : "Classic"));
}
