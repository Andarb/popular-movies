package com.github.andarb.popularmovies;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.andarb.popularmovies.adapters.FavoritesAdapter;
import com.github.andarb.popularmovies.adapters.PosterAdapter;
import com.github.andarb.popularmovies.data.FavoritesContract;
import com.github.andarb.popularmovies.data.Poster;
import com.github.andarb.popularmovies.data.PosterList;
import com.github.andarb.popularmovies.utils.RetrofitClient;
import com.slackar.popularmovies.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    // IDs used to decide which sort order is required
    public static final int SORT_BY_POPULARITY = 100;
    public static final int SORT_BY_RATING = 101;

    @BindView(R.id.posters_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.posters_loading_pb)
    ProgressBar mLoadingPB;

    // Error message
    @BindView(R.id.error_message_posters)
    ViewGroup mErrorMessageView;
    @BindView(R.id.error_tv)
    TextView mErrorTV;
    @BindView(R.id.retry_button)
    Button mRetryButton;

    private PosterAdapter mMovieAdapter;
    private List<Poster> mMoviesList;

    // Initiliazed to the default way of sorting movies
    private int mSortType = SORT_BY_POPULARITY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // When there is a connection issue, retry retrieving posters
        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                retrievePosters();
            }
        });

        // Set recyclerview to have grid layout with 2 columns
        int nrOfGridColumns = 2;
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, nrOfGridColumns));
        mRecyclerView.setHasFixedSize(true);

        mMovieAdapter = new PosterAdapter(this);

        retrievePosters();
    }

    /* Download and parse a list of movies using Retrofit */
    private void retrievePosters() {
        mLoadingPB.setVisibility(View.VISIBLE);
        Call<PosterList> getCall = RetrofitClient.getPosters(mSortType);

        getCall.enqueue(new Callback<PosterList>() {
            @Override
            public void onResponse(Call<PosterList> call, Response<PosterList> response) {
                if (response.isSuccessful()) {
                    hideErrorMessage();

                    mMoviesList = response.body().getResults();
                    if (mMoviesList == null) {
                        showErrorMessage(getString(R.string.error_empty_list));
                        return;
                    }
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
                    showErrorMessage(getString(R.string.error_server));
                    Log.w(TAG, getString(R.string.error_server_status) + response.code());
                }
            }

            @Override
            public void onFailure(Call<PosterList> call, Throwable t) {
                showErrorMessage(getString(R.string.error_internet));
            }
        });
    }

    /* Retrieve a list of favorite movies from local db */
    private void retrieveFavorites() {
        hideErrorMessage();
        mLoadingPB.setVisibility(View.VISIBLE);

        String[] projection = {FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID,
                FavoritesContract.FavoritesEntry.COLUMN_MOVIE_POSTER_PATH};

        Cursor cursor = getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);

        // Check if the returned cursor is empty, meaning there are no favorited movies
        try {
            if (cursor.getCount() != 0) {
                FavoritesAdapter favoritesAdapter = new FavoritesAdapter(this, cursor);
                mRecyclerView.setAdapter(favoritesAdapter);
            } else {
                showErrorMessage(getString(R.string.error_empty_favorites));
            }
        } catch (NullPointerException e) {
            showErrorMessage(getString(R.string.error_empty_favorites));
        }


        mLoadingPB.setVisibility(View.GONE);
    }

    /* Hides the error message and makes the posters visible again */
    private void hideErrorMessage() {
        mErrorMessageView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /* Shows the error message and hides everything else */
    private void showErrorMessage(String error) {
        mLoadingPB.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.GONE);
        mErrorTV.setText(error);

        // Favorites section does not need a retry button as its data is local and always available
        if (error.equals(getString(R.string.error_empty_favorites))) {
            mRetryButton.setVisibility(View.GONE);
        } else {
            mRetryButton.setVisibility(View.VISIBLE);
        }
        mErrorMessageView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movies, menu);
        return true;
    }

    /* Retrieve posters by popularity/rating or show favorites */
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
            case R.id.favorites:
                retrieveFavorites();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
