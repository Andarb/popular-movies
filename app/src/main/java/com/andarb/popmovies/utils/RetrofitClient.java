package com.andarb.popmovies.utils;


import com.andarb.popmovies.BuildConfig;
import com.andarb.popmovies.MainActivity;
import com.andarb.popmovies.data.Movie;
import com.andarb.popmovies.data.PosterList;

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

    // ID paths that help retrieve details for a specific movie
    private static final String MOVIE_ID_PATH_MASK = "{movie_id}";
    private static final String MOVIE_ID_PATH = "movie_id";

    // Append request for videos and reviews to the movie details query
    private static final String APPEND_QUERY = "append_to_response";
    private static final String APPEND_QUERY_VALUE = "videos,reviews";

    // Paths used to retrieve most popular/highest rated movies, trailers and reviews
    private static final String MOST_POPULAR_PATH = "popular";
    private static final String HIGHEST_RATED_PATH = "top_rated";

    /* Retrofit interface for retrieving movies */
    private interface MovieApi {
        @GET(MOST_POPULAR_PATH)
        Call<PosterList> getMostPopular(@Query(API_KEY_QUERY) String apiKey);

        @GET(HIGHEST_RATED_PATH)
        Call<PosterList> getHighestRated(@Query(API_KEY_QUERY) String apiKey);

        @GET(MOVIE_ID_PATH_MASK)
        Call<Movie> getMovieDetails(@Path(MOVIE_ID_PATH) String movieId,
                                    @Query(API_KEY_QUERY) String apiKey,
                                    @Query(APPEND_QUERY) String appendValue);
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
            case MainActivity.MOST_POPULAR:
                return apiService.getMostPopular(API_KEY);
            case MainActivity.HIGHEST_RATED:
                return apiService.getHighestRated(API_KEY);
            default:
                return apiService.getMostPopular(API_KEY);
        }
    }

    /* Retrieve details of a specific movie */
    public static Call<Movie> getMovieDetails(String movieId) {
        MovieApi apiService = setupRetrofit();

        return apiService.getMovieDetails(movieId, API_KEY, APPEND_QUERY_VALUE);
    }
}
