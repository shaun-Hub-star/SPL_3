package bgu.spl.net.api.bidi;

public class Message<T> {
    private short upcode;
    private String data;

    public Message(short upcode,String data){
        this.data=data;
        this.upcode=upcode;
    }

    public short getUpcode() {
        return upcode;
    }

    public String getData() {
        return data;
    }
}
