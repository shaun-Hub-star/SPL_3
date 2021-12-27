//
// Created by Shust on 26/12/2021.
//

#ifndef CLIENT_ECHOCLIENT_H
#define CLIENT_ECHOCLIENT_H
class EchoClient {
private:
    void receive(ConnectionHandler* handler);
    void write(ConnectionHandler* handler);
};
#endif //CLIENT_ECHOCLIENT_H
