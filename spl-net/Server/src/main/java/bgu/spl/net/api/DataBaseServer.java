package bgu.spl.net.api;

import bgu.spl.net.api.MessagePackage.BackMessage;
import bgu.spl.net.api.MessagePackage.Messages;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;

public class DataBaseServer implements DataBaseQueries {
    private ConcurrentHashMap<String, User> userMap;
    private ConcurrentHashMap<String, List<Messages>> userToHerMessages;


    public DataBaseServer() {
        this.userToHerMessages = new ConcurrentHashMap<>();
        this.userMap = new ConcurrentHashMap<>();
    }


    @Override
    public synchronized BackMessage register(String userName, String password, String birthDay, int id) {
        BackMessage backMessage;

        if (!userMap.containsKey(userName)) {
            User user = new User(userName, password, birthDay, id);
            userMap.put(userName, user);
            backMessage = new BackMessage("ACK 1", BackMessage.Status.PASSED);

        } else {
            backMessage = new BackMessage("ERROR 1", BackMessage.Status.ERROR);
        }
        return backMessage;
    }


    @Override
    public synchronized BackMessage login(String userName, String password, int captcha) {//ERROR 2
        BackMessage backMessage;
        if (userMap.containsKey(userName)) {
            User currentUser = userMap.get(userName);
            if (!currentUser.isLogin() && captcha == 1 && currentUser.getPassword().equals(password)) {
                backMessage = new BackMessage("ACK 2", BackMessage.Status.PASSED);
                currentUser.setLogin(true);
            } else
                backMessage = new BackMessage("ERROR 2", BackMessage.Status.ERROR);
        } else {
            backMessage = new BackMessage("ERROR 2", BackMessage.Status.ERROR);
        }
        return backMessage;

    }

    @Override
    public synchronized BackMessage logout(String userName) {
        BackMessage backMessage;

        if (userMap.containsKey(userName)) {
            User currentUser = userMap.get(userName);
            if (currentUser.isLogin()) {
                backMessage = new BackMessage("ACK 3", BackMessage.Status.PASSED);
                currentUser.setLogin(false);
            } else
                backMessage = new BackMessage("ERROR 3", BackMessage.Status.ERROR);

        } else {
            backMessage = new BackMessage("ERROR 3", BackMessage.Status.ERROR);
        }
        return backMessage;
    }


    //CLIENT#1< FOLLOW 1 Bird-person
//CLIENT#1> ACK 4 Bird-person
    @Override
    public synchronized BackMessage follow(String me, String to, int sign) {//backMessage(1) holds the actual message
        BackMessage backMessage;
        if (userMap.containsKey(me) && userMap.containsKey(to) & userMap.get(me).isLogin()) {
            User currentUser = userMap.get(me);
            User requestedUser = userMap.get(to);
            if (sign == 0 & !currentUser.getFollowing().contains(requestedUser.getUserName())&& !requestedUser.getBlocked().contains(currentUser.getUserName())) {
                backMessage = new BackMessage("ACK 4 0 " + requestedUser.getUserName(), BackMessage.Status.PASSED);
                currentUser.addFollowing(requestedUser.getUserName());
                requestedUser.addFollow(currentUser.getUserName());
            } else if (sign == 1 & currentUser.getFollowing().contains(requestedUser.getUserName())) {
                backMessage = new BackMessage("ACK 4 1 " + requestedUser.getUserName(), BackMessage.Status.PASSED);
                currentUser.stopFollowing(requestedUser.getUserName());
                requestedUser.moveFollower(currentUser.getUserName());

            } else {
                backMessage = new BackMessage("ERROR 4", BackMessage.Status.ERROR);

            }
        } else {
            backMessage = new BackMessage("ERROR 4", BackMessage.Status.ERROR);
        }
        return backMessage;

    }

