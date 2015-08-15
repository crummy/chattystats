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
public class DBAndCrawlerTest {
    static Connection connection;
    static Crawler crawler;
    static String testDBFilename;

    @BeforeClass
    public static void setUp() throws IOException, SQLException {
        Settings settings = new Settings("test.properties");
        DayStats.settings = settings;
        testDBFilename = settings.getDatabaseFileName();

        Database db = new Database(settings);
        connection = db.conn;

        crawler = new Crawler(settings, db, false);
    }

    @Test
    public void verifyDatabaseConnection() throws SQLException {
        assertTrue(connection.isValid(5));
    }

    @Test
    public void verifyTablesExist() throws SQLException {
        DatabaseMetaData dbm = connection.getMetaData();

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

        Statement statement = connection.createStatement();
        String sql = "SELECT * FROM SHACKER";
        ResultSet rs = statement.executeQuery(sql);
        assertNotNull(rs.next());

        rs.close();
    }

    @Test
    public void verifyPostsTablePopulates() throws SQLException {
        crawler.getPosts();

        Statement statement = connection.createStatement();
        String sql = "SELECT * FROM POST";
        ResultSet rs = statement.executeQuery(sql);
        assertNotNull(rs.next());

        rs.close();
    }

    @Test public void verifyPopulateDatabaseWithPostsMethodAddsPosts() throws SQLException {
        Statement statement = connection.createStatement();
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
        connection.close();

        File testDB = new File(testDBFilename);
        testDB.delete();
    }
}
