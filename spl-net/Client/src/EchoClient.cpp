#include <iostream>
#include <thread>
#include <boost/thread/thread.hpp>
#include "../include/ConnectionHandler.h"
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
    if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }
    Keyboard keyboard(connectionHandler);
    ServerListener serverListener(connectionHandler);
    std::thread thread1(&Keyboard::run, &keyboard);
    std::thread thread2(&ServerListener::run, &serverListener);
    thread1.join();
    thread2.join();

    return 0;
}

//REGISTER shaun 123 28-04-2002
//REGISTER lior 123 27-08-1997
//LOGIN shaun 123 1
// LOGIN lior 123 1
//LOGSTAT
//FOLLOW 0 shaun
//FOLLOW 0 lior
//FOLLOW 1 lior
//POST new post from lior levy!
//PM lior hiiii how are you?
//REGISTER yuval 123 27-08-1997
//LOGIN yuval 123 1
//FOLLOW 1 lior
//REGISTER noam 123 02-11-2003
//LOGIN noam 123 1
//REGISTER inbar 123 01-10-1992
//LOGIN inbar 123 1