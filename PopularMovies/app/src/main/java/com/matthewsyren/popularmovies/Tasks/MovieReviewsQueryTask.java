package com.matthewsyren.popularmovies.Tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.matthewsyren.popularmovies.Models.MovieReview;
import com.matthewsyren.popularmovies.R;
import com.matthewsyren.popularmovies.Utilities.JsonUtilities;
import com.matthewsyren.popularmovies.Utilities.NetworkUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Used to fetch a list of reviews for a specific movie
 */

public class MovieReviewsQueryTask
        extends AsyncTask<URL, Void, ArrayList<MovieReview>>{
    private Context mContext;
    private IMovieReviewsQueryTaskOnCompleteListener mIMovieReviewsQueryTaskOnCompleteListener;

    public MovieReviewsQueryTask(Context mContext, IMovieReviewsQueryTaskOnCompleteListener mIMovieReviewsQueryTaskOnCompleteListener) {
        this.mContext = mContext;
        this.mIMovieReviewsQueryTaskOnCompleteListener = mIMovieReviewsQueryTaskOnCompleteListener;
    }

    @Override
    protected ArrayList<MovieReview> doInBackground(URL... urls) {
        try{
            String response = NetworkUtilities.getHttpResponse(urls[0]);
            if(response != null && !response.equals("")){
                return  JsonUtilities.getMovieReviews(response);
            }
            else{
                Toast.makeText(mContext, mContext.getResources().getString(R.string.no_movie_found_error), Toast.LENGTH_LONG).show();
            }
        }
        catch(IOException i){
            i.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<MovieReview> reviews) {
        super.onPostExecute(reviews);
        mIMovieReviewsQueryTaskOnCompleteListener.onMovieReviewsQueryTaskComplete(reviews);
    }
}
