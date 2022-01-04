//
// Created by Shust on 31/12/2021.
//

#include "../include/ServerListener.h"
#include "../include/ConnectionHandler.h"

ServerListener::ServerListener(ConnectionHandler &handler1) : handler(handler1), loggedIn(true), bufsize(1024) {
    buf[bufsize];
}

ServerListener::~ServerListener() {

}

void ServerListener::run() {
    while (loggedIn) {

        std::string answer;

        if (!(handler.getLine(answer))) {
            std::cout << "Disconnected. Exiting..\n" << std::endl;
            break;
        }
        std::cout<<answer<<std::endl;
        if (answer == "ACK 3") {
            loggedIn = false;
        }

    }
}

