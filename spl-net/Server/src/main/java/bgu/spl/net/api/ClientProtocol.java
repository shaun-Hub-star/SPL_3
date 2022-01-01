package bgu.spl.net.api;

import bgu.spl.net.api.MessagePackage.BackMessage;
import bgu.spl.net.api.MessagePackage.Notification;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ClientProtocol implements BidiMessagingProtocol<String> {

    private Connections<String> connections;
    private DataBaseServer dataBaseServer;
    private boolean shouldTerminate = false;
    private int connectionId;
    private String userName;

    public ClientProtocol(DataBaseServer dataBaseServer) {
        this.dataBaseServer = dataBaseServer;
    }

    @Override
    public void process(String msg) {
        if (msg != null) {
            String[] separatedBySpace = msg.split("\0");
            switch (getOpcode(msg)) {
                case 1://register
                    register(separatedBySpace);
                    break;
                case 2://login
                    login(separatedBySpace);
                    break;

                case 3://logout
                    logout();
                    break;

                case 4:
                    follow(separatedBySpace);
                    break;
                case 5://post
                    post(separatedBySpace);
                    break;
                case 6://pm
                    PM(separatedBySpace);
                    break;
                case 7:
                    logStat();
                    break;
                case 8:
                    stat(separatedBySpace);
                    break;
            }
        } else {
            System.out.println("message was null");
        }

    }

    private void stat(String[] separated) {
        BackMessage backMessage;
        List<String> users = getUsers(separated);
        backMessage = dataBaseServer.stat(users);
        if (backMessage.getStatus() == BackMessage.Status.PASSED) {
            List<String> ackStatMessages = backMessage.getMessages();
            for (String message : ackStatMessages) {
                if (!connections.send(connectionId, message)) {
                    System.out.println("failed to send ack message " + message);
                }
            }
        } else if (!connections.send(connectionId, backMessage.getMessage())) {
            System.out.println("failed to send an error message" + backMessage.getMessage());
        }
    }

    private void logStat() {
        BackMessage backMessage;
        backMessage = dataBaseServer.logStat();
        if (backMessage.getStatus() == BackMessage.Status.PASSED) {
            List<String> logStatMessages = backMessage.getMessages();
            for (String log : logStatMessages) {
                if (!connections.send(connectionId, log))
                    System.out.println("failed to send ack logstat message " + log);
            }
        } else if (!connections.send(connectionId, backMessage.getMessage())) {
            System.out.println("failed to send error of type logstat message with the error" + backMessage.getMessage());
        }
    }

    private void PM(String[] separated) {
        BackMessage backMessage;
        String to = getToSendMessage(separated);
        String messageContent = getMessageContent(separated);
        String time = getDateAndTime(separated);
        backMessage = dataBaseServer.PM(userName, to, messageContent, time);
        String name = "pm";
        sendMessage(backMessage, name);
    }

    private void sendMessage(BackMessage backMessage, String name) {
        if (backMessage.getStatus() == BackMessage.Status.PASSED) {
            if (!connections.send(connectionId, backMessage.getMessage())) {
                System.out.println("failed to send ack " + name + " message");
            }
        } else {
            if (!connections.send(connectionId, backMessage.getMessage())) {
                System.out.println("failed to send error " + name + " message");
            }
        }
    }

    private void post(String[] separated) {
        BackMessage backMessage;
        String content = getContent(separated);
        List<String> tags = getTags(separated);
        backMessage = dataBaseServer.post(content, this.userName, tags);
        String name = "post";
        sendMessage(backMessage, name);
    }

    private void follow(String[] separated) {
        BackMessage backMessage;
        String follow = getFollow(separated);
        backMessage = dataBaseServer.follow(userName, follow);
        String name = "follow";
        sendMessage(backMessage, name);
    }

    private void logout() {//TODO make the backMessage an ack message
        BackMessage backMessage;
        backMessage = dataBaseServer.logout(this.userName);
        if (backMessage.getStatus() == BackMessage.Status.PASSED) {
            if (!connections.send(connectionId, backMessage.getMessage()))
                System.out.println("error while sending logout ack");
            this.userName = "";
            connections.disconnect(connectionId);
            shouldTerminate = true;
        } else {
            if (!connections.send(connectionId, backMessage.getMessage()))
                System.out.println("error while sending the logout error " + backMessage.getMessages());
        }
    }

    private void login(String[] separated) {//TODO make the backMessage an ack message
        BackMessage backMessage;
        userName = getUserName(separated);
        String password = getUserPassword(separated);
        backMessage = dataBaseServer.login(userName,password);
        if (backMessage.getStatus() == BackMessage.Status.PASSED) {
            if (!connections.send(connectionId, userName)) {
                System.out.println("error while sending login ack");
            } else {
                Queue<Notification> notifications = dataBaseServer.getNotifications(userName);
                while (!notifications.isEmpty()) {
                    if (!connections.send(connectionId, notifications.poll().getMessages()))
                        System.out.println("error while sending the notification");
                }
            }
        } else {
            if (!connections.send(connectionId, backMessage.getMessage())) {
                System.out.println("error while sending register error " + backMessage.getMessage());
            }
        }
    }

    private void register(String[] separated) {//TODO make the backMessage an ack message
        BackMessage backMessage;
        userName = getUserName(separated);
        String password = getUserPassword(separated);
        String date = getUserBirthday(separated);
        
        backMessage = dataBaseServer.register(userName,password,date,connectionId);
        String name = "register";
        sendMessage(backMessage, name);

    }

    private String getUserBirthday(String[] separated) {
        return separated[2];
    }

    private String getUserPassword(String[] separated) {
        return separated[3];
    }


    private int getOpcode(String msg) {
        return Integer.parseInt(msg.substring(2));
    }

    private List<String> getUsers(String[] separated) {
        List<String> users = new LinkedList<>();
        String usersWithDelimiter = separated[1];
        String[] usersArray = usersWithDelimiter.split("\\|");
        Collections.addAll(users, usersArray);
        return users;
    }

    private String getDateAndTime(String[] separated) {
        return separated[3];
    }

    private String getToSendMessage(String[] separated) {
        return separated[2];
    }

    private String getMessageContent(String[] separated) {
        return separated[1];
    }

    private List<String> getTags(String[] separated) {
        List<String> userNameList = new LinkedList<>();
        String[] spaceSeparated = separated[1].split(" ");
        for (String s : spaceSeparated) {
            if (s.charAt(0) == '@') {
                userNameList.add(s.substring(1));
            }
        }
        return userNameList;
    }

    private String getContent(String[] separated) {
        return separated[1];
    }

    private String getFollow(String[] separated) {//the person i want to follow
        return separated[2];
    }

    @Override
    public boolean shouldTerminate() {
        return shouldTerminate;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {
        this.connectionId = connectionId;
        this.connections = connections;
        userName = "";
    }

    private String getUserName(String[] separated) {//in login and in register
        return separated[1];
    }
}


