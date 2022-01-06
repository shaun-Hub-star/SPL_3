package bgu.spl.net.api.MessagePackage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class Messages {
    private String message;
    private LocalDateTime date;
    private List<String> filter;

    public Messages(String message, String date) throws ParseException {
        this.date = getDate(date);
        this.filter = new LinkedList<>();
        this.message = fixFilter(message);
    }
    public Messages(String message, String date,List<String> filter) throws ParseException {
        this.message = fixFilter(message);
        this.date = getDate(date);
        this.filter = new LinkedList<>(filter);
    }
    public Messages(String message,LocalDateTime localDateTime) throws ParseException {
        this.date = localDateTime;
        this.filter = new LinkedList<>();
        this.message = fixFilter(message);
    }

    public LocalDateTime getDate(String massageDate) throws ParseException {
        //change the string of birthday to Local Date
        System.out.println("move getDate "+massageDate );
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH:mm");
        System.out.println("1*********");
        Date D = sdf.parse(massageDate);
        System.out.println("2*********");
        Calendar c = Calendar.getInstance();
        System.out.println("3*********");
        c.setTime(D);
        System.out.println("4*********");
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        System.out.println("5********* "+year+" "+month+" "+date+ " "+hour+ " "+minutes);
        LocalDateTime l1 = LocalDateTime.of(year, month, date, hour, minutes);
        System.out.println("LocalDateTime "+l1.toString());
        return l1;
    }
    public String getDateByString(LocalDateTime localDateTime){
        String output="";
        int date=localDateTime.getDayOfMonth();
        int month=localDateTime.getMonthValue();
        int year =localDateTime.getYear();
        output=date+"-"+month+"-"+year;
        return output;
    }
    public String getDateByStringOfMessage(){
        String output="";
        int date1=date.getDayOfMonth();
        int month=date.getMonthValue();
        int year =date.getYear();
        output=date1+"-"+month+"-"+year;
        return output;
    }

    public String getMessage() {
        return message;
    }

    public String fixFilter(String msg){//TODO - change the word
        if(filter!=null){
        StringBuilder output = new StringBuilder();
        String[] splitBySpace = msg.split(" ");
        for(String word : splitBySpace){
            if(filter.contains(word)){
                output.append("<filtered> ");
            }else{
                output.append(word).append(" ");
            }
        }
        return output.toString();}
        return msg;

    }

    public LocalDateTime getDate() {
        return date;
    }

    public boolean checkIfNeedToGetMessage(String LastTimeGetMessage) throws ParseException {
        LocalDateTime l2 = getDate(LastTimeGetMessage);
        return l2.compareTo(this.date) > 0;
    }
}