package bgu.spl.net.api;

public interface BidiMessagingProtocol<T>{

    public void process(T msg);


    public boolean shouldTerminate();


    public void start(int connectionId, Connections<T> connections);
}
