if (NOT EXISTS "C:/DEV/cnc-td-android/android/app/.cxx/Debug/2mc3y6k7/arm64-v8a/install_manifest.txt")
    message(FATAL_ERROR "Cannot find install manifest: \"C:/DEV/cnc-td-android/android/app/.cxx/Debug/2mc3y6k7/arm64-v8a/install_manifest.txt\"")
endif(NOT EXISTS "C:/DEV/cnc-td-android/android/app/.cxx/Debug/2mc3y6k7/arm64-v8a/install_manifest.txt")

file(READ "C:/DEV/cnc-td-android/android/app/.cxx/Debug/2mc3y6k7/arm64-v8a/install_manifest.txt" files)
string(REGEX REPLACE "\n" ";" files "${files}")
foreach (file ${files})
    message(STATUS "Uninstalling \"$ENV{DESTDIR}${file}\"")
    execute_process(
        COMMAND C:/Users/mprit/scoop/persist/android-clt/cmake/3.22.1/bin/cmake.exe -E remove "$ENV{DESTDIR}${file}"
        OUTPUT_VARIABLE rm_out
        RESULT_VARIABLE rm_retval
    )
    if(NOT ${rm_retval} EQUAL 0)
        message(FATAL_ERROR "Problem when removing \"$ENV{DESTDIR}${file}\"")
    endif (NOT ${rm_retval} EQUAL 0)
endforeach(file)

