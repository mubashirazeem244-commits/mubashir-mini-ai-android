cmake_minimum_required(VERSION 3.18)
project(MubashirMiniAI)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Set output directory for libraries
set(CMAKE_LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

# Find the Android NDK
find_package(GGML REQUIRED)

# Add the native library
add_library(llama SHARED
    src/main/cpp/llama_jni.cpp
    src/main/cpp/model_loader.cpp
    src/main/cpp/inference_engine.cpp
)

# Link against GGML
target_link_libraries(llama PRIVATE ggml)

# Include directories
target_include_directories(llama PRIVATE
    src/main/cpp/include
    ${ANDROID_NDK}/sources/android/native_app_glue
)

# Add GGML library for compilation
add_library(ggml SHARED IMPORTED)
set_target_properties(ggml PROPERTIES
    IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/lib/libggml.so
)
