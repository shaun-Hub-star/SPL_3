package bgu.spl.net.api.bidi;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class clientProtocol implements BidiMessagingProtocol<String> {
//i added to the logout and connections diconnect int the ack
//NEED TO CHECK THE POST AND THE PM'S ..
    // NEED TO START WORKING WITH THE REACTOR
    // MAKE SURE TO LOOK FOR THE SYNCHRONIZATION AND WHERE TO DO IT
    //MAKE SURE TO KNOW WHEN TO CLOSE THE SERVER IF NEEDED .

    private Manager manager ;
    private Connections<String> connections;
    private List<String> followList;
    private List<String> PmPostedMessages;
    private int connectionID;
    private boolean shouldTerminate;

    private ReentrantReadWriteLock lock;
    public clientProtocol(Manager manager){
        this.manager=manager;
    }
    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connectionID=connectionId;
        this.connections=connections;
        this.shouldTerminate=false;
        followList= new ArrayList<>();
        PmPostedMessages= new ArrayList<>();
        manager=manager.getInstance();
        lock= manager.getLock();
    }

    @Override
    public void process(String message) {
        String messageType="";
        if(message.contains(" "))
              messageType=message.substring(0,message.indexOf(' '));
        else
            messageType=message;
        if(messageType.equals("1")){
            lock.writeLock().lock();
            register(message);
            lock.writeLock().unlock();
        }
        if(messageType.equals("2")){
            lock.writeLock().lock();
            logIn(message);
            lock.writeLock().unlock();
        }
        if(messageType.equals("3")){
            lock.writeLock().lock();
            logout();
            lock.writeLock().unlock();
        }
        if(messageType.equals("4")){
            lock.writeLock().lock();
            follow(message);
            lock.writeLock().unlock();
        }
        if(messageType.equals("5")){
            lock.writeLock().lock();
            Postmsg(message);
            lock.writeLock().unlock();
        }
        if(messageType.equals("6")){
            lock.writeLock().lock();
            PmMsg(message);
            lock.writeLock().unlock();
        }
        if(messageType.equals("7")){
            lock.writeLock().lock();
            userList();
            lock.writeLock().unlock();
        }
        if(messageType.equals("8")){
            lock.writeLock().lock();
            stat(message);
            lock.writeLock().unlock();
        }

    }

    private void register(String message){
        short opcode=1;
        if(manager.register(message,connectionID)){
            System.out.println("the client has registed succesfully");
            sendACK(opcode,"");
        }
        else
            sendError(opcode);

    }


    private void logIn(String message) {

            short opcode = 2;
            String mess = message.substring(message.indexOf(' ') + 1);
            String userName = mess.substring(0, mess.indexOf('\0'));

            if (manager.login(message, connectionID)) {
                System.out.println("log in succesfully");
                sendACK(opcode, "");
                if (!manager.getTheNotification(userName).isEmpty()) {
                    while (!manager.getTheNotification(userName).isEmpty()) {
                        try {
                            String s = manager.getTheNotification(userName).take();
                            sendNotificationToOne(s, manager.getUserToNotify(userName),userName);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }


                }
            } else {
                System.out.println("failed to log in ");
                sendError(opcode);
            }
        }

    private void logout() {

            short opcode = 3;
            if (manager.logout(connectionID)) {
                System.out.println("logged out succsesfuly ");
                sendACK(opcode, "");
                shouldTerminate = true;
                connections.disconnect(connectionID);
            } else {
                System.out.println("error to logout");

                sendError(opcode);
            }

        }


    private void follow(String message){
        System.out.println(message+" tttt");
        short opcode=4;
        if (!manager.loggedIn(connectionID)) {
            System.out.println("failed to follow");
            sendError(opcode);
        }
        else {
            boolean doneAtLeastOne = false;
            String userNameList = "";
            int count = 0;
            String mess = message.substring(message.indexOf(' ') + 1);
            char c = mess.charAt(0); //follow or not
            if(c==0){
                System.out.println("the c is : " + c + " ??????????????");
            }
            mess = mess.substring(mess.indexOf(' ') + 1);
            char n = mess.charAt(0);  //num of users to follow
            mess = mess.substring(mess.indexOf(' ') + 1);
            int x = Integer.parseInt(String.valueOf(n));
            System.out.println(x+" this is x");
            String userName = "";
            for (int i = 1; i <= x; i++) {
               if (i == x)
                   userName = mess;
                else
                    userName = mess.substring(0, mess.indexOf('\0'));

                if (c == '1') {   //unfollow
                    if(manager.registered(userName)) {
                        if (followList.contains(userName)) {

                            doneAtLeastOne = true;
                            followList.remove(userName);
                            userNameList += userName + '\0';
                            count++;
                            manager.unFollow(userName,connectionID);
                        }
                    }

                } else {
                    if(manager.registered(userName)) {
                        if (!followList.contains(userName)) {
                            System.out.println(userName+" TATATATATTAATTATATAATATATA");
                            doneAtLeastOne = true;
                            followList.add(userName);
                            userNameList += userName + '\0';
                            count++;
                            manager.follow(userName,connectionID);
                        }
                    }

                }
                mess = mess.substring(mess.indexOf('\0') + 1);
            }

            if (!doneAtLeastOne) {
                System.out.println("failed to follow");
                sendError(opcode);
            }
            else {
                System.out.println("success to follow");
                System.out.println(userNameList);
                sendACK(opcode, " "+count + " " + userNameList);
            }
        }

    }


    private void Postmsg(String message){
        short opcode=5;
        if(manager.loggedIn(connectionID)) {
            System.out.println("im in to post");
            String content = message.substring(message.indexOf(' ') + 1,message.length()-1);
            content=content+" ";
            List<String> userNames = new ArrayList<>();
            if (content.contains("@")) {
                String s = content.substring(content.indexOf('@'));
                System.out.println(s);
                while (s.contains("@")) {
                    if(s.contains(" ")) {
                        userNames.add(s.substring(1, s.indexOf(' ')));
                    }
                    s = s.substring(s.indexOf(' '));
                    if (s.contains("@"))
                        s = s.substring(s.indexOf('@'));
                }
            }


            String thisUser=manager.getUserName(connectionID);
            System.out.println("THIS USER " + thisUser);
            List<String> l=manager.getFollowers(thisUser);
            if(l!=null) {

                for (int i = 0; i < l.size(); i++) {
                    if (!userNames.contains(l.get(i)))
                        userNames.add(l.get(i));
                }
            }else{
                System.out.println("the L is NULLLL");
            }

            PmPostedMessages.add(message);
            manager.postMessage(userNames, content,manager.getUserName(connectionID));
            List<Integer> usersID= manager.getUsersToNotify(userNames);
            String postingUser=manager.getUserName(connectionID);
            short sh=1;
            String notificationtosend = sh+" "+postingUser+" "+content;

            sendNotification(sh+" "+postingUser+" "+content,usersID,userNames);

            System.out.println("posted succesfully :"+postingUser+" "+content);
            sendACK(opcode, "");
        }
        else {
            sendError(opcode);
            System.out.println("failed to post");
        }

    }


     private void PmMsg(String Message){
        short opcode=6;
        String mess = Message.substring(Message.indexOf(' ') + 1);
        String userName=mess.substring(0,mess.indexOf('\0'));
        String content= mess.substring(mess.indexOf('\0') + 1,mess.length()-1);
        if(!manager.loggedIn(connectionID)){
            sendError(opcode);}
        else {
            if(manager.pmMsg(userName, content,manager.getUserName(connectionID))){

                PmPostedMessages.add(Message);
                String postingUser=manager.getUserName(connectionID);
                System.out.println(userName+" is the user to notify");
                int userID=manager.getUserToNotify(userName);
                short sh=0;
                sendNotificationToOne(sh+" "+postingUser+" "+content,userID,userName);
                sendACK(opcode, "");
            }
            else
                sendError(opcode);
        }
    }




    private void userList(){
        short opcode=7;
        if(!manager.loggedIn(connectionID)){
            System.out.println("error");
            sendError(opcode);
            }
        else{
            LinkedBlockingQueue<String> usersList=manager.getOrderdusers();
            List<String> l=new LinkedList<>(usersList);
            int size = usersList.size();
            String userNames="";
            for(int i=0;i<l.size();i++){
                userNames+=l.get(i)+ '\0';
            }
            sendACK(opcode," "+ size +" "+userNames);
        }
    }


    private void stat(String message){
        short opcode=8;
        String mess = message.substring(message.indexOf(' ') + 1);
        String userName=mess.substring(0,mess.length()-1);

        if(!manager.loggedIn(connectionID)) {
            sendError(opcode);

        }
        else if (!manager.registered(userName)) {

            sendError(opcode);
        }

        else{
            int numOfPosts=manager.numOfposts(userName);
            int numOfFollowers=manager.numberOfFollowers(userName);
            int numOfFollowings=manager.numberOfFollowings(userName);
            sendACK(opcode," "+numOfPosts+" "+numOfFollowers+" "+numOfFollowings);
            System.out.println("heererererer3");
        }

    }

    private void sendNotification(String notification,List<Integer> usersID,List<String> usersNames){
        if(usersID!=null) {
            for (int i = 0; i < usersID.size(); i++) {
                if(manager.loggedIn(usersNames.get(i))){
                    connections.send(usersID.get(i), "9" + " "+ notification);

                }else
                    manager.pushNotification(usersNames.get(i),notification);
            }
        }
    }
    private void sendNotificationToOne(String notification,int userID ,String userName) {
        if (manager.loggedIn(userName)) {
            connections.send(userID, "9" + " " + notification);
        } else {
            manager.pushNotification(userName, notification);
        }
    }
    private void sendACK(short opcode,String ACKMess) {
            connections.send(connectionID, "10" + " " + opcode + ACKMess);
    }

    private void sendError(short opcode){
        connections.send(connectionID,"11"+" "+opcode);
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }
}
