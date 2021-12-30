package bgu.spl.net.api;

import java.util.List;
import java.util.Map;

public class DataBaseServer implements DataBaseQueries {
    public Map<String,User>userMap;
    public Map<String,List<messages>> userToHerMessages;
    
    private static DataBaseServer dataBaseServer = null;

    private DataBaseServer(){
        //TODO
    }
    public static DataBaseServer getInstance(){
        if(dataBaseServer == null){
            dataBaseServer = new DataBaseServer();
        }
        return dataBaseServer;
    }

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
    public BackMessage PM(String me, String userTo, String msg, String dateAndTimeD) {
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
}
