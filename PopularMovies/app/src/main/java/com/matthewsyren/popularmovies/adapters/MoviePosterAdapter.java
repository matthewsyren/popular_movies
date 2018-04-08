package com.matthewsyren.popularmovies.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.matthewsyren.popularmovies.models.MoviePoster;
import com.matthewsyren.popularmovies.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to display the movie posters in a GridLayout
 */

public class MoviePosterAdapter
        extends RecyclerView.Adapter<MoviePosterAdapter.MoviePosterViewHolder>{
    private final List<MoviePoster> mMoviePosters;
    private final IRecyclerViewOnItemClickListener mItemClickListener;
    private List<Bitmap> mBitmaps;

    public MoviePosterAdapter(List<MoviePoster> moviePosters, IRecyclerViewOnItemClickListener itemClickListener){
        mMoviePosters = moviePosters;
        mItemClickListener = itemClickListener;
    }

    public void setBitmaps(List<Bitmap> bitmaps){
        mBitmaps = bitmaps;
    }

    @Override
    @NonNull
    public MoviePosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.movie_poster_list_item, parent, false);
        return new MoviePosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoviePosterViewHolder holder, int position) {
        //Displays the movie poster in the appropriate ImageView
        if(mMoviePosters.get(position).getPosterURL() != null){
            Picasso.with(holder.moviePoster.getContext())
                    .load(mMoviePosters.get(position).getPosterURL())
                    .placeholder(R.color.colorGrey)
                    .error(R.color.colorGrey)
                    .fit()
                    .centerInside()
                    .into(holder.moviePoster);
        }
        else{
            //Displays the Bitmaps retrieved from the SQLite database if there is no Internet connection
            if(mBitmaps != null){
                holder.moviePoster.setImageBitmap(mBitmaps.get(position));
            }
        }
    }

    @Override
    public int getItemCount() {
        return mMoviePosters.size();
    }

    public MoviePoster getItem(int position){
        return mMoviePosters.get(position);
    }

    class MoviePosterViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        @BindView(R.id.iv_movie_poster) ImageView moviePoster;

        MoviePosterViewHolder(View view){
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            mItemClickListener.onItemClick(getAdapterPosition());
        }
    }
}