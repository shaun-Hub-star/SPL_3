//
// Created by Shust on 31/12/2021.
//

#include "../include/ServerListener.h"
#include "../include/ConnectionHandler.h"

ServerListener::ServerListener(ConnectionHandler &handler1):handler(handler1), loggedIn(true),bufsize(1024){
    buf[bufsize];
}

ServerListener::~ServerListener() {

}

void ServerListener::run() {
    while(loggedIn){

        /*if(!(std::cin.eof())){
            std::cin.getline(buf, bufsize);
        }
        std::string line(buf);*/
        std::string answer;
        if(loggedIn){
            if (!(handler.getLine(answer))) {
                std::cout << "Disconnected. Exiting..\n" << std::endl;
                break;
            }
            if(answer.compare("LOGOUT") == 0){

                return;
            }
        }
    }
}

