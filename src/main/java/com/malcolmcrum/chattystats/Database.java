package com.malcolmcrum.chattystats;

import org.joda.time.LocalDate;

import java.sql.*;
import java.util.HashMap;

/**
 * Sets up and connects to local database
 * Created by Malcolm on 7/26/2015.
 */
public class Database {
    private Connection db;
    private Settings settings;

    public Database(Settings settings) throws SQLException {
        this.settings = settings;

        connectToDatabase();
        createTablesIfNecessary();
    }

    /**
     * Call this at program termination.
     * TODO: Use or remove this
     */
    public void closeDatabase() {
        try {
            db.close();
        } catch (SQLException e) {
            // Just print an error. We don't really care much at this point.
            System.err.println("Caught an error closing database connection.");
        }
    }

    /**
     * Requests the highest post ID from the database, which should be the most recent.
     * @return PostID of most recent element
     */
    public int getLatestPostID() {
        int id = -1;
        Statement statement = null;
        ResultSet rs = null;
        try {
            statement = db.createStatement();
            String sql = "SELECT * FROM POST ORDER BY id DESC LIMIT 1";
            rs = statement.executeQuery(sql);
            rs.next();
            id = rs.getInt("id");
        } catch (SQLException e) {
            // If we caught an exception, the database is probably empty.
        } finally {
            close(rs, statement);
        }
        return id;
    }


    public boolean addPost(Post post) {
        PreparedStatement statement = null;
        boolean success = true;
        try {
            // TODO: Make sql-injection safe
            String sql = "INSERT INTO Post (id, threadId, parentId, category, author, date) VALUES(?, ?, ?, ?, ?, ?)";
            statement = db.prepareStatement(sql);
            statement.setInt(1, post.id);
            statement.setInt(2, post.threadId);
            statement.setInt(3, post.parentId);
            statement.setString(4, post.category);
            statement.setString(5, post.author);
            statement.setString(6, post.date);
            statement.executeUpdate();
            db.commit();
        } catch (SQLException e) {
            // Probably the post already exists? TODO: Handle it.
            System.out.println("Failed to add post #" + post.id + " to DB: " + e.getMessage());
            success = false;
        } finally {
            close(null, statement);
        }
        return success;
    }

    public boolean addShacker(Shacker shacker) {
        PreparedStatement statement = null;
        boolean success = true;
        try {
            String sql = "INSERT INTO Shacker (name, registrationDate) VALUES (?, ?)";
            statement = db.prepareStatement(sql);
            statement.setString(1, shacker.username);
            statement.setString(2, shacker.date);
            statement.executeUpdate();
            db.commit();
        } catch (SQLException e) {
            // If shacker already exists, we don't mind.
            success = false;
        } finally {
            close(null, statement);
        }
        return success;
    }

    public DayStats getDayStats(int year, int month, int day) {
        DayStats stats = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        LocalDate date = new LocalDate(year, month, day);
        try {
            String sql = "SELECT * FROM Post WHERE date >= '?' AND date < '?'";
            statement = db.prepareStatement(sql);
            statement.setString(1, date.toString());
            statement.setString(2, date.plusDays(1).toString());
            rs = statement.executeQuery();

            stats = new DayStats();
            HashMap<String, Integer> authors = new HashMap<>();
            while (rs.next()) {
                stats.totalPosts++;
                if (rs.getInt("parentId") == 0) {
                    stats.totalRootPosts++;
                }

                String author = rs.getString("author");
                Integer postsByUser = authors.get(author);
                authors.put(author, postsByUser == null? 1 : postsByUser + 1);

                String category = rs.getString("category");
                int categoryCount = stats.postsInCategories.get(category);
                stats.postsInCategories.put(category, categoryCount + 1);
            }

            for (int i = 0; i < 10; ++i) {
                int topPostCount = 0;
                String topAuthor = "";
                for (String author : authors.keySet()) {
                    int posts = authors.get(author);
                    if (posts > topPostCount) {
                        topPostCount = posts;
                        topAuthor = author;
                    }
                }
                authors.remove(topAuthor);
                stats.addTopAuthor(topAuthor, topPostCount);
            }

        } catch (SQLException e) {
            System.err.println("Failed to query database for day " + date.toString() + ": " + e.getMessage());
        } finally {
            close(rs, statement);
        }
        return stats;
    }

