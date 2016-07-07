package com.example.samir.movieappfragment;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class GridViewAdapter  extends ArrayAdapter<TMDB_Data> {
    Context c;
    int layoutID;
    ArrayList<TMDB_Data> Data= new ArrayList<>();
    ImageView image;

    public GridViewAdapter(Context context, int resource, List<TMDB_Data> objects) {
        super(context, resource, objects);
        this.c=context;
        this.layoutID=resource;
        this.Data= (ArrayList<TMDB_Data>) objects;
    }
    void ListenToChange(ArrayList<TMDB_Data> data){
        this.Data=data;
        notifyDataSetChanged();
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View item=convertView;
        if(item == null){
            item= LayoutInflater.from(c).inflate(layoutID, parent, false);
            image= (ImageView) item.findViewById(R.id.imageView);
            item.setTag(image);
        }else{
            image= (ImageView) item.getTag();
        }
        TMDB_Data element = Data.get(position);
        Picasso.with(c).load(element.getImage()).into(image);
        return item;
    }

}
