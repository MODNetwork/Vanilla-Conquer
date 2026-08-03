cmake_minimum_required(VERSION 3.22)

project(VC95)

# SDL2 sources live in the pinned submodule at ./SDL
add_subdirectory(SDL)

# ---------------------------------------------------------------------------
# openal-soft (pinned submodule, 1.24.3).
#
# Built BEFORE the engine on purpose: openal-soft defines OpenAL::OpenAL as an
# ALIAS, and cmake/FindOpenAL.cmake guards target creation with
# "if(NOT TARGET OpenAL::OpenAL)". Building it first means the finder sees the
# real target and does not replace it with an UNKNOWN IMPORTED stub pointing at
# a library file that does not exist yet.
#
# Examples, utilities and tests are off - they pull in extra dependencies and
# nothing in the APK consumes them.
# ---------------------------------------------------------------------------
set(ALSOFT_EXAMPLES   OFF CACHE BOOL "" FORCE)
set(ALSOFT_UTILS      OFF CACHE BOOL "" FORCE)
set(ALSOFT_TESTS      OFF CACHE BOOL "" FORCE)
set(ALSOFT_INSTALL    OFF CACHE BOOL "" FORCE)
set(ALSOFT_INSTALL_CONFIG OFF CACHE BOOL "" FORCE)
set(ALSOFT_INSTALL_HRTF_DATA OFF CACHE BOOL "" FORCE)
set(ALSOFT_INSTALL_AMBDEC_PRESETS OFF CACHE BOOL "" FORCE)
set(ALSOFT_INSTALL_EXAMPLES OFF CACHE BOOL "" FORCE)
set(ALSOFT_INSTALL_UTILS  OFF CACHE BOOL "" FORCE)

add_subdirectory(openal-soft)

# ---------------------------------------------------------------------------
# Vanilla-Conquer engine.
#
# The engine tree is the repository root, four levels up from this file:
#   <repo>/android/app/jni/CMakeLists.txt  ->  <repo>
#
# Options are forced here rather than left to the engine's defaults:
#   NETWORKING        OFF  - D-2, WinSock-era code, irrelevant to the mission
#   BUILD_VANILLARA   OFF  - D-2, TD only through Gate 6 (D-13 keeps the slot)
#   SDL2              ON   - the only video backend on Android
#   OPENAL            OFF  - Phase 3 first pass; audio is wired after the
#                            engine boots, one variable at a time
# ---------------------------------------------------------------------------
set(BUILD_VANILLATD ON  CACHE BOOL "" FORCE)
set(BUILD_VANILLARA OFF CACHE BOOL "" FORCE)
set(BUILD_REMASTERTD OFF CACHE BOOL "" FORCE)
set(BUILD_REMASTERRA OFF CACHE BOOL "" FORCE)
set(NETWORKING      OFF CACHE BOOL "" FORCE)
set(SDL1            OFF CACHE BOOL "" FORCE)
set(SDL2            ON  CACHE BOOL "" FORCE)
set(OPENAL          ON  CACHE BOOL "" FORCE)
set(BUILD_TESTS     OFF CACHE BOOL "" FORCE)
set(BUILD_TOOLS     OFF CACHE BOOL "" FORCE)

# The engine calls find_package(SDL2); point it at the submodule we just built
# rather than letting it search the NDK sysroot.
# cmake/FindSDL2.cmake additionally REQUIRES SDL2_SDLMAIN_LIBRARY on Android.
# find_library() cannot see SDL2main because it is a target built in this same
# configure run, not a file on disk - so all three are seeded as cache vars.
set(SDL2_INCLUDE_DIR    "${CMAKE_CURRENT_SOURCE_DIR}/SDL/include" CACHE PATH   "" FORCE)
set(SDL2_LIBRARY        SDL2                                      CACHE STRING "" FORCE)
set(SDL2_SDLMAIN_LIBRARY SDL2main                                 CACHE STRING "" FORCE)

# Same reasoning for OpenAL: FIND_PACKAGE_HANDLE_STANDARD_ARGS still demands
# OPENAL_LIBRARY and OPENAL_INCLUDE_DIR even though the OpenAL::OpenAL target
# already exists, and find_library() cannot resolve a target built in this run.
# The engine's Android branch also does include_directories(${OPENAL_INCLUDE_DIR}/AL).
set(OPENAL_INCLUDE_DIR "${CMAKE_CURRENT_SOURCE_DIR}/openal-soft/include" CACHE PATH   "" FORCE)
set(OPENAL_LIBRARY     OpenAL                                            CACHE STRING "" FORCE)

add_subdirectory(
    "${CMAKE_CURRENT_SOURCE_DIR}/../../.."
    "${CMAKE_CURRENT_BINARY_DIR}/engine"
)
