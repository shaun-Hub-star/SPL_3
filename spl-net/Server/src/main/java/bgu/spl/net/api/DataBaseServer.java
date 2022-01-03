package bgu.spl.net.api;

import bgu.spl.net.api.MessagePackage.BackMessage;
import bgu.spl.net.api.MessagePackage.Messages;
import bgu.spl.net.api.MessagePackage.Notification;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class DataBaseServer implements DataBaseQueries {
    private ConcurrentHashMap<String,User> userMap;
    private ConcurrentHashMap<String,List<Messages>> userToHerMessages;
    private final Object o1=new Object();
    private Object o2=new Object();

    public DataBaseServer(){
        this.userToHerMessages = new ConcurrentHashMap<>();
        this.userMap = new ConcurrentHashMap<>();
    }


    @Override
    public BackMessage register(String userName,String password,String birthDay,int id) {
        BackMessage backMessage;
        synchronized (o1) {
            if (!userMap.containsKey(userName)) {
                User user = new User(userName, password, birthDay, id);
                userMap.put(userName, user);
                backMessage = new BackMessage("ACK 1", BackMessage.Status.PASSED);
            } else {
                backMessage = new BackMessage("ERROR 1", BackMessage.Status.ERROR);
            }
            return backMessage;
        }
    }

    @Override
    public BackMessage login(String userName,String password) {//ERROR 2
        BackMessage backMessage;
        synchronized (o1) {
            if (userMap.containsKey(userName)) {
                User currentUser = userMap.get(userName);
                if (!currentUser.isLogin()) {
                    backMessage = new BackMessage("ACK 2", BackMessage.Status.PASSED);
                    currentUser.setLogin(true);
                } else
                    backMessage = new BackMessage("ERROR 2", BackMessage.Status.ERROR);

            } else {
                backMessage = new BackMessage("ERROR 2", BackMessage.Status.ERROR);
            }
            return backMessage;
        }
    }

    @Override
    public BackMessage logout(String userName) {
        return null;
    }//shaun

    //CLIENT#1< FOLLOW 1 Bird-person
    //CLIENT#1> ACK 4 Bird-person
    @Override
    public BackMessage follow(String me, String to) {//backMessage(1) holds the actual message
        return null;
    }//lior

    @Override
    public BackMessage post(String msg, String userName, List<String> tags) {//shaun
        //ack will be in 0
        //NOTIFICATION Public Rick Gubba @Bird-person Gubba should be backMessage[1]
        List<User> users = getUsers(tags);
        return null;
    }
    private List<User> getUsers(List<String> userNames){
        List<User> users = new LinkedList<>();
        for(String username : userNames)
            users.add(userMap.get(username));
        return users;
    }
    @Override//NOTIFICATION PM Morty Bird-personaaaa [1]
    public BackMessage PM(String me, String userTo, String msg, String dateAndTime) {
        return null;
    }//lior
    //CLIENT#1> ACK 8 47 1 2 0 NOT EXACTLY
    @Override
    public BackMessage logStat() {
        return null;
    }//lior
    //CLIENT#1> ACK 8 47 1 2 0 NOT EXACTLY
    @Override
    public BackMessage stat(List<String> userNames) {
        return null;
    }//shaun

    @Override
    public BackMessage notification(String userName) {
        return null;
    }

    @Override
    public BackMessage block(String me, String toBlock) {
        return null;
    }//shaun

    @Override
    public Queue<Notification> getNotifications(String userName) {//TODO delete the notifications which i got or to save all the no' in Q and take from there
        return null;
    }//lior

    @Override
    public int getId(String userName) {//todo
        return userMap.get(userName).getId();
    }
}
