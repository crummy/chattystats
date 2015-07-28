package com.malcolmcrum.chattystats;

import java.util.HashMap;
import java.util.Map;

/**
 * Small class to keep day stats together, for later json delivery
 * Created by Malcolm on 7/28/2015.
 */
public class DayStats {
    int totalPosts;
    int totalRootPosts;
    Map<String, Integer> postsInCategories;

    DayStats() {
        HashMap<String, Integer> map = new HashMap<>();
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
}
