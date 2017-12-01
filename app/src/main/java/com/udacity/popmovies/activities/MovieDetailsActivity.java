package com.udacity.popmovies.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.udacity.popmovies.BuildConfig;
import com.udacity.popmovies.R;
import com.udacity.popmovies.adapters.TrailerAdapter;
import com.udacity.popmovies.constants.DbBitmapUtility;
import com.udacity.popmovies.constants.MovieConstants;
import com.udacity.popmovies.database.MovieContract;
import com.udacity.popmovies.database.MovieDbHelper;

import com.udacity.popmovies.models.TrailerResponse;
import com.udacity.popmovies.models.Trailers;
import com.udacity.popmovies.networking.RetrofitApiEndPoints;
import com.udacity.popmovies.networking.RetrofitClient;
import com.udacity.popmovies.stetho.MyApplication;


import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by sagarsrao on 22-09-2017.
 */

public class MovieDetailsActivity extends AppCompatActivity {


    TextView mTitle;

    TextView mVoteAverage;

    TextView mReleaseDate;

    ImageView mPosterImage;

    TextView mMovieReviewLink;


    TextView mOverView;

    private ToggleButton mToggleButton;

    RecyclerView mTrailerView;


    List<Trailers> trailersList;

    long currentVisiblePosition = 0;

    private RecyclerView.LayoutManager mLayoutManager;

    private TrailerAdapter mAdapter;

    private ScrollView mScrollView;

    String textOn = "Mark Me as \n\n Favorite";

    String textOff = "Mark Me as \n\n UnFavorite";

    private MovieDbHelper movieDbHelper;

    Parcelable state;

    byte[] newByteArrayMovieImage;


    private Parcelable recyclerViewState;

    int topView;

