#include <iostream>
#include "hello.h"

void hello(){
    #ifdef NDEBUG
    std::cout << "Hello JFrog Conan!" <<std::endl;
    #else
    std::cout << "Hello JFrog!" <<std::endl;
    #endif
}
