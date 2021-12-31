#include <iostream>
#include <thread>
#include <boost/thread/thread.hpp>
#include "../include/connectionHandler.h"
#include "../include/Keyboard.h"
#include "../include/ServerListener.h"


using namespace std;
int main (int argc, char *argv[]) {

    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    ConnectionHandler connectionHandler(host, port);

    Keyboard keyboard(connectionHandler);
    ServerListener serverListener(connectionHandler);
    std::thread th1(&Keyboard::run, &keyboard);
    std::thread th2(&ServerListener::run, &serverListener);
    th1.join();
    th2.join();

    return 0;
}