    private String movieTitle;
    private String movieId;
    private String movieImage;
    private String moviePosterPath;
    byte[] inputData;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_moviedetails);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitle(getString(R.string.toolbar_title));
        myToolbar.setTitleTextColor(Color.WHITE);
        myToolbar.setBackgroundResource(R.color.colorPrimary);
        myToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
        setSupportActionBar(myToolbar);

        mTitle = findViewById(R.id.tv_movieTitle);
        mVoteAverage = findViewById(R.id.tv_movie_VoteAverage);
        mReleaseDate = findViewById(R.id.tv_movie_ReleaseDate);
        mOverView = findViewById(R.id.tv_movie_overView);
        mPosterImage = findViewById(R.id.iv_selected_movie_image);
        mTrailerView = findViewById(R.id.rv_trailers);
        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        //mLayoutManager.onSaveInstanceState();
        //mLayoutManager.onRestoreInstanceState(state);
        mTrailerView.setLayoutManager(mLayoutManager);
        mScrollView = findViewById(R.id.scrollView_movie_details);
        trailersList = new ArrayList<>();
        mToggleButton = findViewById(R.id.toggleMovie);
        mToggleButton.setTextOff(textOff);
        movieDbHelper = new MovieDbHelper(MovieDetailsActivity.this);
        mMovieReviewLink = findViewById(R.id.tv_movie_review_link);
        movieTitle = getIntent().getExtras().getString(MovieConstants.MOVIE_TITLE);
        movieId = getIntent().getExtras().getString(MovieConstants.MOVIE_ID);
        movieImage = getIntent().getExtras().getString(MovieConstants.MOVIE_POSTER_VIEWS); //you are simply taking it from the bundle
        moviePosterPath = MovieConstants.MOVIE_IMAGE_URL + getIntent().getExtras().getString(MovieConstants.MOVIE_POSTER_VIEWS);


        mMovieReviewLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(MovieDetailsActivity.this, MovieReviewActivity.class);
                Bundle bundle = new Bundle();
                String movieId = getIntent().getExtras().getString(MovieConstants.MOVIE_ID);
                bundle.putString(MovieConstants.MOVIE_ID, movieId);
                intent.putExtras(bundle);
                startActivity(intent);

            }
        });

        mToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    mToggleButton.setTextOn(textOn);
                    //TODO : You could use getContentresolver.query() to check if a movie is already favorited and in this case display a message or better implement un-favorite functionality

                    /*Converting an URL image into BitMap Image So that database should contain byte array of image*/
                    Bitmap newBitMapMovieImage = DbBitmapUtility.getBitmapFromURL(moviePosterPath);

                    byte[] imageByte = DbBitmapUtility.favoriteImage(newBitMapMovieImage);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MovieConstants.MOVIE_ID, movieId);//col 0
                    contentValues.put(MovieConstants.MOVIE_TITLE, movieTitle);//col 1
                    contentValues.put(MovieConstants.MOVIE_POSTER_VIEWS, imageByte);//col 2


                    Uri uri = getContentResolver()
                            .insert(MovieContract.MovieEntry.CONTENT_URI, contentValues);


                    Toast.makeText(MovieDetailsActivity.this, uri.toString(), Toast.LENGTH_SHORT).show();


                } else {
                    mToggleButton.setTextOff(textOff);

                }


            }
        });

        myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        moviePosterPath = MovieConstants.MOVIE_IMAGE_URL + getIntent().getExtras().getString(MovieConstants.MOVIE_POSTER_VIEWS);
        Glide.with(MovieDetailsActivity.this)
                .load(moviePosterPath)
                .placeholder(R.drawable.iv_placeholder__moviedetails)
                .error(R.drawable.iv_placeholder_error_moviedetails)
                .into(mPosterImage);

        mTitle.setText(getIntent().getExtras().getString(MovieConstants.MOVIE_TITLE));
        mVoteAverage.setText(getIntent().getExtras().getString(MovieConstants.MOVIE_VOTE_AVERAGE).concat("/10"));
        mReleaseDate.setText(getIntent().getExtras().getString(MovieConstants.MOVIE_RELEASE_DATE));
        mOverView.setText(getIntent().getExtras().getString(MovieConstants.MOVIE_OVERVIEW));
        /*Call the trailer API*/

        /*Retrofit client class is helpful in creating the connections to the API endpoints*/
        RetrofitApiEndPoints apiEndPoints = RetrofitClient.getClient().create(RetrofitApiEndPoints.class);


        String _id = getIntent().getExtras().getString(MovieConstants.MOVIE_ID);
        Call<TrailerResponse> call = apiEndPoints.getTrailers(_id, BuildConfig.API_KEY);
        call.enqueue(new Callback<TrailerResponse>() {
            @Override
            public void onResponse(Call<TrailerResponse> call, Response<TrailerResponse> response) {

                if (response != null) {

                    trailersList = response.body().getTrailerResults();//server response will be loaded in to the list
                    mAdapter = new
                            TrailerAdapter(MyApplication.getAppContext(), trailersList);//Data is passed to the adapter
                    mTrailerView.setLayoutManager(mLayoutManager);
                    mTrailerView.setAdapter(mAdapter);

                    mAdapter.notifyDataSetChanged();
                }

            }

            @Override
            public void onFailure(Call<TrailerResponse> call, Throwable t) {

                Toast.makeText(MovieDetailsActivity.this, "Failure to load the data", Toast.LENGTH_SHORT).show();
            }
        });


    }


    @Override
    protected void onPause() {
        super.onPause();

        //currentVisiblePosition = ((LinearLayoutManager) mTrailerView.getLayoutManager()).findFirstCompletelyVisibleItemPosition();

    }

    @Override
    protected void onResume() {
        super.onResume();

     /*   (mTrailerView.getLayoutManager()).scrollToPosition((int) currentVisiblePosition);
        currentVisiblePosition = 0;*/

    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        recyclerViewState = mTrailerView.getLayoutManager().onSaveInstanceState();

        /*outState.putIntArray("ARTICLE_SCROLL_POSITION",
                new int[]{mScrollView.getScrollX(), mScrollView.getScrollY()});*/
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mTrailerView.getLayoutManager().onRestoreInstanceState(recyclerViewState);

       /* final int[] position = savedInstanceState.getIntArray("ARTICLE_SCROLL_POSITION");
        if (position != null)
            mScrollView.post(new Runnable() {
                public void run() {
                    mScrollView.scrollTo(position[0], position[1]);
                }
            });*/
    }


}
