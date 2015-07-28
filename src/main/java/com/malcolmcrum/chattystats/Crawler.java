package com.malcolmcrum.chattystats;

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
public class Crawler implements Runnable {
    private Settings settings;
    private Database db;
    private ShackAPI api;

    public Crawler(Settings settings, Database db) throws IOException {
        this.settings = settings;
        this.db = db;
        this.api = new ShackAPI(settings);

        Thread thread = new Thread(this, "Crawler");
        thread.start();
    }

    @Override
    public void run() {
        // Before the timer starts, fill in shackers and posts - because this might take a while.
        getShackers();
        getPosts();

        int interval = settings.getCrawlIntervalMs();
        ScheduledCrawl scheduledCrawl = new ScheduledCrawl(this);
        Timer timer = new Timer();
        timer.schedule(scheduledCrawl, interval, interval);
        System.out.println("Crawler now operating. Checking for new posts and shackers every " + interval/1000 + "s.");
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
