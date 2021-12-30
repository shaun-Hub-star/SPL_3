package bgu.spl.net.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Calendar;
import java.util.Date;

public class messages {
    private String message;
    private LocalDateTime date;

    public messages(String message,String date) throws ParseException {
        this.message=message;
        this.date=getDate(date);
    }

    public LocalDateTime getDate(String mesaageDate) throws ParseException {
            //change the string of birthday to Local Date
            SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy-HH:MM");
            Date D = sdf.parse(mesaageDate);
            Calendar c = Calendar.getInstance();
            c.setTime(D);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int date = c.get(Calendar.DATE);
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minutes =c.get(Calendar.MINUTE);
            LocalDateTime l1 =LocalDateTime.of(year,month,date,hour,minutes);
           return  l1;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getDate() {
        return date;
    }
    public boolean chechIfNeedToGetMessage(String LastTimeGetMessage) throws ParseException {
        LocalDateTime l2=getDate(LastTimeGetMessage);
        if(l2.compareTo(this.date)>0){
            return true;
        }
        else return false;
    }
}
