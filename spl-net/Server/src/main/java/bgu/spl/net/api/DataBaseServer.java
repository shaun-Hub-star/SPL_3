package bgu.spl.net.api;

import bgu.spl.net.api.MessagePackage.BackMessage;
import bgu.spl.net.api.MessagePackage.Messages;
import bgu.spl.net.api.MessagePackage.Notification;

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
    public BackMessage login(String userName,String password) {
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
    }

    @Override
    public BackMessage follow(String me, String to) {
        return null;
    }

    @Override
    public BackMessage post(String msg, String userName, List<String> tags) {
        return null;
    }

    @Override
    public BackMessage PM(String me, String userTo, String msg, String dateAndTime) {
        return null;
    }

    @Override
    public BackMessage logStat() {
        return null;
    }

    @Override
    public BackMessage stat(List<String> userNames) {
        return null;
    }

    @Override
    public BackMessage notification(String userName) {
        return null;
    }

    @Override
    public BackMessage block(String me, String toBlock) {
        return null;
    }

    @Override
    public Queue<Notification> getNotifications(String userName) {
        return null;
    }
}
