package com.udacity.popmovies.activities;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.Toast;

import com.udacity.popmovies.BuildConfig;
import com.udacity.popmovies.R;
import com.udacity.popmovies.adapters.FavoriteMovieAdapter;
import com.udacity.popmovies.adapters.MovieAdapter;
import com.udacity.popmovies.adapters.OnItemClickListener;
import com.udacity.popmovies.constants.DbBitmapUtility;
import com.udacity.popmovies.database.MovieContract;
import com.udacity.popmovies.models.Movie;
import com.udacity.popmovies.models.ResponseFavoriteMovie;
import com.udacity.popmovies.models.ResponseMovie;
import com.udacity.popmovies.networking.RetrofitApiEndPoints;
import com.udacity.popmovies.networking.RetrofitClient;
import com.udacity.popmovies.stetho.MyApplication;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.udacity.popmovies.database.MovieContract.MovieEntry.MOVIE_IMAGE;

/*
* This Class will be used as launcher activity showing the movies and will be sorted according to
* the popular movies and top rated movies
* */

public class MovieActivity extends AppCompatActivity {

    public static final String TAG = MovieActivity.class.getSimpleName();


    private RecyclerView mRecyclerView;

    MovieAdapter mAdapter;

    long currentVisiblePosition = 0;

    private int id = 0;

    FavoriteMovieAdapter mFavoriteMovieAdapter;

    RecyclerView.LayoutManager mLayoutManager;






    List<Movie> movie;

    List<String> favoriteMoviesListFromTheContentProvider;

    List<ResponseFavoriteMovie> responseFavoriteMovieList;






    private List<String> updateFavoritedMoviesPath = null;



    List<String> favoriteMovieList;

    List<Bitmap> favoriteToggleStatuses;

    OnItemClickListener clickListener;

    Cursor mCursor;
    GridLayoutManager layoutManager;

    int positionIndex;

    int topView;

    private ScrollView mScrollView;

    ResponseFavoriteMovie responseFavoriteMovie;



    private static final String KEY_RECYCLER_STATE = "recycler_state";
    private static Bundle mBundleRecyclerViewState;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mRecyclerView = findViewById(R.id.rv_movies);
        movie = new ArrayList<>();
        favoriteMovieList = new ArrayList<>();
        favoriteMoviesListFromTheContentProvider = new ArrayList<>();

