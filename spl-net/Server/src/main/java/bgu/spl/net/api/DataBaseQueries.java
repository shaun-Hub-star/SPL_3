package bgu.spl.net.api;

import java.util.List;

public interface DataBaseQueries {
    BackMessage register(String userName);

    BackMessage login(String userName);

    BackMessage logout(String userName);

    BackMessage follow(String me, String to);

    BackMessage post(String msg, String userName, List<String> tags);

    BackMessage PM(String me,String userTo,String msg,String dateAndTimeD);

    BackMessage logStat();

    BackMessage stat(List<String> userNames);

    BackMessage notification(String userName);
    //BackMessage ack();
   // BackMessage error();
    BackMessage block(String me,String toBlock );


}