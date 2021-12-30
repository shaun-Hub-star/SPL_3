package bgu.spl.net.api;

import bgu.spl.net.srv.ConnectionHandler;

import java.util.HashMap;

public class ConnectionsImplementation<T> implements Connections<T> {

    private HashMap<Integer, ConnectionHandler<T>> usersConnections;
    private int connectionId;
    public ConnectionsImplementation(){
        usersConnections = new HashMap<>();
        this.connectionId = 0;
    }
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
