package com.malcolmcrum.chattystats;

import java.util.*;

/**
 * Small class to keep day stats together, for later json delivery
 * Created by Malcolm on 7/28/2015.
 */
public class DayStats {
    int totalPosts;
    int totalRootPosts;
    Map<String, Integer> postsInCategories;
    List<TopAuthor> topAuthors;

    DayStats() {
        HashMap<String, Integer> map = new HashMap<>();
        topAuthors = new ArrayList<>();

        map.put("ontopic", 0);
        map.put("nws", 0);
        map.put("stupid", 0);
        map.put("political", 0);
        map.put("tangent", 0);
        map.put("informative", 0);
        postsInCategories = map;

        totalPosts = 0;
        totalRootPosts = 0;
    }

    public void addTopAuthor(String author, int postCount) {
        TopAuthor topAuthor = new TopAuthor();
        topAuthor.author = author;
        topAuthor.postCount = postCount;
        topAuthors.add(topAuthor);
    }

    class TopAuthor {
        String author;
        int postCount;
    }
}
