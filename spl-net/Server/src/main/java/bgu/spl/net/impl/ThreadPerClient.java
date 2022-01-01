package bgu.spl.net.impl;

import bgu.spl.net.api.ClientProtocol;
import bgu.spl.net.api.DataBaseServer;
import bgu.spl.net.api.EncDec;
import bgu.spl.net.srv.Server;

public class ThreadPerClient {
    public static void main(String[] args){
        if (args.length < 1)
            throw new IllegalArgumentException("requires port");
        DataBaseServer dataBase = new DataBaseServer();
        Server.threadPerClient(
                Integer.parseInt(args[0]),
                ()-> new ClientProtocol(dataBase),
                EncDec::new
        ).serve();
    }
}
