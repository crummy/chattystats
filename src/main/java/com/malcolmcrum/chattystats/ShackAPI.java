package com.malcolmcrum.chattystats;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

/**
 * Gets data from the Shack API.
 * Created by Malcolm on 7/26/2015.
 */
public class ShackAPI {
    Settings settings;

    public ShackAPI(Settings settings) throws IOException {
        this.settings = settings;
        if (!testAPIConnection()) {
            throw new IOException("Failed to connect to Shack API.");
        }
    }

    public int getLatestPostID() {
        JsonObject result = APIRequest("getNewestPostInfo");
        return result.get("id").getAsInt();
    }

    /**
     * Returns up to settings.getMaxPostsPerBatch posts.
     * Note: Will skip over nuked posts.
     * @param startID Start of range for requesting posts
     * @param isReversed Look backwards through posts if true
     * @return JsonArray of posts
     */
    public JsonArray getPosts(int startID, boolean isReversed) {
        int postsPerBatch = settings.getMaxPostsPerBatch();
        JsonObject result = APIRequest("getPostRange?startId=" + startID +
                                       "&count=" + postsPerBatch +
                                       "&reverse=" + isReversed);
        return result.get("posts").getAsJsonArray();
    }

    /**
     * Gets a few thousand Shackers from the API.
     * This is actually some cached list the API uses and is far from complete! Use it just to fill in some
     * initial user database info.
     * @return JsonArray of shackers, with username and registration dates
     */
    public JsonArray getShackers() {
        JsonObject result = APIRequest("getAllUserRegistrationDates");
        return result.get("users").getAsJsonArray();
    }

    private boolean testAPIConnection() {
        JsonObject result = APIRequest("checkConnection");
        System.out.println("API test result: " + result.toString());
        return true; // TBD: Actually verify results of API.
    }

    private JsonObject APIRequest(String path) {
        JsonObject object = null;
        String sURL = settings.getShackAPIURL();
        try {
            URL url = new URL(sURL + path);
            try {
                HttpURLConnection request = (HttpURLConnection) url.openConnection();
                request.setRequestProperty("Accept-Encoding", "gzip");
                request.connect();
                InputStream in = (InputStream)request.getContent();
                GZIPInputStream gin = new GZIPInputStream(in);
                BufferedReader reader = new BufferedReader(new InputStreamReader(gin));
                JsonParser jp = new JsonParser();
                JsonElement root = jp.parse(reader);
                object = root.getAsJsonObject();
                request.disconnect();
            } catch (IOException e) {
                System.out.println("Failed to connect to API: " + e.getMessage());
            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        return object;
    }
}
