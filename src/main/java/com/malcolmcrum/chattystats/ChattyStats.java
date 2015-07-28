package com.malcolmcrum.chattystats;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Main file for
 * Created by Malcolm on 7/28/2015.
 */
public class ChattyStats {
    private static Database db;
    private static Settings settings;

    public static void main(String args[]) {
        String settingsFileName;
        if (args.length > 1) {
            settingsFileName = args[1];
        } else {
            settingsFileName = "default.properties";
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
            Crawler crawler = new Crawler(settings, db);
        } catch (IOException e) {
            System.err.println("Failed to initialize crawler. Probably the API connection failed.");
            db.closeDatabase();
            System.exit(1);
        }

        System.out.println("Done initializing stuff");
    }
}
