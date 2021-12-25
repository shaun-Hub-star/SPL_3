package bgu.spl.net.api.bidi;

import bgu.spl.net.srv.bidi.ConnectionHandler;

import java.io.IOException;
import java.util.HashMap;

public class ConnectionsImpl<T> implements Connections<T> {

    private HashMap<Integer, ConnectionHandler<T>> connections = new HashMap<>();
    private int connectionID = 0;



    @Override
    public boolean send(int connectionId, T msg) {
        if(!connections.containsKey(connectionId))
            return false;
        connections.get(connectionId).send(msg);
        return true;
    }


    @Override
    public void broadcast(T msg) {
        for(Integer client : connections.keySet()){
            ConnectionHandler<T> ch = connections.get(client);
            ch.send(msg);
        }
    }

    @Override
    public void disconnect(int connectionId) {
        ConnectionHandler ch=connections.get(connectionId);
        connections.remove(connectionId);

    }



    public int addClient(ConnectionHandler<T> client){
        int clientID = connectionID;
        connectionID++;
        connections.put(clientID, client);
        return clientID;
    }
}
