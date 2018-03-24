package com.matthewsyren.popularmovies;

import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.popularmovies.Models.Movie;
import com.matthewsyren.popularmovies.Utilities.JsonUtilities;
import com.matthewsyren.popularmovies.Utilities.NetworkUtilities;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to display the details about the Movie that the user clicked on the MainActivity
 */

public class MovieActivity extends AppCompatActivity {
    //View assignments
    @BindView(R.id.pb_movie_loading) ProgressBar mProgressBar;
    @BindView(R.id.tv_movie_title) TextView mMovieTitle;
    @BindView(R.id.tv_movie_overview) TextView mMovieOverview;
    @BindView(R.id.tv_movie_rating) TextView mMovieRating;
    @BindView(R.id.tv_movie_release_date) TextView mMovieReleaseDate;
    @BindView(R.id.tv_movie_runtime) TextView mMovieRuntime;
    @BindView(R.id.iv_movie_poster) ImageView mMoviePoster;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        Resources resources = getResources();
        Intent intent = getIntent();
        ButterKnife.bind(this);

        //Fetches the ID of the movie and uses that to fetch data about the movie
        if(intent.hasExtra(resources.getString(R.string.EXTRA_MOVIE_ID)) && NetworkUtilities.isOnline(this)){
            int id = intent.getIntExtra(resources.getString(R.string.EXTRA_MOVIE_ID), 0);
            mProgressBar.setVisibility(View.VISIBLE);
            URL url = JsonUtilities.buildMovieURL(this, id);
            MovieQueryTask movieQueryTask = new MovieQueryTask();
            movieQueryTask.execute(url);
        }
    }

    //Used to manage the fetching and displaying of movie data
    class MovieQueryTask extends AsyncTask<URL, Void, Movie>{

        @Override
        protected Movie doInBackground(URL... urls) {
            try{
                String response = NetworkUtilities.getHttpResponse(urls[0]);
                if(response != null && !response.equals("")){
                    return  JsonUtilities.getMovieDetails(response);
                }
                else{
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_movie_found_error), Toast.LENGTH_LONG).show();
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
            Resources resources = getResources();

            if(movie != null){
                //Displays the movie poster in the ImageView
                Picasso.with(getApplicationContext())
                        .load(movie.getPosterURL())
                        .into(mMoviePoster);

                //Displays the Movie's information in the appropriate Views
                setTitle(resources.getString(R.string.movie));

                mMovieTitle.setText(movie.getTitle());

                mMovieRuntime.setText(movie.getRuntime() == null || movie.getRuntime().equals("") || movie.getRuntime().equals("null")
                        ? resources.getString(R.string.movie_runtime_not_available)
                        : resources.getString(R.string.movie_runtime,movie.getRuntime()));

                mMovieReleaseDate.setText(resources.getString(R.string.movie_release_date, movie.getReleaseDate() == null || movie.getReleaseDate().equals("") || movie.getReleaseDate().equals("null")
                        ? resources.getString(R.string.not_available)
                        : movie.getReleaseDate()));

                mMovieRating.setText(movie.getUserRating() == null || movie.getUserRating().equals("") || movie.getUserRating().equals("null")
                        ? resources.getString(R.string.movie_rating_not_available)
                        : resources.getString(R.string.movie_rating, movie.getUserRating()));

                mMovieOverview.setText(resources.getString(R.string.movie_overview, movie.getOverview() == null || movie.getOverview().equals("") || movie.getOverview().equals("null")
                        ? resources.getString(R.string.not_available)
                        : movie.getOverview()));
            }
            else{
                Toast.makeText(getApplicationContext(), resources.getString(R.string.no_movie_found_error), Toast.LENGTH_LONG).show();
            }
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }
}