package bgu.spl.net.api;

public class DataBaseServer {
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

}
