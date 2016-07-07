package com.example.samir.movieappfragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



public class MovieDetailsFragment extends Fragment  {
    TextView title, voteAverage, date, overView, txtreviews;
    ImageView poster;
    ListView Tlist,Rlist;
    String movie_id;
    String originalTitle, detailsPoster, overViewData, votedAverage, releaseDate, result,revResult;
    private String Base__url = "http://image.tmdb.org/t/p/w154/";
    MainActivityFragment madf=new MainActivityFragment();
    private String POPULARITY_DESC = "popularity.desc";
    private String RATING_DESC = "vote_average.desc";
    CheckBox checkFavourit;
    public MovieDetailsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_movie_details, container, false);
        Bundle extras = getActivity().getIntent().getExtras();
        createDB();
        if(getArguments() != null){
            originalTitle = getArguments().getString("originalTitle");
            detailsPoster = getArguments().getString("path");
            votedAverage = getArguments().getString("voteAverage");
            overViewData = getArguments().getString("overView");
            movie_id = getArguments().getString("ID");
            releaseDate = getArguments().getString("date");
        }else {
            if (extras != null) {
                originalTitle = extras.getString("originalTitle");
                detailsPoster = extras.getString("path");
                votedAverage = extras.getString("voteAverage");
                overViewData = extras.getString("overView");
                movie_id = extras.getString("ID");
                releaseDate = extras.getString("date");
            }
        }
        title = (TextView) v.findViewById(R.id.title);
        title.setText(originalTitle);

        LinearLayout date_vote = (LinearLayout) v.findViewById(R.id.date_vote);
        date = (TextView) date_vote.findViewById(R.id.date);
        voteAverage = (TextView) date_vote.findViewById(R.id.vote);
        date.setText(releaseDate);
        voteAverage.setText(votedAverage);

        LinearLayout overViewLayout = (LinearLayout) v.findViewById(R.id.overViewTextView);
        overView = (TextView) v.findViewById(R.id.overView);
        overView.setText(overViewData);

        LinearLayout imgTextView = (LinearLayout) v.findViewById(R.id.imgTextView);
        poster = (ImageView) imgTextView.findViewById(R.id.image);
        Picasso.with(getActivity()).load(Base__url + detailsPoster).into(poster);
        if(movie_id !=null) {
            new ListTrailerTask().execute(movie_id);
            new ListReviewTask().execute(movie_id);
        }
        checkFavourit=(CheckBox)v.findViewById(R.id.checkFavourit);
        if(movie_id !=null)
        if(search() ){
            checkFavourit.setChecked(true);
        }
        checkFavourit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked == true){
                    try{
                        insert();
                    }catch (Exception e){
                        Toast.makeText(getActivity(),"This Movie is in your Favourit List",Toast.LENGTH_LONG).show();
                    }
                }else {
                    try {
                        deleteRaw();
                    }catch(Exception e){
                        Toast.makeText(getActivity(),"An error occured please you try to delete no exit value many times",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        Tlist=(ListView)v.findViewById(R.id.TList);
        Rlist=(ListView)v.findViewById(R.id.RList);
        txtreviews=(TextView)v.findViewById(R.id.txtreviews);

        return v;
    }




    public class ListReviewTask extends AsyncTask<String, Void,  ArrayList<String>> {
        int flag = 0;
        ArrayList<String> res =new ArrayList<>();

        @Override
        protected  ArrayList<String> doInBackground(String... params) {
            HttpURLConnection http;
            BufferedReader reader;
            String fisrt = "http://api.themoviedb.org/3/movie/";
            String last =  "/reviews?api_key="+API_KEY.getKEY();
            try {
                URL url = new URL(fisrt + params[0] + last);
                http = (HttpURLConnection) url.openConnection();
                http.connect();
                InputStream in = http.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                result = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                    flag = 1;
                }
                if (flag == 1) {
                    res= parseJSON(result);
                }
                if (null != in) {
                    in.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute( final ArrayList<String>  st) {
            if (flag == 1) {
                final int size=st.size();
                String [] names=new String[st.size()];
                for(int i=0;i<size;i++){
                    names[i]="Review "+(i+1);
                }
                ArrayAdapter<String> listAdapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, names);
                Rlist.setAdapter(listAdapter);
                Rlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        txtreviews.setText(st.get(position));

                    }
                });

            } else {

                Log.v("Failed", "Failed to fetch review!");
            }

        }

        ArrayList<String> parseJSON(String result) {
            ArrayList<String> keys=new ArrayList<>();
            try {
                JSONObject response = new JSONObject(result);
                JSONArray results = response.optJSONArray("results");
                TMDB_Data item;
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.optJSONObject(i);
                    keys.add(movie.optString("content"));

                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
            return keys;
        }


    }

    public void insert()
    {
        SQLiteDatabase db = getActivity().openOrCreateDatabase("Movies", getActivity().MODE_PRIVATE, null);
        db.execSQL("insert into MoviesID values (" + Integer.parseInt(movie_id) + ",'" + originalTitle + "')");
        db.close();
        Toast.makeText(getActivity(), "Marked As Favourit", Toast.LENGTH_LONG).show();
    }

    public void createDB(){
            SQLiteDatabase db = getActivity().openOrCreateDatabase("Movies", getActivity().MODE_PRIVATE, null);
            db.execSQL("CREATE TABLE IF NOT EXISTS MoviesID (ID integer primary key ,MoviePath varchar);");
        }

        public void deleteRaw() {
            SQLiteDatabase db = getActivity().openOrCreateDatabase("Movies", getActivity().MODE_PRIVATE, null);
            db.execSQL("delete from MoviesID where ID=(" + Integer.parseInt(movie_id) + ");");
            db.close();
            Toast.makeText(getActivity(), "Marked As UnFavourit", Toast.LENGTH_LONG).show();

        }
        public boolean search(){
            SQLiteDatabase db = getActivity().openOrCreateDatabase("Movies", getActivity().MODE_PRIVATE, null);
            Cursor c= db.rawQuery("select * from MoviesID where ID=(" + Integer.parseInt(movie_id) + ");",null);
            if(c!=null) {
                if(c.getCount()>0) {
                    c.close();
                    return true;
                }
                c.close();
            }
            return false;
        }



    public String getID(){
        return movie_id;
    }


    public class ListTrailerTask extends AsyncTask<String, Void,  ArrayList<String>> {
        int flag = 0;
        ArrayList<String> res =new ArrayList<>();

        @Override
        protected  ArrayList<String> doInBackground(String... params) {
            HttpURLConnection http;
            BufferedReader reader;
            String fisrt = "http://api.themoviedb.org/3/movie/";
            String last = "/videos?api_key="+API_KEY.getKEY();

            try {
                URL url = new URL(fisrt + params[0] + last);
                http = (HttpURLConnection) url.openConnection();
                http.connect();
                InputStream in = http.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                result = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                    flag = 1;
                }
                if (flag == 1) {
                    res= parseJSON(result);
                }
                if (null != in) {
                    in.close();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            return res;
        }

        @Override
        protected void onPostExecute( final ArrayList<String>  st) {
            if (flag == 1) {
                    final int size = st.size();
                    String[] names = new String[st.size()];
                    for (int i = 0; i < size; i++) {
                        names[i] = "Trailer" + (i + 1);
                    }

                    ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, names);
                    Tlist.setAdapter(listAdapter);
                    Tlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            String url = "https://www.youtube.com/watch?v=";
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setData(Uri.parse(url + st.get(position)));
                            startActivity(intent);

                        }
                    });

                    for(String name:st) {
                        Log.v("Keyss", name);

                    }
            } else {

                Log.v("Failed", "Failed to fetch Trailer!");
            }

        }

        ArrayList<String> parseJSON(String result) {
            ArrayList<String> keys=new ArrayList<>();
            try {
                JSONObject response = new JSONObject(result);
                JSONArray results = response.optJSONArray("results");
                TMDB_Data item;
                for (int i = 0; i < results.length(); i++) {
                    JSONObject movie = results.optJSONObject(i);
                   keys.add(movie.optString("key"));

                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
            return keys;
        }


    }

    public static class MovieDetails extends Activity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_movie_details);

    //        Bundle arg=new Bundle();
    //        arg.putString("path", getIntent().getExtras().getString("path"));
    //        arg.putString("overView", getIntent().getExtras().getString("overView"));
    //        arg.putString("date", getIntent().getExtras().getString("date"));
    //        arg.putString("originalTitle", getIntent().getExtras().getString("originalTitle"));
    //        arg.putString("voteAverage", getIntent().getExtras().getString("voteAverage"));
    //        arg.putString("ID", getIntent().getExtras().getString("ID"));
    //        MovieDetailsFragment mdf=new MovieDetailsFragment();
    //        mdf.setArguments(arg);
    //        getFragmentManager().beginTransaction().add(R.id.movie_details_container,mdf).commit();

            getFragmentManager().beginTransaction().replace(R.id.movie_details_container,new MovieDetailsFragment()).commit();
        }
    }
}