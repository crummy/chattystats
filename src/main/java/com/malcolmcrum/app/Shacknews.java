package com.malcolmcrum.app;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.joda.time.LocalDate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Properties;
import java.util.zip.GZIPInputStream;

/**
 * Handles communication to Shacknews
 * Created by Malcolm on 7/22/2015.
 */
public class Shacknews {
    private class Day {
        LocalDate date;
        int earliestPostId = -1;
        int latestPostId = -1;
        HashMap<Flags, Integer> flags;
    }
    private enum Flags { ontopic, nws, stupid, political, tangent, informative, nuked }
    private HashMap<LocalDate, Day> days;
    private String sURL;

    public Shacknews(Properties settings) {
        sURL = settings.getProperty("chatty_api_url");
        days = new HashMap<LocalDate, Day>();
    }

    private JsonObject APIRequest(String path) {
        JsonObject object = null;
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
            } catch (IOException e) {
                System.out.println("Failed to connect to API: " + e.getMessage());
            }
        } catch (MalformedURLException e) {
            System.out.println("Malformed URL: " + e.getMessage());
        }
        return object;
    }

    private JsonObject getLatestPost() {
        return APIRequest("/getNewestPostInfo");
    }

    private JsonArray getPostsBefore(int id) {
        JsonObject result = APIRequest("/getPostRange?startId=" + id + "&count=1000&reverse=true");
        return result.get("posts").getAsJsonArray();
    }

    private Day findDayAfter(LocalDate requestedDate) {
        LocalDate closestDayAfter = null;
        for (LocalDate date : days.keySet()) {
            if (date.isAfter(requestedDate) && closestDayAfter == null) {
                closestDayAfter = date;
            } else if (date.isAfter(requestedDate) && closestDayAfter != null && date.isBefore(closestDayAfter)) {
                closestDayAfter = date;
            }
        }
        return days.get(closestDayAfter);
    }

    private int findEarliestPostForDay(Day day) {
        assert(day.latestPostId > 0);

        int earliestPostId = -1;
        int lastCheckedPostId = day.latestPostId;
        while (earliestPostId == -1) {
            JsonArray postsBefore = getPostsBefore(lastCheckedPostId);
            for (int postIndex = 0; postIndex < postsBefore.size(); ++postIndex) {
                JsonObject post = postsBefore.get(postIndex).getAsJsonObject();
                String sDate = post.get("date").getAsString();
                LocalDate postDate = LocalDate.parse(sDate.substring(0, 10));
                if (postDate.compareTo(day.date) < 0) {
                    earliestPostId = post.get("id").getAsInt();
                    break;
                }
            }
            JsonObject lastPostFound = postsBefore.get(postsBefore.size() - 1).getAsJsonObject();
            lastCheckedPostId = lastPostFound.get("id").getAsInt();
        }
        return -1;
    }

    private void populateDay(LocalDate date) {
        Day nearestFollowingDay = findDayAfter(date);

        if (nearestFollowingDay == null) {
            // If there is no following day, then this must be the first request.
            // In this case, get the latest day (right now), and populate it.
            JsonObject latestPost = getLatestPost();

            Day today = new Day();
            String sDateTime = latestPost.get("date").getAsString();
            today.date = LocalDate.parse(sDateTime.substring(0, 10));
            today.latestPostId = latestPost.get("id").getAsInt();
            today.earliestPostId = findEarliestPostForDay(today);
            days.put(today.date, today);
            nearestFollowingDay = today;
        }

        while (nearestFollowingDay.date.isAfter(date)) {
            // TBD: What if no posts are made on a day?
            // Also I assume all postIds are consecutive (no gaps)
            Day previousDay = new Day();
            previousDay.date = nearestFollowingDay.date.minusDays(1);
            previousDay.latestPostId = nearestFollowingDay.earliestPostId - 1;
            previousDay.earliestPostId = findEarliestPostForDay(previousDay);
            days.put(previousDay.date, previousDay);
            nearestFollowingDay = previousDay;
        }
    }

    private JsonObject getPostsForDay(LocalDate date) {
        return null;
    }

    public int getPostCountForDay(LocalDate requestedDate) {
        if (!days.containsKey(requestedDate)) {
            populateDay(requestedDate);
        }
        Day day = days.get(requestedDate);
        assert(day.latestPostId > 0);
        assert(day.earliestPostId > 0);
        return day.latestPostId - day.earliestPostId;
    }

    public String getOfftopicPostsForDay(LocalDate requestedDate) {
        if (!days.containsKey(requestedDate)) {
            populateDay(requestedDate);
        }

        JsonObject posts = getPostsForDay(requestedDate);
        return "";
    }
}
