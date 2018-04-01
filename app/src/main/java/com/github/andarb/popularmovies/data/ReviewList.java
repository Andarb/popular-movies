package com.github.andarb.popularmovies.data;

import java.util.List;

/**
 * themoviedb JSON returns an array of 'results'.
 * 'results' contains review objects
 **/
public class ReviewList {

    private List<Review> results = null;

    public List<Review> getResults() {
        return results;
    }

    public void setResults(List<Review> results) {
        this.results = results;
    }
}
