cmake_minimum_required(VERSION 3.19.2)
project(Client)
set(CMAKE_CXX_STANDARD 14)
set(Boost_INCLUDE_DIR /cygdrive/c/cygwin64/usr/include)
find_package(Boost REQUIRED COMPONENTS system)
include_directories(${Boost_INCLUDE_DIRS})
add_executable(Client src/ConnectionHandler.cpp include/ConnectionHandler.h src/EchoClient.cpp include/EchoClient.h src/Keyboard.cpp src/ServerListener.cpp
        include/ServerListener.h include/Keyboard.h)
target_link_libraries(${PROJECT_NAME} ${Boost_LIBRARIES})