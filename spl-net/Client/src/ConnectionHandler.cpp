#include "../include/ConnectionHandler.h"
#include <bits/stdc++.h>
#include <boost/algorithm/string.hpp>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;


ConnectionHandler::ConnectionHandler(string host, short port) : host_(host), port_(port), io_service_(),
                                                                socket_(io_service_) {}


ConnectionHandler::~ConnectionHandler() {

    close();

}


bool ConnectionHandler::connect() {

    std::cout << "Starting connect to "

              << host_ << ":" << port_ << std::endl;

    try {

        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_); // the server endpoint

        boost::system::error_code error;

        socket_.connect(endpoint, error);

        if (error)

            throw boost::system::system_error(error);

    }

    catch (std::exception &e) {

        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;

        return false;

    }

    return true;

}


bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {

    size_t tmp = 0;

    boost::system::error_code error;

    try {

        while (!error && bytesToRead > tmp) {

            tmp += socket_.read_some(boost::asio::buffer(bytes + tmp, bytesToRead - tmp), error);

        }

        if (error)

            throw boost::system::system_error(error);

    } catch (std::exception &e) {

        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;

        return false;

    }

    return true;

}


bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {

    int tmp = 0;

    boost::system::error_code error;

    try {

        while (!error && bytesToWrite > tmp) {

            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);

        }

        if (error)

            throw boost::system::system_error(error);

    } catch (std::exception &e) {

        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;

        return false;

    }

    return true;

}


bool ConnectionHandler::getLine(std::string &line) {

    return getFrameAscii(line, '\n');

}


bool ConnectionHandler::sendLine(std::string &line) {

    return sendFrameAscii(line, '\n');

}


bool ConnectionHandler::getFrameAscii(std::string &frame, char delimiter) {

    char ch;

    // Stop when we encounter the null character.

    // Notice that the null character is not appended to the frame string.

    try {

        do {

            getBytes(&ch, 1);

            frame.append(1, ch);

        } while (delimiter != ch);

    } catch (std::exception &e) {

        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;

        return false;

    }

    return true;

}

/**------------------------------register structure---------------------------------|
 *  2 bytes       string      1 byte      string       1 byte     string     1 byte |
 *   Opcode      Username       0        Password        0       birthday       0   |
 * */

bool ConnectionHandler::sendFrameAscii(const std::string &frame, char delimiter) {
    char *opcodeBytes = new char[2];
    char *separator = new char('\0');
    char *captcha = new char('1');

    /** I can use the "short to opcodeBytes method" in order to put the opcode
        in the "opcodeBytes array" and then use the sendBytes method which is provided on each part.*/
    unsigned const int indexOfEndOfFirstWord = frame.find(' ');
    /*if (frame == "LOGSTAT") {
        //logstat logic

    } else if (indexOfEndOfFirstWord == string::npos)return false;*/

    string keyWord = frame.substr(0, indexOfEndOfFirstWord);
    string result = frame.substr(indexOfEndOfFirstWord, frame.size());
    std::vector<string> keyWordsList;

    boost::split(keyWordsList, result, boost::is_any_of(" "));
    if (keyWord == "REGISTER") {
        return registerCommand(keyWordsList, frame, opcodeBytes,
                               separator);

    } else if (keyWord == "LOGIN") {//LOGIN <Username> <Password>
        return loginCommand(keyWordsList, frame, opcodeBytes,
                            separator, captcha);

    } else if (frame == "LOGOUT") {
        return logoutCommand(opcodeBytes);
    } else if (keyWord == "FOLLOW") {
        return followCommand(keyWordsList, opcodeBytes);

    } else {
        return false;
    }

}


// Close down the connection properly.

void ConnectionHandler::close() {

    try {

        socket_.close();

    } catch (...) {

        std::cout << "closing failed: connection already closed" << std::endl;

    }

}

void ConnectionHandler::shortToBytes(short num, char *bytesArr) {
    bytesArr[0] = static_cast<char>((num >> 8) & 0xFF);
    bytesArr[1] = static_cast<char>(num & 0xFF);
}

short ConnectionHandler::bytesToShort(char *bytesArr) {
    short result = (short) ((bytesArr[0] & 0xff) << 8);
    result += (short) (bytesArr[1] & 0xff);
    return result;
}

