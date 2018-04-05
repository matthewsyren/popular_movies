package com.matthewsyren.popularmovies.Tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.matthewsyren.popularmovies.Models.Movie;
import com.matthewsyren.popularmovies.R;
import com.matthewsyren.popularmovies.Utilities.JsonUtilities;
import com.matthewsyren.popularmovies.Utilities.NetworkUtilities;

import java.io.IOException;
import java.net.URL;

/**
 * Used to query the movie information
 */

public class MovieQueryTask extends AsyncTask<URL, Void, Movie> {
    private Context mContext;
    private MovieQueryTaskOnCompleteListener mMovieQueryTaskOnCompleteListener;

    public MovieQueryTask(Context context, MovieQueryTaskOnCompleteListener movieQueryTaskOnCompleteListener){
        mContext = context;
        mMovieQueryTaskOnCompleteListener = movieQueryTaskOnCompleteListener;
    }

    @Override
    protected Movie doInBackground(URL... urls) {
        try{
            String response = NetworkUtilities.getHttpResponse(urls[0]);
            if(response != null && !response.equals("")){
                return  JsonUtilities.getMovieDetails(response);
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
    protected void onPostExecute(Movie movie) {
        super.onPostExecute(movie);
        mMovieQueryTaskOnCompleteListener.onTaskComplete(movie);
    }
}