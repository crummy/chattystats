package com.malcolmcrum.app;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads local settings
 * Created by Malcolm on 7/22/2015.
 */
public class Settings {
    private String filename;

    public Settings() {
        filename = "default.properties";
    }

    public Properties getSettings() {
        Properties prop = new Properties();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filename);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException e) {
                System.out.println("Failed to read settings file: " + e.getMessage());
            }
        } else {
            System.out.println("inputStream is null! File does not exist?");
        }
        return prop;
    }
}
