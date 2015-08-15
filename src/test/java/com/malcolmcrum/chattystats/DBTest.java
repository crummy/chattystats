package com.malcolmcrum.chattystats;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.sql.*;

import static junit.framework.Assert.*;

/**
 * Tests the live API.
 * Created by Malcolm on 8/15/2015.
 */
public class DBTest {
    static Connection connection;
    static Crawler crawler;

    @BeforeClass
    public static void setUp() throws IOException, SQLException {
        Settings settings = new Settings("test.properties");
        DayStats.settings = settings;

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

    @AfterClass
    public static void tearDown() throws SQLException {
        connection.close();
    }
}
