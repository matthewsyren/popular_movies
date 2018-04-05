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
import java.util.List;

/**
 * Used to query the movie posters for the appropriate movies
 */

public class MovieImagesQueryTask extends AsyncTask<URL, Void, List<MoviePoster>> {
    private Context mContext;
    private MovieImagesQueryTaskOnCompleteListener mMovieImagesQueryTaskOnCompleteListener;

    public MovieImagesQueryTask(Context context, MovieImagesQueryTaskOnCompleteListener movieImagesQueryTaskOnCompleteListener){
        mContext = context;
        mMovieImagesQueryTaskOnCompleteListener = movieImagesQueryTaskOnCompleteListener;
    }

    @Override
    protected List<MoviePoster> doInBackground(URL... urls) {
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
    protected void onPostExecute(List<MoviePoster> moviePosters) {
        super.onPostExecute(moviePosters);
        mMovieImagesQueryTaskOnCompleteListener.onTaskComplete(moviePosters);
    }
}