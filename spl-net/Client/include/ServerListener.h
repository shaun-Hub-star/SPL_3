//
// Created by Shust on 31/12/2021.
//

#ifndef CLIENT_SERVERLISTENER_H
#define CLIENT_SERVERLISTENER_H

#include "ConnectionHandler.h"

class ServerListener {
private:
    ConnectionHandler &handler;
    bool loggedIn;
    const int bufsize = 1024;
    char buf[];
public:
    ServerListener(ConnectionHandler &);

    virtual ~ServerListener();

    void run();
};
#endif //CLIENT_SERVERLISTENER_H
