package com.example.samir.movieappfragment;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class GridAsyncTask extends AsyncTask<String,Void,Integer> {

    public static String getResult() {
        return result;
    }

    public ArrayList<TMDB_Data> getData() {
        return Data;
    }

    static String result;
    GridViewAdapter adapter;
    ArrayList<TMDB_Data> Data =new ArrayList<>();
    String Base__url="http://image.tmdb.org/t/p/w185/";
    public GridAsyncTask(String result, ArrayList<TMDB_Data> data,GridViewAdapter adapter) {
        this.result=result;
        this.adapter=adapter;
        this.Data=data;
    }

    int flag=0;
    @Override
    protected Integer doInBackground(String... params) {
        HttpURLConnection http;
        BufferedReader reader;
        String fisrt="http://api.themoviedb.org/3/discover/movie?sort_by=";
        String last="&api_key="+API_KEY.getKEY();


        try {
            URL url=new URL(fisrt+params[0]+last);

            http= (HttpURLConnection) url.openConnection();
            http.connect();
            InputStream in =http.getInputStream();
            reader=new BufferedReader(new InputStreamReader(in));
            String line="";
            result="";
            while((line=reader.readLine()) != null){
                result+=line;
                flag=1;
            }
            if(flag==1){
                parseJSON(result);
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

    void parseJSON(String result) {

        try {
            JSONObject response = new JSONObject(result);
            JSONArray results = response.optJSONArray("results");
            TMDB_Data item;
            for (int i = 0; i < results.length(); i++) {
                JSONObject poster = results.optJSONObject(i);
                String path = poster.optString("poster_path");
                item = new TMDB_Data();
                if (path != null)
                    item.setImage(Base__url + path);

                Data.add(item);

            }
        } catch (JSONException e) {

            e.printStackTrace();
        }
    }


}
