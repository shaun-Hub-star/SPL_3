package bgu.spl.net.impl.BGSRServer;

import bgu.spl.net.api.ClientProtocol;
import bgu.spl.net.api.DataBaseServer;
import bgu.spl.net.api.EncDec;
import bgu.spl.net.srv.Server;

public class ReactorMain {
    public static void main(String[] args) {
        if (args.length < 2)
            throw new IllegalArgumentException("requires port and threads number");
        DataBaseServer dataBase = new DataBaseServer();
        Server.reactor(
                Integer.parseInt(args[1]),
                Integer.parseInt(args[0]),
                () -> new ClientProtocol(dataBase),
                EncDec::new
        ).serve();
    }
}