    public Post getPost(int id) {
        Post post = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT * FROM Post WHERE id == ?";
            statement = db.prepareStatement(sql);
            statement.setInt(1, id);
            rs = statement.executeQuery();

            post = new Post();
            rs.next();
            post.author = rs.getString("author");
            post.body = rs.getString("body");
            post.category = rs.getString("category");
            post.date = rs.getString("date");
            post.id = rs.getInt("id");
            post.parentId = rs.getInt("parentId");
            post.threadId = rs.getInt("threadId");
        } catch (SQLException e) {
            System.err.println("Failed to query database for post " + id + ": " + e.getMessage());
        } finally {
            close(rs, statement);
        }
        return post;
    }

    public PostRange getRangeOfPosts(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay) {
        PostRange range = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            String sql = "SELECT DATE(date) AS date, COUNT(DATE(date)) AS count FROM Post WHERE date >= ? AND date <= ? GROUP BY DATE(date)";
            System.out.println(sql);
            statement = db.prepareStatement(sql);
            LocalDate start = new LocalDate(startYear, startMonth, startDay);
            statement.setString(1, start.toString());
            LocalDate end = new LocalDate(endYear, endMonth, endDay);
            statement.setString(2, end.toString());
            rs = statement.executeQuery();

            range = new PostRange();
            while (rs.next()) {
                String date = rs.getString("date");
                int count = rs.getInt("count");
                System.out.println(date + ": " + count);
                range.days.put(date, count);
            }
        } catch (SQLException e) {
            System.err.println("Failed to query database for post range: " + e.getMessage());
        } finally {
            close(rs, statement);
        }
        return range;
    }

    /**
     * Connects to the database. If none exists, will create it.
     */
    private void connectToDatabase() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            db = DriverManager.getConnection("jdbc:sqlite:" + settings.getDatabaseFileName());
            db.setAutoCommit(false);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC class not found. Library not installed? " + e.getMessage());
        }
        System.out.println("Successfully connected to database.");
    }

    private void close(ResultSet rs, Statement statement) {
        if (rs != null) {
            try {
                rs.close();
            } catch(SQLException e) {
                System.err.println("The result set cannot be closed: " + e.getMessage());
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                System.err.println("The statement cannot be closed: " + e.getMessage());
            }
        }
    }

    /**
     * Looks up Post and Shacker tables. If either does not exist, creates it.
     * @throws SQLException
     */
    private void createTablesIfNecessary() throws SQLException {
        try {
            DatabaseMetaData dbm = db.getMetaData();

            ResultSet rs = dbm.getTables(null, null, "Post", null);
            if (!rs.next()) createPostTable();

            rs = dbm.getTables(null, null, "Shacker", null);
            if (!rs.next()) createShackerTable();

        } catch (SQLException e){
            System.err.println("Failed to get or set table information: " + e.getMessage());
            System.exit(0);
        }
    }

    private void createPostTable() throws SQLException {
        Statement statement = db.createStatement();
        String sql = "CREATE TABLE [Post](\n" +
                "    [id] INTEGER PRIMARY KEY NOT NULL UNIQUE, \n" +
                "    [threadId] INTEGER NOT NULL, \n" +
                "    [parentId] INTEGER NOT NULL, \n" +
                "    [category] TEXT, \n" +
                "    [author] TEXT NOT NULL, \n" +
                "    [body] TEXT, \n" +
                "    [date] TEXT NOT NULL)";
        statement.executeUpdate(sql);
        sql = "CREATE INDEX [Date Index]\n" +
                "ON [Post](\n" +
                "    [date])";
        statement.executeUpdate(sql);
        db.commit();
        statement.close();
    }

    private void createShackerTable() throws SQLException {
        Statement statement = db.createStatement();
        String sql = "CREATE TABLE [Shacker](\n" +
                "    [name] TEXT PRIMARY KEY NOT NULL UNIQUE, \n" +
                "    [registrationDate] TEXT)";
        statement.executeUpdate(sql);
        db.commit();
        statement.close();
    }
}
