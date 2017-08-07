package io.github.flowfree.popularmovies.utilities;


import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    private static final String THEMOVIEDB_URL = "https://api.themoviedb.org/3/movie";
    private static final String THEMOVIEDB_POSTER_URL = "http://image.tmdb.org/t/p";
    private static String THEMOVIEDB_API_KEY = null;
    private static final String API_KEY_PARAM = "api_key";

    /**
     * Globally set the API key
     *
     * @param key The Movie DB API key
     */
    public static void setTheMovieDbApiKey(String key) {
        THEMOVIEDB_API_KEY = key;
    }

    public static URL buildMostPopularUrl() {
        final String[] paths = {"popular"};
        return buildTmdbUrl(paths);
    }

    public static URL buildTopRatedUrl() {
        final String[] paths = {"top_rated"};
        return buildTmdbUrl(paths);
    }

    public static URL buildMovieTrailersUrl(String movieId) {
        final String[] paths = {movieId, "trailers"};
        return buildTmdbUrl(paths);
    }

    public static URL buildMovieReviewsUrl(String movieId) {
        final String[] paths = {movieId, "reviews"};
        return buildTmdbUrl(paths);
    }

    private static URL buildTmdbUrl(String[] paths) {
        URL url = null;
        Uri.Builder uriBuilder = Uri.parse(THEMOVIEDB_URL).buildUpon();

        for (String path : paths) {
            uriBuilder.appendPath(path);
        }
        uriBuilder.appendQueryParameter(API_KEY_PARAM, THEMOVIEDB_API_KEY);

        try {
            url = new URL(uriBuilder.build().toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return url;
    }

    public static URL buildMoviePosterUrl(String path) {
        Uri builtUri = Uri.parse(THEMOVIEDB_POSTER_URL).buildUpon()
                .appendPath("w185")
                .appendEncodedPath(path)
                .build();
        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        return url;
    }

    /**
     * Method for fetching HTTP resource
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();
            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");
            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }
}

