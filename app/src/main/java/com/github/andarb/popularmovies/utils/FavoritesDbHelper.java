package com.github.andarb.popularmovies.utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.github.andarb.popularmovies.data.FavoritesContract;

/* Helper class to manage the database of favorite movies */
public class FavoritesDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "favorites.db";

    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + FavoritesContract.FavoritesEntry.TABLE_NAME + " (" +
                        FavoritesContract.FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                        FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL," +
                        FavoritesContract.FavoritesEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL)";

        db.execSQL(SQL_CREATE_ENTRIES);
    }

    /**
     * As the database is at version 1, this method drops the table without preserving
     * any user data. This behaviour should be modified  on the next database upgrade.
     */
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + FavoritesContract.FavoritesEntry.TABLE_NAME;

        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
}
