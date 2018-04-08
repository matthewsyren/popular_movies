package com.matthewsyren.popularmovies.tasks;

import com.matthewsyren.popularmovies.models.MoviePoster;

import java.util.ArrayList;

/**
 * Used to provide a callback for the MoviePosterQueryTask AsyncTask
 */

public interface IMoviePosterQueryTaskOnCompleteListener {
    void onMoviePosterQueryTaskComplete(ArrayList<MoviePoster> moviePosters);
}