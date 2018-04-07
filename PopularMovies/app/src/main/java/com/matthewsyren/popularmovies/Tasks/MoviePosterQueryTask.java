package com.matthewsyren.popularmovies.Tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.matthewsyren.popularmovies.Models.MoviePoster;
import com.matthewsyren.popularmovies.R;
import com.matthewsyren.popularmovies.Utilities.JsonUtilities;
import com.matthewsyren.popularmovies.Utilities.NetworkUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Used to query the movie posters for the appropriate movies
 */

public class MoviePosterQueryTask
        extends AsyncTask<URL, Void, ArrayList<MoviePoster>> {
    private Context mContext;
    private IMoviePosterQueryTaskOnCompleteListener mMoviePosterQueryTaskOnCompleteListener;

    public MoviePosterQueryTask(Context context, IMoviePosterQueryTaskOnCompleteListener moviePosterQueryTaskOnCompleteListener){
        mContext = context;
        mMoviePosterQueryTaskOnCompleteListener = moviePosterQueryTaskOnCompleteListener;
    }

    @Override
    protected ArrayList<MoviePoster> doInBackground(URL... urls) {
        try{
            String response = NetworkUtilities.getHttpResponse(urls[0]);
            if(response != null && !response.equals("")){
                return  JsonUtilities.getMoviePosters(response);
            }
            else{
                Toast.makeText(mContext, mContext.getResources().getString(R.string.no_movies_found_error), Toast.LENGTH_LONG).show();
            }
        }
        catch(IOException i){
            i.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<MoviePoster> moviePosters) {
        super.onPostExecute(moviePosters);
        mMoviePosterQueryTaskOnCompleteListener.onMoviePosterQueryTaskComplete(moviePosters);
    }
}