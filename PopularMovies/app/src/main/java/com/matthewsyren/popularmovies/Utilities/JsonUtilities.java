package com.matthewsyren.popularmovies.Utilities;

import android.content.Context;
import android.net.Uri;

import com.matthewsyren.popularmovies.Models.Movie;
import com.matthewsyren.popularmovies.Models.MoviePoster;
import com.matthewsyren.popularmovies.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to parse JSON that is retrieved from the Movies DB API
 */

public class JsonUtilities {
    //Constants
    private static final String MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie";
    private static final String INDIVIDUAL_MOVIE_DB_BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String QUERY_PARAMETER_API_KEY = "api_key";
    public static final String SORT_BY_POPULARITY = "popular";
    public static final String SORT_BY_RATING = "top_rated";
    private static final String MOVIE_POSTER_BASE_URL = "http://image.tmdb.org/t/p/w185/";

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
    public static URL buildMovieURL(Context context, int movieID){
        try{
            Uri uri = Uri.parse(INDIVIDUAL_MOVIE_DB_BASE_URL)
                    .buildUpon()
                    .appendPath(String.valueOf(movieID))
                    .appendQueryParameter(QUERY_PARAMETER_API_KEY, context.getResources().getString(R.string.API_KEY))
                    .build();
            return new URL(uri.toString());
        }
        catch(MalformedURLException m){
            m.printStackTrace();
        }
        return null;
    }

    //Parses the JSON retrieved from the API and returns a list of the data about the movie posters for the movies
    public static List<MoviePoster> getMoviePosters(String json){
        List<MoviePoster> moviePosters = new ArrayList<>();
        if(json != null){
            try{
                JSONObject jsonObject = new JSONObject(json);
                JSONArray moviesJSON = jsonObject.getJSONArray("results");
                for(int i = 0; i < moviesJSON.length(); i++){
                    JSONObject movieJSON = moviesJSON.getJSONObject(i);
                    int movieID = movieJSON.getInt("id");
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

    //Parses the JSON retrieved from the API and returns the data in the form of a Movie object
    public static Movie getMovieDetails(String json){
        try{
            JSONObject movieJSON = new JSONObject(json);
            String title = movieJSON.getString("title");
            String overview = movieJSON.getString("overview");
            String releaseDate = movieJSON.getString("release_date");
            String runtime = movieJSON.getString("runtime");
            String posterURL = MOVIE_POSTER_BASE_URL + movieJSON.getString("poster_path");
            String rating = movieJSON.getString("vote_average");
            return new Movie(title, posterURL, overview, rating, releaseDate, runtime);
        }
        catch(JSONException j){
            j.printStackTrace();
        }
        return null;
    }
}