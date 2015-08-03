package com.malcolmcrum.chattystats;

import java.util.*;

/**
 * Small class to keep day stats together, for later json delivery
 * Created by Malcolm on 7/28/2015.
 */
public class DayStats {
    static Settings settings;

    int totalPosts;
    int totalRootPosts;
    int busiestPostId;
    Map<String, Integer> postsInCategories;
    List<TopWord> topWords;
    List<TopAuthor> topAuthors;

    private transient Map<String, Integer> words;

    DayStats() {
        HashMap<String, Integer> map = new HashMap<>();
        topAuthors = new ArrayList<>();
        words = new HashMap<>();

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
        topAuthors.add(new TopAuthor(author, postCount));
    }

    public void addWords(String body) {
        if (body != null) {
            for (String word : body.split(" ")) {
                Integer previousCount = words.get(word.toLowerCase());
                words.put(word.toLowerCase(), previousCount == null ? 1 : ++previousCount);
            }
        }
    }

    // A dangerous method; it changes the contents of the words array, so only call it when you're done adding to words
    public void calculateTopWords() {
        topWords = new ArrayList<>(10);
        List<String> ignoredWords = new ArrayList<>();
        if (settings != null) {
            ignoredWords = settings.getIgnoredWords();
        }
        for (int i = 0; i < 10; i++) {
            int mostCommonWordCount = -1;
            String mostCommonWord = "";
            for (String word : words.keySet()) {
                if (words.get(word) > mostCommonWordCount && !ignoredWords.contains(word) && word.matches("[a-zA-Z]+")) {
                    mostCommonWordCount = words.get(word);
                    mostCommonWord = word;
                }
            }
            words.remove(mostCommonWord);
            topWords.add(new TopWord(mostCommonWord, mostCommonWordCount));
        }
    }

    class TopAuthor {
        TopAuthor(String author, int postCount) {
            this.author = author;
            this.postCount = postCount;
        }
        String author;
        int postCount;
    }

    class TopWord {
        TopWord(String word, int count) {
            this.word = word;
            this.count = count;
        }
        String word;
        int count;
    }
}
