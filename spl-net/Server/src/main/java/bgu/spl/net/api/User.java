package bgu.spl.net.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;

public class User {
    private final String userName;
    private final String password;
    private final String birthday;
    private final int id;
    private boolean login;
    private List<String> followers;
    private List<String> following;
    private List<String> blocked;
    private int numPosts;
    private Queue<String> notification;

    public User(String userName, String password, String birthday, int id) {
        this.userName = userName;
        this.password = password;
        this.birthday = birthday;
        this.id = id;
        this.login = false;
        this.followers = new LinkedList<>();
        this.following = new LinkedList<>();
        this.numPosts = 0;
        this.blocked = new LinkedList<>();
        this.notification = new LinkedList<>();
    }

    public List<String> getBlocked() {
        return blocked;
    }

    public void setBlocked(List<String> blocked) {
        this.blocked = blocked;
    }
    public void addBlock(String userToBlock){
        blocked.add(userName);
    }
    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getId() {
        return id;
    }

    public boolean isLogin() {
        return login;
    }

    public List<String> getFollowers() {
        return followers;
    }

    public List<String> getFollowing() {
        return following;
    }

    public int getNumPosts() {
        return numPosts;
    }

    public void setFollowers(List<String> followers) {
        this.followers = followers;
    }

    public void setFollowing(List<String> following) {
        this.following = following;
    }

    public void setLogin(boolean login) {
        this.login = login;
    }

    public void setNumPosts(int numPosts) {
        this.numPosts = numPosts;
    }

    public int getAge() throws ParseException {
        LocalDate now = LocalDate.now();
        //change the string of birthday to Local Date
        SimpleDateFormat sdf = new SimpleDateFormat("dd-mm-yyyy");
        Date D = sdf.parse(birthday);
        Calendar c = Calendar.getInstance();
        c.setTime(D);
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int date = c.get(Calendar.DATE);
        LocalDate l1 = LocalDate.of(year, month, date);
        //calculate the age
        Period diff = Period.between(l1, now);
        return diff.getYears();
    }

    public void addFollow(String userName) {
        this.followers.add(userName);
    }

    public void addFollowing(String userName) {
        this.following.add(userName);
    }

    public void moveFollower(String userName) {
        this.followers.remove(userName);
    }

    public void stopFollowing(String userName) {
        this.following.remove(userName);
    }

    public void addPost() {
        this.numPosts = numPosts++;
    }
    public Queue<String> getNotification() {
        return notification;
    }

    public void addNotification(String msg) {
        notification.add(msg);
    }

    public void deleteNotification()
    {
        while (!notification.isEmpty()){
            notification.remove();}
    }


    public String details(){
        String output;
        try {
            output= this.getAge()+" "+this.getNumPosts()+" "+this.getFollowers().size()+" "+this.getFollowers().size();
        }
        catch (ParseException e){
            output="ERROR";
        }
        return output;


    }

}
