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
#include "paths.h"
#include "debugstring.h"
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <errno.h>
#include <pwd.h>
#include <stdexcept>
#include <sys/stat.h>
#include <sys/types.h>
#include <unistd.h>
#include <vector>
#include <libgen.h>

#ifdef __ANDROID__
// SDL2 is already linked into the engine on Android and exposes the two
// sandbox paths directly from C, so no JNI plumbing or exported setter is
// required:
//   SDL_AndroidGetInternalStoragePath() -> /data/data/<pkg>/files
//       app-private, always writable. Config and saves live here.
//   SDL_AndroidGetExternalStoragePath() -> /sdcard/Android/data/<pkg>/files
//       app-private but reachable over adb push and by a file manager, which
//       is what the Phase 4 asset-supply procedure requires.
// Without this the engine falls back to Linux desktop defaults and resolves
// /system/share/vanillatd and /data/.config/..., neither of which an Android
// app may touch.
#include <SDL_system.h>
#endif

#if defined(__linux__)
#include <linux/limits.h>
#else
#include <limits.h>
#endif

#ifndef __STDC_FORMAT_MACROS
#define __STDC_FORMAT_MACROS
#endif
#include <inttypes.h>

#if defined(__APPLE__)
#define _DARWIN_BETTER_REALPATH
#include <mach-o/dyld.h>
#include <TargetConditionals.h>
#elif defined(__DragonFly__) || defined(__FreeBSD__) || defined(__FreeBSD_kernel__) || defined(__NetBSD__)
#include <sys/sysctl.h>
#endif

namespace
{
    const std::string& User_Home()
    {
        static std::string _path;

        if (_path.empty()) {
#ifdef __ANDROID__
            // There is no HOME and no passwd entry inside the app sandbox.
            const char* internal = SDL_AndroidGetInternalStoragePath();
            _path = internal ? internal : ".";
            return _path;
#else
            int uid = getuid();
            const char* home_env = std::getenv("HOME");

            if (home_env) {
                _path = home_env;
            } else if (uid == 0) {
                _path = "/root";
            } else {
                struct passwd* pw = nullptr;
                struct passwd pwd;
                long bufsize = sysconf(_SC_GETPW_R_SIZE_MAX);

                if (bufsize < 0) {
                    bufsize = 16384;
                }

                std::vector<char> buffer;
                buffer.resize(bufsize);
                int error_code = getpwuid_r(uid, &pwd, buffer.data(), buffer.size(), &pw);

                if (error_code) {
                    DBG_ERROR("Unable to get passwd entry for uid %d, error was %d.", uid, error_code);
                    return _path;
                }

                const char* tmp = pw->pw_dir;

                if (!tmp) {
                    DBG_ERROR("User does not appear to have a home directory?");
                    return _path;
                }

                _path = tmp;
            }
#endif /* __ANDROID__ */
        }

        return _path;
    }

    std::string Get_Posix_Default(const char* env_var, const char* relative_path)
    {
        const char* tmp = std::getenv(env_var);

        if (tmp != nullptr && tmp[0] != '\0') {
            if (tmp[0] != '/') {
                char buffer[200];
                std::snprintf(buffer,
                              sizeof(buffer),
                              "'%s' should start with '/' as per XDG specification that the value must be absolute. "
                              "The current value is: "
                              "'%s'.",
                              tmp,
                              env_var);
                DBG_WARN(buffer);
            } else {
                return tmp;
            }
        }

        return User_Home() + "/" + relative_path;
    }
} // namespace

#if defined(__sun)
#define PROC_SELF_EXE "/proc/self/path/a.out"
#else
#define PROC_SELF_EXE "/proc/self/exe"
#endif

const char* PathsClass::Program_Path()
{
    if (ProgramPath.empty()) {
        /*
        ** Adapted from https://github.com/gpakosz/whereami
        ** dual licensed under the WTFPL v2 and MIT licenses without any warranty. by Gregory Pakosz (@gpakosz)
        */
        std::string tmp;
        char buffer[PATH_MAX];
        char* resolved = nullptr;
#if defined(__linux__) || defined(__CYGWIN__) || defined(__sun)
        resolved = realpath(PROC_SELF_EXE, buffer);
#elif defined(__APPLE__)
        char buffer1[PATH_MAX];
        char* path = buffer1;

        uint32_t size = (uint32_t)sizeof(buffer1);
        if (_NSGetExecutablePath(path, &size) == -1) {
            path = (char*)malloc(size);
            if (!_NSGetExecutablePath(path, &size)) {
                free(path);
                return ProgramPath.c_str();
            }
        }

        resolved = realpath(path, buffer);

        if (path != buffer1) {
            free(path);
        }
#elif defined(__DragonFly__) || defined(__FreeBSD__) || defined(__FreeBSD_kernel__) || defined(__NetBSD__)
        char buffer1[PATH_MAX];
        char* path = buffer1;

#if defined(__NetBSD__)
        int mib[4] = {CTL_KERN, KERN_PROC_ARGS, -1, KERN_PROC_PATHNAME};
#else
        int mib[4] = {CTL_KERN, KERN_PROC, KERN_PROC_PATHNAME, -1};
#endif
        size_t size = sizeof(buffer1);

        if (sysctl(mib, (u_int)(sizeof(mib) / sizeof(mib[0])), path, &size, NULL, 0) != 0) {
            return ProgramPath.c_str();
        }

        resolved = realpath(path, buffer);
#endif
        if (!resolved) {
            return ProgramPath.c_str();
        }

        tmp = resolved;
        ProgramPath = tmp.substr(0, tmp.find_last_of("/"));
    }

    return ProgramPath.c_str();
}

