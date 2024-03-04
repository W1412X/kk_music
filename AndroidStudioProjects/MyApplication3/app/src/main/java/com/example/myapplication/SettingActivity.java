package com.example.myapplication;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {

    private Button develop_button,source_button,about_button,privacy_button,permission_button,edit_msg_button,clear_cache_button;
    private TextView source_view,about_view,privacy_view,permission_view;
    private WebView develop_web;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init_view();
    }
    public void init_view(){

        about_view=findViewById(R.id.settings_about_text_view);
        privacy_view=findViewById(R.id.settings_privacy_text_view);
        permission_view=findViewById(R.id.settings_permission_text_view);
        privacy_view=findViewById(R.id.settings_privacy_text_view);
        permission_button=findViewById(R.id.settings_permission_button);
        privacy_button=findViewById(R.id.settings_privacy_button);
        edit_msg_button=findViewById(R.id.settings_self_msg_button);
        clear_cache_button=findViewById(R.id.settings_clear_cache_button);
        develop_button=findViewById(R.id.settings_developer_button);
        source_button=findViewById(R.id.settings_source_button);
        source_view=findViewById(R.id.settings_source_add_text_view);
        develop_web=findViewById(R.id.settings_developer_web);
        develop_web.getSettings().setDomStorageEnabled(true);
        about_button=findViewById(R.id.settings_about_button);
        source_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                source_view.setVisibility(View.VISIBLE);
            }
        });
        develop_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                develop_web.setVisibility(View.VISIBLE);
                try{
                    develop_web.loadUrl("http://39.101.160.55/kkmusic/kk_dev.html");
                }catch (Exception e){
                    Log.e("develop_web",e.getMessage().toString());
                    Toast.makeText(SettingActivity.this,"无法获取资源",Toast.LENGTH_SHORT).show();
                }
            }
        });
        about_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                about_view.setVisibility(View.VISIBLE);
            }
        });
        privacy_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                privacy_view.setVisibility(View.VISIBLE);
            }
        });
        permission_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permission_view.setVisibility(View.VISIBLE);
            }
        });
        clear_cache_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(SettingActivity.this);
                builder.setMessage("无权限");
                builder.show();
            }
        });
        edit_msg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder=new AlertDialog.Builder(SettingActivity.this);
                builder.setMessage("无权限");
                builder.show();
            }
        });
    }
    @Override
    public void onBackPressed(){
        if(source_view.getVisibility()== View.VISIBLE){
            source_view.setVisibility(View.GONE);
        }else if(develop_web.getVisibility()==View.VISIBLE){
            develop_web.setVisibility(View.GONE);
        } else if (privacy_view.getVisibility()==View.VISIBLE) {
            privacy_view.setVisibility(View.GONE);
        } else if (permission_view.getVisibility()==View.VISIBLE) {
            permission_view.setVisibility(View.GONE);
        } else if (about_view.getVisibility()==View.VISIBLE) {
            about_view.setVisibility(View.GONE);
        } else{
            super.onBackPressed();
        }
    }
}