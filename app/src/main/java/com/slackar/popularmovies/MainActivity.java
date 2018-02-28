package com.slackar.popularmovies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.slackar.popularmovies.Utils.RetrofitClient;
import com.slackar.popularmovies.data.Movie;
import com.slackar.popularmovies.data.MoviePoster;
import com.slackar.popularmovies.data.MoviesList;

import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    // IDs used to decide which sort order is required
    public static final int SORT_BY_POPULARITY = 101;
    public static final int SORT_BY_RATING = 102;

    @BindView(R.id.posters_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.posters_loading_pb)
    ProgressBar mLoadingPB;

    // Error message
    @BindView(R.id.error_message_posters)
    LinearLayout mErrorMessageView;
    @BindView(R.id.error_tv)
    TextView mErrorTV;
    @BindView(R.id.retry_button)
    Button mRetryButton;

    private MoviePosterAdapter mMovieAdapter;
    private List<MoviePoster> mMoviesList;

    // Initiliazed to the default way of sorting movies
    private int mSortType = SORT_BY_POPULARITY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        mRetryButton.setOnClickListener(this);

        // Set recyclerview to have grid layout with 2 columns
        int nrOfGridColumns = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, nrOfGridColumns));
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new MoviePosterAdapter(this);

        retrievePosters();
    }

    /* Download and parse a list of movies using Retrofit */
    public void retrievePosters() {
        hideErrorMessage();
        Call<MoviesList> getCall = RetrofitClient.getPosters(mSortType);

        getCall.enqueue(new Callback<MoviesList>() {
            @Override
            public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {
                if (response.isSuccessful()) {
                    mMoviesList = response.body().getResults();
                    mMovieAdapter.setMovies(mMoviesList);
                    mRecyclerView.setAdapter(mMovieAdapter);

                    mLoadingPB.setVisibility(View.GONE);
                    if (mSortType == SORT_BY_POPULARITY) {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.toast_most_popular), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this,
                                getString(R.string.toast_highest_rated), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    showErrorMessage(getString(R.string.error_internet));
                    Log.w(TAG, getString(R.string.error_server_status) + response.code());
                }
            }

            @Override
            public void onFailure(Call<MoviesList> call, Throwable t) {
                showErrorMessage(getString(R.string.error_internet));
            }
        });
    }

    /* Hides the error message and makes the posters visible again */
    private void hideErrorMessage() {
        mErrorMessageView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mLoadingPB.setVisibility(View.VISIBLE);
    }

    /* Shows the error message and hides everything else */
    private void showErrorMessage(String error) {
        mLoadingPB.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mErrorTV.setText(error);
        mErrorMessageView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movies, menu);
        return true;
    }

    /* Retrieve posters by popularity or rating */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.most_popular:
                mSortType = SORT_BY_POPULARITY;
                retrievePosters();
                return true;
            case R.id.highest_rated:
                mSortType = SORT_BY_RATING;
                retrievePosters();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* Try to retrieve posters again, when the 'Retry' button is clicked in the error message */
    @Override
    public void onClick(View v) {
        retrievePosters();
    }
}
