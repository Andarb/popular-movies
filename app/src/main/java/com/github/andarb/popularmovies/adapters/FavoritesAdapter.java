package com.github.andarb.popularmovies.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.github.andarb.popularmovies.data.FavoritesContract;
import com.github.andarb.popularmovies.utils.BitmapIO;
import com.slackar.popularmovies.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/* Display posters of favorite movies.
 * Poster image file path retrieved from the local db.
 * Image retrieved from internal storage and bound to a grid of ImageViews
 */
public class FavoritesAdapter extends CursorAdapter {
    private LayoutInflater mLayoutInflater;

    public FavoritesAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);

        mLayoutInflater = LayoutInflater.from(context);
    }

    /* Inflate grid item layout, set up a ViewHolder for view caching, and return both to be used in bindView() */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = mLayoutInflater.inflate(R.layout.poster_grid_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /* Get the filename (which is the same as movie ID) for the poster image, retrieve it from disk,
     * and bind it to ImageView.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int movieIdColumnIndex = cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID);
        String posterFileName = cursor.getString(movieIdColumnIndex);

        Bitmap posterBitmap = BitmapIO.loadImage(context, posterFileName);

        // Retrieve cached views returned from `newView()`
        ViewHolder holder = (ViewHolder) view.getTag();

        holder.listItemPoster.setImageBitmap(posterBitmap);
    }

    /* Bind ImageView of the poster */
    public static class ViewHolder {
        @BindView(R.id.list_item_poster)
        ImageView listItemPoster;

        public ViewHolder(View itemView) {
            ButterKnife.bind(this, itemView);
        }
    }
}
