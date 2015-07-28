package com.malcolmcrum.crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;

/**
 * Gets posts and shackers from ShackAPI. Sends posts and shackers to Database.
 * Created by Malcolm on 7/26/2015.
 */
public class Crawler {
    static Settings settings;
    static Database db;
    static ShackAPI api;

    public static void main(String args[]) {
        String settingsFileName;
        if (args.length > 1) {
            settingsFileName = args[1];
        } else {
            settingsFileName = "crawler.properties";
        }
        try {
            settings = new Settings(settingsFileName);
        } catch (IOException e) {
            System.err.println("Failed to read settings file: " + e.getMessage());
        }

        try {
            db = new Database(settings);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            db.closeDatabase();
            System.exit(1);
        }

        try {
            api = new ShackAPI(settings);
        } catch (IOException e) {
            System.err.println("Shack API connection failed: " + e.getMessage());
            System.exit(1);
        }

        Crawler c = new Crawler();

        // Before the timer starts, fill in shackers and posts - because this might take a while.
        c.getShackers();
        c.getPosts();

        int millisecondsInHour = 1000 * 60 * 60;
        ScheduledCrawl scheduledCrawl = new ScheduledCrawl(c);
        Timer timer = new Timer();
        timer.schedule(scheduledCrawl, millisecondsInHour, millisecondsInHour);
        System.out.println("Crawler now operating. Checking for new posts and shackers every hour.");
    }

    private static class ScheduledCrawl extends TimerTask {
        Crawler crawler;

        public ScheduledCrawl(Crawler c) {
            this.crawler = c;
        }

        @Override
        public void run() {
            System.out.println();
            System.out.println("CRAWL BEGUN AT: " + new Date());
            crawler.getShackers();
            crawler.getPosts();
            System.out.println("CRAWL FINISHED AT: " + new Date());
        }
    }

    private void getShackers() {
        System.out.println("Checking for new shackers...");
        JsonArray shackers = api.getShackers();
        int newShackers = 0;

        for (JsonElement shackerElement : shackers) {
            JsonObject shackerObj = shackerElement.getAsJsonObject();
            Shacker shacker = new Shacker();
            shacker.username = shackerObj.get("username").getAsString();
            shacker.date = shackerObj.get("date").getAsString();
            if (db.addShacker(shacker)) {
                newShackers++;
            }
        }

        if (newShackers > 0) {
            System.out.println("Added " + newShackers + " new Shackers.");
        }
    }

    private void getPosts() {
        System.out.println("Checking for new posts...");
        int latestPostIdInDatabase = db.getLatestPostID();
        System.out.println("Latest post ID from database: " + latestPostIdInDatabase);

        if (latestPostIdInDatabase == -1) {
            int latestPostIdFromShack = api.getLatestPostID();
            System.out.println("No posts found in database. Populating from " + latestPostIdFromShack + " backwards.");
            populateDatabaseWithPosts(latestPostIdFromShack, true, true);
        } else {
            int nextPost = latestPostIdInDatabase + 1;
            System.out.println("Populating posts from " + nextPost + " onwards.");
            populateDatabaseWithPosts(nextPost, false, true);
        }
    }

    /**
     * Requests a batch of posts, then inserts them into the database and reports results via System.out.
     * @param startID The ID of the first post to get
     * @param isReversed Indicates whether to look for posts prior to startID or after
     * @param recurse If true, will find next set of posts, until no more are left to find.
     */
    private void populateDatabaseWithPosts(int startID, boolean isReversed, boolean recurse ) {
        if (startID < 0) {
            startID = 0;
        }
        JsonArray posts = api.getPosts(startID, isReversed);
        int successfulPosts = 0;

        for (final JsonElement postElement : posts) {
            JsonObject postObj = postElement.getAsJsonObject();
            Post post = new Post();
            post.id = postObj.get("id").getAsInt();
            if (post.id == 0) continue; // id=0 is bad data; date is out of order and fields are empty. Ignore it.
            post.parentId = postObj.get("parentId").getAsInt();
            post.threadId = postObj.get("threadId").getAsInt();
            post.author = postObj.get("author").getAsString();
            post.date = postObj.get("date").getAsString();
            post.category = postObj.get("category").getAsString();
            if (db.addPost(post)) {
                successfulPosts++;
            }
        }

        if (posts.size() == 0) {
            System.out.println("No additional posts found.");
        } else  {
            int firstID = posts.get(0).getAsJsonObject().get("id").getAsInt();
            int lastID = posts.get(posts.size()-1).getAsJsonObject().get("id").getAsInt();
            int totalPosts = posts.size();
            int failedPosts = totalPosts - successfulPosts;
            System.out.print("Adding " + totalPosts + " posts [" + firstID + ".." + lastID + "]: ");
            if (failedPosts == 0) {
                System.err.println("Success.");
            } else {
                System.out.println(failedPosts + "failed!");
            }
        }

        if (posts.size() != 0 && recurse) {
            if (isReversed) {
                int nextStartID = posts.get(posts.size()-1).getAsJsonObject().get("id").getAsInt() - 1;
                populateDatabaseWithPosts(nextStartID, true, true);
            } else {
                int nextStartID = posts.get(posts.size()-1).getAsJsonObject().get("id").getAsInt() + 1;
                populateDatabaseWithPosts(nextStartID, false, true);
            }
        }
    }
}
