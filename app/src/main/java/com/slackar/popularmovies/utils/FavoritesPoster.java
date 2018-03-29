package com.slackar.popularmovies.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


/* Helper class to download the poster image of a favorited movie from `themoviedb`. Or to
save/load/delete that image to/from internal storage. */
public final class FavoritesPoster {

    private static final String TAG = FavoritesPoster.class.getSimpleName();

    // Picasso stores a weak reference of Target object which we don't want in our case
    private static Target mTarget;

    // Keeps track if there were errors saving poster image to storage
    private static boolean mSaveSuccessful;


    /* Loads an earlier downloaded poster from internal storage */
    public static Bitmap loadImage(Context context, String posterFile) {
        Bitmap posterBitmap = null;

        try {
            FileInputStream inputStream = context.openFileInput(posterFile);

            posterBitmap = BitmapFactory.decodeStream(inputStream);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "Poster image not found: " + posterFile);
        }

        return posterBitmap;
    }

    /* Saves a downloaded poster to internal storage */
    public static boolean saveImage(final Context context, final String filename, String posterPath) {
        mSaveSuccessful = true;

        // Override Picasso's target to save the image to disk instead of binding to ImageView
        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                FileOutputStream outputStream = null;

                try {
                    outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);

                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                } catch (FileNotFoundException e) {
                    mSaveSuccessful = false;
                    Log.w(TAG, "Unable to save poster image: " + filename);
                }

                try {
                    if (outputStream != null)
                        outputStream.close();
                } catch (IOException e) {
                    Log.w(TAG, "Unable to close output stream for image: " + filename);
                }
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                mSaveSuccessful = false;
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        // Download and save the image
        Picasso.with(context)
                .load("http://image.tmdb.org/t/p/w185/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg")
                .into(mTarget);

        return mSaveSuccessful;
    }

    /* Deletes an earlier downloaded poster from internal storage */
    public static boolean deleteImage(Context context, String posterFile)
    {
        return context.deleteFile(posterFile);
    }


}
