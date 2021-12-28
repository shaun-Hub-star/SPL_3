#include <iostream>
#include <thread>
#include <boost/thread/thread.hpp>
#include "../include/connectionHandler.h"

bool loggedIn = true;
const int bufsize = 1024;
char buf[bufsize];

void receiving(ConnectionHandler* handler);
void writer(ConnectionHandler* handler);
using namespace std;
int main (int argc, char *argv[]) {

    if (argc < 3) {
        std::cerr << "Usage: " << argv[0] << " host port" << std::endl << std::endl;
        return -1;
    }
    std::string host = argv[1];
    short port = atoi(argv[2]);
    ConnectionHandler connectionHandler(host, port);
    /*if (!connectionHandler.connect()) {
        std::cerr << "Cannot connect to " << host << ":" << port << std::endl;
        return 1;
    }*/


    std::thread sender(writer,&connectionHandler);
   // std::thread getter(receiving, &connectionHandler);

    //getter.join();
    sender.detach();

    return 0;
}

void receiving(ConnectionHandler* handler){

    std::string result;
    while(loggedIn){

        while(handler->availableHandler()){

            if(!(handler->getLine(result))){
                break;
            }

            std::cout<<result<<std::endl;

            string OUT = "ACK signout succeeded\n";

            if (result.compare("ACK 3")==0) {
                cout<<"need to sign out" << endl;
                loggedIn = false;

                return;
            }
            else{
                result="";
            }
        }
    }

}

void writer(ConnectionHandler* handler){

    while(loggedIn){

        /*if(!(std::cin.eof())){
            std::cin.getline(buf, bufsize);
        }
        std::string line(buf);*/
        string line = "LOGOUT";
        if(loggedIn){
            if (!(handler->getLine(line))) {
                break;
            }
            /*if(line.compare("LOGOUT") == 0){

                return;
            }*/
        }
    }
}