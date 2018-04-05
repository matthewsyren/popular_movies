package com.matthewsyren.popularmovies.Models;

/**
 * Used to provide a template for a MoviePoster object
 */

public class MoviePoster {
    private final String id;
    private final String posterURL;

    public MoviePoster(String id, String posterURL) {
        this.id = id;
        this.posterURL = posterURL;
    }

    public String getId() {
        return id;
    }

    public String getPosterURL() {
        return posterURL;
    }
}