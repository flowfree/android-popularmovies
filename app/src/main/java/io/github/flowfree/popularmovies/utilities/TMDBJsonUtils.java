package io.github.flowfree.popularmovies.utilities;


import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.github.flowfree.popularmovies.data.MovieInfo;
import io.github.flowfree.popularmovies.data.ReviewInfo;
import io.github.flowfree.popularmovies.data.TrailerInfo;


public final class TMDBJsonUtils {

    static final String TMDB_STATUS_CODE = "status_code";
    static final String TMDB_STATUS_MESSAGE = "status_message";

    static final String TMDB_MOVIES_RESULT = "results";
    static final String TMDB_TRAILERS_RESULT = "youtube";
    static final String TMDB_REVIEWS_RESULT = "results";

    static final String TMDB_MOVIE_ID = "id";
    static final String TMDB_MOVIE_TITLE = "title";
    static final String TMDB_MOVIE_SYNOPSIS = "overview";
    static final String TMDB_MOVIE_POSTER_PATH = "poster_path";
    static final String TMDB_MOVIE_USER_RATING = "vote_average";
    static final String TMDB_MOVIE_RELEASE_DATE = "release_date";

    private static final String LOG_TAG = TMDBJsonUtils.class.getSimpleName();

    /**
     * Helper method for parsing JSON response into list of MovieInfo objects.
     *
     * @param jsonStr
     * @return
     * @throws JSONException
     */
    public static List<MovieInfo> getMovieListFromJson(String jsonStr)
            throws JSONException {
        List<MovieInfo> parsedMoviesData = new ArrayList<>();

        JSONObject moviesJson = new JSONObject(jsonStr);

        if (moviesJson.has(TMDB_STATUS_CODE)) {
            String message = moviesJson.getString(TMDB_STATUS_MESSAGE);
            Log.e(LOG_TAG, message);
            return null;
        }

        JSONArray moviesArray = moviesJson.getJSONArray(TMDB_MOVIES_RESULT);

        for (int i = 0; i < moviesArray.length(); i++) {
            JSONObject movieObj = moviesArray.getJSONObject(i);

            MovieInfo movieInfo = new MovieInfo();
            movieInfo._id = movieObj.getString(TMDB_MOVIE_ID);
            movieInfo.title = movieObj.getString(TMDB_MOVIE_TITLE);
            movieInfo.synopsis = movieObj.getString(TMDB_MOVIE_SYNOPSIS);
            movieInfo.userRating = String.valueOf(movieObj.getDouble(TMDB_MOVIE_USER_RATING));
            movieInfo.posterPath = movieObj.getString(TMDB_MOVIE_POSTER_PATH);
            movieInfo.releaseDate = movieObj.getString(TMDB_MOVIE_RELEASE_DATE);

            parsedMoviesData.add(movieInfo);
        }

        return parsedMoviesData;
    }

    public static List<TrailerInfo> getTrailerListFromJson(String jsonStr)
            throws JSONException {
        List<TrailerInfo> parsedTrailersData = new ArrayList<>();

        JSONObject trailersJson = new JSONObject(jsonStr);

        if (trailersJson.has(TMDB_STATUS_CODE)) {
            String message = trailersJson.getString(TMDB_STATUS_MESSAGE);
            Log.e(LOG_TAG, message);
            return null;
        }

        JSONArray trailersArray = trailersJson.getJSONArray(TMDB_TRAILERS_RESULT);

        for (int i = 0; i < trailersArray.length(); i++) {
            JSONObject trailerObj = trailersArray.getJSONObject(i);

            TrailerInfo trailerInfo = new TrailerInfo();
            trailerInfo.title = trailerObj.getString("name");
            trailerInfo.youtubeId = trailerObj.getString("source");

            parsedTrailersData.add(trailerInfo);
        }

        return parsedTrailersData;
    }

    public static List<ReviewInfo> getReviewListFromJson(String jsonStr)
            throws JSONException {
        List<ReviewInfo> parsedReviewsData = new ArrayList<>();

        JSONObject reviewsJson = new JSONObject(jsonStr);

        if (reviewsJson.has(TMDB_STATUS_CODE)) {
            String message = reviewsJson.getString(TMDB_STATUS_MESSAGE);
            Log.e(LOG_TAG, message);
            return null;
        }

        JSONArray reviewsArray = reviewsJson.getJSONArray(TMDB_REVIEWS_RESULT);

        for (int i = 0; i < reviewsArray.length(); i++) {
            JSONObject reviewObj = reviewsArray.getJSONObject(i);

            ReviewInfo reviewInfo = new ReviewInfo();
            reviewInfo.author = reviewObj.getString("author");
            reviewInfo.content = reviewObj.getString("content");

            parsedReviewsData.add(reviewInfo);
        }

        return parsedReviewsData;
    }
}

