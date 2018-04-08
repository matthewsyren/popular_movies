package com.matthewsyren.popularmovies.tasks;

import com.matthewsyren.popularmovies.models.MovieReview;

import java.util.ArrayList;

/**
 * Used to provide a callback for the MovieReviewsQueryTask
 */

public interface IMovieReviewsQueryTaskOnCompleteListener {
    void onMovieReviewsQueryTaskComplete(ArrayList<MovieReview> reviews);
}
