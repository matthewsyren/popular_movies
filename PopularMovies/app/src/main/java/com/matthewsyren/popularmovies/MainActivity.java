package com.matthewsyren.popularmovies;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.matthewsyren.popularmovies.Models.MoviePoster;
import com.matthewsyren.popularmovies.MoviePosterAdapter.RecyclerViewItemClickListener;
import com.matthewsyren.popularmovies.Utilities.JsonUtilities;
import com.matthewsyren.popularmovies.Utilities.NetworkUtilities;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity
implements RecyclerViewItemClickListener{
    @BindView(R.id.rv_movie_posters) RecyclerView mRecyclerView;
    @BindView(R.id.pb_poster_loading) ProgressBar mProgressBar;
    private int mCheckedItem = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Fetches movies sorted by popularity
        queryMovies(JsonUtilities.SORT_BY_POPULARITY);
    }

    //Used to execute the AsyncTask that will fetch the data for the movies in the appropriate sort order (if there is a connection to the Internet)
    private void queryMovies(String sortBy){
        if(NetworkUtilities.isOnline(this)) {
            mRecyclerView.setAdapter(null);
            URL url = JsonUtilities.buildMoviePosterURL(this, sortBy);
            mProgressBar.setVisibility(View.VISIBLE);
            MovieImagesQueryTask movieImagesQueryTask = new MovieImagesQueryTask();
            movieImagesQueryTask.execute(url);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sort, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //Calls method that displays a Dialog that allows the user to choose their sorting method
        if(item.getItemId() == R.id.mi_sort){
            displayDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* Displays a Dialog which allows the user to choose what to sort the movies by
     * Adapted from https://developer.android.com/guide/topics/ui/dialogs.html
     */
    private void displayDialog(){
        Resources resources = getResources();

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(resources.getString(R.string.sort_by))
                .setSingleChoiceItems(resources.getStringArray(R.array.sort_methods), mCheckedItem, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mCheckedItem = which;
                        if(which == 0){
                            queryMovies(JsonUtilities.SORT_BY_POPULARITY);
                        }
                        else{
                            queryMovies(JsonUtilities.SORT_BY_RATING);
                        }
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onItemClick(int positionClicked) {
        //Takes the user to the MovieActivity when they click on a movie poster
        MoviePosterAdapter adapter = (MoviePosterAdapter) mRecyclerView.getAdapter();
        int id = adapter.getItem(positionClicked).getId();
        Intent intent = new Intent(MainActivity.this, MovieActivity.class);
        intent.putExtra(getResources().getString(R.string.EXTRA_MOVIE_ID), id);
        startActivity(intent);
    }

    class MovieImagesQueryTask extends AsyncTask<URL, Void, List<MoviePoster>>{

        @Override
        protected List<MoviePoster> doInBackground(URL... urls) {
            try{
                String response = NetworkUtilities.getHttpResponse(urls[0]);
                if(response != null && !response.equals("")){
                    return  JsonUtilities.getMoviePosters(response);
                }
                else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_movies_found_error), Toast.LENGTH_LONG).show();
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

            //Displays data if data is returned from the API, otherwise displays error message
            if(moviePosters == null || moviePosters.size() == 0){
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_movies_found_error), Toast.LENGTH_LONG).show();
            }
            else{
                MoviePosterAdapter moviePostAdapter;
                moviePostAdapter = new MoviePosterAdapter(moviePosters, MainActivity.this);
                GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), 3);
                mRecyclerView.setLayoutManager(gridLayoutManager);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setAdapter(moviePostAdapter);
            }
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}