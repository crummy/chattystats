package com.malcolmcrum.app;

import org.joda.time.LocalDate;

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
        System.out.println("Get post intervals for last ten days");

        LocalDate localDate = LocalDate.now();
        for (int i = 0; i < 10; ++i) {
            int totalPostsForDay = shack.getPostCountForDay(localDate.minusDays(i));
            System.out.println("Total posts for day " + i + ": " + totalPostsForDay);
        }

        System.out.println("Total offtopic posts for day 5: " + shack.getOfftopicPostsForDay(localDate.minusDays(5)));

    }

}