        updateFavoritedMoviesPath = new ArrayList<>();
        responseFavoriteMovieList = new ArrayList<>();
        favoriteToggleStatuses = new ArrayList<>();
        mScrollView = findViewById(R.id.scrollView_movie_details);
        layoutManager = new GridLayoutManager(MovieActivity.this, numberOfColumns());
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new MovieAdapter(MyApplication.getAppContext(), movie, clickListener);
        mFavoriteMovieAdapter = new FavoriteMovieAdapter(MovieActivity.this, favoriteMovieList, favoriteToggleStatuses);//favoriteMoviesListFromTheContentProvider
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setAdapter(mFavoriteMovieAdapter);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            id = savedInstanceState.getInt("last_category_selected");
        }
        final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
        if (position != null)
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(position[0], position[1]);
                }
            });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_movie, menu);
        return true;
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("last_category_selected", id);
        outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{mScrollView.getScrollX(), mScrollView.getScrollY()});
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        id = item.getItemId();
        return populateMovies();
    }

    private boolean populateMovies() {

        if (id == 0)
            id = R.id.action_most_popular;

        switch (id) {
            case R.id.action_most_popular: {
                Log.d(TAG, "onOptionsItemSelected:Most_popularMovies" + id);
                Toast.makeText(this, getString(R.string.toast_pop_movies_clicked) + id, Toast.LENGTH_SHORT).show();
            /*Make an retrofit API networking operations here*/

                RetrofitApiEndPoints apiEndPoints = RetrofitClient.getClient().create(RetrofitApiEndPoints.class);

                Call<ResponseMovie> call = apiEndPoints.getTopPopularMovies(BuildConfig.API_KEY);
                call.enqueue(new Callback<ResponseMovie>() {
                    @Override
                    public void onResponse(Call<ResponseMovie> call, Response<ResponseMovie> response) {

                        if (response != null) {

                            Log.d(TAG, "onResponse: " + response.body().getResults());
                            int statusCode = response.code();
                            Log.d(TAG, "onResponse: " + statusCode);
                            movie = response.body().getResults();

                            mRecyclerView.setLayoutManager(layoutManager);
                            mAdapter = new MovieAdapter(MyApplication.getAppContext(), movie, clickListener);

                            mRecyclerView.setAdapter(mAdapter);
                            mAdapter.notifyDataSetChanged();

                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseMovie> call, Throwable t) {

                        Toast.makeText(MovieActivity.this, "Failure to load the data", Toast.LENGTH_SHORT).show();
                    }
                });


                return true;
            }
            case R.id.action_top_rated: {

                Log.d(TAG, "onOptionsItemSelected:topRated" + id);

                Toast.makeText(this, getString(R.string.toast_top_rated_movies_clicked) + id, Toast.LENGTH_SHORT).show();
                RetrofitApiEndPoints apiEndPoints = RetrofitClient.getClient().create(RetrofitApiEndPoints.class);

                Call<ResponseMovie> call = apiEndPoints.getTopRatedMovies(BuildConfig.API_KEY);
                call.enqueue(new Callback<ResponseMovie>() {
                    @Override
                    public void onResponse(Call<ResponseMovie> call, Response<ResponseMovie> response) {

                        if (response != null) {

                            Log.d(TAG, "onResponse: " + response.body().getResults());
                            int statusCode = response.code();
                            Log.d(TAG, "onResponse: " + statusCode);
                            movie = response.body().getResults();

                            mRecyclerView.setLayoutManager(layoutManager);
                            mAdapter = new MovieAdapter(MyApplication.getAppContext(), movie, clickListener);

                            mRecyclerView.setAdapter(mAdapter);
                        /*This will help us in refreshing the whole adapter*/
                            mAdapter.notifyDataSetChanged();
                        }

                    }

                    @Override
                    public void onFailure(Call<ResponseMovie> call, Throwable t) {

                        Toast.makeText(MovieActivity.this, "Failure to load the data", Toast.LENGTH_SHORT).show();
                    }
                });


                return true;
            }
            case R.id.action_favorite:
                Log.d(TAG, "onOptionsItemSelected:fav_Movies" + id);

                Toast.makeText(this, "You clicked on favorite option!!!!!!!!" + id, Toast.LENGTH_SHORT).show();


                mCursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
                if (null != mCursor && mCursor.getCount() > 0) {
                    mCursor.moveToFirst();
                    if (mCursor.moveToFirst()) {
                        do {

                            favoriteMovieList.add(mCursor.getString(mCursor.getColumnIndexOrThrow(MovieContract.MovieEntry.MOVIE_TITLE)));
                            byte[] favoriteMovie = mCursor.getBlob(mCursor.getColumnIndexOrThrow(MovieContract.MovieEntry.MOVIE_IMAGE));
                            Bitmap newFavoriteMovie = DbBitmapUtility.convertToBitmap(favoriteMovie);
                            favoriteToggleStatuses.add(newFavoriteMovie);


                        } while (mCursor.moveToNext());
                        mCursor.close();

                    }


                    mRecyclerView.setLayoutManager(layoutManager);

                    mFavoriteMovieAdapter = new FavoriteMovieAdapter(MovieActivity.this, favoriteMovieList, favoriteToggleStatuses);//favoriteMoviesListFromTheContentProvider

                    mRecyclerView.setAdapter(mFavoriteMovieAdapter);

                    mFavoriteMovieAdapter.notifyDataSetChanged();
                }
                break;
            default:

        }
        return false;
    }

    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        // You can change this divider to adjust the size of the poster
        int widthDivider = 500;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
    }

    @Override
    protected void onResume() {
        super.onResume();
        // restore RecyclerView state
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
        }
        populateMovies();

    }

    /*Store*/
    @Override
    protected void onPause() {
        super.onPause();

        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }


}