package com.matthewsyren.popularmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.popularmovies.Data.MovieContract;
import com.matthewsyren.popularmovies.Data.MovieContract.MovieEntry;
import com.matthewsyren.popularmovies.Models.Movie;
import com.matthewsyren.popularmovies.Tasks.MovieQueryTask;
import com.matthewsyren.popularmovies.Tasks.MovieQueryTaskOnCompleteListener;
import com.matthewsyren.popularmovies.Utilities.BitmapUtilities;
import com.matthewsyren.popularmovies.Utilities.JsonUtilities;
import com.matthewsyren.popularmovies.Utilities.NetworkUtilities;
import com.squareup.picasso.Picasso;

import java.net.URL;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to display the details about the Movie that the user clicked on the MainActivity
 */

public class MovieActivity extends AppCompatActivity implements MovieQueryTaskOnCompleteListener, LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.pb_movie_loading) ProgressBar mProgressBar;
    @BindView(R.id.tv_movie_title) TextView mMovieTitle;
    @BindView(R.id.tv_movie_overview) TextView mMovieOverview;
    @BindView(R.id.tv_movie_rating) TextView mMovieRating;
    @BindView(R.id.tv_movie_release_date) TextView mMovieReleaseDate;
    @BindView(R.id.tv_movie_runtime) TextView mMovieRuntime;
    @BindView(R.id.iv_movie_poster) ImageView mMoviePoster;
    @BindView(R.id.ib_favourite) ImageButton mFavourite;
    private Movie mMovie;
    private String mMovieId;
    private static final int FAVOURITE_LOADER_ID = 102;
    private static final int CHECK_IF_FAVOURITE_LOADER_ID = 103;
    private boolean mIsFavourite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        ButterKnife.bind(this);
        showProgressBar();

        Resources resources = getResources();
        Intent intent = getIntent();

        //Fetches the ID of the movie and uses that to fetch data about the movie
        if(intent.hasExtra(resources.getString(R.string.EXTRA_MOVIE_ID))) {
            mMovieId = intent.getStringExtra(resources.getString(R.string.EXTRA_MOVIE_ID));
            getSupportLoaderManager().restartLoader(CHECK_IF_FAVOURITE_LOADER_ID, null, this);
        }
    }

    //Method fetches the movie's data from the online API, or from the SQLite database if there is no Internet connection and the movie has been added to favourites
    public void getMovieData(){
        if(NetworkUtilities.isOnline(this)){
            //Fetches the data about the movie from the MovieDB API
            URL url = JsonUtilities.buildMovieURL(this, mMovieId);
            MovieQueryTask movieQueryTask = new MovieQueryTask(this, this);
            movieQueryTask.execute(url);
        }
        else{
            if(mIsFavourite){
                //Fetches the data for the movie from the SQLite database
                getSupportLoaderManager().restartLoader(FAVOURITE_LOADER_ID, null, this);
            }
            else{
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                hideProgressBar();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch(id){
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Displays the ProgressBar and hides the favourite button
    public void showProgressBar(){
        mProgressBar.setVisibility(View.VISIBLE);
        mFavourite.setVisibility(View.INVISIBLE);
    }

    //Hides the ProgressBar and displays the favourite button
    public void hideProgressBar(){
        mProgressBar.setVisibility(View.INVISIBLE);
        mFavourite.setVisibility(View.VISIBLE);
    }

    //Method performs the appropriate action when the favourite button is clicked
    public void favouriteOnClick(View view){
        if(mIsFavourite){
            removeMovieFromFavourites();
        }
        else{
            addMovieToFavourites();
        }
        setFavouriteButtonTint();
    }

    //Displays the appropriate tint on the favourite button (based on whether the movie has been added to favourites by the user)
    public void setFavouriteButtonTint(){
        if(mIsFavourite){
            mFavourite.setColorFilter(ContextCompat.getColor(this, R.color.colorOrange), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else{
            mFavourite.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }

    //Method adds the movie to the user's favourites
    public void addMovieToFavourites(){
        //Puts the movie's details into a ContentValues object
        Bitmap bitmap = null;
        if(mMoviePoster.getDrawable() != null){
            bitmap = ((BitmapDrawable)mMoviePoster.getDrawable()).getBitmap();
        }
        ContentResolver contentResolver = getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieEntry.COLUMN_TITLE, mMovie.getTitle());
        contentValues.put(MovieEntry.COLUMN_MOVIE_DB_ID, mMovie.getId());
        contentValues.put(MovieEntry.COLUMN_POSTER, BitmapUtilities.getByteArrayFromBitmap(bitmap));
        contentValues.put(MovieEntry.COLUMN_POSTER_URL, mMovie.getPosterURL());
        contentValues.put(MovieEntry.COLUMN_OVERVIEW, mMovie.getOverview());
        contentValues.put(MovieEntry.COLUMN_RELEASE_DATE, mMovie.getReleaseDate());
        contentValues.put(MovieEntry.COLUMN_RUNTIME, mMovie.getRuntime());
        contentValues.put(MovieEntry.COLUMN_USER_RATING, mMovie.getUserRating());

        //Sends the data to the ContentProvider to be inserted
        Uri uri = MovieContract.BASE_CONTENT_URI
                .buildUpon()
                .appendPath(MovieContract.PATH_MOVIES)
                .build();

        Uri newUri = contentResolver.insert(uri, contentValues);

        //Displays the appropriate message based on whether the data was inserted
        if(newUri != null){
            Toast.makeText(getApplicationContext(), getString(R.string.movie_added_to_favourites), Toast.LENGTH_LONG).show();
            mIsFavourite = true;
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.favourite_error_message), Toast.LENGTH_LONG).show();
        }
    }

    //Method removes the movie from the user's favourites
    public void removeMovieFromFavourites(){
        Uri uri = MovieContract.BASE_CONTENT_URI
                .buildUpon()
                .appendPath(MovieContract.PATH_MOVIES)
                .build();

        int rowsDeleted = getContentResolver().delete(uri, MovieEntry.COLUMN_MOVIE_DB_ID + "=?", new String[]{mMovie.getId()});

        //Displays the appropriate message based on whether the data was deleted
        if(rowsDeleted > 0){
            Toast.makeText(getApplicationContext(), getString(R.string.movie_removed_from_favourites), Toast.LENGTH_LONG).show();
            mIsFavourite = false;
        }
        else {
            Toast.makeText(getApplicationContext(), getString(R.string.favourite_error_message), Toast.LENGTH_LONG).show();
        }
    }

    //Method displays the Movie object data in the appropriate Views
    public void displayMovieInformation(Movie movie){
        Resources resources = getResources();

        if(movie != null){
            //Displays the movie poster in the ImageView if there is an Internet connection
            if(NetworkUtilities.isOnline(this)){
                Picasso.with(getApplicationContext())
                        .load(movie.getPosterURL())
                        .into(mMoviePoster);
            }

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

            mMovie = movie;
            setFavouriteButtonTint();
        }
        else{
            Toast.makeText(getApplicationContext(), resources.getString(R.string.no_movie_found_error), Toast.LENGTH_LONG).show();
        }
        hideProgressBar();
    }

    //Displays the movie's information
    @Override
    public void onTaskComplete(Movie movie) {
        displayMovieInformation(movie);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        //Builds URI to access the ContentProvider
        Uri uri = MovieContract.BASE_CONTENT_URI
                .buildUpon()
                .appendPath(MovieContract.PATH_MOVIES)
                .build();

        //Executes the appropriate action based on the Loader's ID
        switch(id){
            case FAVOURITE_LOADER_ID:
                return new CursorLoader(
                        this,
                        uri,
                        null,
                        MovieEntry.COLUMN_MOVIE_DB_ID + "=?",
                        new String[]{mMovieId},
                        null
                );
            case CHECK_IF_FAVOURITE_LOADER_ID:
                return new CursorLoader(
                        this,
                        uri,
                        new String[]{ MovieEntry.MOVIE_ID },
                        MovieEntry.COLUMN_MOVIE_DB_ID + "=?",
                        new String[]{mMovieId},
                        null
                );
        }
        throw new RuntimeException("Loader ID not recognised: " + id);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        int id = loader.getId();

        //Executes the appropriate action based on the Loader's ID
        switch(id){
            case FAVOURITE_LOADER_ID:
                parseCursorData(cursor);
                break;
            case CHECK_IF_FAVOURITE_LOADER_ID:
                mIsFavourite = cursor.moveToFirst();
                getMovieData();
                break;
        }
    }

    //Parses the data from the Cursor and sends the data to be displayed
    public void parseCursorData(Cursor cursor){
        if(cursor != null && cursor.moveToFirst()){
            //Creates a Movie object with the data from the Cursor
            Movie movie = new Movie(
                    cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_DB_ID)),
                    cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE)),
                    cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_URL)),
                    cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_OVERVIEW)),
                    cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_USER_RATING)),
                    cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE)),
                    cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RUNTIME))
            );

            //Displays the data from the Cursor
            displayMovieInformation(movie);

            //Loads the poster from the SQLite database and displays it
            Bitmap bitmap = BitmapUtilities.getBitmapFromByteArray(cursor.getBlob(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER)));
            mMoviePoster.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }
}