#include <iostream>
#include "hello.h"

void hello(){
    #ifdef NDEBUG
    std::cout << "Hello Conan!" <<std::endl;
    #else
    std::cout << "Hello JFrog!" <<std::endl;
    #endif
}
