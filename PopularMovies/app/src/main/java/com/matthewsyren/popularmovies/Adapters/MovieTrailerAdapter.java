package com.matthewsyren.popularmovies.Adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.matthewsyren.popularmovies.R;

import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Used to display the trailers for the movie
 */

public class MovieTrailerAdapter
        extends RecyclerView.Adapter<MovieTrailerAdapter.TrailerViewHolder>{
    private List<URL> mTrailerUrls;
    private final IRecyclerViewOnItemClickListener mItemClickListener;

    public MovieTrailerAdapter(List<URL> trailerUrls, IRecyclerViewOnItemClickListener itemClickListener){
        mTrailerUrls = trailerUrls;
        mItemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public TrailerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.trailer_list_item, parent, false);
        return new TrailerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrailerViewHolder holder, int position) {
        Context context = holder.trailerTitle.getContext();
        holder.trailerTitle.setText(context.getString(R.string.trailer_name, position + 1));
    }

    @Override
    public int getItemCount() {
        return mTrailerUrls.size();
    }

    public URL getUrl(int position){
        return mTrailerUrls.get(position);
    }

    class TrailerViewHolder
            extends RecyclerView.ViewHolder
            implements View.OnClickListener{
        @BindView(R.id.tv_play_trailer) TextView trailerTitle;

        TrailerViewHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(getAdapterPosition());
        }
    }
}