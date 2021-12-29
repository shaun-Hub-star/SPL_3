package bgu.spl.net.api;

public class ClientProtocol implements BidiMessagingProtocol<String> {
    @Override
    public String process(String msg) {
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
