# app-conan
## 工程说明
### 1） conan_timer_install
timer.cpp 定时器，演示 conan install 从 Artifacory 获取三方依赖。
获取 Artifactory Conan Local 仓库中的二方包


### 2） conan_hello_src_upload
通过 Conan 编译 .a 并打包上传至 Artifactory Conan Local 二方仓库

### 3） hello_a_upload
有时候无法获取源码，只有编译好的 .a 文件，通过 Conan 直接打包并发布到 Artifactory Conan Local 二方仓库

## 演示实操说明
### 1） 不编译直接打包上传二方库
```
cd hello_a_upload
mkdir build
cd build

# Cmake 编译 .a
cmake ../src/
make
```

将 libhello.a 放到 lib 目录
将 hello.h 放到 include 目录

```
# conan 打包
conan export-pkg . hello_a/0.1@slash/testing

# conan 上传发布到 Artifactory Conan Local
conan upload hello_a/0.1@slash/testing --all -r=slash-conan-virtual
```
OK

### 2） 编译并打包上传二方库





### 2） 依赖下载三方包和二方包
```
cd conan_timer_install
mkdir build
cd build

# Conan install 依赖下载
conan install .. --build missing -r slash-conan-virtual

# 编译
cmake ..
cmake --build .

# 执行可执行文件 timer
bin/timer
```
OK
