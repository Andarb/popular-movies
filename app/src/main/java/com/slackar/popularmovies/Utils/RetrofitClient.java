package com.slackar.popularmovies.Utils;


import com.slackar.popularmovies.BuildConfig;
import com.slackar.popularmovies.MainActivity;
import com.slackar.popularmovies.data.Movie;
import com.slackar.popularmovies.data.MoviesList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public final class RetrofitClient {
    // Information used to communicate with 'themoviedb' API
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String API_KEY_QUERY = "api_key";

    /** The way to hide the API key when uploading the project to github found in:
     * https://richardroseblog.wordpress.com/2016/05/29/hiding-secret-api-keys-from-git/
     */
    private static final String API_KEY = BuildConfig.API_KEY;
    private static final String MOST_POPULAR_PATH = "popular";
    private static final String TOP_RATED_PATH = "top_rated";
    private static final String MOVIE_ID_PATH_MASK = "{movie_id}";
    private static final String MOVIE_ID_PATH = "movie_id";

    /* Retrofit interface to get popular or highest rated movies */
    private interface SortedMovieApi {
        @GET(MOST_POPULAR_PATH)
        Call<MoviesList> getPopular(@Query(API_KEY_QUERY) String apiKey);

        @GET(TOP_RATED_PATH)
        Call<MoviesList> getHighestRated(@Query(API_KEY_QUERY) String apiKey);

        @GET(MOVIE_ID_PATH_MASK)
        Call<Movie> getMovieDetails(@Path(MOVIE_ID_PATH) String movieId, @Query(API_KEY_QUERY) String apiKey);
    }

    /* Set up retrofit and its service */
    private static SortedMovieApi setupRetrofit()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(SortedMovieApi.class);
    }

    /* Retrieve posters for either most popular or highest rated movies */
    public static Call<MoviesList> getPosters(int sortType) {
        SortedMovieApi apiService = setupRetrofit();

        switch (sortType) {
            case MainActivity.SORT_BY_POPULARITY:
                return apiService.getPopular(API_KEY);
            case MainActivity.SORT_BY_RATING:
                return apiService.getHighestRated(API_KEY);
            default:
                return apiService.getPopular(API_KEY);
        }
    }

    /* Retrieve details of a specific movie */
    public static Call<Movie> getMovieDetails(String movieId) {
        SortedMovieApi apiService = setupRetrofit();

        return apiService.getMovieDetails(movieId, API_KEY);
    }
}
