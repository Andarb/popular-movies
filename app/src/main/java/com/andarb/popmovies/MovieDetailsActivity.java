package com.andarb.popmovies;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andarb.popmovies.adapters.PosterAdapter;
import com.andarb.popmovies.adapters.ReviewAdapter;
import com.andarb.popmovies.adapters.VideoAdapter;
import com.andarb.popmovies.data.FavoritesContract;
import com.andarb.popmovies.data.Movie;
import com.andarb.popmovies.data.Review;
import com.andarb.popmovies.data.Video;
import com.andarb.popmovies.utils.BitmapIO;
import com.andarb.popmovies.utils.RetrofitClient;
import com.andarb.popmovies.utils.ReviewRecycler;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsActivity extends AppCompatActivity {
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    // Key for the movie ID value passed with intent from MainActivity
    public static final String MOVIE_ID_INTENT_KEY = "movie_id";

    // Backdrop url details
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String BACKDROP_SIZE = "w342";
    private static final String BACKDROP_BASE_URL = BASE_URL + BACKDROP_SIZE;

    // Loading indicator and floating action button
    @BindView(R.id.details_loading_pb)
    ProgressBar mDetailsPB;
    @BindView(R.id.favorite_button)
    FloatingActionButton mFavoriteButton;

    // Movie details and labels
    @BindView(R.id.movie_details_view)
    ViewGroup mMovieDetailsView;
    @BindView(R.id.backdrop_iv)
    ImageView mBackdropIV;
    @BindView(R.id.title_tv)
    TextView mTitleTV;
    @BindView(R.id.release_date_tv)
    TextView mReleaseDateTV;
    @BindView(R.id.vote_average_tv)
    TextView mVoteAverageTV;
    @BindView(R.id.overview_tv)
    TextView mOverviewTV;

    // RecyclerViews for trailers and reviews
    @BindView(R.id.trailers_rv)
    RecyclerView mTrailerRV;
    @BindView(R.id.reviews_rv)
    ReviewRecycler mReviewRV;

    // Connection error message
    @BindView(R.id.error_message_details)
    ViewGroup mErrorMessageView;
    @BindView(R.id.error_tv)
    TextView mErrorTV;
    @BindView(R.id.retry_button)
    Button mRetryButton;

    // Error messages for missing trailers/reviews
    @BindView(R.id.trailer_error_tv)
    TextView mTrailerErrorTV;
    @BindView(R.id.reviews_error_tv)
    TextView mReviewErrorTV;

    // Movie details object and a unique movie ID
    private Movie mMovie;
    private String mMovieId;

    // Adapters for movie videos and reviews
    private VideoAdapter mTrailerAdapter;
    private ReviewAdapter mReviewAdapter;

    // Used to add/remove movie to/from Favorites, and check if the movie is already in Favorites
    private ContentResolver mContentResolver;
    private boolean mIsFavorite;

    // Keep track of the current Toast, in case we need to show another one, and cancel it
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);
        ButterKnife.bind(this);

        mMovieId = getIntent().getStringExtra(MOVIE_ID_INTENT_KEY);

        // Check if the movie was favorited
        mContentResolver = getContentResolver();
        new CheckIfFavoriteTask().execute();

        // Set up button click listeners for `Retry` and `Favorite` buttons
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When there is a connection issue, retry retrieving movies
                retrieveMovieDetails();
            }
        });
        mFavoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amendFavorites();
            }
        });

        // Set up recyclerview and adapter for movie trailers
        mTrailerRV.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        mTrailerRV.setHasFixedSize(true);
        mTrailerAdapter = new VideoAdapter(this);

        // Set up recyclerview and adapter for reviews
        mReviewRV.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.HORIZONTAL, false));
        SnapHelper snapHelper = new PagerSnapHelper();
        snapHelper.attachToRecyclerView(mReviewRV);
        mReviewAdapter = new ReviewAdapter(this);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (NullPointerException e) {
            // Action bar is not crucial for the operation of this activity, so continue as is
            e.printStackTrace();
        }

        retrieveMovieDetails();
    }

    /* Download and parse movie details using Retrofit */
    private void retrieveMovieDetails() {
        mDetailsPB.setVisibility(View.VISIBLE);
        Call<Movie> getCall = RetrofitClient.getMovieDetails(mMovieId);

        getCall.enqueue(new Callback<Movie>() {
            @Override
            public void onResponse(Call<Movie> call,
                                   Response<Movie> response) {
                if (response.isSuccessful()) {
                    hideErrorMessage();

                    mMovie = response.body();
                    populateBasicDetails();
                    retrieveVideos();
                    retrieveReviews();

                    mDetailsPB.setVisibility(View.GONE);
                } else {
                    showErrorMessage(getString(R.string.error_server));
                    Log.w(TAG, getString(R.string.error_server_status) + response.code());
                }
            }

            @Override
            public void onFailure(Call<Movie> call, Throwable t) {
                showErrorMessage(getString(R.string.error_internet));
            }
        });
    }

    /* Set up video adapter if there any videos to show */
    private void retrieveVideos() {
        List<Video> videos = mMovie.getVideos().getResults();
        if (videos == null || videos.isEmpty()) {
            // If no trailers are present, show a warning message and return
            mTrailerErrorTV.setVisibility(View.VISIBLE);
            return;
        }

        mTrailerErrorTV.setVisibility(View.GONE);
        mTrailerAdapter.setVideos(videos);
        mTrailerRV.setAdapter(mTrailerAdapter);
    }

    /* Set up review adapter if there any reviews to show */
    private void retrieveReviews() {
        List<Review> reviews = mMovie.getReviews().getResults();
        if (reviews == null || reviews.isEmpty()) {
            // If no reviews are present, show a warning message and return
            mReviewErrorTV.setVisibility(View.VISIBLE);
            return;
        }

        mReviewErrorTV.setVisibility(View.GONE);
        mReviewAdapter.setReviews(reviews);
        mReviewRV.setAdapter(mReviewAdapter);
    }

    /* Set basic movie details */
    private void populateBasicDetails() {
        Picasso.with(this).load(BACKDROP_BASE_URL + mMovie.getBackdropPath())
                .error(R.drawable.ic_broken_backdrop_image_24dp)
                .into(mBackdropIV);

        mTitleTV.setText(mMovie.getTitle());

        // Check if the release date is missing
        String releaseDate = mMovie.getReleaseDate();
        if (releaseDate.isEmpty()) {
            mReleaseDateTV.setText(getString(R.string.error_missing_info));
        } else {
            mReleaseDateTV.setText(releaseDate);
        }

        // Check if there are no votes
        float voteAverage = mMovie.getVoteAverage();
        if (voteAverage == 0) {
            mVoteAverageTV.setText(getString(R.string.error_missing_info));
        } else {
            mVoteAverageTV.setText(String.valueOf(voteAverage));
        }

        mOverviewTV.setText(mMovie.getOverview());
    }

    /* Add or remove the movie from `Favorites` using content provider */
    private void amendFavorites() {
        if (mIsFavorite) {
            // Remove movie poster from internal storage
            if (!(BitmapIO.deleteImage(this, mMovieId))) {
                showToast(R.string.error_remove_favorite, Toast.LENGTH_LONG);
                return;
            }

            // Remove movie details from `Favorites` db
            String selection = FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ?";
            String[] selectionArgs = new String[]{mMovieId};

            int deletedRows = mContentResolver.delete(FavoritesContract.FavoritesEntry.CONTENT_URI,
                    selection,
                    selectionArgs);

            if (deletedRows < 1) {
                showToast(R.string.error_remove_favorite, Toast.LENGTH_LONG);
            } else {
                showToast(R.string.removed_from_favorites, Toast.LENGTH_SHORT);
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_border_white_24dp);
                mIsFavorite = false;
            }

        } else {
            // Save movie poster to internal memory
            String posterImageUrl = PosterAdapter.POSTER_BASE_URL + mMovie.getPosterPath();

            /* As we are already saving a unique movie ID to the database, we can reuse it as our
             * filename for the poster image. This saves us from creating an extra filename column
             * in the db table.
             */
            if (!(BitmapIO.saveImage(this, mMovieId, posterImageUrl))) {
                showToast(R.string.error_add_favorite, Toast.LENGTH_LONG);
                return;
            }

            // Add movie details to `Favorites` db
            ContentValues movieCV = new ContentValues();
            movieCV.put(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID, mMovieId);
            movieCV.put(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_TITLE, mMovie.getTitle());

            Uri insertedRowUri = mContentResolver.insert(FavoritesContract.FavoritesEntry.CONTENT_URI, movieCV);

            if (Uri.EMPTY.equals(insertedRowUri)) {
                showToast(R.string.error_add_favorite, Toast.LENGTH_LONG);
            } else {
                showToast(R.string.added_to_favorites, Toast.LENGTH_SHORT);
                mFavoriteButton.setImageResource(R.drawable.ic_favorite_white_24dp);
                mIsFavorite = true;
            }
        }
    }

    /* Check database for movie's ID. If it exists, change `Favorite` button icon to indicate that */
    private class CheckIfFavoriteTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {
            String[] projection = {FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID};
            String selection = FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID + " = ?";
            String[] selectionArgs = new String[]{mMovieId};

            Cursor cursor = mContentResolver.query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                    projection,
                    selection,
                    selectionArgs,
                    null);

            try {
                if (cursor.getCount() == 0) {
                    mIsFavorite = false;
                } else {
                    mIsFavorite = true;
                }
                cursor.close();
            } catch (NullPointerException e) {
                mIsFavorite = false;
            }

            return mIsFavorite;
        }

        @Override
        protected void onPostExecute(Boolean isFavorite) {
            // Set `Favorite` icon from a heart outline to filled heart, indicating a favorited movie
            if (isFavorite) mFavoriteButton.setImageResource(R.drawable.ic_favorite_white_24dp);

            mFavoriteButton.setVisibility(View.VISIBLE);
        }
    }

    /* Cancel any toast before going back to MainActivity */
    @Override
    protected void onPause() {
        if (mToast != null) mToast.cancel();

        super.onPause();
    }

    // Cancels a previous toast if applicable, and shows a new one
    private void showToast(int stringResource, int length) {
        if (mToast != null) mToast.cancel();

        mToast = Toast.makeText(this, getString(stringResource), length);
        mToast.show();
    }

    /* Hides the error message, and makes the movie details visible again */
    private void hideErrorMessage() {
        mErrorMessageView.setVisibility(View.GONE);
        mMovieDetailsView.setVisibility(View.VISIBLE);
    }

    /* Shows the error message, and hides everything else */
    private void showErrorMessage(String error) {
        mDetailsPB.setVisibility(View.GONE);
        mMovieDetailsView.setVisibility(View.GONE);
        mErrorTV.setText(error);
        mErrorMessageView.setVisibility(View.VISIBLE);
    }
}
