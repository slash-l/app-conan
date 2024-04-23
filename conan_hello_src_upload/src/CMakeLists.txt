cmake_minimum_required(VERSION 3.1)
project(hello CXX)

# 使用 Conan，需要在 CMakeLists.txt 文件中添加如下内容
include(${CMAKE_BINARY_DIR}/conanbuildinfo.cmake)
conan_basic_setup()

add_library(hello hello.cpp)
