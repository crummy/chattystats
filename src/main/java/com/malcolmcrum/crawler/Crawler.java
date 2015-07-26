package com.malcolmcrum.crawler;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.*;

/**
 * Maintains database integrity in regards to external ShackAPI
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
    }

    private Crawler() {
        int latestPostIdInDatabase = db.getLatestPostID();
        System.out.println("Latest post ID: " + latestPostIdInDatabase);

        if (latestPostIdInDatabase == -1) {
            System.out.println("No posts found in database. Populating from beginning.");
            int latestPostIdFromShack = api.getLatestPostID();
            populateDatabaseWithPosts(latestPostIdFromShack, true);
        } else {
            System.out.println("Latest postID in database: " + latestPostIdInDatabase + ". Populating newer posts");
            populateDatabaseWithPosts(latestPostIdInDatabase, false);
        }
    }

    private void populateDatabaseWithPosts(int startID, boolean reverse) {
        JsonArray posts = api.getPosts(startID, reverse);
        for (final JsonElement postElement : posts) {
            JsonObject postObj = postElement.getAsJsonObject();
            Post post = new Post();
            post.id = postObj.get("id").getAsInt();
            post.parentId = postObj.get("parentId").getAsInt();
            post.threadId = postObj.get("threadId").getAsInt();
            post.author = postObj.get("author").getAsString();
            post.date = postObj.get("date").getAsString();
            post.category = postObj.get("category").getAsString();
            db.addPost(post);
        }
    }
}