    @Override
    public synchronized BackMessage post(String msg, String userName, List<String> tags) {//shaun

        List<User> tagged = getUsers(tags);
        String notification;
        BackMessage backMessage;

        if (userMap.containsKey(userName)) {
            User currentUser = userMap.get(userName);
            List<String> blocking = currentUser.getBlocked();
            currentUser.addPost();
            if (currentUser.isLogin()) {
                Messages keepMessage = null;
                try {
                    keepMessage = new Messages(msg, LocalDateTime.now());
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (!userToHerMessages.containsKey(userName)) {
                    List<Messages> messages = new LinkedList<>();
                    userToHerMessages.put(currentUser.getUserName(), messages);

                }
                backMessage = new BackMessage("ACK 5", BackMessage.Status.PASSED);
                notification = "NOTIFICATION Public " + userName + " " + keepMessage.getMessage();
                backMessage.setMessage(notification);
                userToHerMessages.get(currentUser.getUserName()).add(keepMessage);
                for (String s : userToHerMessages.keySet()) {
                    for (Messages m : userToHerMessages.get(s)) {
                    }
                }
                for (String tag : tags) {
                    if (blocking.contains(tag)){
                        tags.remove(tag);
                        continue;}
                    else if (userMap.containsKey(tag)) {
                        User tagUser = userMap.get(tag);
                        if (!tagUser.isLogin()) {
                            //push to the queue of notifications
                            tagUser.addNotification(notification);
                            tags.remove(tag);
                        }
                    } else {
                        backMessage = new BackMessage("ERROR 5", BackMessage.Status.ERROR);
                        return backMessage;
                    }
                }
                for (String followingMe : currentUser.getFollowers()) {
                    User followingMeUser = userMap.get(followingMe);
                    if (!tags.contains(followingMe)) {
                        if (!followingMeUser.isLogin()) {
                            //push to the queue of notifications
                            followingMeUser.addNotification(notification);
                        } else {
                            tags.add(followingMe);
                        }
                    }
                }
            } else
                backMessage = new BackMessage("ERROR 5", BackMessage.Status.ERROR);
            // }
        } else {
            backMessage = new BackMessage("ERROR 5", BackMessage.Status.ERROR);
        }
        return backMessage;
    }


    private List<User> getUsers(List<String> userNames) {
        List<User> users = new LinkedList<>();
        for (String username : userNames)
            users.add(userMap.get(username));
        return users;
    }

    @Override//NOTIFICATION PM Morty Bird-personaaaa [1]
    public synchronized BackMessage PM(String me, String userTo, String msg, LocalDateTime dateAndTime) {
        BackMessage backMessage;
        if (userMap.containsKey(me) && userMap.get(me).isLogin() && userMap.containsKey(userTo)) {
            User currentUser = userMap.get(me);
            User requestedUser = userMap.get(userTo);
            if (currentUser.getFollowing().contains(requestedUser.getUserName()) && !currentUser.getBlocked().contains(userTo)) {
                try {
                    Messages keepMessage = new Messages(msg, dateAndTime);
                    if (!userToHerMessages.containsKey(me)) {
                        List<Messages> messages = new LinkedList<>();
                        userToHerMessages.put(currentUser.getUserName(), messages);
                    }
                    userToHerMessages.get(currentUser.getUserName()).add(keepMessage);
                    backMessage = new BackMessage();
                    backMessage.setMessage("ACK 6");
                    String outputRequestedUser = "NOTIFICATION PM " + me + " " + keepMessage.getMessage()+keepMessage.getDateByStringOfMessage();
                    if (requestedUser.isLogin()) {
                        backMessage.setMessage(outputRequestedUser);//TODO check infront shoun
                    } else {
                        requestedUser.addNotification(outputRequestedUser);
                    }


                } catch (Exception ParseException) {
                    backMessage = new BackMessage("ERROR 6", BackMessage.Status.ERROR);
                }
            } else {
                backMessage = new BackMessage("ERROR 6", BackMessage.Status.ERROR);
            }

        } else {
            backMessage = new BackMessage("ERROR 6", BackMessage.Status.ERROR);
        }

        return backMessage;
    }

    //CLIENT#1> ACK 8 47 1 2 0 NOT EXACTLY
    public synchronized BackMessage logStat(String me) {
        BackMessage backMessage;
        if (userMap.containsKey(me) && userMap.get(me).isLogin()) {
            User currentUser = userMap.get(me);
            List<String> blockByCurrentUser = currentUser.getBlocked();
            List<String> logStatMessage = new LinkedList<>();
            for (String userName : userMap.keySet()) {
                if (!blockByCurrentUser.contains(userName) && userMap.get(userName).isLogin()) {
                    User loginUser = userMap.get(userName);
                    if (loginUser.details().equals("ERROR")) {
                        backMessage = new BackMessage("ERROR 7 ", BackMessage.Status.ERROR);
                        return backMessage;
                    } else {
                        logStatMessage.add("ACK 7 " + loginUser.details());
                    }
                }
            }
            backMessage = new BackMessage("", BackMessage.Status.PASSED);
            backMessage.setMessages(logStatMessage, BackMessage.Status.PASSED);
            return backMessage;
        } else {
            backMessage = new BackMessage("ERROR 7", BackMessage.Status.ERROR);
        }
        return backMessage;
    }

    //
    @Override
    public synchronized BackMessage stat(String userName, List<String> userNames) throws ParseException {//im not allowing any user not to be unregistered.
        BackMessage backMessage;
        User me;
        List<String> messages = new LinkedList<>();

        if (userMap.containsKey(userName)) {
            me = userMap.get(userName);
            List<String> blocking = me.getBlocked();

            if (me.isLogin()) {
                for (String user : userNames) {
                    if (blocking.contains(user)) {
                        backMessage = new BackMessage("ERROR 8", BackMessage.Status.ERROR);
                        return backMessage;
                    }
                    else{
                           User currentUser = userMap.get(user);
                    if (currentUser != null) {
                        int age = currentUser.getAge();
                        int numOfPosts = currentUser.getNumPosts();
                        int numOfFollowers = currentUser.getFollowing().size();
                        int numOfFollowing = currentUser.getFollowers().size();
                        String message = "ACK 8 " + age + " " + numOfPosts + " " + numOfFollowers + " " + numOfFollowing;
                        messages.add(message);

                    } else {//could be changed to do nothing
                        backMessage = new BackMessage("ERROR 8", BackMessage.Status.ERROR);
                        return backMessage;
                    }}
                }
                backMessage = new BackMessage(" ", BackMessage.Status.PASSED);
                backMessage.setMessages(messages, BackMessage.Status.PASSED);
                return backMessage;
            } else {
                backMessage = new BackMessage("ERROR 8", BackMessage.Status.ERROR);
                return backMessage;
            }

        } else {
            backMessage = new BackMessage("ERROR 8", BackMessage.Status.ERROR);
            return backMessage;
        }

    }
    //shaun

    @Override
    public BackMessage notification(String userName) {
        return null;
    }

    @Override
    public synchronized BackMessage block(String me, String toBlock) {
        BackMessage backMessage;
        User currentUser;
        User userToBlock;
        if (userMap.containsKey(me) && userMap.containsKey(toBlock)) {
            currentUser = userMap.get(me);
            userToBlock = userMap.get(toBlock);
            if (currentUser.isLogin()) {
                currentUser.addBlock(toBlock);
                userToBlock.addBlock(me);
                currentUser.stopFollowing(toBlock);
                currentUser.moveFollower(toBlock);
                userToBlock.stopFollowing(me);
                userToBlock.moveFollower(me);
                backMessage = new BackMessage("ACK 12", BackMessage.Status.PASSED);
            } else {
                backMessage = new BackMessage("ERROR 12", BackMessage.Status.ERROR);
            }
        } else {
            backMessage = new BackMessage("ERROR 12", BackMessage.Status.ERROR);
        }
        return backMessage;
    }

    @Override
    public synchronized Queue<String> getNotifications(String userName) {
        try {
            if (userMap.containsKey(userName) && userMap.get(userName).isLogin()) {
                User currentUser = userMap.get(userName);
                return currentUser.getNotification();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void deleteNotifications(String userName) {
        if (userMap.containsKey(userName)) {
            userMap.get(userName).deleteNotification();
        }

    }

    @Override
    public int getId(String userName) {//todo make sure when there is no such user
        if (userMap.containsKey(userName)) {
            if(userMap.get(userName).isLogin()){
            return userMap.get(userName).getId();}
            else return -1;
        }
        return -2;
    }
}
