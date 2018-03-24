package com.matthewsyren.popularmovies.Models;

/**
 * Used to provide a template for a Movie object
 */

public class Movie {
    private final String title;
    private final String posterURL;
    private final String overview;
    private final String userRating;
    private final String releaseDate;
    private final String runtime;

    //Constructor
    public Movie(String title, String posterURL, String overview, String userRating, String releaseDate, String runtime) {
        this.title = title;
        this.posterURL = posterURL;
        this.overview = overview;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
    }

    //Getter methods
    public String getTitle() {
        return title;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public String getOverview() {
        return overview;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getRuntime() {
        return runtime;
    }
}