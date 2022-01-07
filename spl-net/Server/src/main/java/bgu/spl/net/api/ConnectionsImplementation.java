package bgu.spl.net.api;

import bgu.spl.net.srv.ConnectionHandler;

import java.util.HashMap;
import java.util.Map;

public class ConnectionsImplementation<T> implements Connections<T> {

    private HashMap<Integer, ConnectionHandler<T>> clientsConnections;
    public ConnectionsImplementation(){
        clientsConnections = new HashMap<>();

    }
    @Override
    public boolean send(int connectionId, T msg) {
        ConnectionHandler<T> connectionHandler = clientsConnections.get(connectionId);
        if (connectionHandler != null) {
            connectionHandler.send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void broadcast(T msg) {
        for (Map.Entry<Integer, ConnectionHandler<T>> pair : clientsConnections.entrySet())
            pair.getValue().send(msg);
    }

    @Override
    public void disconnect(int connId) {
        clientsConnections.remove(connId);
    }
    public void addConnection(int id,ConnectionHandler<T> connectionHandler){
        clientsConnections.put(id, connectionHandler);
    }
}
