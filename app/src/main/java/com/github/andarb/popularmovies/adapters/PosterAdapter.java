package com.github.andarb.popularmovies.adapters;


import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.github.andarb.popularmovies.MovieDetailsActivity;
import com.github.andarb.popularmovies.data.Poster;
import com.github.andarb.popularmovies.utils.BitmapIO;
import com.slackar.popularmovies.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/* Movie posters downloaded from `themoviedb` and bound to Imageviews
 * in a grid recyclerview
 */
public class PosterAdapter extends RecyclerView.Adapter<PosterAdapter.PosterViewHolder> {

    private final Context mContext;
    private List<Poster> mMovies;

    /* Poster url details. If POSTER_SIZE is modified, BitmapIO.BITMAP_WIDTH and
     * BitmapIO.BITMAP_HEIGHT should be changed appropriately.
     */
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String POSTER_SIZE = "w185";
    public static final String POSTER_BASE_URL = BASE_URL + POSTER_SIZE;

    public PosterAdapter(Context context, List<Poster> movies) {
        mContext = context;
        mMovies = movies;
    }

    class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.list_item_poster)
        ImageView listItemPoster;

        /* Bind ImageView of the poster, and set an OnClickListener on the list item */
        public PosterViewHolder(View itemView) {
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
            String movieId = String.valueOf(mMovies.get(position).getId());

            movieDetailsIntent.putExtra(MovieDetailsActivity.MOVIE_ID_INTENT_KEY, movieId);
            mContext.startActivity(movieDetailsIntent);
        }
    }

    /* Inflate list item and intialize with it a new viewholder */
    @Override
    public PosterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.poster_grid_item, parent, false);

        return new PosterViewHolder(view);
    }

    /* Download the movie poster and set it for the list item */
    @Override
    public void onBindViewHolder(PosterViewHolder holder, int position) {
        String posterFileName = mMovies.get(position).getPosterPath();

        Picasso.with(mContext).load(POSTER_BASE_URL + posterFileName)
                .error(R.drawable.ic_broken_poster_image_24dp)
                .into(holder.listItemPoster);
    }

    /* Number of movies retrieved from 'themoviedb' */
    @Override
    public int getItemCount() {
        return mMovies.size();
    }
}
