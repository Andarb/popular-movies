package com.github.andarb.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/* Database contract for favorite movies */
public final class FavoritesContract {

    // Content provider constants
    public static final String AUTHORITY = "com.github.andarb.popularmovies";
    private static final Uri BASE_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVORITES = "favorites";

    // Private constructor to prevent anyone from instantiating the class
    private FavoritesContract(){}

    public static class FavoritesEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        public static final String TABLE_NAME = "favorites";
        public static final String COLUMN_MOVIE_ID = "movieId";
        public static final String COLUMN_MOVIE_TITLE = "movieName";
    }
}
