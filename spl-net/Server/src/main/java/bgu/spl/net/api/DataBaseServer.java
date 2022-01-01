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
    
    private static DataBaseServer dataBaseServer = null;//TODO thread safe singleton


    public DataBaseServer(){
        this.userToHerMessages = new ConcurrentHashMap<>();
        this.userMap = new ConcurrentHashMap<>();
    }

   /* private DataBaseServer(){
        //TODO
        userMap = new ConcurrentHashMap<>();
        userToHerMessages = new ConcurrentHashMap<>();
    }
    public static DataBaseServer getInstance(){
        if(dataBaseServer == null){
            dataBaseServer = new DataBaseServer();
        }
        return dataBaseServer;
    }*/

    @Override
    public BackMessage register(String userName) {
        return null;
    }

    @Override
    public BackMessage login(String userName) {
        return null;
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
