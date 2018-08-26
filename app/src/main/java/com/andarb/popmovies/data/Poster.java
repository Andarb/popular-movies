package com.andarb.popmovies.data;

import com.google.gson.annotations.SerializedName;

/* Movie poster and accompanying ID retrieved from themoviedb JSON */
public class Poster {

    @SerializedName("poster_path")
    private String posterPath;

    private int id;

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}