const char* PathsClass::Data_Path()
{
    if (DataPath.empty()) {
#ifdef __ANDROID__
        // Game data (the MIX files) is user-supplied and never shipped in the
        // APK - the asset wall is a licence requirement, not hygiene. The
        // external files dir is app-private yet reachable via:
        //     adb push *.MIX /sdcard/Android/data/<pkg>/files/
        // Deriving it from ProgramPath is meaningless here: argv[0] is
        // 'app_process' (the zygote), which is why realpath() failed.
        const char* external = SDL_AndroidGetExternalStoragePath();

        if (external != nullptr) {
            DataPath = external;
        } else {
            // No external storage mounted; fall back to the private dir so the
            // engine still has a real, readable location rather than /system.
            const char* internal = SDL_AndroidGetInternalStoragePath();
            DataPath = internal ? internal : ".";
            DBG_WARN("Android: external storage unavailable, using internal for game data");
        }

        /*
        ** D-13/D-25: one app, two titles, one external files directory - so the
        ** data has to be separated per title or the games silently poison each
        ** other. Tiberian Dawn and Red Alert both ship files named CONQUER.MIX,
        ** SOUNDS.MIX, SPEECH.MIX, SCORES.MIX and TRANSIT.MIX. Sharing a flat
        ** directory means whichever engine loads second reads the other's data,
        ** which would not fail loudly - it would produce wrong art and wrong
        ** audio and look like a port defect.
        **
        ** Suffix is the engine's own per-title identifier, set in Init():
        **   startup.cpp:234  Paths.Init("vanillatd", ...)
        **   startup.cpp:292  Paths.Init("vanillara", ...)
        ** so this reuses an existing mechanism rather than inventing one. Yields
        **   .../files/vanillatd   and   .../files/vanillara
        **
        ** Desktop is unaffected: there, each title already lives in its own
        ** directory alongside its own executable.
        */
        if (!Suffix.empty()) {
            DataPath = Concatenate_Paths(DataPath.c_str(), Suffix.c_str());

            // The user pushes MIX files here, so it must exist even when empty -
            // otherwise the first run of a title reports a missing data path
            // rather than missing data, which is a misleading error.
            if (!Create_Directory(DataPath.c_str())) {
                DBG_WARN("Android: could not create data directory '%s'", DataPath.c_str());
            }
        }

        DBG_INFO("Android: game data path is '%s'", DataPath.c_str());
        return DataPath.c_str();
#else
        if (ProgramPath.empty()) {
            // Init the program path first if it hasn't been done already.
            Program_Path();
        }

        DataPath = ProgramPath.substr(0, ProgramPath.find_last_of("/")) + SEP + "share";
#endif

        if (!Suffix.empty()) {
            DataPath += SEP + Suffix;
        }
    }

    return DataPath.c_str();
}

const char* PathsClass::User_Path()
{
    if (UserPath.empty()) {
#ifdef __APPLE__
        UserPath = User_Home() + "/Library/Application Support/Vanilla-Conquer";
#elif defined(__ANDROID__)
        // Config and saves: app-private internal storage, always writable,
        // survives across launches and is wiped only on uninstall.
        UserPath = User_Home();
        DBG_INFO("Android: user data path is '%s'", UserPath.c_str());
#else
        UserPath = Get_Posix_Default("XDG_CONFIG_HOME", ".config") + "/vanilla-conquer";
#endif

        if (!Suffix.empty()) {
            UserPath += SEP + Suffix;
        }

        Create_Directory(UserPath.c_str());
    }

    return UserPath.c_str();
}

bool PathsClass::Create_Directory(const char* dirname)
{
    bool ret = true;

    if (dirname == nullptr) {
        return ret;
    }

    std::string temp = dirname;
    size_t pos = 0;
    do {
        pos = temp.find_first_of("/", pos + 1);
        if (mkdir(temp.substr(0, pos).c_str(), 0700) != 0) {
            if (errno != EEXIST) {
                ret = false;
                break;
            }
        }
    } while (pos != std::string::npos);

    return ret;
}

bool PathsClass::Is_Absolute(const char* path)
{
    return path != nullptr && path[0] == '/';
}

std::string PathsClass::Concatenate_Paths(const char* path1, const char* path2)
{
    return std::string(path1) + SEP + path2;
}

std::string PathsClass::Get_Filename(const char* path)
{
    char buff[PATH_MAX];
    strncpy(buff, path, PATH_MAX);
    buff[PATH_MAX - 1] = '\0';
    return std::string(basename(buff));
}

std::string PathsClass::Argv_Path(const char* cmd_arg)
{
    std::string ret(PATH_MAX, '\0');
    char arg_dir[PATH_MAX];
    strncpy(arg_dir, cmd_arg, sizeof(arg_dir));

    // remove the executable from path, if any
    char* dir = strrchr(arg_dir, '/');
    if (dir != nullptr) {
        *dir = '\0';
    }

    if (realpath(arg_dir, &ret[0]) == nullptr) {
        DBG_WARN("PathsClass::Argv_Path: realpath() failed");
        ret = arg_dir;
    } else {
        ret.resize(strlen(&ret[0]));
    }
    return ret;
}
