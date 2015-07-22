package com.malcolmcrum.app;

import java.util.Properties;


/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );

        Settings settings = new Settings();
        Properties props = settings.getSettings();

        Shacknews shack = new Shacknews(props);
        System.out.println("Latest post ID: " + shack.getLatestPostId());

    }

}
