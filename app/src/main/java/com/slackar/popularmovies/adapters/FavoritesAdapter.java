package com.slackar.popularmovies.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.slackar.popularmovies.MovieDetailsActivity;
import com.slackar.popularmovies.R;

import com.slackar.popularmovies.data.FavoritesContract;
import com.slackar.popularmovies.utils.FavoritesPoster;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoritesAdapter.FavoritesViewHolder> {

    private final Context mContext;
    private Cursor mCursor;
    private int mMovieIdColumnIndex;

    public FavoritesAdapter(Context context, Cursor cursor) {
        mContext = context;
        mCursor = cursor;
        mMovieIdColumnIndex = cursor.getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID);
    }

    class FavoritesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.list_item_poster)
        ImageView listItemPoster;

        /* Bind ImageView of the poster, and set an OnClickListener on the list item */
        public FavoritesViewHolder(View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        /* When one of the posters is clicked, start MovieDetailsActivity
        passing it the ID of the movie.
         */
        @Override
        public void onClick(View view) {
            Intent movieDetailsIntent = new Intent(mContext, MovieDetailsActivity.class);

            int position = getAdapterPosition();
            mCursor.moveToPosition(position);

            String movieId = mCursor.getString(mMovieIdColumnIndex);

            movieDetailsIntent.putExtra(MovieDetailsActivity.MOVIE_ID_INTENT_KEY, movieId);
            mContext.startActivity(movieDetailsIntent);
        }
    }

    /* Inflate list item and intialize with it a new viewholder */
    @Override
    public FavoritesAdapter.FavoritesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.poster_grid_item, parent, false);

        return new FavoritesAdapter.FavoritesViewHolder(view);
    }

    /* Load the movie poster from internal storage, and set it for the list item */
    @Override
    public void onBindViewHolder(FavoritesAdapter.FavoritesViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String posterFileName = mCursor.getString(mMovieIdColumnIndex);
        Bitmap poster = FavoritesPoster.loadImage(mContext, posterFileName);

        holder.listItemPoster.setImageBitmap(poster);
    }

    /* Number of movies retrieved from 'themoviedb' */
    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

}
