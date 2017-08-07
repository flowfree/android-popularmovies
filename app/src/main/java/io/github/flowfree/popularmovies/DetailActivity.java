package io.github.flowfree.popularmovies;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Movie;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import io.github.flowfree.popularmovies.data.MovieContract;
import io.github.flowfree.popularmovies.data.MovieContract.*;
import io.github.flowfree.popularmovies.data.MovieInfo;
import io.github.flowfree.popularmovies.data.ReviewInfo;
import io.github.flowfree.popularmovies.data.TrailerInfo;
import io.github.flowfree.popularmovies.utilities.NetworkUtils;
import io.github.flowfree.popularmovies.utilities.TMDBJsonUtils;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<MovieInfo>,
        TrailerListAdapter.TrailerListClickHandler {

    private TextView mMovieTitle;
    private ImageView mMoviePoster;
    private TextView mMovieYear;
    private TextView mMovieRating;
    private Button mAddToFavoritesButton;
    private Button mRemoveFromFavoritesButton;
    private TextView mMovieSynopsis;
    private RecyclerView mTrailerListRecyclerView;
    private RecyclerView mReviewListRecyclerView;

    private TrailerListAdapter mTrailerListAdapter;
    private ReviewListAdapter mReviewListAdapter;

    private MovieInfo mMovieInfo;

    private static final int LOADER_ID = 1;

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get references to UI elements
        // I've tried using Data Binding but I don't how to use DataBinding with recyclerview yet.
        mMovieTitle = (TextView) findViewById(R.id.tv_movie_title);
        mMoviePoster = (ImageView) findViewById(R.id.im_movie_poster);
        mMovieYear = (TextView) findViewById(R.id.tv_movie_year);
        mMovieRating = (TextView) findViewById(R.id.tv_movie_rating);
        mAddToFavoritesButton = (Button) findViewById(R.id.btn_mark_as_favorite);
        mRemoveFromFavoritesButton = (Button) findViewById(R.id.btn_remove_from_favorites);
        mMovieSynopsis = (TextView) findViewById(R.id.tv_movie_synopsis);
        mTrailerListRecyclerView = (RecyclerView) findViewById(R.id.rv_trailer_list);
        mReviewListRecyclerView = (RecyclerView) findViewById(R.id.rv_review_list);

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("movieInfo")) {
                mMovieInfo = (MovieInfo) intent.getSerializableExtra("movieInfo");
                displayMovieInfo();
            }
        }

        // Setup the trailer list
        LinearLayoutManager trailerLayoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false);
        mTrailerListRecyclerView.setLayoutManager(trailerLayoutManager);
        mTrailerListRecyclerView.setHasFixedSize(true);

        mTrailerListAdapter = new TrailerListAdapter(this);
        mTrailerListRecyclerView.setAdapter(mTrailerListAdapter);

        // Setup the reviews list
        LinearLayoutManager reviewsLayoutManager = new LinearLayoutManager(
                this, LinearLayoutManager.VERTICAL, false);
        mReviewListRecyclerView.setLayoutManager(reviewsLayoutManager);
        mReviewListRecyclerView.setHasFixedSize(true);

        mReviewListAdapter = new ReviewListAdapter();
        mReviewListRecyclerView.setAdapter(mReviewListAdapter);
    }

    @Override
    protected void onResume() {
        if (isOnline()) {
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
        super.onResume();
    }

    private void displayMovieInfo() {
        mMovieTitle.setText(mMovieInfo.title);
        mMovieSynopsis.setText(mMovieInfo.synopsis);
        mMovieRating.setText(mMovieInfo.userRating + "/10");

        // Display year from the release date
        String[] str = mMovieInfo.releaseDate.split("-");
        mMovieYear.setText(str[0]);

        // Load image
        if (isOnline()) {
            URL url = NetworkUtils.buildMoviePosterUrl(mMovieInfo.posterPath);
            Picasso.with(this).load(url.toString()).into(mMoviePoster);
        }

        // If this movie is in favorites, show the "Remove from favorites" button, otherwise
        // show "Add to favorites" button.
        Cursor cursor = getContentResolver().query(MovieEntry.CONTENT_URI,
                null, "_id=?", new String[]{mMovieInfo._id}, null );
        if (cursor.getCount() == 0) {
            showAddToFavoritesButton();
        } else {
            showRemoveToFavoritesButton();
        }
    }

    /**
     * Click handler for the "Add to Favorites" button.
     *
     * @param view
     */
    public void onClickMarkAsFavorite(View view) {
        removeCurrentMovieFromFavorites();
        addCurrentMovieToFavorites();
        showRemoveToFavoritesButton();

        Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
    }

    /**
     * Click handler for the "Remove from favorites" button.
     *
     * @param view
     */
    public void onClickRemoveFromFavorites(View view) {
        removeCurrentMovieFromFavorites();
        showAddToFavoritesButton();

        Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
    }

    /**
     * Remove the current movie from favorites
     */
    private void removeCurrentMovieFromFavorites() {
        Uri deleteUri = MovieEntry.CONTENT_URI;
        deleteUri = deleteUri.buildUpon().appendPath(mMovieInfo._id).build();
        int deleted = getContentResolver().delete(deleteUri, null, null);
    }

    /**
     * Add the current movie to favorites
     */
    private void addCurrentMovieToFavorites() {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieEntry._ID, mMovieInfo._id);
        contentValues.put(MovieEntry.COLUMN_TITLE, mMovieInfo.title);
        contentValues.put(MovieEntry.COLUMN_SYNOPSIS, mMovieInfo.synopsis);
        contentValues.put(MovieEntry.COLUMN_USER_RATING, mMovieInfo.userRating);
        contentValues.put(MovieEntry.COLUMN_RELEASE_DATE, mMovieInfo.releaseDate);
        contentValues.put(MovieEntry.COLUMN_POSTER, mMovieInfo.posterPath);

        Uri insertedUri = getContentResolver().insert(MovieEntry.CONTENT_URI, contentValues);
    }

    @Override
    public Loader<MovieInfo> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<MovieInfo>(this) {

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public MovieInfo loadInBackground() {
                try {
                    URL trailersUrl = NetworkUtils.buildMovieTrailersUrl(mMovieInfo._id);
                    String trailersJson = NetworkUtils.getResponseFromHttpUrl(trailersUrl);
                    mMovieInfo.trailers = TMDBJsonUtils.getTrailerListFromJson(trailersJson);

                    URL reviewsUrl = NetworkUtils.buildMovieReviewsUrl(mMovieInfo._id);
                    String reviewsJson = NetworkUtils.getResponseFromHttpUrl(reviewsUrl);
                    mMovieInfo.reviews = TMDBJsonUtils.getReviewListFromJson(reviewsJson);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return mMovieInfo;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<MovieInfo> loader, MovieInfo movieInfo) {
        if (movieInfo.trailers == null || movieInfo.reviews == null) {
            return;
        }
        mMovieInfo = movieInfo;
        mTrailerListAdapter.setTrailersData(movieInfo.trailers);
        mReviewListAdapter.setReviewsData(movieInfo.reviews);
    }

    @Override
    public void onLoaderReset(Loader<MovieInfo> loader) {}

    @Override
    public void onClickTrailerItem(int position) {
        TrailerInfo trailerInfo = mMovieInfo.trailers.get(position);
        try {
            Intent appIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("vnd.youtube:" + trailerInfo.youtubeId));
            startActivity(appIntent);
        } catch (ActivityNotFoundException e) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/watch?v=" + trailerInfo.youtubeId)) ;
            startActivity(browserIntent);
        }
    }

    /**
     * Helper function to check if internet is available.
     *
     * @return true if online, false otherwise
     */
    private boolean isOnline() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

    /**
     * Helper method to show the "Add to favorites" button.
     */
    private void showAddToFavoritesButton() {
        mAddToFavoritesButton.setVisibility(View.VISIBLE);
        mRemoveFromFavoritesButton.setVisibility(View.GONE);
    }

    /**
     * Helper method to show the "Remove from favorites" button.
     */
    private void showRemoveToFavoritesButton() {
        mAddToFavoritesButton.setVisibility(View.GONE);
        mRemoveFromFavoritesButton.setVisibility(View.VISIBLE);
    }
}
