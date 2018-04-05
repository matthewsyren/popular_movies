package com.matthewsyren.popularmovies.Tasks;

import com.matthewsyren.popularmovies.Models.Movie;

/**
 * Used to provide a callback for the MovieQueryTask AsyncTask
 */

public interface MovieQueryTaskOnCompleteListener {
    void onTaskComplete(Movie movie);
}