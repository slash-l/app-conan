from conans import ConanFile, CMake

# Recipe 会包含以下一些内容
# source
# build
# package
# package info

class HelloConan(ConanFile):
    name = "hello"
    version = "0.4"
    # version = "0.1.BUILD_NUMBER"
    license = "<Put the package license here>"
    author = "<Put your name here> <And your email here>"
    url = "<Package recipe repository url here, for issues about the package>"
    description = "<Description of Hello here>"
    topics = ("<Put some tag here>", "<here>", "<and here>")
    settings = "os", "compiler", "build_type", "arch"
    options = {"shared": [True, False], "fPIC": [True, False]}
    default_options = {"shared": False, "fPIC": True}
    generators = "cmake"
    exports_sources = "src/*"

    def requirements(self):
        self.requires("poco/1.10.0")    # -> depend on boost 1.74.0

    def config_options(self):
        if self.settings.os == "Windows":
            del self.options.fPIC

    def build(self):
        cmake = CMake(self)
        cmake.configure(source_folder="src")
        cmake.build()

        # Explicit way:
        # 通过 self.run 可以在 conan repice 的任何地方使用、通过本地shell调用任何外部工具命令
        # self.run('cmake %s/hello %s'
        #          % (self.source_folder, cmake.command_line))
        # self.run("cmake --build . %s" % cmake.build_config)

    def package(self):
        # 将选定的文件从 build 目录复制到 package 目录
        self.copy("*.h", dst="include", src="src")
        self.copy("*.lib", dst="lib", keep_path=False)
        self.copy("*.dll", dst="bin", keep_path=False)
        self.copy("*.dylib*", dst="lib", keep_path=False)
        self.copy("*.so", dst="lib", keep_path=False)
        self.copy("*.a", dst="lib", keep_path=False)

    # package_info 定义将传递给包使用者的变量
    def package_info(self):
        self.cpp_info.libs = ["hello"]
