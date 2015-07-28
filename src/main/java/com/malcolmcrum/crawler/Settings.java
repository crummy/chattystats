package com.malcolmcrum.crawler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Interfaces with properties file to handle crawler settings
 * Created by Malcolm on 7/26/2015.
 */
public class Settings {
    private Properties prop;

    public Settings(String fileName) throws IOException {
        prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(fileName);

        if (inputStream != null) {
            prop.load(inputStream);
        } else {
            throw new IOException("inputStream is null. '" + fileName + "' is inaccessible.");
        }
    }

    public String getDatabaseFileName() {
        return prop.getProperty("db_file_name", "shack.db");
    }

    public String getShackAPIURL() {
        return prop.getProperty("chatty_api_url", "http://winchatty.com/v2/");
    }

    public int getMaxPostsPerBatch() { return Integer.parseInt(prop.getProperty("max_posts_per_batch", "1000")); }
}
