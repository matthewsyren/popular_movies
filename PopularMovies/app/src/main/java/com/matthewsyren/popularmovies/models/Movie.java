package com.matthewsyren.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used to provide a template for a Movie object
 */

public class Movie
        implements Parcelable {
    private final String id;
    private final String title;
    private final String posterURL;
    private final String overview;
    private final String userRating;
    private final String releaseDate;
    private final String runtime;

    private Movie(Parcel in){
        id = in.readString();
        title = in.readString();
        posterURL = in.readString();
        overview = in.readString();
        userRating = in.readString();
        releaseDate = in.readString();
        runtime = in.readString();
    }

    public Movie(String id, String title, String posterURL, String overview, String userRating, String releaseDate, String runtime) {
        this.id = id;
        this.title = title;
        this.posterURL = posterURL;
        this.overview = overview;
        this.userRating = userRating;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getPosterURL() {
        return posterURL;
    }

    public String getOverview() {
        return overview;
    }

    public String getUserRating() {
        return userRating;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getRuntime() {
        return runtime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(posterURL);
        dest.writeString(overview);
        dest.writeString(userRating);
        dest.writeString(releaseDate);
        dest.writeString(runtime);
    }
}