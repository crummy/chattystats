package com.malcolmcrum.chattystats;

import static spark.Spark.*;

import com.google.gson.Gson;
import spark.*;

/**
 * API for exposing internal database. Uses an old version of Spark so I don't need JDK8.
 * Created by Malcolm on 7/28/2015.
 */
public class ChattyStatsAPI {
    private Database db;

    public ChattyStatsAPI(Database db) {
        this.db = db;

        get(new Route("/day/:year/:month/:day") {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                int year, month, day;
                try {
                    year = Integer.parseInt(request.params(":year"));
                    month = Integer.parseInt(request.params(":month"));
                    day = Integer.parseInt(request.params(":day"));
                    return getDay(year, month, day);
                } catch (NumberFormatException e) {
                    return error("Year/month/day formatted incorrectly");
                }
            }
        });

        get(new Route("/from/:startYear/:startMonth/:startDay/to/:endYear/:endMonth/:endDay") {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                int startYear, startMonth, startDay, endYear, endMonth, endDay;
                try {
                    startYear = Integer.parseInt(request.params(":startYear"));
                    startMonth = Integer.parseInt(request.params(":startMonth"));
                    startDay = Integer.parseInt(request.params(":startDay"));
                    endYear = Integer.parseInt(request.params(":endYear"));
                    endMonth = Integer.parseInt(request.params(":endMonth"));
                    endDay = Integer.parseInt(request.params(":endDay"));
                    return getRangeOfPosts(startYear, startMonth, startDay, endYear, endMonth, endDay);
                } catch (NumberFormatException e) {
                    return error("Year/month/day formatted incorrectly");
                }
            }
        });

        get(new Route("/post/:id") {
            @Override
            public Object handle(Request request, Response response) {
                response.header("Access-Control-Allow-Origin", "*");
                int id;
                try {
                    id = Integer.parseInt(request.params(":id"));
                    return getPost(id);
                } catch (NumberFormatException e) {
                    return error("Post ID formatted incorrectly");
                }
            }
        });
    }

    private String error(String message) {
        return "{ \"error\": \"" + message + "\" }";
    }

    private String getDay(int year, int month, int day) {
        DayStats stats = db.getDayStats(year, month, day);
        return new Gson().toJson(stats);
    }

    private String getPost(int id) {
        Post post = db.getPost(id);
        return new Gson().toJson(post);
    }

    private String getRangeOfPosts(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        PostRange range = db.getRangeOfPosts(startYear, startMonth, startDay, endYear, endMonth, endDay);
        return new Gson().toJson(range);
    }
}
