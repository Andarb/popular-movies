package com.slackar.popularmovies.data;

import java.util.List;

/**
 * themoviedb JSON returns an array of 'results'.
 * 'results' contains objects with details for each movie
 **/
public class MoviesList {
    private List<MoviePoster> results = null;

    public List<MoviePoster> getResults() {
        return results;
    }

    public void setResults(List<MoviePoster> results) {
        this.results = results;
    }
}
