package com.matthewsyren.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used to provide a template for a MoviePoster object
 */

public class MoviePoster
        implements Parcelable{
    private final String id;
    private final String posterURL;

    public MoviePoster(String id, String posterURL) {
        this.id = id;
        this.posterURL = posterURL;
    }

    private MoviePoster(Parcel in) {
        id = in.readString();
        posterURL = in.readString();
    }

    public static final Creator<MoviePoster> CREATOR = new Creator<MoviePoster>() {
        @Override
        public MoviePoster createFromParcel(Parcel in) {
            return new MoviePoster(in);
        }

        @Override
        public MoviePoster[] newArray(int size) {
            return new MoviePoster[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getPosterURL() {
        return posterURL;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(posterURL);
    }
}