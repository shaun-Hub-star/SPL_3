package bgu.spl.net.api.bidi;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Manager {
    private static Manager ourInstance = new Manager();
    private ConcurrentHashMap<String,String> users;
    private LinkedBlockingQueue<String> orderdusers;
    private ConcurrentHashMap<Integer,String> loggedInUsers;
    private ConcurrentHashMap<String,List<String>> postMsg;
    private ConcurrentHashMap<String,List<String>> pmMsg;
    private ConcurrentHashMap<String,Integer> numOfFollowers;
    private ConcurrentHashMap<String,Integer> numOfFollowings;
    private ConcurrentHashMap<String,Integer> usersList;
    private ConcurrentHashMap<Integer,String> ListOfUsers;
    private ConcurrentHashMap<String,Boolean> alreadyloggedin;
    private ConcurrentHashMap<String,List<String>> followers;
    private ConcurrentHashMap<String, LinkedBlockingQueue<String>> notifications;
    private ConcurrentHashMap<String,List<String>> userMessages;




    private static  ReentrantReadWriteLock userRwl_ ;
    private static final Lock userReadLock_ = userRwl_.readLock();
    private static final Lock userWriteLock_ = userRwl_.writeLock();


    public static Manager getInstance() {
        return ourInstance;
    }

    public Manager() {
        users= new ConcurrentHashMap<>();
        loggedInUsers= new ConcurrentHashMap<>();
        postMsg= new ConcurrentHashMap<>();
        pmMsg= new ConcurrentHashMap<>();
        numOfFollowers= new ConcurrentHashMap<>();
        numOfFollowings= new ConcurrentHashMap<>();
        usersList=new ConcurrentHashMap<>();
        ListOfUsers= new ConcurrentHashMap<>();
        alreadyloggedin = new ConcurrentHashMap<>();
        followers= new ConcurrentHashMap<>();
        notifications = new ConcurrentHashMap<>();
        orderdusers=new LinkedBlockingQueue<>();
        userRwl_ = new ReentrantReadWriteLock();
        userMessages=new ConcurrentHashMap<>();

    }
     public ReentrantReadWriteLock getLock(){
        return userRwl_;
     }

     public boolean register(String data,int connectionID){
        String mess=data.substring(data.indexOf(' ')+1);
        String userName=mess.substring(0,mess.indexOf('\0'));
        String password=mess.substring(mess.indexOf('\0')+1,mess.length()-1);
        if(users.containsKey(userName))
        return false;
        else{
            users.put(userName, password);
            usersList.put(userName,connectionID);
            System.out.println(userName+"  PIer DAmouny");
            notifications.put(userName,  new LinkedBlockingQueue<String>());
            try {
                orderdusers.put(userName);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return true;
        }
    }

    public boolean login(String data,int connectionID) {
        String mess = data.substring(data.indexOf(' ') + 1);
        String userName = mess.substring(0, mess.indexOf('\0'));
        String password = mess.substring(mess.indexOf('\0') + 1, mess.length() - 1);

            if (!users.containsKey(userName)) {
                System.out.println("here 1");
                return false;
            }

            if (loggedInUsers.containsKey(connectionID) | loggedInUsers.containsValue(userName)) {
                System.out.println("here 2");
                return false;
            } else {
                if (!users.get(userName).equals(password)) {
                    System.out.println("here 3");
                    return false;
                } else {
                    loggedInUsers.put(connectionID, userName);

                    usersList.replace(userName,connectionID);
                    System.out.println(usersList.get(userName)+" !#@!@!@!@!@!@!@!@!!@"+ userName);
                    return true;
                }
            }
        }


    public boolean logout(int connectionID) {


            if (!loggedInUsers.containsKey(connectionID))
                return false;
            else {
                String username = loggedInUsers.get(connectionID);
                loggedInUsers.remove(connectionID);

            }

            return true;
        }



    public boolean loggedIn(int id) {

            return loggedInUsers.containsKey(id);
    }

    public boolean loggedIn(String name) {

        return loggedInUsers.containsValue(name);
    }


    public boolean registered(String userName){

        return users.containsKey(userName);
    }

    public void postMessage(List<String> listOfUsers,String Message,String user){
        if(!userMessages.containsKey(user)){
            userMessages.put(user,new ArrayList<>());
        }
        userMessages.get(user).add(Message);
        if(listOfUsers!=null && listOfUsers.size()>0)
        for (int i=0;i<listOfUsers.size();i++){
            String userName=listOfUsers.get(i);
            if(postMsg.containsKey(userName)){
                postMsg.get(userName).add(Message);
            }
            if(!postMsg.containsKey(userName)){
                if(users.containsKey(userName)) {
                    postMsg.put(userName, new ArrayList<>());
                    postMsg.get(userName).add(Message);
                }

            }
        }
    }

    public int numOfposts(String userName){
        int n=0;
        if(userMessages.containsKey(userName))
            n=userMessages.get(userName).size();
        return n;
    }

    public Boolean pmMsg(String userName,String Message,String user){
        if(!userMessages.containsKey(user)){
            userMessages.put(user,new ArrayList<>());
        }
        userMessages.get(user).add(Message);
        if(!this.registered(userName))
           return false;
        if(pmMsg.containsKey(userName))
            pmMsg.get(userName).add(Message);
        else{
            pmMsg.put(userName,new ArrayList<>());
            pmMsg.get(userName).add(Message);
        }
        return true;

    }

    public LinkedBlockingQueue<String> getOrderdusers() {
        return orderdusers;
    }

    public List<String> usersList(){
        Set<String> set=users.keySet();
        return new ArrayList<>(set);
    }

    public List<String> getFollowers(String userName){
        if(followers.containsKey(userName)) {
            return followers.get(userName);
        }
        else
            return null;
    }

    public void follow(String userName,int connectionID){
        if(followers.containsKey(userName)){

            followers.get(userName).add(loggedInUsers.get(connectionID));
        }
        else
        {
            followers.put(userName,new ArrayList<String>());
            followers.get(userName).add(loggedInUsers.get(connectionID));
        }
        System.out.println("the follwers of " + userName + "is"+followers.get(userName).get(0));

        if(numOfFollowers.containsKey(userName))
            numOfFollowers.replace(userName,numOfFollowers.get(userName)+1);
        else
            numOfFollowers.put(userName,1);
        String user=loggedInUsers.get(connectionID);
        if(numOfFollowings.containsKey(user))
            numOfFollowings.replace(user,numOfFollowings.get(user)+1);
        else
            numOfFollowings.put(user,1);

    }

    public void unFollow(String userName,int connectionID){
        if(followers.containsKey(userName)){
            followers.get(userName).remove(loggedInUsers.get(connectionID));

        }

        if(numOfFollowers.get(userName)>0)
          numOfFollowers.replace(userName,numOfFollowers.get(userName)-1);
        String user=loggedInUsers.get(connectionID);
        if(numOfFollowings.get(user)>0)
            numOfFollowings.replace(user,numOfFollowings.get(user)-1);
    }

    public int numberOfFollowers(String userName){
        if(numOfFollowers.containsKey(userName))
           return numOfFollowers.get(userName);
        else
            return 0;
    }
    public int numberOfFollowings(String userName){
        System.out.println(userName);
        if(numOfFollowings.containsKey(userName))
            return numOfFollowings.get(userName);
        else
            return 0;
    }

    public List<Integer> getUsersToNotify(List<String> users){
        List<Integer> toReturn=new ArrayList<>();
        for(int i=0;i<users.size();i++){
            toReturn.add(usersList.get(users.get(i)));
        }
        return toReturn;
    }
    public int getUserToNotify(String user){
        return (usersList.get(user));
    }

    public String getUserName(int connectionsID){
        String username =  loggedInUsers.get(connectionsID);
        return username;
    }

    public void pushNotification(String username,String notification) {
        try {
            notifications.get(username).put(notification);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public LinkedBlockingQueue<String> getTheNotification(String username){
        return notifications.get(username);
    }
}
