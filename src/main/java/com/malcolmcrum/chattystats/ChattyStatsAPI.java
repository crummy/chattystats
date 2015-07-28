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
    }

    public String error(String message) {
        return "{ \"error\": \"" + message + "\" }";
    }

    public String getDay(int year, int month, int day) {
        DayStats stats = db.getDayStats(year, month, day);
        return new Gson().toJson(stats);
    }
}
