Server:
mvn clean
mvn compile
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.TPCMain" -Dexec.args="7777"
// for reactor:
mvn exec:java -Dexec.mainClass="bgu.spl.net.impl.BGSServer.ReactorMain" -Dexec.args="7777 8"

Client:
make
./bin/BGSClient 127.0.0.1 7777

valgrind --leak-check=full --show-reachable=yes BGSClient 127.0.0.1 7777

Examples:
REGISTER shaun 123 28-04-2002
LOGIN shaun 123 1
REGISTER lior 123 27-08-1997
LOGIN lior 123 1
FOLLOW 0 shaun
PM shaun hi shaun its me lior
//in shaun
POST hello to my followers
LOGSTAT
BLOCK lior
POST hello to my followers 2
POST hello to my followers @lior
PM lior hi lior i blocked you
LOGIN <name> <password> <0/1>
LOGOUT
FOLLOW <0=follow / 1=unfollow> <name>
POST <content>
PM <name> <content>
LOGSTAT
STAT <userlist = name1|name2|...>
BLOCK <name>


You can find the filtered words at line 12 in src.java.bgu.spl.net.MessagePackage.Messages
