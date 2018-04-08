package com.matthewsyren.popularmovies.utilities;

import android.content.Context;
import android.net.Uri;

import com.matthewsyren.popularmovies.models.Movie;
import com.matthewsyren.popularmovies.models.MoviePoster;
import com.matthewsyren.popularmovies.models.MovieReview;
import com.matthewsyren.popularmovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Used to parse JSON that is retrieved from the Movies DB API
 */

public class JsonUtilities {
    //URL constants
    private static final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie";
    private static final String INDIVIDUAL_MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";
    private static final String YOUTUBE_VIDEO_BASE_URL = "https://www.youtube.com/watch";
    private static final String QUERY_PARAMETER_API_KEY = "api_key";
    private static final String QUERY_PARAMETER_TRAILER_KEY = "v";
    public static final String SORT_BY_POPULARITY = "popular";
    public static final String SORT_BY_RATING = "top_rated";
    public static final String SORT_BY_FAVOURITES = "favourites";
    private static final String PATH_VIDEO = "videos";
    private static final String PATH_REVIEWS = "reviews";

    //Creates a URL that can be used to fetch either the most popular or highest rated movies
    public static URL buildMoviePosterURL(Context context, String sortBy){
        try{
            Uri uri = Uri.parse(MOVIE_DB_BASE_URL)
                    .buildUpon()
                    .appendPath(sortBy)
                    .appendQueryParameter(QUERY_PARAMETER_API_KEY, context.getResources().getString(R.string.API_KEY))
                    .build();
            return new URL(uri.toString());
        }
        catch(MalformedURLException m){
            m.printStackTrace();
        }
        return null;
    }

    //Creates a URL that can be used to retrieve information about a specific movie
    public static URL buildMovieUrl(Context context, String movieId){
        try{
            Uri uri = Uri.parse(INDIVIDUAL_MOVIE_DB_BASE_URL)
                    .buildUpon()
                    .appendPath(movieId)
                    .appendQueryParameter(QUERY_PARAMETER_API_KEY, context.getResources().getString(R.string.API_KEY))
                    .build();
            return new URL(uri.toString());
        }
        catch(MalformedURLException m){
            m.printStackTrace();
        }
        return null;
    }

    //Creates a URL that can be used to access the information about the trailers for the movie
    public static URL buildTrailerRetrievalUrl(Context context, String movieId){
        try{
            Uri uri = Uri.parse(MOVIE_DB_BASE_URL)
                    .buildUpon()
                    .appendPath(movieId)
                    .appendPath(PATH_VIDEO)
                    .appendQueryParameter(QUERY_PARAMETER_API_KEY, context.getResources().getString(R.string.API_KEY))
                    .build();
            return new URL(uri.toString());
        }
        catch(MalformedURLException m){
            m.printStackTrace();
        }
        return null;
    }

    //Creates a URL that can be used to access the trailers for the movie
    private static URL buildTrailerVideoUrl(String trailerKey){
        try{
            Uri uri = Uri.parse(YOUTUBE_VIDEO_BASE_URL)
                    .buildUpon()
                    .appendQueryParameter(QUERY_PARAMETER_TRAILER_KEY, trailerKey)
                    .build();
            return new URL(uri.toString());
        }
        catch(MalformedURLException m){
            m.printStackTrace();
        }
        return null;
    }

    //Creates a URL that can be used to fetch the reviews for a movie
    public static URL buildReviewUrl(Context context, String movieId){
        try{
            Uri uri = Uri.parse(MOVIE_DB_BASE_URL)
                    .buildUpon()
                    .appendPath(movieId)
                    .appendPath(PATH_REVIEWS)
                    .appendQueryParameter(QUERY_PARAMETER_API_KEY, context.getResources().getString(R.string.API_KEY))
                    .build();
            return new URL(uri.toString());
        }
        catch(MalformedURLException m){
            m.printStackTrace();
        }
        return null;
    }

    //Parses the JSON retrieved from the API and returns an ArrayList of URLs that point to the trailers for a specific movie
    public static ArrayList<URL> getMovieTrailerUrls(String json){
        ArrayList<URL> urls = new ArrayList<>();
        try{
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray("results");
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject trailer = jsonArray.getJSONObject(i);
                URL url = buildTrailerVideoUrl(trailer.getString("key"));
                if(url != null){
                    urls.add(url);
                }
            }
        }
        catch(JSONException j){
            j.printStackTrace();
        }
        return urls;
    }

    //Parses the JSON retrieved from the API and returns an ArrayList of the data about the movie posters for the movies
    public static ArrayList<MoviePoster> getMoviePosters(String json){
        ArrayList<MoviePoster> moviePosters = new ArrayList<>();
        if(json != null){
            try{
                JSONObject jsonObject = new JSONObject(json);
                JSONArray moviesJSON = jsonObject.getJSONArray("results");
                for(int i = 0; i < moviesJSON.length(); i++){
                    JSONObject movieJSON = moviesJSON.getJSONObject(i);
                    String movieID = movieJSON.getString("id");
                    String posterURL = MOVIE_POSTER_BASE_URL + movieJSON.getString("poster_path");
                    MoviePoster moviePoster = new MoviePoster(movieID, posterURL);
                    moviePosters.add(moviePoster);
                }
                return moviePosters;
            }
            catch(JSONException j){
                j.printStackTrace();
            }
        }
        return null;
    }

    //Parses the JSON retrieved from the API and returns an ArrayList of MovieReview objects
    public static ArrayList<MovieReview> getMovieReviews(String json){
        ArrayList<MovieReview> movieReviews = new ArrayList<>();
        if(json != null){
            try{
                JSONObject jsonObject = new JSONObject(json);
                JSONArray reviews = jsonObject.getJSONArray("results");
                for(int i = 0; i < reviews.length(); i++){
                    JSONObject movieJSON = reviews.getJSONObject(i);
                    String author = movieJSON.getString("author");
                    String content = movieJSON.getString("content");
                    MovieReview movieReview = new MovieReview(author, content);
                    movieReviews.add(movieReview);
                }
                return movieReviews;
            }
            catch(JSONException j){
                j.printStackTrace();
            }
        }
        return null;
    }

    //Parses the JSON retrieved from the API and returns the data in the form of a Movie object
    public static Movie getMovieDetails(String json){
        try{
            JSONObject movieJSON = new JSONObject(json);
            String id = movieJSON.getString("id");
            String title = movieJSON.getString("title");
            String overview = movieJSON.getString("overview");
            String releaseDate = movieJSON.getString("release_date");
            String runtime = movieJSON.getString("runtime");
            String posterURL = MOVIE_POSTER_BASE_URL + movieJSON.getString("poster_path");
            String rating = movieJSON.getString("vote_average");
            return new Movie(id, title, posterURL, overview, rating, releaseDate, runtime);
        }
        catch(JSONException j){
            j.printStackTrace();
        }
        return null;
    }
}