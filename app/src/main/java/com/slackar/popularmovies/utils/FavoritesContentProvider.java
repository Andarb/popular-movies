package com.slackar.popularmovies.utils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentFilter;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.slackar.popularmovies.data.FavoritesContract;

import static com.slackar.popularmovies.data.FavoritesContract.FavoritesEntry.TABLE_NAME;

/* Content provider for the user's favorite movies */
public class FavoritesContentProvider extends ContentProvider {
    // Initialized in onCreate()
    private FavoritesDbHelper mDbHelper;

    // Help matching URIs
    public static final int URI_FAVORITES = 200;
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Create a URI matcher
    public static UriMatcher buildUriMatcher() {
        // NO_MATCH creates an empty URI matcher
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(FavoritesContract.AUTHORITY, FavoritesContract.PATH_FAVORITES,
                URI_FAVORITES);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new FavoritesDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        final SQLiteDatabase db = mDbHelper.getReadableDatabase();
        Context context = getContext();

        // ID of the URI that was matched
        int uriId = sUriMatcher.match(uri);

        // Returned query results from db
        Cursor returnedCursor;

        switch (uriId) {
            case URI_FAVORITES:
                returnedCursor = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("No match for URI: " + uri);
        }

        if (context != null) {
            returnedCursor.setNotificationUri(context.getContentResolver(), uri);
        } else {
            throw new NullPointerException("Could not get context for content resolver: " + uri);
        }

        return returnedCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Context context = getContext();

        // ID of the URI that was matched
        int uriId = sUriMatcher.match(uri);

        // Returned value from inserting data into db
        Uri returnedUri;

        switch (uriId) {
            case URI_FAVORITES:
                long insertedRowId = db.insert(TABLE_NAME, null, values);

                // Check if there was an error inserting
                if (insertedRowId != -1) {
                    returnedUri = ContentUris.withAppendedId(
                            FavoritesContract.FavoritesEntry.CONTENT_URI, insertedRowId);
                } else {
                    throw new android.database.SQLException("Insert failed: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("No match for URI: " + uri);
        }


        if (context != null) {
            context.getContentResolver().notifyChange(uri, null);
        } else {
            throw new NullPointerException("Could not get context for content resolver: " + uri);
        }

        return returnedUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mDbHelper.getWritableDatabase();
        Context context = getContext();

        // ID of the URI that was matched
        int uriId = sUriMatcher.match(uri);

        // Number of rows deleted from db
        int nrDeletedRows;

        switch (uriId) {
            case URI_FAVORITES:
                nrDeletedRows = db.delete(TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("No match for URI: " + uri);
        }

        if (context != null) {
            if (nrDeletedRows > 0)
                context.getContentResolver().notifyChange(uri, null);
        } else {
            throw new NullPointerException("Could not get context for content resolver: " + uri);
        }

        return nrDeletedRows;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        return 0;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        int uriId = sUriMatcher.match(uri);

        switch (uriId) {
            case URI_FAVORITES:
                return "vnd.android.cursor.dir" + "/" + FavoritesContract.AUTHORITY + "/" + FavoritesContract.PATH_FAVORITES;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

    }
}
