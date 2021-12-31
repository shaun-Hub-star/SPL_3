package bgu.spl.net.api;

import bgu.spl.net.api.MessagePackage.BackMessage;
import bgu.spl.net.api.MessagePackage.Notification;

import java.util.List;
import java.util.Queue;

public class ClientProtocol implements BidiMessagingProtocol<String> {

    private Connections<String> connections;
    private DataBaseServer dataBaseServer;
    private boolean shouldTerminate = false;
    private int connectionId;
    private String userName;

    @Override
    public void process(String msg) {
        BackMessage backMessage;
        if (msg != null) {
            switch (getOpcode(msg)) {
                case 1://register
                    register(msg);
                    break;
                case 2://login
                    login(msg);
                    break;

                case 3://logout
                    logout(msg);
                    break;

                case 4:
                    follow(msg);
                    break;
                case 5://post
                    post(msg);
                    break;
                case 6://pm
                    PM(msg);
                    break;
                case 7:
                    backMessage = dataBaseServer.logStat();
                    break;
                case 8:
                    List<String> users = getUsers(msg);
                    backMessage = dataBaseServer.stat(users);
                    break;

            }
        }

    }

    private void PM(String msg) {
        BackMessage backMessage;
        String to = getToSendMessage(msg);
        String messageContent = getMessageContent(msg);
        String time = getDateAndTime(msg);
        backMessage = dataBaseServer.PM(userName, to, messageContent, time);
        if(backMessage.getStatus() == BackMessage.Status.PASSED){
            if(!connections.send(connectionId, backMessage.getMessage())){
                System.out.println("failed to send ack pm message");
            }
        }else{
            if(!connections.send(connectionId, backMessage.getMessage())){
                System.out.println("failed to send error pm message");
            }
        }
    }

    private void post(String msg) {
        BackMessage backMessage;
        String content = getContent(msg);
        List<String> tags = getTags(msg);
        backMessage = dataBaseServer.post(content, this.userName, tags);
        if(backMessage.getStatus() == BackMessage.Status.PASSED){
            if(!connections.send(connectionId, backMessage.getMessage())){
                System.out.println("failed to send ack post message");
            }
        }else{
            if(!connections.send(connectionId, backMessage.getMessage())){
                System.out.println("failed to send error post message");
            }
        }
    }

    private void follow(String msg) {
        BackMessage backMessage;
        String follow = getFollow(msg);
        backMessage = dataBaseServer.follow(userName, follow);
        if(backMessage.getStatus() == BackMessage.Status.PASSED){
            if(!connections.send(connectionId, backMessage.getMessage())){
                System.out.println("failed to send ack follow message");
            }
        }else{
            if(!connections.send(connectionId, backMessage.getMessage())){
                System.out.println("failed to send error follow message");
            }
        }
    }

    private void logout(String userName) {//TODO make the backMessage an ack message
        BackMessage backMessage;
        backMessage = dataBaseServer.logout(this.userName);
        if (backMessage.getStatus() == BackMessage.Status.PASSED) {
            if (!connections.send(connectionId, userName))
                System.out.println("error while sending logout ack");
            this.userName = "";
            connections.disconnect(connectionId);
            shouldTerminate = true;
        } else {
            if (!connections.send(connectionId, backMessage.getMessage()))
                System.out.println("error while sending the logout error " + backMessage.getMessages());
        }
    }

    private void login(String userName) {//TODO make the backMessage an ack message
        BackMessage backMessage;
        userName = getUserName(userName);
        backMessage = dataBaseServer.login(userName);
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
                System.out.println("error while sending register error "+backMessage.getMessage());
            }
        }
    }

    private void register(String userName) {//TODO make the backMessage an ack message
        BackMessage backMessage;
        userName = getUserName(userName);
        backMessage = dataBaseServer.register(userName);
        if (backMessage.getStatus() == BackMessage.Status.PASSED) {
            if (!connections.send(connectionId, backMessage.getMessage())) {
                System.out.println("error while sending register ack");
            }
        } else {
            if (!connections.send(connectionId, backMessage.getMessage())) {
                System.out.println("error while sending register error");
            }

        }

    }


    private int getOpcode(String msg) {
        return Integer.parseInt(msg.substring(2));
    }

    private List<String> getUsers(String msg) {
        return null;
    }

    private String getDateAndTime(String msg) {
        return null;
    }

    private String getToSendMessage(String msg) {
        return null;
    }

    private String getMessageContent(String msg) {
        return null;
    }

    private List<String> getTags(String msg) {
        return null;
    }

    private String getContent(String msg) {
        return null;
    }

    private String getFollow(String msg) {
        return null;
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

    private String getUserName(String msg) {
        return null;
    }
}


