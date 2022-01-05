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

    std::cout << "Starting connect to " << host_ << ":" << port_ << std::endl;

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
    std::cout << "are you in?1" << std::endl;
    char ch;

    // Stop when we encounter the null character.

    // Notice that the null character is not appended to the frame string.
    char firstbytes;
    char secondebyts;
    char thirdbytes;
    char fourthbytes;
    char fiftbyts;
    char sixthbyts;
    string messageToClient = "";
    try {
        char *opcodeBytes = new char[2];
        getBytes(opcodeBytes, 2);
        short opcode = bytesToShort(opcodeBytes);
        //std::cout<<"opcode "<<opcode<<std::endl;
        switch (opcode) {

        /*    case 9:
                messageToClient = "NOTIFICATION";
                char *opcodeBytes9 = new char[2];
                getBytes(opcodeBytes9, 2);
                short opcode9 = bytesToShort(opcodeBytes9);


                getBytes(&secondebyts, 1);
                if (secondebyts == 0)
                    messageToClient += " PM ";
                else if (secondebyts == 1)
                    messageToClient += " Public ";
                string UserName = findWord(thirdbytes, '\0');
                messageToClient += UserName;
                string Content = findWord(fourthbytes, '\0');
                messageToClient += " " + Content;
                break;

*/
            case (10):{
                // std::cout << "case 10" << std::endl;
                messageToClient = "ACK ";

                char *opcodemes = new char[2];
                getBytes(opcodemes, 2);
                short messageOpcode = bytesToShort(opcodemes);
                //std::cout << (int) messageOpcode << std::endl;
                messageToClient += std::to_string((int) messageOpcode);
                std::cout << "Message to client" << messageToClient << std::endl;
                if (messageOpcode == 8 | messageOpcode == 7) {
                    char *age = getChar(thirdbytes, 2);
                    char *NumPosts = getChar(thirdbytes, 2);
                    char *NumFollowers = getChar(thirdbytes, 2);
                    char *NumFollowing = getChar(thirdbytes, 2);
                    messageToClient =
                            messageToClient + " " + age + " " + NumPosts + " " + NumFollowers + " " + NumFollowing;

                } else if (messageOpcode == 4) {

                    getBytes(&thirdbytes, 1);
                    string UserName = findWord(thirdbytes, '\0');
                    messageToClient += UserName;
                }
                frame = messageToClient;
                break;}

            case(11):
                messageToClient = "ERROR ";
                std::cout << "Message to client" << messageToClient << std::endl;
                char *opcodemes11 = new char[2];
                getBytes(opcodemes11, 2);
                short messageOpcode11 = bytesToShort(opcodemes11);
                messageToClient += std::to_string((int) messageOpcode11);
                std::cout << "Message to client" << messageToClient << std::endl;
                frame = messageToClient;
                     break;

                /* case 12:
                     messageToClient = "BLOCK ";
                     getBytes(&secondebyts, 1);
                     string username = findWord(secondebyts, delimiter);
                     messageToClient += username;
                     break;

                 default:
                     messageToClient = "";
                     break;*/



        frame = messageToClient;
        //std::cout<<frame<<std::endl;
    }}
    catch (std::exception &e) {

        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;

        return false;

    }

    return true;

}

/*short ConnectionHandler::getShort(char ch, int a) {
    short opcodeshort;
    char *bytestoSort = new char[a];
    int counter = 0;
    while (counter < a) {
        getBytes(&ch, a);
        bytestoSort[counter] = ch;
    }
    opcodeshort = bytesToShort(bytestoSort);
    return opcodeshort;
}*/

std::string ConnectionHandler::findWord(char ch, char delimiter) {
    string output = "";
    getBytes(&ch, 1);
    while (ch != delimiter) {
        output += ch;
        getBytes(&ch, 1);
    }
    return output;
}

char *ConnectionHandler::getChar(char ch, int a) {

    char *bytestoChar = new char[a];
    int counter = 0;
    while (counter < a) {
        getBytes(&ch, 1);
        bytestoChar[counter] = ch;
    }
    return bytestoChar;
}

