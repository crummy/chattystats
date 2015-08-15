package com.malcolmcrum.chattystats;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;

import static junit.framework.Assert.*;

/**
 * Verify ChattyStatsAPI
 * Created by Malcolm on 8/15/2015.
 */
public class APITest {
    private static Database db;
    private static ChattyStatsAPIRunner apiRunner;

    @BeforeClass
    public static void setUp() throws IOException, SQLException {
        Settings settings = new Settings("test.properties");
        DayStats.settings = settings;

        db = new Database(settings);

        apiRunner = new ChattyStatsAPIRunner(db);
        Thread thread = new Thread(apiRunner, "ChattyStatsAPI");
        thread.start();
    }

    @Test
    public void verifyOKResponse() throws IOException {
        URL url = new URL("http://localhost:4567/test");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        assertEquals(request.getResponseCode(), 200);
    }

    @Test
    public void verifyPost() throws IOException {
        URL url = new URL("http://localhost:4567/post/1");
        HttpURLConnection request = (HttpURLConnection) url.openConnection();
        request.connect();
        InputStream in = (InputStream)request.getContent();
        assertEquals(in.read(), "");
    }

    @AfterClass
    public static void tearDown() throws SQLException {
        db.conn.close();
    }
}

class ChattyStatsAPIRunner implements Runnable {
    private ChattyStatsAPI api;
    private Database db;

    public ChattyStatsAPIRunner(Database db) {
        this.db = db;
    }

    @Override
    public void run() {
        this.api = new ChattyStatsAPI(db);
    }
}
