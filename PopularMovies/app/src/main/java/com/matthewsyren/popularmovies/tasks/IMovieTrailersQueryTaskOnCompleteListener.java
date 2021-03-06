package com.matthewsyren.popularmovies.tasks;

import java.net.URL;
import java.util.ArrayList;

/**
 * Used to provide a callback method for the MovieTrailersQueryTask
 */

public interface IMovieTrailersQueryTaskOnCompleteListener {
    void onMovieTrailerQueryTaskComplete(ArrayList<URL> urls);
}