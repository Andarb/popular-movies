package com.slackar.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.slackar.popularmovies.Utils.RetrofitClient;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MovieDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    // Key for the movie ID value passed with intent from MainActivity
    public static final String MOVIE_ID_INTENT_KEY = "movie_id";

    // Backdrop url details
    private static final String BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String BACKDROP_SIZE = "w342";
    private static final String BACKDROP_URL = BASE_URL + BACKDROP_SIZE;

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

    // Loading indicator
    @BindView(R.id.details_loading_pb)
    ProgressBar mLoadingPB;

    // Error message
    @BindView(R.id.error_message_details)
    ViewGroup mErrorMessageView;
    @BindView(R.id.error_tv)
    TextView mErrorTV;
    @BindView(R.id.retry_button)
    Button mRetryButton;

    private com.slackar.popularmovies.data.Movie mMovie;
    private String mMovieId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        ButterKnife.bind(this);
        mRetryButton.setOnClickListener(this);

        mMovieId = getIntent().getStringExtra(MOVIE_ID_INTENT_KEY);
        retrieveMovieDetails();
    }

    /* Download and parse movie details using Retrofit */
    private void retrieveMovieDetails() {
        mLoadingPB.setVisibility(View.VISIBLE);
        Call<com.slackar.popularmovies.data.Movie> getCall = RetrofitClient.getMovieDetails(mMovieId);

        getCall.enqueue(new Callback<com.slackar.popularmovies.data.Movie>() {
            @Override
            public void onResponse(Call<com.slackar.popularmovies.data.Movie> call, Response<com.slackar.popularmovies.data.Movie> response) {
                if (response.isSuccessful()) {
                    hideErrorMessage();
                    mMovie = response.body();

                    populateUI();
                    mLoadingPB.setVisibility(View.GONE);
                } else {
                    showErrorMessage(getString(R.string.error_server));
                    Log.w(TAG, getString(R.string.error_server_status) + response.code());
                }
            }

            @Override
            public void onFailure(Call<com.slackar.popularmovies.data.Movie> call, Throwable t) {
                showErrorMessage(getString(R.string.error_internet));
            }
        });

    }

    /* Set all the movie details */
    private void populateUI() {
        Picasso.with(this).load(BACKDROP_URL + mMovie.getBackdropPath()).into(mBackdropIV);
        mTitleTV.setText(mMovie.getTitle());
        mReleaseDateTV.setText(mMovie.getReleaseDate());
        mVoteAverageTV.setText(String.valueOf(mMovie.getVoteAverage()));
        mOverviewTV.setText(mMovie.getOverview());
    }

    /* Hides the error message and makes the movie details visible again */
    private void hideErrorMessage() {
        mErrorMessageView.setVisibility(View.GONE);
        mMovieDetailsView.setVisibility(View.VISIBLE);
    }

    /* Shows the error message and hides everything else */
    private void showErrorMessage(String error) {
        mLoadingPB.setVisibility(View.GONE);
        mMovieDetailsView.setVisibility(View.GONE);
        mErrorTV.setText(error);
        mErrorMessageView.setVisibility(View.VISIBLE);
    }

    /* Try to retrieve movie details again, when the 'Retry' button is clicked in the error message */
    @Override
    public void onClick(View v) {
        retrieveMovieDetails();
    }
}
