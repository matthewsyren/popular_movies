package com.matthewsyren.popularmovies;

import android.content.ActivityNotFoundException;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matthewsyren.popularmovies.adapters.IRecyclerViewOnItemClickListener;
import com.matthewsyren.popularmovies.adapters.MovieTrailerAdapter;
import com.matthewsyren.popularmovies.data.MovieContract;
import com.matthewsyren.popularmovies.data.MovieContract.MovieEntry;
import com.matthewsyren.popularmovies.models.Movie;
import com.matthewsyren.popularmovies.models.MovieReview;
import com.matthewsyren.popularmovies.tasks.IMovieQueryTaskOnCompleteListener;
import com.matthewsyren.popularmovies.tasks.IMovieReviewsQueryTaskOnCompleteListener;
import com.matthewsyren.popularmovies.tasks.IMovieTrailersQueryTaskOnCompleteListener;
import com.matthewsyren.popularmovies.tasks.MovieQueryTask;
import com.matthewsyren.popularmovies.tasks.MovieReviewsQueryTask;
import com.matthewsyren.popularmovies.tasks.MovieTrailersQueryTask;
import com.matthewsyren.popularmovies.utilities.BitmapUtilities;
import com.matthewsyren.popularmovies.utilities.JsonUtilities;
import com.matthewsyren.popularmovies.utilities.NetworkUtilities;
import com.squareup.picasso.Picasso;

import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to display the details about the Movie that the user clicked on the MainActivity
 */

