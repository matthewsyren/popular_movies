package com.matthewsyren.popularmovies.Models;

/**
 * Used to provide a template for a MoviePoster object
 */

public class MoviePoster {
    private final int id;
    private final String posterURL;

    public MoviePoster(int id, String posterURL) {
        this.id = id;
        this.posterURL = posterURL;
    }

    public int getId() {
        return id;
    }

    public String getPosterURL() {
        return posterURL;
    }
}