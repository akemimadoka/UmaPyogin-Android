cmake_minimum_required(VERSION 3.20)

project(UmaPyogin C CXX)

if(NOT EXISTS "${CMAKE_BINARY_DIR}/conan.cmake")
    message(STATUS "Downloading conan.cmake")
    file(DOWNLOAD "https://raw.githubusercontent.com/akemimadoka/cmake-conan/develop/conan.cmake"
                    "${CMAKE_BINARY_DIR}/conan.cmake" SHOW_PROGRESS
                    STATUS _download_status)
    list(GET _download_status 0 _download_status_code)
    list(GET _download_status 1 _download_status_msg)
    if(NOT _download_status_code EQUAL 0)
        file(REMOVE "${CMAKE_BINARY_DIR}/conan.cmake")
        message(FATAL_ERROR "Failed to download conan.cmake, status code is ${_download_status_code}, msg is ${_download_status_msg}")
    endif()
endif()

include(${CMAKE_BINARY_DIR}/conan.cmake)

conan_check(REQUIRED)

execute_process(COMMAND ${CONAN_CMD} export ../UmaPyogin)

conan_cmake_run(
    CONANFILE conanfile.txt
    BASIC_SETUP CMAKE_TARGETS
    BUILD missing
    PROFILE_AUTO ALL
    PROFILE android.profile
    OPTIONS android_ndk:NDKRootPath=${CMAKE_ANDROID_NDK}
)

set(And64InlineHookSources
        ../And64InlineHook/And64InlineHook.cpp
        ../And64InlineHook/And64InlineHook.hpp
)

set(Nougat_dlfunctionsSources
        ../Nougat_dlfunctions/jni/fake_dlfcn.c
)

add_library(UmaPyogin SHARED
        src/main/cpp/UmaPyogin.cpp
        ${And64InlineHookSources}
        ${Nougat_dlfunctionsSources}
)

target_include_directories(UmaPyogin PRIVATE ../And64InlineHook)
target_link_libraries(UmaPyogin PRIVATE log ${CONAN_TARGETS})
target_compile_features(UmaPyogin PRIVATE cxx_std_20)
