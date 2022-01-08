package bgu.spl.net.api.MessagePackage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.*;

public class Messages {
    private String message;
    private LocalDateTime date;
    private String[] filter = new String[]{"nudity","tramp","war","porn"};


    public Messages(String message, LocalDateTime localDateTime) throws ParseException {
        this.date = localDateTime;
        this.message = fixFilter(message);
    }

    public LocalDateTime getDate(String massageDate) throws ParseException {
        //change the string of birthday to Local Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy-HH:mm");
        Date D = sdf.parse(massageDate);
        Calendar c = Calendar.getInstance();
        c.setTime(D);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE);
        LocalDateTime l1 = LocalDateTime.of(year, month, date, hour, minutes);
        return l1;
    }

    public String getDateByString(LocalDateTime localDateTime) {
        String output = "";
        int date = localDateTime.getDayOfMonth();
        int month = localDateTime.getMonthValue();
        int year = localDateTime.getYear();
        output = date + "-" + month + "-" + year;
        return output;
    }

    public String getDateByStringOfMessage() {
        String output = "";
        int date1 = date.getDayOfMonth();
        int month = date.getMonthValue();
        int year = date.getYear();
        output = date1 + "-" + month + "-" + year;
        return output;
    }

    public String getMessage() {
        return message;
    }

    public String fixFilter(String msg) {//TODO - change the word
        if (filter != null) {
            StringBuilder output = new StringBuilder();
            String[] splitBySpace = msg.split(" ");
            List<String> list = Arrays.asList(filter);
            for (String word : splitBySpace) {
                if (list.contains(word)) {
                    output.append("<filtered> ");
                } else {
                    output.append(word).append(" ");
                }
            }
            return output.toString();
        }
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