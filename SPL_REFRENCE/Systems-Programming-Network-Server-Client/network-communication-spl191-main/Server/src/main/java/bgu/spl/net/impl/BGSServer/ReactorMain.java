package bgu.spl.net.impl.BGSServer;

import bgu.spl.net.api.bidi.EncDec;
import bgu.spl.net.api.bidi.Manager;
import bgu.spl.net.api.bidi.clientProtocol;
import bgu.spl.net.srv.Reactor;

public class ReactorMain {
    public static void main(String[] args){


        Reactor<String> myserver = new Reactor<String>(Integer.parseInt(args[1]), Integer.parseInt(args[0]),()->new clientProtocol(new Manager()),()->new EncDec());
        myserver.serve();
    }




}


