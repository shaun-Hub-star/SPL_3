package bgu.spl.net.api.MessagePackage;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class BackMessage {

    public enum Status{
        ERROR, PASSED
    }
    private Status status;
    private List<String> messages = new LinkedList<>();

    public BackMessage(){
        this.status = Status.PASSED;
        this.messages = new LinkedList<>();
    }

    public BackMessage(String message,Status status){
        setMessage(message);
        this.status = status;
    }

    public void setMessages(List<String> messages,Status status) {
        this.messages = messages;
        this.status= status;
    }
    public void setMessage(String msg){
        this.messages.add(msg);
    }

    public List<String> getMessages() {
        return messages;
    }

    public String getMessage(){
        return messages.get(0);
    }

    public Status getStatus(){
        return this.status;
    }
}
