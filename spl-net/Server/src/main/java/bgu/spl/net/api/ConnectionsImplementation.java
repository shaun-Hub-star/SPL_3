package bgu.spl.net.api;

public class ConnectionsImplementation<T> implements Connections<T> {

    @Override
    public boolean send(int connectionId, T msg) {
        return false;
    }

    @Override
    public void broadcast(T msg) {

    }

    @Override
    public void disconnect(int connId) {

    }
}
