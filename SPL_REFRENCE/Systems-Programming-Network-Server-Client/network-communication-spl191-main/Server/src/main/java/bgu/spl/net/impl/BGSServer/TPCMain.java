package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.EncDec;
import bgu.spl.net.api.bidi.Manager;
import bgu.spl.net.api.bidi.clientProtocol;
import bgu.spl.net.srv.threadPerClient;
import org.omg.CORBA.ARG_IN;

public class TPCMain {
    public static void main(String[] args) {
                threadPerClient<String> myServer = new threadPerClient<String>(
                Integer.parseInt(args[0]),
                () -> new clientProtocol(new Manager()),
                () -> new EncDec());

        myServer.serve();
    }
}