bool ConnectionHandler::sendFrameAscii(const std::string &frame, char delimiter) {
    char *opcodeBytes = new char[2];
    char *separator = new char('\0');

    /** I can use the "short to opcodeBytes method" in order to put the opcode
        in the "opcodeBytes array" and then use the sendBytes method which is provided on each part.*/
    unsigned const int indexOfEndOfFirstWord = frame.find(' ');

    string keyWord = frame.substr(0, indexOfEndOfFirstWord);
    string result = frame.substr(indexOfEndOfFirstWord + 1, frame.size());
    std::vector<string> keyWordsList;

    boost::split(keyWordsList, result, boost::is_any_of(" "));
    if (keyWord == "REGISTER") {
        return registerCommand(keyWordsList, frame, opcodeBytes,
                               separator);

    } else if (keyWord == "LOGIN") {//LOGIN <Username> <Password>
        return loginCommand(keyWordsList, frame, opcodeBytes,
                            separator);
    } else if (frame == "LOGOUT") {
        return logoutCommand(opcodeBytes);
    } else if (keyWord == "FOLLOW") {
        return followCommand(keyWordsList, opcodeBytes);
    } else if (keyWord == "POST") {
        return postCommand(keyWordsList, opcodeBytes);
    } else if (keyWord == "PM") {//PM <Username> <Content>
        return pmCommand(keyWordsList, opcodeBytes);
    } else if (frame == "LOGSTAT") {
        return logStatCommand(opcodeBytes);
    } else if (frame == "STAT") {
        return statCommand(keyWordsList, opcodeBytes);
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
    return (dateVector.size() == 3 &&
            checkDate(stoi(dateVector.at(0)), stoi(dateVector.at(1)), stoi(dateVector.at(2))));
}

bool ConnectionHandler::registerCommandValidator(const string &frame, std::vector<string> result) {

    return (result.size() == 3 && !result.at(0).empty() &&
            !result.at(1).empty() &&
            !result.at(2).empty() && validDate(result.at(2)));


}

/**------------------------------register structure---------------------------------|
 *  2 bytes       string      1 byte      string       1 byte     string     1 byte |
 *   Opcode      Username       0        Password        0       birthday       0   |
 * */
bool
ConnectionHandler::registerCommand(std::vector<string> keyWordsList, string frame, char *opcodeBytes,
                                   char *separator) {
    if (registerCommandValidator(frame, keyWordsList)) {

        string endLineString = ";";
        const string userName = keyWordsList.at(0);
        const string password = keyWordsList.at(1);
        const string date = keyWordsList.at(2);
        const char *userNameBytes = userName.c_str();
        const char *passwordBytes = password.c_str();
        const char *dateBytes = date.c_str();
        const char *endOfLine = endLineString.c_str();
        short opcode = 1;
        shortToBytes(opcode, opcodeBytes);

        return sendBytes(opcodeBytes, 2) && sendBytes(userNameBytes, (int) userName.size()) &&
               sendBytes(separator, 1) && sendBytes(passwordBytes, (int) password.size()) &&
               sendBytes(separator, 1) && sendBytes(dateBytes, (int) date.size()) && sendBytes(separator, 1)
               && sendBytes(endOfLine, 1);
    } else return false;

}

bool ConnectionHandler::loginCommandValidator(std::vector<string> keyWordsList) {
    return (keyWordsList.size() == 3 && !keyWordsList.at(0).empty() &&
            !keyWordsList.at(1).empty() && !keyWordsList.at(2).empty());
}

bool ConnectionHandler::loginCommand(std::vector<string> keyWordsList, const string &basicString, char *opcodeBytes,
                                     char *separator) {
    if (loginCommandValidator(keyWordsList)) {
        std::cout << "login" << std::endl;
        string endLineString = ";";
        const string userName = keyWordsList.at(0);
        const string password = keyWordsList.at(1);
        const string captcha = keyWordsList.at(2);
        const char *userNameBytes = userName.c_str();
        const char *passwordBytes = password.c_str();
        const char *captchaBytes = captcha.c_str();
        const char *endOfLine = endLineString.c_str();
        short opcode = 2;
        shortToBytes(opcode, opcodeBytes);

        return sendBytes(opcodeBytes, 2) && sendBytes(userNameBytes, (int) userName.size()) &&
               sendBytes(separator, 1) && sendBytes(passwordBytes, (int) password.size()) &&
               sendBytes(separator, 1) && sendBytes(captchaBytes, 1) && sendBytes(separator, 1) &&
               sendBytes(endOfLine, 1);
    } else return false;
}

bool ConnectionHandler::logoutCommand(char *opcodeBytes) {
    short opcode = 3;
    shortToBytes(opcode, opcodeBytes);
    string endLineString = ";";
    const char *endOfLine = endLineString.c_str();
    return sendBytes(opcodeBytes, 2) && sendBytes(endOfLine, 1);
}

bool ConnectionHandler::followCommand(std::vector<string> keyWordsList, char *opcodeBytes) {
    if (keyWordsList.size() == 2 && (keyWordsList.at(0) == "1" || keyWordsList.at(0) == "0") &&
        !keyWordsList.at(1).empty()) {
        short opcode = 4;
        string endLineString = ";";
        const char *endOfLine = endLineString.c_str();
        shortToBytes(opcode, opcodeBytes);
        short follow = std::stoi(keyWordsList.at(0));
        char *followBytes = new char[2];
        char *delimiter = new char('\0');
        shortToBytes(follow, followBytes);
        const char *userNameBytes = keyWordsList.at(1).c_str();
        return sendBytes(opcodeBytes, 2) && sendBytes(followBytes, 1) &&
               sendBytes(userNameBytes, (int) keyWordsList.at(1).size()) && sendBytes(delimiter, 1) &&
               sendBytes(endOfLine, 1);

    } else return false;
}

bool ConnectionHandler::checkDate(int d, int m, int y) {
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

bool ConnectionHandler::postCommand(std::vector<std::string> keyWordsList, char *opcodeBytes) {
    string message = "";
    short opcode = 5;
    string endLineString = ";";

    const char *endOfLine = endLineString.c_str();
    shortToBytes(opcode, opcodeBytes);

    for (int i = 0; i < keyWordsList.size(); i++) {
        message += keyWordsList.at(i);

        if (i < keyWordsList.size() - 1)
            message += " ";

    }
    const char *messageBytes = message.c_str();
    char *delimiter = new char('\0');
    return sendBytes(opcodeBytes, 2) && sendBytes(messageBytes, (int) message.size()) &&
           sendBytes(delimiter, 1) && sendBytes(endOfLine, 1);

}

bool ConnectionHandler::pmCommand(std::vector<std::string> keyWordsList, char *opcodeBytes) {
    if (keyWordsList.size() == 2 && !keyWordsList.at(0).empty() && !keyWordsList.at(1).empty()) {
        short opcode = 6;
        string endLineString = ";";
        const char *endOfLine = endLineString.c_str();
        time_t now = time(0);
        tm *ltm = localtime(&now);
        int day = ltm->tm_mday;
        int month = ltm->tm_mon;
        int year = ltm->tm_year;
        string date(std::to_string(day) + "--" + std::to_string(month) + "--" + std::to_string(year) + " " +
                    std::to_string(ltm->tm_hour) + "--" + std::to_string(ltm->tm_min));
        const char *dateBytes = date.c_str();
        shortToBytes(opcode, opcodeBytes);
        char *delimiter = new char('\0');
        string userName = keyWordsList.at(0);
        string content = keyWordsList.at(1);
        const char *userNameBytes = userName.c_str();
        const char *contentBytes = content.c_str();

        return sendBytes(opcodeBytes, 2) && sendBytes(userNameBytes, (int) userName.size()) &&
               sendBytes(delimiter, 1) && sendBytes(contentBytes, (int) content.size()) &&
               sendBytes(delimiter, 1) && sendBytes(dateBytes, (int) date.size()) &&
               sendBytes(delimiter, 1) && sendBytes(endOfLine, 1);

    } else return false;

}

bool ConnectionHandler::logStatCommand(char *opcodeBytes) {
    short opcode = 7;
    string endLineString = ";";
    const char *endOfLine = endLineString.c_str();
    shortToBytes(opcode, opcodeBytes);
    return sendBytes(opcodeBytes, 2) && sendBytes(endOfLine, 1);
}

bool ConnectionHandler::statCommand(std::vector<std::string> keyWordsList, char *opcodeBytes) {
    if (keyWordsList.size() == 1) {
        //std::vector<std::string> users;
        //boost::split(users, keyWordsList.at(0), boost::is_any_of("|"));
        short opcode = 8;
        string endLineString = ";";
        const char *endOfLine = endLineString.c_str();
        string users = keyWordsList.at(0);
        const char *usersByte = users.c_str();
        const char *delimiter = new char('\0');
        shortToBytes(opcode, opcodeBytes);
        return sendBytes(opcodeBytes, 2) && sendBytes(usersByte, (int) users.size())
               && sendBytes(delimiter, 1) && sendBytes(endOfLine, 1);
    } else return false;

}


