package io.github.flowfree.popularmovies.data;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MovieInfo implements Serializable {
    public String _id;
    public String title;
    public String posterPath;
    public String synopsis;
    public String userRating;
    public String releaseDate;

    public List<TrailerInfo> trailers;
    public List<ReviewInfo> reviews;

    public MovieInfo() {
        trailers = new ArrayList<>();
        reviews = new ArrayList<>();
    }

}

