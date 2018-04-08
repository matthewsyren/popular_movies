package com.matthewsyren.popularmovies.tasks;

import com.matthewsyren.popularmovies.models.Movie;

/**
 * Used to provide a callback for the MovieQueryTask AsyncTask
 */

public interface IMovieQueryTaskOnCompleteListener {
    void onMovieQueryTaskComplete(Movie movie);
}