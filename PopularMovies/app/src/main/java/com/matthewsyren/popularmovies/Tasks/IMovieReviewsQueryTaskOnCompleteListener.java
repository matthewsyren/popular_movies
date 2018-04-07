package com.matthewsyren.popularmovies.Tasks;

import com.matthewsyren.popularmovies.Models.MovieReview;

import java.util.ArrayList;

/**
 * Used to provide a callback for the MovieReviewsQueryTask
 */

public interface IMovieReviewsQueryTaskOnCompleteListener {
    void onMovieReviewsQueryTaskComplete(ArrayList<MovieReview> reviews);
}
