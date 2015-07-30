package com.malcolmcrum.chattystats;

import java.util.HashMap;
import java.util.Map;

/**
 * Simple class to hold a range of days, with post counts
 * Created by Malcolm on 7/30/2015.
 */
public class PostRange {
    Map<String, Integer> days;

    public PostRange() {
        days = new HashMap<>();
    }
}
