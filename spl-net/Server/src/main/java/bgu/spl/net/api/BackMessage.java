package bgu.spl.net.api;

import java.util.LinkedList;
import java.util.List;

public class BackMessage {

    enum Status{
        ERROR, PASSED
    }
    private Status status;
    private List<String> messages;

    public BackMessage(){
        this.status = Status.PASSED;
        this.messages = new LinkedList<>();
    }


    public void setMessage(List<String> messages,Status status) {
        this.messages = messages;
        this.status= status;
    }


    public List<String> getMessages() {
        return messages;
    }
}
