package main.java.bgu.spl.net.impl.newsfeed;


import main.java.bgu.spl.net.impl.rci.Command;

import java.io.Serializable;

public class FetchNewsCommand implements Command<NewsFeed> {

    private String channel;

    public FetchNewsCommand(String channel) {
        this.channel = channel;
    }

    @Override
    public Serializable execute(NewsFeed feed) {
        return feed.fetch(channel);
    }

}
