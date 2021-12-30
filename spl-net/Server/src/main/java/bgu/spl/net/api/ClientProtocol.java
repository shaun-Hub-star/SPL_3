package bgu.spl.net.api;

public class ClientProtocol implements BidiMessagingProtocol<String> {

    private Connections<String> connections;
    @Override
    public String process(String msg) {
        //if opcode of message is PM
        //from message we can get the person to send the PM
        //BackMessage b =  dataBase.sendPM(shaun)
        //if(b.gotError()) sendError(b.errorMessage())
        //connections.send(lior)
        return null;
    }

    @Override
    public boolean shouldTerminate() {
        return false;
    }

    @Override
    public void start(int connectionId, Connections<String> connections) {

    }

}
