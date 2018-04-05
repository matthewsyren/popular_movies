package com.matthewsyren.popularmovies.Tasks;

import com.matthewsyren.popularmovies.Models.MoviePoster;

import java.util.List;

/**
 * Used to provide a callback for the MovieImagesQueryTask AsyncTask
 */

public interface MovieImagesQueryTaskOnCompleteListener {
    void onTaskComplete(List<MoviePoster> moviePosters);
}