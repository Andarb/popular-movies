package com.slackar.popularmovies.data;

import java.util.List;

/**
 * themoviedb JSON returns an array of 'results'.
 * 'results' contains movie reviews for a specific movie (based on movie ID)
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
