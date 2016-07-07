package com.example.samir.movieappfragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;


public class MainActivity extends Activity implements MainActivityFragment.callBack {
   static boolean mTwoPane;
    Intent intent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.movie_details_container) != null) {
            mTwoPane = true;

            if(savedInstanceState == null) {
                getFragmentManager().beginTransaction().replace(R.id.movie_details_container, new MovieDetailsFragment()).commit();
            }

        }else{
            mTwoPane=false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


    @Override
    public void selected(String path, String overView, String date, String originalTitle, String voteAverage, String id,String result) {

            if(mTwoPane){
                Bundle arg=new Bundle();
                arg.putString("path", path);
                arg.putString("overView", overView);
                arg.putString("date", date);
                arg.putString("originalTitle", originalTitle);
                arg.putString("voteAverage", voteAverage);
                arg.putString("ID", id);
                arg.putString("result", result);
                MovieDetailsFragment mdf=new MovieDetailsFragment();
                mdf.setArguments(arg);
                getFragmentManager().beginTransaction().replace(R.id.movie_details_container, mdf).commit();
            }else {
                Log.v("Tablet", "NOtablet");
                intent = new Intent(this, MovieDetailsFragment.MovieDetails.class);
                intent.putExtra("path", path);
                intent.putExtra("overView", overView);
                intent.putExtra("date", date);
                intent.putExtra("originalTitle", originalTitle);
                intent.putExtra("voteAverage", voteAverage);
                intent.putExtra("ID", id);
                intent.putExtra("result", result);
                startActivity(intent);
            }

    }
}
