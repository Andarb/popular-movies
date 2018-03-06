package com.slackar.popularmovies.Utils;


import android.util.Log;

import com.slackar.popularmovies.BuildConfig;
import com.slackar.popularmovies.MainActivity;
import com.slackar.popularmovies.data.Movie;
import com.slackar.popularmovies.data.PosterList;
import com.slackar.popularmovies.data.Review;
import com.slackar.popularmovies.data.ReviewList;
import com.slackar.popularmovies.data.Trailer;
import com.slackar.popularmovies.data.TrailerList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public final class RetrofitClient {
    /** Information used to communicate with 'themoviedb' API.
     * `Richard Rose` blog consulted on hiding the API key.
     */
    private static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    private static final String API_KEY_QUERY = "api_key";
    private static final String API_KEY = BuildConfig.API_KEY;

    // ID paths that help retrieve a specific movie
    private static final String MOVIE_ID_PATH_MASK = "{movie_id}";
    private static final String MOVIE_ID_PATH = "movie_id";

    // Paths used to retrieve most popular/highest rated movies, trailers and reviews
    private static final String MOST_POPULAR_PATH = "popular";
    private static final String TOP_RATED_PATH = "top_rated";
    private static final String TRAILERS_PATH = MOVIE_ID_PATH_MASK + "/videos";
    private static final String REVIEWS_PATH = MOVIE_ID_PATH_MASK + "/reviews";

    /* Retrofit interface for retrieving movies */
    private interface MovieApi {
        @GET(MOST_POPULAR_PATH)
        Call<PosterList> getPopular(@Query(API_KEY_QUERY) String apiKey);

        @GET(TOP_RATED_PATH)
        Call<PosterList> getHighestRated(@Query(API_KEY_QUERY) String apiKey);

        @GET(MOVIE_ID_PATH_MASK)
        Call<Movie> getMovieDetails(@Path(MOVIE_ID_PATH) String movieId, @Query(API_KEY_QUERY) String apiKey);

        @GET(TRAILERS_PATH)
        Call<TrailerList> getTrailers(@Path(MOVIE_ID_PATH) String movieId, @Query(API_KEY_QUERY) String apiKey);

        @GET(REVIEWS_PATH)
        Call<ReviewList> getReviews(@Path(MOVIE_ID_PATH) String movieId, @Query(API_KEY_QUERY) String apiKey);
    }

    /* Set up retrofit and its service */
    private static MovieApi setupRetrofit()
    {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(MovieApi.class);
    }

    /* Retrieve posters for either most popular or highest rated movies */
    public static Call<PosterList> getPosters(int sortType) {
        MovieApi apiService = setupRetrofit();

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
        MovieApi apiService = setupRetrofit();

        return apiService.getMovieDetails(movieId, API_KEY);
    }

    /* Retrieve trailers of a specific movie */
    public static Call<TrailerList> getTrailers(String movieId) {
        MovieApi apiService = setupRetrofit();

        return apiService.getTrailers(movieId, API_KEY);
    }

    /* Retrieve reviews of a specific movie */
    public static Call<ReviewList> getReviews(String movieId) {
        MovieApi apiService = setupRetrofit();

        return apiService.getReviews(movieId, API_KEY);
    }
}
