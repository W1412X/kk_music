package com.example.myapplication;


import android.content.Context;
import android.content.Intent;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SongListItem extends LinearLayout{
    private ImageView imageView;
    private TextView titletextView;
    private TextView statustextView;
    String image_url;
    String title;
    String more_msg;
    String url;
    public SongListItem(Context context, String title, String more_msg, String img_url, String page_url){
        super(context);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.home_page_song_list_item, this, true);
        imageView = view.findViewById(R.id.item_image_view);
        titletextView = view.findViewById(R.id.item_title);
        statustextView=view.findViewById(R.id.item_status);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        int i=0;
        this.title=title;
        this.more_msg=more_msg;
        this.image_url=img_url;
        this.url=page_url;
        titletextView.setText(this.title);
        statustextView.setText(this.more_msg);
        Picasso.get().load(img_url).into(imageView);
    }
}

