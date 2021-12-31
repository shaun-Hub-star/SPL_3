//
// Created by Shust on 31/12/2021.
//
#include <boost/algorithm/string/split.hpp>
#include <boost/algorithm/string/classification.hpp>
#include "../include/Keyboard.h"
#include "../include/ConnectionHandler.h"

Keyboard::Keyboard(ConnectionHandler &handler) : handler(handler), loggedIn(false) {}

Keyboard::~Keyboard() {

}

void Keyboard::run() {
    std::string result;
    while (loggedIn) {

        //while (handler.availableHandler()) {
        std::vector<std::string> keyWordsList;
        boost::split(keyWordsList, result, boost::is_any_of(" "));


        getline(std::cin, result);

        handler.sendLine(result);

        result = keyWordsList.at(0);

        //std::string out = "ACK signout succeeded\n";

        if (result == "LOGOUT") {
            std::cout << "need to sign out" << std::endl;
            logout();
        }
        // }
    }
}

void Keyboard::logout() {
    loggedIn = false;
}