bool ConnectionHandler::availableHandler() {
    return false;
}

bool ConnectionHandler::validDate(std::string date) {

    std::vector<string> dateVector;

    boost::split(dateVector, date, boost::is_any_of("-"));
    return (dateVector.size()==3 && checkDate(stoi(dateVector.at(0)),stoi(dateVector.at(1)),stoi(dateVector.at(2))));
}

bool ConnectionHandler::registerCommandValidator(const string &frame, std::vector<string> result) {

    return (result.size() == 3 && !result.at(0).empty() &&
            !result.at(1).empty() &&
            !result.at(2).empty() && validDate(result.at(2)));


}

bool
ConnectionHandler::registerCommand(std::vector<string> keyWordsList, string frame, char *opcodeBytes,
                                   char *separator) {
    if (registerCommandValidator(frame, keyWordsList)) {


        const string userName = keyWordsList.at(0);
        const string password = keyWordsList.at(1);
        const string date = keyWordsList.at(2);
        const char *userNameBytes = userName.c_str();
        const char *passwordBytes = password.c_str();
        const char *dateBytes = date.c_str();

        short opcode = 1;
        shortToBytes(opcode, opcodeBytes);

        return sendBytes(opcodeBytes, 2) && sendBytes(userNameBytes, (int) userName.size()) &&
               sendBytes(separator, 1) && sendBytes(passwordBytes, (int) password.size()) &&
               sendBytes(separator, 1) && sendBytes(dateBytes, (int) date.size());
    } else return false;

}

bool ConnectionHandler::loginCommandValidator(std::vector<string> keyWordsList) {
    return (keyWordsList.size() == 3 && !keyWordsList.at(0).empty() &&
            !keyWordsList.at(1).empty());
}

bool ConnectionHandler::loginCommand(std::vector<string> keyWordsList, const string &basicString, char *opcodeBytes,
                                     char *separator, char *captcha) {
    if (loginCommandValidator(keyWordsList)) {

        const string userName = keyWordsList.at(0);
        const string password = keyWordsList.at(1);
        const char *userNameBytes = userName.c_str();
        const char *passwordBytes = password.c_str();

        short opcode = 2;
        shortToBytes(opcode, opcodeBytes);

        return sendBytes(opcodeBytes, 2) && sendBytes(userNameBytes, (int) userName.size()) &&
               sendBytes(separator, 1) && sendBytes(passwordBytes, (int) password.size()) &&
               sendBytes(separator, 1) && sendBytes(captcha, 1);
    } else return false;
}

bool ConnectionHandler::logoutCommand(char *opcodeBytes) {
    short opcode = 3;
    shortToBytes(opcode, opcodeBytes);
    return sendBytes(opcodeBytes, 2);
}

bool ConnectionHandler::followCommand(std::vector<string> keyWordsList, char *opcodeBytes) {
    if (keyWordsList.size() == 2 && (keyWordsList.at(0) == "1" || keyWordsList.at(0) == "0") &&
        !keyWordsList.at(1).empty()) {
        short opcode = 4;
        shortToBytes(opcode, opcodeBytes);
        short follow = std::stoi(keyWordsList.at(0));
        char *followBytes = new char[2];
        shortToBytes(follow, followBytes);
        const char *userNameBytes = keyWordsList.at(1).c_str();
        return sendBytes(opcodeBytes, 2) && sendBytes(followBytes, 1) &&
               sendBytes(userNameBytes, keyWordsList.at(1).size());

    } else return false;
}

bool ConnectionHandler::checkDate( int d,int m, int y) {
    if (1582 > y)
        return false;
    if (!(1 <= m && m <= 12))
        return false;
    if (!(1 <= d && d <= 31))
        return false;
    if ((d == 31) && (m == 2 || m == 4 || m == 6 || m == 9 || m == 11))
        return false;
    if ((d == 30) && (m == 2))
        return false;
    if ((m == 2) && (d == 29) && (y % 4 != 0))
        return false;
    if ((m == 2) && (d == 29) && (y % 400 == 0))
        return true;
    if ((m == 2) && (d == 29) && (y % 100 == 0))
        return false;
    if ((m == 2) && (d == 29) && (y % 4 == 0))
        return true;

    return true;
}


