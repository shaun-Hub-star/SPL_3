#ifndef CONNECTION_HANDLER__

#define CONNECTION_HANDLER__


#include <string>

#include <iostream>

#include <boost/asio.hpp>


using boost::asio::ip::tcp;


class ConnectionHandler {

private:

    const std::string host_;

    const short port_;

    boost::asio::io_service io_service_;   // Provides core I/O functionality

    tcp::socket socket_;

    void shortToBytes(short num, char *bytesArr);

    short bytesToShort(char *bytesArr);

    bool validDate(std::string basicString);

    bool registerCommandValidator(const std::string &frame, std::vector<std::string> result);

    bool registerCommand(std::vector<std::string> keyWordsList, std::string frame, char *opcodeBytes, char *separator);

    bool loginCommandValidator(std::vector<std::string> vector);

    bool loginCommand(std::vector<std::string> keyWordsList,
                      const std::string &basicString,
                      char *opcodeBytes, char *separator,
                      char *captcha);

    bool logoutCommand(char *opcodeBytes);

    bool followCommand(std::vector<std::string> keyWordsList, char *opcodeBytes);

    bool checkDate(int d, int m, int y);

    bool postCommand(std::vector<std::string> keyWordsList, char *opcodeBytes);

    short getShort(char ch, int a);

    char *getChar(char ch, int a);

    std::string findWord(char ch, char delimiter);

public:

    ConnectionHandler(std::string host, short port);

    virtual ~ConnectionHandler();



    // Connect to the remote machine

    bool connect();



    // Read a fixed number of bytes from the server - blocking.

    // Returns false in case the connection is closed before bytesToRead bytes can be read.

    bool getBytes(char bytes[], unsigned int bytesToRead);



    // Send a fixed number of bytes from the client - blocking.

    // Returns false in case the connection is closed before all the data is sent.

    bool sendBytes(const char bytes[], int bytesToWrite);



    // Read an ascii line from the server

    // Returns false in case connection closed before a newline can be read.

    bool getLine(std::string &line);



    // Send an ascii line from the server

    // Returns false in case connection closed before all the data is sent.

    bool sendLine(std::string &line);



    // Get Ascii data from the server until the delimiter character

    // Returns false in case connection closed before null can be read.

    bool getFrameAscii(std::string &frame, char delimiter);



    // Send a message to the remote host.

    // Returns false in case connection is closed before all the data is sent.

    bool sendFrameAscii(const std::string &frame, char delimiter);



    // Close down the connection properly.

    void close();

    bool availableHandler();


    bool pmCommand(std::vector<std::string> keyWordsList, char *opcodeBytes);

    bool logStatCommand(char *opcodeBytes);

    bool statCommand(std::vector<std::string> keyWordsList, char *opcodeBytes);
}; //class ConnectionHandler



#endif