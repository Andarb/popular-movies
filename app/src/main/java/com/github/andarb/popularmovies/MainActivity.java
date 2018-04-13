package com.github.andarb.popularmovies;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.andarb.popularmovies.adapters.FavoritesAdapter;
import com.github.andarb.popularmovies.adapters.PosterAdapter;
import com.github.andarb.popularmovies.data.FavoritesContract;
import com.github.andarb.popularmovies.data.Poster;
import com.github.andarb.popularmovies.data.PosterList;
import com.github.andarb.popularmovies.utils.RetrofitClient;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MainActivity.class.getSimpleName();

    // Used to retrieve mMoviesType when restoring/saving instance state
    private static final String MOVIES_TYPE_STATE_KEY = "movies_type";

    private static final int FAVORITES_CURSORLOADER_ID = 1000;

    // IDs to decide which movies to retrieve
    public static final int MOST_POPULAR = 100;
    public static final int HIGHEST_RATED = 101;
    public static final int FAVORITES = 102;

    // Retrieve popular movies by default
    private int mMoviesType = MOST_POPULAR;

    // Adapter used to query display Favorite movies
    private FavoritesAdapter mFavoritesAdapter;

    // Keep track of the current Toast, in case we need to show another one, and cancel it
    private Toast mToast;

    @BindView(R.id.posters_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.posters_gv)
    GridView mGridView;
    @BindView(R.id.posters_loading_pb)
    ProgressBar mLoadingPB;

    // Error message
    @BindView(R.id.error_message_posters)
    ViewGroup mErrorMessageView;
    @BindView(R.id.error_tv)
    TextView mErrorTV;
    @BindView(R.id.retry_button)
    Button mRetryButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        // When there is a connection issue, retry retrieving movie posters
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

        // Initial setup of the Favorites adapter and grid view
        mFavoritesAdapter = new FavoritesAdapter(this, null, 0);
        mGridView.setAdapter(mFavoritesAdapter);

        // When a favorite movie is clicked, open it's details in MovieDetailsActvity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent movieDetailsIntent = new Intent(MainActivity.this,
                        MovieDetailsActivity.class);

                // Retrieve cursor from adapter, and get the clicked movie's ID
                Cursor adapterCursor = (Cursor) mFavoritesAdapter.getItem(position);
                int movieIdColumnIndex = adapterCursor.
                        getColumnIndex(FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID);
                String movieId = adapterCursor.getString(movieIdColumnIndex);

                movieDetailsIntent.putExtra(MovieDetailsActivity.MOVIE_ID_INTENT_KEY, movieId);
                startActivity(movieDetailsIntent);
            }
        });

        // Check if state was saved, and if `Favorites` were open at that time
        if (savedInstanceState != null) {
            mMoviesType = savedInstanceState.getInt(MOVIES_TYPE_STATE_KEY);

            if (mMoviesType == FAVORITES) {
                getSupportLoaderManager().restartLoader(FAVORITES_CURSORLOADER_ID, null, this);
                return;
            }
        }

        retrievePosters();
    }

    /* Save the type of movie list was displayed at the time: popular, highest rated or favorites */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(MOVIES_TYPE_STATE_KEY, mMoviesType);

        super.onSaveInstanceState(outState);
    }

    /* Reload the favorites list in case a movie was added/removed in MovieDetailsActivity */
    @Override
    protected void onRestart() {
        if (mMoviesType == FAVORITES) {
            getSupportLoaderManager().restartLoader(FAVORITES_CURSORLOADER_ID, null, this);
        }

        super.onRestart();
    }

    /* Cancel any toast before going into MovieDetailsActivity */
    @Override
    protected void onPause() {
        if (mToast != null) mToast.cancel();

        super.onPause();
    }

    /* Download and parse a list of movies using Retrofit */
    private void retrievePosters() {
        mLoadingPB.setVisibility(View.VISIBLE);
        Call<PosterList> getCall = RetrofitClient.getPosters(mMoviesType);

        getCall.enqueue(new Callback<PosterList>() {
            @Override
            public void onResponse(Call<PosterList> call, Response<PosterList> response) {
                if (response.isSuccessful()) {

                    List<Poster> moviesList = response.body().getResults();
                    if (moviesList == null) {
                        showErrorMessage(getString(R.string.error_empty_list));
                        return;
                    }

                    PosterAdapter movieAdapter = new PosterAdapter(MainActivity.this, moviesList);
                    mRecyclerView.setAdapter(movieAdapter);

                    // Loading complete - show appropriate `Toast`
                    hideErrorMessage();
                    mLoadingPB.setVisibility(View.GONE);
                    if (mMoviesType == MOST_POPULAR) {
                        showToast(R.string.toast_most_popular);
                    } else {
                        showToast(R.string.toast_highest_rated);
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

    /* Query ContentProvider on the list of favorite movies */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        mLoadingPB.setVisibility(View.VISIBLE);

        String[] projection = {FavoritesContract.FavoritesEntry._ID,
                FavoritesContract.FavoritesEntry.COLUMN_MOVIE_ID};

        return new android.support.v4.content.CursorLoader(this, FavoritesContract.FavoritesEntry.CONTENT_URI,
                projection, null, null, null);
    }

    /* If the list isn't empty, show a list of movies. Otherwise, warn the user there are no
     * favorites to show.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        try {
            if (data.getCount() != 0) {
                mFavoritesAdapter.swapCursor(data);
                mFavoritesAdapter.notifyDataSetChanged();

                mLoadingPB.setVisibility(View.GONE);
                hideErrorMessage();
            } else {
                showErrorMessage(getString(R.string.error_empty_favorites));
            }
        } catch (NullPointerException e) {
            showErrorMessage(getString(R.string.error_empty_favorites));
        }
    }

    /* Relieve adapter off old cursor that is about to be closed */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoritesAdapter.swapCursor(null);
    }

    // Cancels a previous toast if applicable, and shows a new one
    private void showToast(int stringResource) {
        if (mToast != null) mToast.cancel();

        mToast = Toast.makeText(this, getString(stringResource), Toast.LENGTH_SHORT);
        mToast.show();
    }


    /* Hides the error message and makes the posters visible again */
    private void hideErrorMessage() {
        mErrorMessageView.setVisibility(View.GONE);

        if (mMoviesType == FAVORITES) {
            mRecyclerView.setVisibility(View.GONE);
            mGridView.setVisibility(View.VISIBLE);
        } else {
            mGridView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /* Shows the error message, and hides whatever was visible at the time: recyclerview or gridview */
    private void showErrorMessage(String error) {
        mLoadingPB.setVisibility(View.GONE);

        // Favorites section does not need a retry button as its data is local and always available
        if (mMoviesType == FAVORITES) {
            mGridView.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.GONE);
        } else {
            mRecyclerView.setVisibility(View.GONE);
            mRetryButton.setVisibility(View.VISIBLE);
        }

        mErrorTV.setText(error);
        mErrorMessageView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.movies, menu);
        return true;
    }

    /* Retrieve posters by popularity/rating, or show favorites */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.most_popular:
                mMoviesType = MOST_POPULAR;
                retrievePosters();
                return true;
            case R.id.highest_rated:
                mMoviesType = HIGHEST_RATED;
                retrievePosters();
                return true;
            case R.id.favorites:
                mMoviesType = FAVORITES;
                getSupportLoaderManager().initLoader(0, null, this);
                showToast(R.string.toast_favorites);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
