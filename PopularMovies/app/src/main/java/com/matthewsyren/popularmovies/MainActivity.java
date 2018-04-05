package com.matthewsyren.popularmovies;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.matthewsyren.popularmovies.Data.MovieContract;
import com.matthewsyren.popularmovies.Data.MovieContract.MovieEntry;
import com.matthewsyren.popularmovies.Models.MoviePoster;
import com.matthewsyren.popularmovies.MoviePosterAdapter.RecyclerViewItemClickListener;
import com.matthewsyren.popularmovies.Tasks.MovieImagesQueryTask;
import com.matthewsyren.popularmovies.Tasks.MovieImagesQueryTaskOnCompleteListener;
import com.matthewsyren.popularmovies.Utilities.BitmapUtilities;
import com.matthewsyren.popularmovies.Utilities.JsonUtilities;
import com.matthewsyren.popularmovies.Utilities.NetworkUtilities;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements RecyclerViewItemClickListener, MovieImagesQueryTaskOnCompleteListener, LoaderManager.LoaderCallbacks<Cursor>{
    @BindView(R.id.rv_movie_posters) RecyclerView mRecyclerView;
    @BindView(R.id.pb_poster_loading) ProgressBar mProgressBar;
    @BindView(R.id.b_refresh) Button mRefresh;
    private int mCheckedItem = 0;
    private static final String CHECKED_ITEM = "checked_item";
    private static final int SORT_BY_POPULARITY_KEY = 0;
    private static final int SORT_BY_RATING_KEY = 1;
    private static final int FAVOURITES_LOADER_ID = 100;
    private static final int FAVOURITES_NO_INTERNET_ID = 101;
    private static final String[] FAVOURITES_PROJECTION = {
            MovieEntry.COLUMN_MOVIE_DB_ID,
            MovieEntry.COLUMN_POSTER_URL};
    private static final String[] FAVOURITES_NO_INTERNET_PROJECTION = {
            MovieEntry.COLUMN_MOVIE_DB_ID,
            MovieEntry.COLUMN_POSTER};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        //Restores the sorting method using savedInstanceState
        if(savedInstanceState != null && savedInstanceState.containsKey(CHECKED_ITEM)){
            mCheckedItem = savedInstanceState.getInt(CHECKED_ITEM);
        }

        //Fetches movies sorted by the appropriate method
        queryMovies(getSortingMethod());
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Fetches movies sorted by the appropriate method
        queryMovies(getSortingMethod());
    }

    //Returns the sorting method that the user has chosen
    public String getSortingMethod(){
        String sortBy;
        if(mCheckedItem == SORT_BY_POPULARITY_KEY){
            sortBy = JsonUtilities.SORT_BY_POPULARITY;
        }
        else if(mCheckedItem == SORT_BY_RATING_KEY){
            sortBy = JsonUtilities.SORT_BY_RATING;
        }
        else{
            sortBy = JsonUtilities.SORT_BY_FAVOURITES;
        }
        return sortBy;
    }

    //Used to execute the AsyncTask that will fetch the data for the movies in the appropriate sort order (if there is a connection to the Internet)
    private void queryMovies(String sortBy){
        if(NetworkUtilities.isOnline(this)) {
            mRecyclerView.setAdapter(null);
            hideRefreshButton();
            mProgressBar.setVisibility(View.VISIBLE);

            if(sortBy.equals(JsonUtilities.SORT_BY_FAVOURITES)){
                //Starts the Loader that will be used to fetch the user's favourite movies when there is an Internet connection
                getSupportLoaderManager().restartLoader(FAVOURITES_LOADER_ID, null, this);
            }
            else{
                URL url = JsonUtilities.buildMoviePosterURL(this, sortBy);
                MovieImagesQueryTask movieImagesQueryTask = new MovieImagesQueryTask(this, this);
                movieImagesQueryTask.execute(url);
            }
        }
        else{
            if(getSortingMethod().equals(JsonUtilities.SORT_BY_FAVOURITES)) {
                //Starts the Loader that will be used to fetch the user's favourite movies when there is no Internet connection
                getSupportLoaderManager().restartLoader(FAVOURITES_NO_INTERNET_ID, null, this);
            }
            else{
                displayRefreshButton();
            }
        }
    }

    //Displays a button that allows the user to refresh the data
    public void displayRefreshButton(){
        mRecyclerView.setAdapter(null);
        Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
        mProgressBar.setVisibility(View.INVISIBLE);
        mRefresh.setVisibility(View.VISIBLE);
    }

    //Hides the button that allows the user to refresh the data
    public void hideRefreshButton(){
        mProgressBar.setVisibility(View.INVISIBLE);
        mRefresh.setVisibility(View.INVISIBLE);
    }

    //Tries to load the data again
    public void refreshOnClick(View view){
        mRefresh.setVisibility(View.GONE);
        queryMovies(getSortingMethod());
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Saves the sorting method that the user has chosen
        outState.putInt(CHECKED_ITEM, mCheckedItem);
        super.onSaveInstanceState(outState);
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
                        if(which == SORT_BY_POPULARITY_KEY){
                            queryMovies(JsonUtilities.SORT_BY_POPULARITY);
                        }
                        else if(which == SORT_BY_RATING_KEY){
                            queryMovies(JsonUtilities.SORT_BY_RATING);
                        }
                        else{
                            queryMovies(JsonUtilities.SORT_BY_FAVOURITES);
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
        String id = adapter.getItem(positionClicked).getId();
        Intent intent = new Intent(MainActivity.this, MovieActivity.class);
        intent.putExtra(getResources().getString(R.string.EXTRA_MOVIE_ID), id);
        startActivity(intent);
    }

    //Displays data if data is returned from the API, otherwise displays error message
    public void displayMoviePosters(List<MoviePoster> moviePosters, List<Bitmap> bitmaps){
        if(moviePosters == null || moviePosters.size() == 0){
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.no_movies_found_error), Toast.LENGTH_LONG).show();
        }
        else{
            MoviePosterAdapter moviePostAdapter;
            moviePostAdapter = new MoviePosterAdapter(moviePosters, MainActivity.this);
            if(bitmaps != null && bitmaps.size() > 0){
                moviePostAdapter.setBitmaps(bitmaps);
            }
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getApplicationContext(), numberOfColumns());
            mRecyclerView.setLayoutManager(gridLayoutManager);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.setAdapter(moviePostAdapter);
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    /* Used to calculate the number of columns that can be displayed in the RecyclerView
     * Adapted from the feedback received for Stage 1 of Popular Movies
     */
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        //Calculates how many columns can be displayed based on the screen's width, with 3 columns being the minimum
        int widthDivider = 400;
        int width = displayMetrics.widthPixels;
        int numberOfColumns = width / widthDivider;
        if (numberOfColumns < 3){
            return 3;
        }
        return numberOfColumns;
    }

    @Override
    public void onTaskComplete(List<MoviePoster> moviePosters) {
        displayMoviePosters(moviePosters, null);
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
            case FAVOURITES_LOADER_ID:
                return new CursorLoader(
                    this,
                    uri,
                    FAVOURITES_PROJECTION,
                    null,
                    null,
                    null
                );
            case FAVOURITES_NO_INTERNET_ID:
                return new CursorLoader(
                        this,
                        uri,
                        FAVOURITES_NO_INTERNET_PROJECTION,
                        null,
                        null,
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
            case FAVOURITES_LOADER_ID:
                parseFavouritesCursor(cursor);
                break;
            case FAVOURITES_NO_INTERNET_ID:
                parseFavouritesWithNoInternetCursor(cursor);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    /* Parses the Cursor and fetches the movie posters from the URLs retrieved from the database
     * This is used when there is an Internet connection, as the latest posters will then be able to be retrieved from the online API
     */
    public void parseFavouritesCursor(Cursor cursor){
        //Converts the data to a List of MoviePoster objects and passes the List to a method to display them
        if(cursor != null){
            if(cursor.getCount() == 0){
                mRecyclerView.setAdapter(null);
                Toast.makeText(getApplicationContext(), getString(R.string.no_favourites_added), Toast.LENGTH_LONG).show();
            }
            else{
                ArrayList<MoviePoster> moviePosters = new ArrayList<>();
                while(cursor.moveToNext()){
                    String movieId = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_DB_ID));
                    String moviePosterUrl = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER_URL));
                    moviePosters.add(new MoviePoster(movieId, moviePosterUrl));
                }
                if(moviePosters.size() > 0){
                    displayMoviePosters(moviePosters, null);
                }
            }
            hideRefreshButton();
        }
    }

    /* Parses the Cursor and displays the posters retrieved from the database
     * This is used when there is no Internet connection, as the posters can't be retrieved from the online API without an Internet connection
     */
    public void parseFavouritesWithNoInternetCursor(Cursor cursor){
        ArrayList<Bitmap> bitmaps = new ArrayList<>();
        ArrayList<MoviePoster> moviePosters = new ArrayList<>();

        if(cursor != null){
            if(cursor.getCount() == 0){
                mRecyclerView.setAdapter(null);
                Toast.makeText(getApplicationContext(), getString(R.string.no_favourites_added), Toast.LENGTH_LONG).show();
            }
            else{
                while(cursor.moveToNext()) {
                    String movieId = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_MOVIE_DB_ID));
                    moviePosters.add(new MoviePoster(movieId, null));
                    Bitmap bitmap = BitmapUtilities.getBitmapFromByteArray(cursor.getBlob(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER)));
                    bitmaps.add(bitmap);
                }
                if(moviePosters.size() > 0){
                    displayMoviePosters(moviePosters, bitmaps);
                }
            }
            hideRefreshButton();
        }
    }
}