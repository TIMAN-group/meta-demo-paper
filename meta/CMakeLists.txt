cmake_minimum_required(VERSION 3.0.0)
project(meta-trec-eval)

set(CMAKE_EXPORT_COMPILE_COMMANDS 1)

option(USE_LIBCXX "Use libc++ for the C++ standard library" ON)

include(CTest)
include(CheckCXXCompilerFlag)
include_directories(include git-meta/include)

if(UNIX)
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wall -Wextra -pedantic")

    # if we don't already set the standard for the compiler, detect the
    # best one available and use it
    if(NOT "${CMAKE_CXX_FLAGS}" MATCHES "std=c\\+\\+(0x|11|1y|14)")
        check_cxx_compiler_flag(-std=c++14 HAS_CXX14)
        if(HAS_CXX14)
            message("-- Compiler supports C++14 (using it)")
            set(STDOPT "-std=c++14")
        endif()

        if(NOT STDOPT)
            check_cxx_compiler_flag(-std=c++1y HAS_CXX1Y)
            if(HAS_CXX1Y)
                message("-- Compiler supports C++1y (using it)")
                set(STDOPT "-std=c++1y")
            endif()
        endif()

        if(NOT STDOPT)
            check_cxx_compiler_flag(-std=c++11 HAS_CXX11)
            if(HAS_CXX11)
                message("-- Compiler supports C++11 (using it)")
                set(STDOPT "-std=c++11")
            endif()
        endif()

        if(NOT STDOPT)
            message(FATAL_ERROR
                   "meta-trec-eval requires a compiler with at least C++11 support")
        endif()

        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${STDOPT}")
    endif()

    if("${CMAKE_CXX_COMPILER_ID}" STREQUAL "GNU")
        if(CMAKE_GENERATOR STREQUAL "Ninja")
          set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fdiagnostics-color")
        endif()
    endif()

    if("${CMAKE_CXX_COMPILER_ID}" STREQUAL "Clang")
        if(CMAKE_GENERATOR STREQUAL "Ninja")
          set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -fcolor-diagnostics")
        endif()
        if(USE_LIBCXX)
            message("-- Locating libc++...")
            find_library(LIBCXX_LIBRARY NAMES c++ cxx)
            if(LIBCXX_LIBRARY)
                message("-- Located libc++, using it.")
                set(LIBCXX_OPTIONS "-stdlib=libc++")
                message("--     Locating libc++'s abi...")
                find_library(LIBCXXABI_LIBRARY NAMES c++abi)
                find_library(LIBCXXRT_LIBRARY NAMES cxxrt)
                if(LIBCXXABI_LIBRARY)
                    message("--     Found libc++abi, using it.")
                    set(CXXABI_LIBRARY ${LIBCXXABI_LIBRARY})
                elseif(LIBCXXRT_LIBRARY)
                    message("--     Found libcxxrt, using it.")
                    set(CXXABI_LIBRARY ${LIBCXXRT_LIBRARY})
                else()
                    message("--     No abi library found. "
                            "Attempting to continue without one...")
                endif()
            else()
                message("-- Could not find libc++, will not use it.")
            endif()
        endif()
    endif()

    find_library(LIBDL_LIBRARY NAMES dl ldl)
    if(LIBDL_LIBRARY)
      set(CMAKE_REQUIRED_FLAGS "${CMAKE_REQUIRED_FLAGS} ${LIBDL_LIBRARY}")
    endif()

    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${LIBCXX_OPTIONS}")
    set(CMAKE_REQUIRED_FLAGS "${CMAKE_REQUIRED_FLAGS} ${CXXABI_LIBRARY}")
endif()

add_subdirectory(git-meta EXCLUDE_FROM_ALL)

find_package(Threads REQUIRED)

add_executable(trec-eval src/trec_eval.cpp)
target_link_libraries(trec-eval cpptoml meta-util meta-analyzers meta-ranker
  ${CMAKE_THREAD_LIBS_INIT})
