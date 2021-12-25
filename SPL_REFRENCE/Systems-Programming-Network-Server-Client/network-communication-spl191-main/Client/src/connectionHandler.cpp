#include "../include/connectionHandler.h"
#include <string>
#include <boost/asio/ip/tcp.hpp>

using boost::asio::ip::tcp;

using std::cin;
using std::cout;
using std::cerr;
using std::endl;
using std::string;

ConnectionHandler::ConnectionHandler(string host, short port): host_(host), port_(port), io_service_(), socket_(io_service_){}

ConnectionHandler::~ConnectionHandler() {
    close();
}
void shortToBytes(short num, char* bytesArr)
{
    bytesArr[0] = static_cast<char>((num >> 8) & 0xFF);
    bytesArr[1] = static_cast<char>(num & 0xFF);

}
short bytesToShort(char* bytesArr)
{
    short result = (short)((bytesArr[0] & 0xff) << 8);
    result += (short)(bytesArr[1] & 0xff);
    return result;
}

bool ConnectionHandler::connect() {
    try {
        tcp::endpoint endpoint(boost::asio::ip::address::from_string(host_), port_);
        boost::system::error_code error;
        socket_.connect(endpoint, error);
        if (error)
            throw boost::system::system_error(error);
    }
    catch (std::exception& e) {
        std::cerr << "Connection failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::getBytes(char bytes[], unsigned int bytesToRead) {
    size_t tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToRead > tmp ) {
            tmp += socket_.read_some(boost::asio::buffer(bytes+tmp, bytesToRead-tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendBytes(const char bytes[], int bytesToWrite) {
    int tmp = 0;
    boost::system::error_code error;
    try {
        while (!error && bytesToWrite > tmp ) {
            tmp += socket_.write_some(boost::asio::buffer(bytes + tmp, bytesToWrite - tmp), error);
        }
        if(error)
            throw boost::system::system_error(error);
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}


bool ConnectionHandler::getLine(std::string& line) {
    return getFrameAscii(line, '\n');
}

bool ConnectionHandler::sendLine(std::string& line) {
    return sendFrameAscii(line, '\0');
}


bool ConnectionHandler::getFrameAscii(std::string& frame, char delimiter) {
    char ch;
    char op1;
    char op2;
    string s="";
    string toreturn = "";
    string opcode2="";
    char delimitertocut = '\0';
    try {
        getBytes(&op1, 1);
        getBytes(&op2, 1);
        char* bytestoconvert = new char[2];
        bytestoconvert[0]=op1;
        bytestoconvert[1]=op2;
        short opcode = bytesToShort(bytestoconvert);



        if(opcode==9){
            s="NOTIFICATION";
            char op3;
            getBytes(&op3, 1);
            if(op3==0){
                s+=" PM ";
            }
            if(op3==1){
                s+=" Public ";
            }
            string postinUser="";
            do {
                getBytes(&ch, 1);
                postinUser.append(1, ch);
            } while (delimitertocut != ch);
            s+=postinUser+" ";
            string content="";
            do {
                getBytes(&ch, 1);
                content.append(1, ch);
            } while (delimitertocut != ch);
            //getBytes(&op3, 1);

            s+=content;
            frame=s;
        }
        if(opcode==10){
            s="ACK";
            char op3;
            char op4;
            getBytes(&op3, 1);
            getBytes(&op4, 1);
            char* bytestoconvert1 = new char[2];
            bytestoconvert1[0]=op3;
            bytestoconvert1[1]=op4;
            short opcode1 = bytesToShort(bytestoconvert1);

            if(opcode1==4) {//FOLLOW OPCODE
                opcode2 = std::to_string(opcode1);
                string  frame1 = "ACK " + opcode2 + " ";
                char op5;
                char op6;
                getBytes(&op5, 1);
                getBytes(&op6, 1);
                char *bytestoconvert12 = new char[2];
                bytestoconvert12[0] = op5;
                bytestoconvert12[1] = op6;
                short opcode12 = bytesToShort(bytestoconvert12);//NumOFUSers
                string numofusers = std::to_string(opcode12);//numofUsers
                int i = 0;
                std::vector<string> usernamesStrings;
                string nameofuser ="";

                while (i < opcode12) {
                    do {
                        getBytes(&ch, 1);
                        nameofuser.append(1, ch);
                    } while (delimitertocut != ch);

                    usernamesStrings.push_back(nameofuser);
                    nameofuser="";
                    i++;
                }
                string userlist = "";
                for (unsigned int j = 0; j < usernamesStrings.size() ; ++j) {
                    userlist = userlist + usernamesStrings.at(j) + " ";
                }

               // cout<<"ASASASSA"<<endl;
               frame1+=numofusers+" "+ userlist;

                frame = frame1;

            }


            if(opcode1==7){
                opcode2 = std::to_string(opcode1);
                string  frame1 = "ACK " + opcode2 + " ";
                char op5;
                char op6;
                getBytes(&op5, 1);
                getBytes(&op6, 1);
                char *bytestoconvert12 = new char[2];
                bytestoconvert12[0] = op5;
                bytestoconvert12[1] = op6;
                short opcode12 = bytesToShort(bytestoconvert12);//NumOFUSers
                string numofusers = std::to_string(opcode12);//numofUsers
                int i = 0;
                std::vector<string> usernamesStrings;
                string nameofuser ="";


                while (i < opcode12) {
                    do {
                        getBytes(&ch, 1);

                        nameofuser.append(1, ch);
                    } while (delimitertocut != ch);



                    usernamesStrings.push_back(nameofuser);
                    nameofuser="";
                    i++;
                }
                string userlist = "";
                for (unsigned int j = 0; j < usernamesStrings.size() ; ++j) {

                    userlist = userlist + usernamesStrings.at(j) + " ";
                }

                frame1+=numofusers+" "+ userlist;
                frame = frame1;
            }

            if (opcode1==8){
                opcode2 = std::to_string(opcode1);
                string  frame1 = "ACK " + opcode2 + " ";
                char op5;
                char op6;
                getBytes(&op5, 1);
                getBytes(&op6, 1);
                char *bytestoconvert12 = new char[2];
                bytestoconvert12[0] = op5;
                bytestoconvert12[1] = op6;
                short opcode12 = bytesToShort(bytestoconvert12);//NumOFPOSTS
                string numofPOSTS = std::to_string(opcode12);//numofPOSTS
                char op7;
                char op8;
                getBytes(&op7, 1);
                getBytes(&op8, 1);
                char *bytestoconvert123 = new char[2];
                bytestoconvert123[0] = op7;
                bytestoconvert123[1] = op8;
                short opcode123 = bytesToShort(bytestoconvert123);//numofFOLLOWERS
                string numofFOLLOWERS = std::to_string(opcode123);//numofFOLLOWERS
                char op9;
                char op10;
                getBytes(&op9, 1);
                getBytes(&op10, 1);
                char *bytestoconvert1234 = new char[2];
                bytestoconvert1234[0] = op9;
                bytestoconvert1234[1] = op10;
                short opcode1234 = bytesToShort(bytestoconvert1234);//numofFOLLOWEINGS
                string numofFOLLOWEINGS = std::to_string(opcode1234);//numofFOLLOWEINGS

                frame1+=numofPOSTS+" "+numofFOLLOWERS+" "+numofFOLLOWEINGS;
                frame=frame1;


            }
            if(opcode1==1||opcode1==2||opcode1==3||opcode1==5||opcode1==6){
                opcode2 = std::to_string(opcode1);
                frame="ACK "+opcode2;

            }







        }if (opcode==11){
            char op3;
            char op4;
            s="ERROR";
            getBytes(&op3, 1);
            getBytes(&op4, 1);
            char* bytestoconvert1 = new char[2];
            bytestoconvert1[0]=op3;
            bytestoconvert1[1]=op4;
            short opcode1 = bytesToShort(bytestoconvert1);

            opcode2 = std::to_string(opcode1);
            frame="ERROR "+opcode2;

        }

      /*  do{
            getBytes(&ch, 1);
            frame.append(1, ch);
        }while (delimiter != ch);*/
    } catch (std::exception& e) {
        std::cerr << "recv failed (Error: " << e.what() << ')' << std::endl;
        return false;
    }
    return true;
}

bool ConnectionHandler::sendFrameAscii(const std::string& frame, char delimiter) {
     short opcode=0;
    bool result= false;
    char *bytesarr=new char[2];
    string type="";
    if(frame.find(" ")!= std::string::npos){
        type=frame.substr(0,frame.find(' '));
    } else
        type=frame;



    if (type=="REGISTER"){
        opcode=1;
        shortToBytes(opcode,bytesarr);
        string s3=frame.substr(frame.find(' ')+1);
        string s=s3.substr(0,s3.find(' '));  //userName
        string s1= s3.substr(s3.find(' ')+1);
        const char* el = s.c_str();
        const char* el1 = s1.c_str();
        const char* el2=new char('\0');
        result=sendBytes(bytesarr,2) & sendBytes(el,s.length())& sendBytes(el2,1) &sendBytes(el1,s1.length()) & sendBytes(el2,1) ;

    }
    if (type=="LOGIN"){
        opcode=2;
        shortToBytes(opcode,bytesarr);
        string s3=frame.substr(frame.find(' ')+1);
        string s=s3.substr(0,s3.find(' '));
        string s1= s3.substr(s3.find(' ')+1);
        const char* el = s.c_str();
        const char* el1 = s1.c_str();
        const char* el2=new char('\0');
        result=sendBytes(bytesarr,2) & sendBytes(el,s.length())& sendBytes(el2,1) &sendBytes(el1,s1.length())& sendBytes(el2,1);

    }
    if (type=="LOGOUT"){
        opcode=3;
        shortToBytes(opcode,bytesarr);

        result=sendBytes(bytesarr,2);
    }
    if (type=="FOLLOW"){
        char *bytesarr1=new char[2];
        opcode=4;
        shortToBytes(opcode,bytesarr);
        result=sendBytes(bytesarr,2);
        string s3=frame.substr(frame.find(' ')+1);  //s3=0 2 ..
        string type = s3.substr(0,s3.find(' '));
        char* ch=new char((char)std::stoi(type));
        s3=s3.substr(s3.find(' ')+1);
        string numOfUsersToF=s3.substr(0,s3.find(' '));
        short numOfUsersToFollow= static_cast<short>(std::stoi(numOfUsersToF));


        shortToBytes(numOfUsersToFollow,bytesarr1);
        result=result & sendBytes(ch,1) & sendBytes(bytesarr1,2);
        const char* el2=new char('\0');

        string userName="";
        s3=s3.substr(s3.find(' ')+1);
        for(int i=1;i<=numOfUsersToFollow;i++){
            if (i == numOfUsersToFollow)
                userName = s3;
            else
                userName = s3.substr(0, s3.find(' '));
            const char* el=userName.c_str();

            result= result & sendBytes(el, static_cast<int>(userName.length())) & sendBytes(el2, 1);
            s3=s3.substr(s3.find(' ')+1);
        }

    }
    if (type=="POST"){
        opcode=5;
        shortToBytes(opcode,bytesarr);
        string s3=frame.substr(frame.find(' ')+1);  //s3=0 2 ..
        const char* el = s3.c_str();
        const char* el2=new char('\0');
        result=sendBytes(bytesarr,2) & sendBytes(el, static_cast<int>(s3.length())) & sendBytes(el2,1);
    }
    if (type=="PM"){
        opcode=6;
        shortToBytes(opcode,bytesarr);
        string s3=frame.substr(frame.find(' ')+1);
        string s=s3.substr(0,s3.find(' '));  //userName
        string s1= s3.substr(s3.find(' ')+1);  //content
        const char* el = s.c_str();
        const char* el1 = s1.c_str();
        const char* el2=new char('\0');
        result=sendBytes(bytesarr,2) & sendBytes(el,s.length())& sendBytes(el2,1) &sendBytes(el1,s1.length()) & sendBytes(el2,1);

    }
    if (type=="USERLIST"){
        opcode=7;
        shortToBytes(opcode,bytesarr);
        result=sendBytes(bytesarr,2);
    }
    if (type=="STAT"){
        opcode=8;
        shortToBytes(opcode,bytesarr);
        string s3=frame.substr(frame.find(' ')+1);

        const char* el = s3.c_str();
        const char* el2=new char('\0');
        result=sendBytes(bytesarr,2) & sendBytes(el,s3.length())& sendBytes(el2,1);
    }

   // bool result=sendBytes(bytesarr,2) & sendBytes(el,s.length())& sendBytes(el2,1) &sendBytes(el1,s1.length());
    if(!result) return false;

    return true;
}


void ConnectionHandler::close() {
    try{
        socket_.close();
    } catch (...) {
        std::cout << "closing failed: connection already closed" << std::endl;
    }
}

bool ConnectionHandler::availableHandler(){
    return socket_.available() > 0;
}
