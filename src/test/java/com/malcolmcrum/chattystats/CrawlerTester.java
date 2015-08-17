package com.malcolmcrum.chattystats;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.*;

import static junit.framework.Assert.*;

/**
 * Tests the live API.
 * Created by Malcolm on 8/15/2015.
 */
public class CrawlerTester {
    static Database db;
    static String testDBFilename;
    static Crawler crawler;

    @BeforeClass
    public static void setUp() throws IOException, SQLException {
        Settings settings = new Settings("test.properties");
        DayStats.settings = settings;
        testDBFilename = settings.getDatabaseFileName();

        db = new Database(settings);
        crawler = new Crawler(settings, db);
    }

    @Test
    public void verifyDatabaseConnection() throws SQLException {
        assertTrue(db.conn.isValid(5));
    }

    @Test
    public void verifyTablesExist() throws SQLException {
        DatabaseMetaData dbm = db.conn.getMetaData();

        ResultSet rs = dbm.getTables(null, null, "Post", null);
        assertNotNull(rs.next());

        rs = dbm.getTables(null, null, "Shacker", null);
        assertNotNull(rs.next());

        rs.close();
    }

    @Test
    public void verifyShackersTablePopulates() throws SQLException {
        assertTrue(false);
        crawler.getShackers();

        Statement statement = db.conn.createStatement();
        String sql = "SELECT * FROM SHACKER";
        ResultSet rs = statement.executeQuery(sql);
        assertNotNull(rs.next());

        rs.close();
    }

    @Test
    public void verifyPostsTablePopulates() throws SQLException {
        crawler.getPosts();

        Statement statement = db.conn.createStatement();
        String sql = "SELECT * FROM POST";
        ResultSet rs = statement.executeQuery(sql);
        assertNotNull(rs.next());

        rs.close();
    }

    @Test public void verifyPopulateDatabaseWithPostsMethodAddsPosts() throws SQLException {
        Statement statement = db.conn.createStatement();
        String sql = "SELECT count(id) FROM POST";
        ResultSet rs = statement.executeQuery(sql);
        int postCountBefore = rs.getInt("count(id)");

        crawler.populateDatabaseWithPosts(1, false, false);

        rs = statement.executeQuery(sql);
        int postCountAfter = rs.getInt("count(id)");

        assertFalse(postCountAfter == postCountBefore);
    }

    @AfterClass
    public static void tearDown() throws SQLException, IOException {
        db.conn.close();

        File testDB = new File(testDBFilename);
        testDB.delete();
    }
}