public class MovieActivity
        extends AppCompatActivity
        implements IMovieQueryTaskOnCompleteListener,
        IMovieTrailersQueryTaskOnCompleteListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        IRecyclerViewOnItemClickListener,
        IMovieReviewsQueryTaskOnCompleteListener{

    //View bindings
    @BindView(R.id.pb_movie_loading) ProgressBar mProgressBar;
    @BindView(R.id.tv_movie_title) TextView mMovieTitle;
    @BindView(R.id.tv_movie_overview) TextView mMovieOverview;
    @BindView(R.id.tv_movie_rating) TextView mMovieRating;
    @BindView(R.id.tv_movie_release_date) TextView mMovieReleaseDate;
    @BindView(R.id.tv_movie_runtime) TextView mMovieRuntime;
    @BindView(R.id.iv_movie_poster) ImageView mMoviePoster;
    @BindView(R.id.ib_favourite) ImageButton mFavourite;
    @BindView(R.id.rv_movie_trailers) RecyclerView mTrailersRecyclerView;
    @BindView(R.id.tv_trailers) TextView mTrailersTitle;
    @BindView(R.id.tv_reviews) TextView mReviews;
    @BindView(R.id.tv_movie_overview_label) TextView mOverviewLabel;
    @BindView(R.id.tv_reviews_label) TextView mReviewsLabel;

    //Variables and constants
    private Movie mMovie;
    private String mMovieId;
    private String mMovieReview = "";
    private List<URL> mMovieTrailerUrls = new ArrayList<>();
    private boolean mIsFavourite;
    private Bitmap mBitmap;
    private static final int FAVOURITE_LOADER_ID = 102;
    private static final int CHECK_IF_FAVOURITE_LOADER_ID = 103;
    private static final String MOVIE_BUNDLE_KEY = "movie_bundle";
    private static final String MOVIE_REVIEW_BUNDLE_KEY = "movie_review_bundle";
    private static final String MOVIE_TRAILER_BUNDLE_KEY = "movie_trailer_bundle";
    private static final String IS_FAVOURITE_BUNDLE_KEY = "is_favourite_bundle";
    private static final String BITMAP_BUNDLE_KEY = "bitmap_bundle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        ButterKnife.bind(this);

        Resources resources = getResources();
        Intent intent = getIntent();

        //Restores cached data, or fetches the data with a Loader if there are no cached data
        if(savedInstanceState != null){
            restoreData(savedInstanceState);
        }
        else{
            mProgressBar.setVisibility(View.VISIBLE);

            //Fetches the ID of the movie and uses that to fetch data about the movie
            if(intent.hasExtra(resources.getString(R.string.EXTRA_MOVIE_ID))) {
                mMovieId = intent.getStringExtra(resources.getString(R.string.EXTRA_MOVIE_ID));
                getSupportLoaderManager().restartLoader(CHECK_IF_FAVOURITE_LOADER_ID, null, this);
            }
        }
    }

    //Method fetches the savedInstanceStateData and uses the data to assign values to the appropriate variables
    private void restoreData(Bundle savedInstanceState){
        if(savedInstanceState.containsKey(BITMAP_BUNDLE_KEY)){
            mBitmap = savedInstanceState.getParcelable(BITMAP_BUNDLE_KEY);
        }

        if(savedInstanceState.containsKey(MOVIE_BUNDLE_KEY)){
            mMovie = savedInstanceState.getParcelable(MOVIE_BUNDLE_KEY);
            displayMovieInformation(mMovie);
        }

        if(savedInstanceState.containsKey(MOVIE_REVIEW_BUNDLE_KEY)){
            mMovieReview = savedInstanceState.getString(MOVIE_REVIEW_BUNDLE_KEY);
            displayMovieReviews();
        }

        if(savedInstanceState.containsKey(MOVIE_TRAILER_BUNDLE_KEY)){
            mMovieTrailerUrls = (ArrayList<URL>) savedInstanceState.getSerializable(MOVIE_TRAILER_BUNDLE_KEY);
            displayTrailers(mMovieTrailerUrls);
        }

        if(savedInstanceState.containsKey(IS_FAVOURITE_BUNDLE_KEY)){
            mIsFavourite = savedInstanceState.getBoolean(IS_FAVOURITE_BUNDLE_KEY);
            setFavouriteButtonTint();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.share, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if(mMovie != null){
            outState.putParcelable(MOVIE_BUNDLE_KEY, mMovie);
        }

        if(mMovieReview.length() != 0){
            outState.putString(MOVIE_REVIEW_BUNDLE_KEY, mMovieReview);
        }

        if(mMovieTrailerUrls.size() != 0){
            outState.putSerializable(MOVIE_TRAILER_BUNDLE_KEY, (Serializable) mMovieTrailerUrls);
        }

        try{
            Bitmap bitmap = ((BitmapDrawable) mMoviePoster.getDrawable()).getBitmap();
            if(bitmap != null){
                outState.putParcelable(BITMAP_BUNDLE_KEY, bitmap);
            }
        }
        catch(NullPointerException n){
            n.printStackTrace();
        }

        outState.putBoolean(IS_FAVOURITE_BUNDLE_KEY, mIsFavourite);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        restoreData(savedInstanceState);
    }

    //Method fetches the movie's data from the online API, or from the SQLite database if there is no Internet connection and the movie has been added to favourites
    private void getMovieData(){
        if(NetworkUtilities.isOnline(this)){
            //Fetches and displays the data about the selected movie that hasn't been cached already
            if(mMovie == null){
                URL url = JsonUtilities.buildMovieUrl(this, mMovieId);
                MovieQueryTask movieQueryTask = new MovieQueryTask(this, this);
                movieQueryTask.execute(url);
            }

            if(mMovieTrailerUrls.size() == 0){
                URL trailerURL = JsonUtilities.buildTrailerRetrievalUrl(this, mMovieId);
                MovieTrailersQueryTask movieTrailersQueryTask = new MovieTrailersQueryTask(this, this);
                movieTrailersQueryTask.execute(trailerURL);
            }

            if(mMovieReview.length() == 0){
                URL reviewUrl = JsonUtilities.buildReviewUrl(this, mMovieId);
                MovieReviewsQueryTask movieReviewsQueryTask = new MovieReviewsQueryTask(this, this);
                movieReviewsQueryTask.execute(reviewUrl);
            }
        }
        else{
            if(mIsFavourite){
                //Fetches the data for the movie from the SQLite database
                getSupportLoaderManager().restartLoader(FAVOURITE_LOADER_ID, null, this);
            }
            else{
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.connection_error), Toast.LENGTH_LONG).show();
                onBackPressed();
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
            case R.id.mi_share:
                shareTrailer();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Allows the user to share the first trailer for the selected movie
    private void shareTrailer(){
        if(mMovieTrailerUrls.size() >= 1){
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, mMovieTrailerUrls.get(0).toString());
            startActivity(Intent.createChooser(intent, getString(R.string.share_trailer)));
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.no_trailers_to_share), Toast.LENGTH_LONG).show();
        }
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
    private void setFavouriteButtonTint(){
        if(mIsFavourite){
            mFavourite.setColorFilter(ContextCompat.getColor(this, R.color.colorOrange), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
        else{
            mFavourite.setColorFilter(ContextCompat.getColor(this, R.color.colorWhite), android.graphics.PorterDuff.Mode.MULTIPLY);
        }
    }

    //Method adds the movie to the user's favourites
    private void addMovieToFavourites(){
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

        //Displays the appropriate message based on whether the data was inserted or not
        if(newUri != null){
            Toast.makeText(getApplicationContext(), getString(R.string.movie_added_to_favourites), Toast.LENGTH_LONG).show();
            mIsFavourite = true;
        }
        else{
            Toast.makeText(getApplicationContext(), getString(R.string.favourite_error_message), Toast.LENGTH_LONG).show();
        }
    }

    //Method removes the movie from the user's favourites
    private void removeMovieFromFavourites(){
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
    private void displayMovieInformation(Movie movie){
        Resources resources = getResources();

        if(movie != null){
            //Displays the movie poster in the ImageView if there is an Internet connection or if the Bitmap is cached
            if(mBitmap != null){
                mMoviePoster.setImageBitmap(mBitmap);
            }
            else if(NetworkUtilities.isOnline(this)){
                Picasso.with(getApplicationContext())
                        .load(movie.getPosterURL())
                        .placeholder(R.color.colorGrey)
                        .error(R.color.colorGrey)
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

            mMovieOverview.setText(movie.getOverview() == null || movie.getOverview().equals("") || movie.getOverview().equals("null")
                    ? resources.getString(R.string.not_available)
                    : movie.getOverview());
            mOverviewLabel.setVisibility(View.VISIBLE);

            mMovie = movie;
            setFavouriteButtonTint();
        }
        else{
            Toast.makeText(getApplicationContext(), resources.getString(R.string.no_movie_found_error), Toast.LENGTH_LONG).show();
        }
        mProgressBar.setVisibility(View.INVISIBLE);
        mFavourite.setVisibility(View.VISIBLE);
    }

    //Displays the movie's information
    @Override
    public void onMovieQueryTaskComplete(Movie movie) {
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
        throw new RuntimeException(getString(R.string.loader_id_not_found, id));
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        int id = loader.getId();

        //Executes the appropriate action based on the Loader's ID
        switch(id){
            case FAVOURITE_LOADER_ID:
                parseCursorData(cursor);
                getSupportLoaderManager().destroyLoader(FAVOURITE_LOADER_ID);
                break;
            case CHECK_IF_FAVOURITE_LOADER_ID:
                mIsFavourite = cursor.moveToFirst();
                getMovieData();
                getSupportLoaderManager().destroyLoader(CHECK_IF_FAVOURITE_LOADER_ID);
                break;
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {

    }

    //Parses the data from the Cursor and sends the data to be displayed
    private void parseCursorData(Cursor cursor){
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
    public void onMovieTrailerQueryTaskComplete(ArrayList<URL> urls) {
        //Displays the trailers
        mMovieTrailerUrls = urls;
        displayTrailers(urls);
    }

    //Displays a list of trailers for the movie
    private void displayTrailers(List<URL> urls){
        if(urls.size() > 0){
            mTrailersTitle.setVisibility(View.VISIBLE);
            MovieTrailerAdapter movieTrailerAdapter = new MovieTrailerAdapter(urls, this);
            LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
            linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            mTrailersRecyclerView.setLayoutManager(linearLayoutManager);
            mTrailersRecyclerView.setAdapter(movieTrailerAdapter);
            mTrailersTitle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(int positionClicked) {
        URL url = ((MovieTrailerAdapter)mTrailersRecyclerView.getAdapter()).getUrl(positionClicked);

        //Opens an app that lets the user watch the trailer
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url.toString()));
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.movie_trailer_intent_chooser)));
        }
        catch (ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), getString(R.string.trailer_error), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onMovieReviewsQueryTaskComplete(ArrayList<MovieReview> reviews) {
        //Adds all reviews into a String variable and displays it
        for(MovieReview movieReview : reviews){
            mMovieReview += getString(R.string.review_line, movieReview.getAuthor(), movieReview.getContent());
        }

        if(mMovieReview.length() > 0){
            displayMovieReviews();
        }
    }

    //Displays the movie reviews fetched from the MovieDB API and displays them in a TextView
    private void displayMovieReviews(){
        mReviewsLabel.setVisibility(View.VISIBLE);
        mReviews.setText(mMovieReview);
    }
}