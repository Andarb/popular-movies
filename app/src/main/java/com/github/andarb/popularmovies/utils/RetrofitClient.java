package com.github.andarb.popularmovies.utils;


import com.github.andarb.popularmovies.MainActivity;
import com.github.andarb.popularmovies.data.Movie;
import com.github.andarb.popularmovies.data.PosterList;
import com.slackar.popularmovies.BuildConfig;

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
    private static final String VIDEOS_AND_REVIEWS = "videos,reviews";

    // Paths used to retrieve most popular/highest rated movies, trailers and reviews
    private static final String MOST_POPULAR_PATH = "popular";
    private static final String TOP_RATED_PATH = "top_rated";

    /* Retrofit interface for retrieving movies */
    private interface MovieApi {
        @GET(MOST_POPULAR_PATH)
        Call<PosterList> getPopular(@Query(API_KEY_QUERY) String apiKey);

        @GET(TOP_RATED_PATH)
        Call<PosterList> getHighestRated(@Query(API_KEY_QUERY) String apiKey);

        @GET(MOVIE_ID_PATH_MASK)
        Call<Movie> getMovieDetails(@Path(MOVIE_ID_PATH) String movieId,
                                    @Query(API_KEY_QUERY) String apiKey,
                                    @Query(APPEND_QUERY) String videosAndReviews);
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

        return apiService.getMovieDetails(movieId, API_KEY, VIDEOS_AND_REVIEWS);
    }
}
