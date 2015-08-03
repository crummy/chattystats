package com.malcolmcrum.chattystats;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main file for
 * Created by Malcolm on 7/28/2015.
 */
public class ChattyStats {

    public static void main(String args[]) {
        String settingsFileName;
        if (args.length > 1) {
            settingsFileName = args[1];
        } else {
            settingsFileName = "default.properties";
        }

        Settings settings = null;
        try {
            settings = new Settings(settingsFileName);
        } catch (IOException e) {
            System.err.println("Failed to read settings file: " + e.getMessage());
        }
        DayStats.settings = settings;

        Database db = null;
        try {
            db = new Database(settings);
        } catch (SQLException e) {
            System.err.println("Failed to connect to database: " + e.getMessage());
            System.exit(1);
        }

        try {
            Crawler crawler = new Crawler(settings, db);
        } catch (IOException e) {
            System.err.println("Failed to initialize crawler. Probably the API connection failed.");
            System.exit(1);
        }

        ChattyStatsAPI api = new ChattyStatsAPI(db);
    }
}
