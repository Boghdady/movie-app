package com.example.samir.movieappfragment;

import android.app.Fragment;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;



public class MainActivityFragment extends Fragment implements AdapterView.OnItemClickListener {
    GridView posterGrid;
    ArrayList<TMDB_Data> Data;
    ArrayList<String> list =new ArrayList<>();

    GridViewAdapter adapter;
    private String Base__url="http://image.tmdb.org/t/p/w185/";

    private String POPULARITY_DESC = "popularity.desc";
    private String RATING_DESC = "vote_average.desc";

    String result="";
    Intent intent;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View v=inflater.inflate(R.layout.fragment_main, container, false);
        posterGrid= (GridView) v.findViewById(R.id.gridView);
        Data=new ArrayList<>();

        adapter=new GridViewAdapter(getActivity(),R.layout.gridview_item,Data);
        posterGrid.setAdapter(adapter);
        posterGrid.setOnItemClickListener(this);
        new GridAsyncTask(result,Data,adapter).execute(POPULARITY_DESC);

        return v;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mostPopular: {
                Data.clear();
                new GridAsyncTask(result,Data,adapter).execute(POPULARITY_DESC);
                break;
            }
            case R.id.highestRated: {

                Data.clear();
                new GridAsyncTask(result,Data,adapter).execute(RATING_DESC);
                Toast.makeText(getActivity(), "2", Toast.LENGTH_LONG).show();

                break;
            }
            case R.id.favourit: {
                getID_FromDB();
                Data.clear();

                new FavouritGridAsyncTask().execute(POPULARITY_DESC);
                new FavouritGridAsyncTask().execute(RATING_DESC);
                break;
            }
        } return true;
    }
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TMDB_Data imagePath= Data.get(position);
        String realpath=imagePath.getImage().substring(Base__url.length(), imagePath.getImage().length());
        try {
            String path="",ovewView="",Date="",originalTitle="",voteAverage="",movie_id="";
            JSONObject response = new JSONObject(GridAsyncTask.getResult());
            JSONArray results = response.optJSONArray("results");
            TMDB_Data item;
            for (int i = 0; i < results.length(); i++) {

                 JSONObject poster = results.optJSONObject(i);
                 path = poster.optString("poster_path");
                if(realpath.equals(path)){
                ovewView=poster.optString("overview");
                 Date=poster.optString("release_date");
                 originalTitle=poster.optString("original_title");
                 voteAverage=poster.optString("vote_average");
                 movie_id=poster.optString("id");
                    ((callBack)getActivity()).selected(path,ovewView,Date,originalTitle,voteAverage,movie_id,result);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }


    }
    public interface callBack{
        public void selected(String path,String overView,String date,String originalTitle,String voteAverage,String id,String result);
    }

    public void getID_FromDB(){
        try{
            SQLiteDatabase db = getActivity().openOrCreateDatabase("Movies", getActivity().MODE_PRIVATE, null);

            Cursor c=db.rawQuery("SELECT * FROM MoviesID", null);
            c.moveToFirst();
            do{
                // Toast.makeText(this,c.getString(c.getColumnIndex("ID"))+" "+c.getString(c.getColumnIndex("MoviePath")),Toast.LENGTH_LONG).show();
                list.add(c.getString(c.getColumnIndex("ID")));
            }while (c.moveToNext());

            db.close();

        }catch (Exception e){

            Log.v("DataBase Error", e.getMessage());
        }


    }
    public class FavouritGridAsyncTask extends AsyncTask<String,Void,Integer> {
        int flag=0;

        @Override
        protected Integer doInBackground(String ... params) {

            HttpURLConnection http;
            BufferedReader reader;
            String fisrt="http://api.themoviedb.org/3/discover/movie?sort_by=";
            String last="&api_key="+API_KEY.getKEY();

            try {

                URL url = new URL(fisrt + params[0] + last);
                http = (HttpURLConnection) url.openConnection();
                http.connect();
                InputStream in=http.getInputStream();
                reader = new BufferedReader(new InputStreamReader(in));
                String line = "";
                result = "";
                while ((line = reader.readLine()) != null) {
                    result += line;
                    flag = 1;
                }
                if (flag == 1) {
                    searchFavouritJSON(result);
                }

                if (null != in) {
                    in.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return flag;
        }


        @Override
        protected void onPostExecute(Integer integer) {

            if(flag==1){
                adapter.ListenToChange(Data);
            } else {

                Log.v("Failed", "Failed to fetch data!");
            }

        }

        void searchFavouritJSON(String result) {
            try {
                JSONObject response = new JSONObject(result);
                JSONArray results = response.optJSONArray("results");
                TMDB_Data item;
                for (int i = 0; i < results.length(); i++) {
                    JSONObject poster = results.optJSONObject(i);
                    String id = poster.optString("id");

                    for (int j = 0; j < list.size(); j++) {
                        if (list.get(j).equals(id)) {
                            String path = poster.optString("poster_path");
                            item = new TMDB_Data();
                            if (path != null)
                                item.setImage(Base__url + path);
                            Data.add(item);
                            break;
                        }
                    }
                }
            } catch (JSONException e) {

                e.printStackTrace();
            }
        }

    }
}




