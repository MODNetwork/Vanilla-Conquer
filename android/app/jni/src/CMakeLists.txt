cmake_minimum_required(VERSION 3.22)

project(VC95_APP)

# SDL2 is built from the sibling SDL submodule (see ../CMakeLists.txt).
find_library(SDL2 SDL2)

# Gate 2 skeleton: SDLActivity launches, clears the screen to a solid colour,
# and survives home/resume. Engine sources are wired in at Phase 3.
add_library(main SHARED)

target_sources(main PRIVATE vc95_main.c)

target_link_libraries(main SDL2 android log)
