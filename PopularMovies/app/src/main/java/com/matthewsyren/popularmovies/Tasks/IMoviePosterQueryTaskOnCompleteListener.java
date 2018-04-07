package com.matthewsyren.popularmovies.Tasks;

import com.matthewsyren.popularmovies.Models.MoviePoster;

import java.util.ArrayList;

/**
 * Used to provide a callback for the MoviePosterQueryTask AsyncTask
 */

public interface IMoviePosterQueryTaskOnCompleteListener {
    void onMoviePosterQueryTaskComplete(ArrayList<MoviePoster> moviePosters);
}