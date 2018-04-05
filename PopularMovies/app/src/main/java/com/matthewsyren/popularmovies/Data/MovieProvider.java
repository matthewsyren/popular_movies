package com.matthewsyren.popularmovies.Data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.matthewsyren.popularmovies.Data.MovieContract.MovieEntry;

/**
 * Used to provide a ContentProvider to the Movies SQLite database
 */

public class MovieProvider extends ContentProvider{
    private UriMatcher mUriMatcher = buildUriMatcher();
    private MovieDbHelper mMovieDbHelper;
    private static final int CODE_MOVIES = 100;
    private static final int CODE_MOVIE_WITH_ID = 101;

    public static UriMatcher buildUriMatcher(){
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES, CODE_MOVIES);
        uriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIES + "/#", CODE_MOVIE_WITH_ID);
        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mMovieDbHelper = new MovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;

        switch(mUriMatcher.match(uri)){
            case CODE_MOVIES:
                cursor = mMovieDbHelper.getReadableDatabase()
                        .query(
                                MovieEntry.TABLE_NAME,
                                projection,
                                selection,
                                selectionArgs,
                                null,
                                null,
                                sortOrder
                        );
                break;
            default:
                throw new UnsupportedOperationException("Uri not recognised: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri newUri;

        switch(mUriMatcher.match(uri)){
            case CODE_MOVIES:
                long id = mMovieDbHelper.getWritableDatabase()
                        .insert(
                                MovieEntry.TABLE_NAME,
                                null,
                                values
                        );
                newUri = ContentUris.withAppendedId(MovieContract.BASE_CONTENT_URI, id);
                break;
            default:
                throw new UnsupportedOperationException("Uri not recognised: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return newUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int rowsDeleted = 0;

        //Allows the returning of the number of rows deleted if the selection passed in is null
        if(selection == null){
            selection = "1";
        }

        switch(mUriMatcher.match(uri)){
            case CODE_MOVIES:
                rowsDeleted = mMovieDbHelper.getWritableDatabase()
                        .delete(
                                MovieEntry.TABLE_NAME,
                                selection,
                                selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Uri not recognised: " + uri);
        }

        if(rowsDeleted > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}