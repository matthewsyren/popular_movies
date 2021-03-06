package com.matthewsyren.popularmovies.tasks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.matthewsyren.popularmovies.R;
import com.matthewsyren.popularmovies.utilities.JsonUtilities;
import com.matthewsyren.popularmovies.utilities.NetworkUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Used to fetch a list of URLs pointing to the trailers for a specific movie
 */

@SuppressLint("StaticFieldLeak")
public class MovieTrailersQueryTask
        extends AsyncTask<URL, Void, ArrayList<URL>>{
    private final Context mContext;
    private final IMovieTrailersQueryTaskOnCompleteListener mMovieTrailersQueryTaskOnCompleteListener;

    public MovieTrailersQueryTask(Context context, IMovieTrailersQueryTaskOnCompleteListener movieTrailersQueryTask){
        mContext = context;
        mMovieTrailersQueryTaskOnCompleteListener = movieTrailersQueryTask;
    }

    @Override
    protected ArrayList<URL> doInBackground(URL... urls) {
        try{
            String response = NetworkUtilities.getHttpResponse(urls[0]);
            if(response != null && !response.equals("")){
                return  JsonUtilities.getMovieTrailerUrls(response);
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
    protected void onPostExecute(ArrayList<URL> trailers) {
        super.onPostExecute(trailers);
        mMovieTrailersQueryTaskOnCompleteListener.onMovieTrailerQueryTaskComplete(trailers);
    }
}