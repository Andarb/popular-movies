package com.slackar.popularmovies.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * themoviedb JSON returns an array of 'results'.
 * 'results' contains movie trailers for a specific movie (based on movie ID)
 **/
public class TrailerList {

    private List<Trailer> results = null;

    public List<Trailer> getResults() {
        return results;
    }

    public void setResults(List<Trailer> results) {
        this.results = results;
    }
}
