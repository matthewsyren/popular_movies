package com.matthewsyren.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Used as the contract to define the structure of the SQLite database
 */

@SuppressWarnings("WeakerAccess")
public class MovieContract {
    public static final String CONTENT_AUTHORITY = "com.matthewsyren.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_MOVIES = "movies";

    public class MovieEntry implements BaseColumns{
        public static final String TABLE_NAME = "movies";
        public static final String MOVIE_ID = "_id";
        public static final String COLUMN_MOVIE_DB_ID = "movie_db_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_POSTER = "poster";
        public static final String COLUMN_POSTER_URL = "poster_url";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_USER_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_RUNTIME = "runtime";
    }
}