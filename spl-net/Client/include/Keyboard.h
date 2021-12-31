//
// Created by Shust on 31/12/2021.
//

#ifndef CLIENT_KEYBOARD_H
#define CLIENT_KEYBOARD_H

#include "ConnectionHandler.h"

class Keyboard {
private:
    ConnectionHandler &handler;
    bool loggedIn;
public:
    Keyboard(ConnectionHandler &handler);

    virtual ~Keyboard();

    void run();
};
#endif //CLIENT_KEYBOARD_H