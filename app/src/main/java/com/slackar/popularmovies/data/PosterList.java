package com.slackar.popularmovies.data;

import java.util.List;

/**
 * themoviedb JSON returns an array of 'results'.
 * 'results' contains objects with details for each movie including poster URL
 **/
public class PosterList {

    private List<Poster> results = null;

    public List<Poster> getResults() {
        return results;
    }

    public void setResults(List<Poster> results) {
        this.results = results;
    }
}
