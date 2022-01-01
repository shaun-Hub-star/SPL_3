package bgu.spl.net.api;

import bgu.spl.net.api.MessagePackage.BackMessage;
import bgu.spl.net.api.MessagePackage.Notification;

import java.util.List;
import java.util.Queue;

public interface DataBaseQueries {
    BackMessage register(String userName,String password,String birthDay,int id);

    BackMessage login(String userName,String password);

    BackMessage logout(String userName);

    BackMessage follow(String me, String to);

    BackMessage post(String msg, String userName, List<String> tags);

    BackMessage PM(String me, String userTo, String msg, String dateAndTimeD);

    BackMessage logStat();

    BackMessage stat(List<String> userNames);

    BackMessage notification(String userName);

    //BackMessage ack();
    // BackMessage error();
    BackMessage block(String me, String toBlock);

    Queue<Notification> getNotifications(String userName);
}
