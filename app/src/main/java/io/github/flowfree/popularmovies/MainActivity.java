package io.github.flowfree.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import io.github.flowfree.popularmovies.data.MovieContract;
import io.github.flowfree.popularmovies.data.MovieContract.*;
import io.github.flowfree.popularmovies.data.MovieInfo;
import io.github.flowfree.popularmovies.utilities.NetworkUtils;
import io.github.flowfree.popularmovies.utilities.TMDBJsonUtils;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<List<MovieInfo>>,
        MovieListAdapter.MovieListClickHandler {

    private RecyclerView mMovieListRecyclerView;
    private TextView mErrorMessageTextView;
    private ProgressBar mLoadingIndicator;

    private List<MovieInfo> mMoviesData;
    private MovieListAdapter mMovieListAdapter;

    private static final int FILTER_MOST_POPULAR = 1;
    private static final int FILTER_TOP_RATED = 2;
    private static final int FILTER_FAVORITES = 3;
    private int mFilterMoviesBy = FILTER_MOST_POPULAR;

    private final String FILTER_MOVIES_BY_KEY = "filter";

    private static final int LOADER_ID = 10;

    // Tag for displaying the logs
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get references to the UI objects
        mMovieListRecyclerView = (RecyclerView) findViewById(R.id.rv_movie_list);
        mErrorMessageTextView = (TextView) findViewById(R.id.tv_error_message);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        // Setup the recyclerview
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        mMovieListRecyclerView.setLayoutManager(layoutManager);
        mMovieListRecyclerView.setHasFixedSize(true);

        mMovieListAdapter = new MovieListAdapter(this);
        mMovieListRecyclerView.setAdapter(mMovieListAdapter);

        // Load and set the key for TMDB API
        String apiKey = getResources().getString(R.string.themoviedb_api_key);
        NetworkUtils.setTheMovieDbApiKey(apiKey);

        // Restore previously saved state
        if (savedInstanceState != null)  {
            if (savedInstanceState.containsKey(FILTER_MOVIES_BY_KEY)) {
                mFilterMoviesBy = savedInstanceState.getInt(FILTER_MOVIES_BY_KEY);
            }
        }
    }

    @Override
    protected void onResume() {
        if (isOnline()) {
            getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        }
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_most_popular:
                mFilterMoviesBy = FILTER_MOST_POPULAR;
                break;
            case R.id.action_top_rated:
                mFilterMoviesBy = FILTER_TOP_RATED;
                break;
            case R.id.action_favorites:
                mFilterMoviesBy = FILTER_FAVORITES;
                break;
        }
        loadMovies();
        return super.onOptionsItemSelected(item);
    }

    /**
     * Fetch movies data by calling the TMBD API
     */
    private void loadMovies() {
        if (isOnline()) {
            LoaderManager loaderManager = getSupportLoaderManager();
            if (loaderManager.getLoader(LOADER_ID) == null) {
                loaderManager.initLoader(LOADER_ID, null, this);
            } else {
                loaderManager.restartLoader(LOADER_ID, null, this);
            }
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

    @Override
    public Loader<List<MovieInfo>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<List<MovieInfo>>(this) {

            @Override
            protected void onStartLoading() {
                showLoadingIndicator();
                forceLoad();
            }

            @Override
            public List<MovieInfo> loadInBackground() {
                switch (mFilterMoviesBy) {
                    case FILTER_MOST_POPULAR:
                        return fetchFromTMDBApi(NetworkUtils.buildMostPopularUrl());
                    case FILTER_TOP_RATED:
                        return fetchFromTMDBApi(NetworkUtils.buildTopRatedUrl());
                    case FILTER_FAVORITES:
                        return fetchFromLocalDb();
                    default:
                        return null;
                }
            }

            private List<MovieInfo> fetchFromTMDBApi(URL url) {
                try {
                    String jsonResponse = NetworkUtils.getResponseFromHttpUrl(url);
                    return TMDBJsonUtils.getMovieListFromJson(jsonResponse);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            private List<MovieInfo> fetchFromLocalDb() {
                List<MovieInfo> data = new ArrayList<>();
                Cursor cursor = getContentResolver().query(MovieEntry.CONTENT_URI,
                        null, null, null, MovieEntry.COLUMN_TIMESTAMP + " DESC");
                try {
                    while (cursor.moveToNext()) {
                        MovieInfo movieInfo = new MovieInfo();
                        movieInfo._id = String.valueOf(cursor.getLong(cursor.getColumnIndex(MovieEntry._ID)));
                        movieInfo.title = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_TITLE));
                        movieInfo.synopsis = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_SYNOPSIS));
                        movieInfo.userRating = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_USER_RATING));
                        movieInfo.releaseDate = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_RELEASE_DATE));
                        movieInfo.posterPath = cursor.getString(cursor.getColumnIndex(MovieEntry.COLUMN_POSTER));
                        data.add(movieInfo);
                    }
                } finally {
                    cursor.close();
                    return data;
                }
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<MovieInfo>> loader, List<MovieInfo> data) {
        if (data == null) {
            showErrorMessage();
            return;
        }
        Log.v(LOG_TAG, "total result = " + data.size());
        showMovieList();
        mMoviesData = data;
        mMovieListAdapter.setMoviesData(mMoviesData);
    }

    @Override
    public void onLoaderReset(Loader<List<MovieInfo>> loader) {}

    @Override
    public void onClickMovieItem(int position) {
        MovieInfo movieInfo = mMoviesData.get(position);
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("movieInfo", movieInfo);
        startActivity(intent);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(FILTER_MOVIES_BY_KEY, mFilterMoviesBy);
    }

    /**
     * Show recyclerview and hide everything else.
     */
    private void showMovieList() {
        mMovieListRecyclerView.setVisibility(View.VISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    /**
     * Show error message and hide everything else.
     */
    private void showErrorMessage() {
        mMovieListRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.VISIBLE);
        mLoadingIndicator.setVisibility(View.INVISIBLE);
    }

    /**
     * Show loading indicator and hide everything else
     */
    private void showLoadingIndicator() {
        mMovieListRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessageTextView.setVisibility(View.INVISIBLE);
        mLoadingIndicator.setVisibility(View.VISIBLE);
    }
}

