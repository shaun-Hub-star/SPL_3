package bgu.spl.net.api.MessagePackage;

public class Notification implements Message{

    private String notification;

    public Notification(String notification){
        this.notification = notification;
    }

    @Override
    public void setMessage(String msg) {
        this.notification = msg;
    }

    @Override
    public String getMessages() {
        return this.notification;
    }
}
