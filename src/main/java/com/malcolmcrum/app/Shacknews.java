package com.malcolmcrum.app;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

/**
 * Handles communication to Shacknews
 * Created by Malcolm on 7/22/2015.
 */
public class Shacknews {
    String sURL;
    public Shacknews(Properties settings) {
        sURL = settings.getProperty("chatty_api_url");
    }

    public int getLatestPostId() {
        int latestPostId = -1;
        try {
            URL url = new URL(sURL + "/getNewestPostInfo");
            try {
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.connect();
                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
                JsonObject rootobj = root.getAsJsonObject();
                latestPostId = rootobj.get("id").getAsInt();
            } catch (IOException e) {
                System.out.println("Failed to connect to API: " + e.getMessage());
            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        return latestPostId;
    }
}
