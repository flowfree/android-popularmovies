package io.github.flowfree.popularmovies;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.net.URL;
import java.util.List;

import io.github.flowfree.popularmovies.data.MovieInfo;
import io.github.flowfree.popularmovies.utilities.NetworkUtils;


public class MovieListAdapter extends RecyclerView.Adapter<MovieListAdapter.MovieListViewHolder> {

    private Context mContext;
    private List<MovieInfo> mMoviesData;

    private final MovieListClickHandler mClickHandler;

    public class MovieListViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        public final ImageView mMoviePoster;

        public MovieListViewHolder(View view) {
            super(view);
            mMoviePoster = view.findViewById(R.id.im_movie_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            mClickHandler.onClickMovieItem(position);
        }
    }

    public interface MovieListClickHandler {
        void onClickMovieItem(int position);
    }

    public MovieListAdapter(MovieListClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    @Override
    public MovieListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.movie_list_item, parent, false);
        return new MovieListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieListViewHolder holder, int position) {
        MovieInfo movieInfo = mMoviesData.get(position);
        URL url = NetworkUtils.buildMoviePosterUrl(movieInfo.posterPath);
        Picasso.with(mContext).load(url.toString()).into(holder.mMoviePoster);
    }

    @Override
    public int getItemCount() {
        return mMoviesData == null ? 0 : mMoviesData.size();
    }

    public void setMoviesData(List<MovieInfo> moviesData) {
        mMoviesData = moviesData;
        if (mMoviesData != null) {
            notifyDataSetChanged();
        }
    }
}
