package com.matthewsyren.popularmovies.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Used to provide a template for a MovieReview object
 */

public class MovieReview
        implements Parcelable{
    private final String author;
    private final String content;

    public MovieReview(String author, String content) {
        this.author = author;
        this.content = content;
    }

    private MovieReview(Parcel in) {
        author = in.readString();
        content = in.readString();
    }

    public static final Creator<MovieReview> CREATOR = new Creator<MovieReview>() {
        @Override
        public MovieReview createFromParcel(Parcel in) {
            return new MovieReview(in);
        }

        @Override
        public MovieReview[] newArray(int size) {
            return new MovieReview[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(content);
    }
}
