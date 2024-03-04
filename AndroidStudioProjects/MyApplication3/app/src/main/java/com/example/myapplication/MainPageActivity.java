package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.squareup.picasso.BuildConfig;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.logging.ConsoleHandler;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import android.provider.Settings;

import org.json.JSONException;
import org.json.JSONObject;


public class MainPageActivity extends AppCompatActivity {
    public interface Circle_modes{
        int ONE_CIRCLE = 1;
        int LIST_CIRCLE = 2;
    }
    private ViewPager viewPager;
    private static final long TIME_INTERVAL = 2000; // 两次点击的时间间隔
    private long backPressedTime;
    int play_count=0;
    private ArrayList<View>views;
    private TextView player_bar_title_text_view,self_page_name_text_view;
    private String play_list_string;
    private String latest_version_url = "http://39.101.160.55/download_app_source/kk_music_version.json";
    private String download_url = "http://39.101.160.55/download_app_source/kk_music.apk";
    private SeekBar seekBar;
    private int circle_mode=Circle_modes.LIST_CIRCLE;
    private ImageButton search_button,home_page_search_button,self_settings_button,search_song_play_list_button,search_one_song_circle_button,search_song_list_circle_button;
    private Button self_page_recent_button,self_page_downloaded_button,self_page_stared_button,self_page_song_list_button;
    private ImageView home_page_main_image;
    private ImageButton home_button,play_button,self_button;
    private EditText search_input;
    private ServerConnect serverConnect;
    private boolean is_prepared=false;
    private LinearLayout home_page_horizontal_linear;
    private HomeListContainer home_page_vertical_linear;
    private boolean is_paused=true;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Handler handler;
    MediaPlayer mediaPlayer;
    private LinearLayout music_item_container;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        setContentView(R.layout.activity_main_page);
        handler=new android.os.Handler(Looper.getMainLooper());
        sharedPreferences=getSharedPreferences("MSG",MODE_PRIVATE);
        editor=sharedPreferences.edit();
        if(sharedPreferences.getString("play_list","").equals("")){
            editor.putString("play_list",sharedPreferences.getString("stared_songs_list",""));
            editor.apply();
        }
        init_view();
        viewPager.setCurrentItem(0);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                init_home_page();
                home_button.callOnClick();
                load_song_item();
            }
        },100);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 更新 SeekBar 的进度
                new Thread(() -> {
                    while (true){
                        if(mediaPlayer != null&&is_prepared){
                            try {
                                int progress=(int)(((double)mediaPlayer.getCurrentPosition()/mediaPlayer.getDuration())*1000);
                                Log.e("UPDATE SEEK",String.valueOf(progress));
                                seekBar.setProgress(progress);
                                Thread.sleep(1000); // 每隔 1 秒更新一次进度
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }).start();
                MainPageActivity.CheckUpdate checkUpdate=new MainPageActivity.CheckUpdate();
                MainPageActivity.CheckUpdate.CheckVersionTask checkVersionTask=checkUpdate.new CheckVersionTask();
                checkVersionTask.execute(latest_version_url);
            }
        },500);
    }
    @Override
    public void onBackPressed() {
        if (backPressedTime + TIME_INTERVAL > System.currentTimeMillis()) {
            this.finish();
        } else {
            Toast.makeText(this, "再按一次返回键退出应用", Toast.LENGTH_SHORT).show();
        }
        backPressedTime = System.currentTimeMillis();
    }
    public void init_view(){
        //init the player
        mediaPlayer=new MediaPlayer();
        serverConnect=new ServerConnect();
        viewPager=findViewById(R.id.viewpager);
        home_button=findViewById(R.id.bar_home);
        play_button=findViewById(R.id.bar_play);
        self_button=findViewById(R.id.bar_self);
        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(0);
            }
        });
        play_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewPager.getCurrentItem()==1){//if in the list page
                    if(is_prepared){
                        if(!is_paused){//if is playing,and prepared,pause it
                            play_button.setImageResource(R.drawable.player_purple);
                            mediaPlayer.pause();
                            is_paused=true;
                        }else{//if is paused and prepared,play it
                            play_button.setImageResource(R.drawable.pause_purple);
                            mediaPlayer.start();
                            is_paused=false;
                        }
                    }else{
                        Toast.makeText(MainPageActivity.this,"无播放曲目",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    viewPager.setCurrentItem(1);
                }
            }
        });
        self_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });
        views=new ArrayList<View>();//加载，添加视图
        views.add(LayoutInflater.from(this).inflate(R.layout.home_page,null));
        views.add(LayoutInflater.from(this).inflate(R.layout.list_search,null));
        views.add(LayoutInflater.from(this).inflate(R.layout.self_msg_page,null));
        viewPager.setAdapter(new MyViewPagerAdapter());
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position){
                    case 0://对应homepage
                        init_home_page();
                        home_button.setImageResource(R.drawable.home_purple);
                        if(!is_paused){
                            play_button.setImageResource(R.drawable.pause);
                        }else{
                            play_button.setImageResource(R.drawable.play);
                        }
                        self_button.setImageResource(R.drawable.self);
                        break;
                    case 1://对应playbutton
                        init_search_page();
                        home_button.setImageResource(R.drawable.home);
                        if(!is_paused){
                            play_button.setImageResource(R.drawable.pause_purple);
                        }else{
                            play_button.setImageResource(R.drawable.player_purple);
                        }
                        self_button.setImageResource(R.drawable.self);
                        break;
                    case 2:
                        init_self_page();
                        home_button.setImageResource(R.drawable.home);
                        if(!is_paused){
                            play_button.setImageResource(R.drawable.pause);
                        }else{
                            play_button.setImageResource(R.drawable.play);
                        }
                        self_button.setImageResource(R.drawable.self_purple);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        //deal when there's error while playing
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        player_bar_title_text_view.setText("无播放曲目");
                        Toast.makeText(MainPageActivity.this,"播放错误",Toast.LENGTH_LONG).show();
                    }
                });
                return false;
            }
        });
        //set the play finished to do
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if(circle_mode==Circle_modes.ONE_CIRCLE){
                    mediaPlayer.start();
                }else if(circle_mode==Circle_modes.LIST_CIRCLE){
                    try{
                        String msg[]=get_song_from_song_list();
                        play(msg[0],msg[2],msg[1]);
                    }catch (Exception e){
                        //
                    }
                }
            }
        });
        //set the player's prepare listener
        mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mediaPlayer.start();
                is_paused=false;
                is_prepared=true;
            }
        });
        //set the search button's listener
    }
    //ViewPager适配器
    private class MyViewPagerAdapter extends PagerAdapter {
        @Override
        public int getCount() {
            return views.size();
        }


        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view==object;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(views.get(position));
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(views.get(position));
            return views.get(position);
        }
    }
    String[] get_song_from_song_list(){
        String[] re={"无待播放曲目","无",""};
        String list=sharedPreferences.getString("play_list_string","");
        if(list.equals("")){
            return re;
        }
        String[] songs_msg=list.split("\\|OUTERDIVIDE\\|");
        re[0]=songs_msg[play_count%songs_msg.length].split("\\|INNERDIVIDE\\|")[0];
        re[1]=songs_msg[play_count%songs_msg.length].split("\\|INNERDIVIDE\\|")[1];
        re[2]=songs_msg[play_count%songs_msg.length].split("\\|INNERDIVIDE\\|")[2];
        play_count++;
        return re;
    }
    //play function,when want to play a new music,use this function
    public void play(String name,String author,String url){
        final String tmp_url=url,tmp_name=name,tmp_author=author;
        handler.post(new Runnable() {
            @Override
            public void run() {
                player_bar_title_text_view.setText(name+" の "+author);
                play_button.setImageResource(R.drawable.pause_purple);
                mediaPlayer.reset();
                is_prepared=false;
                mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                try {
                    mediaPlayer.setDataSource(tmp_url);
                    mediaPlayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
        String song=name+"|INNERDIVIDE|"+url+"|INNERDIVIDE|"+author;
        add_to_string_unique(song,"recent_songs_string");
    }
    public void add_to_string_unique(String msg,String which){
        String[] songs_list=sharedPreferences.getString(which,"").split("\\|OUTERDIVIDE\\|");
        boolean if_in=false;
        for(int i=0;i<songs_list.length;i++){
            if(msg.equals(songs_list[i])){
                String tmp=songs_list[0];
                songs_list[0]=songs_list[i];
                songs_list[i]=tmp;
                if_in=true;
                break;
            }
        }
        if(if_in){
            String re="";
            for(int i=0;i<songs_list.length;i++){
                if(i!=songs_list.length-1){
                    re+=songs_list[i]+"|OUTERDIVIDE|";
                }else{
                    re+=songs_list[i];
                }
            }
            SharedPreferences.Editor editor1=sharedPreferences.edit();
            editor1.putString(which,re);
            editor1.apply();
        }else{
            add_to_list(msg.split("\\|INNERDIVIDE\\|")[0],msg.split("\\|INNERDIVIDE\\|")[2],msg.split("\\|INNERDIVIDE\\|")[1],which);
        }

    }
    public boolean if_in_recent(String msg){//msg使用|INNERDIVIDE|隔开
        String[] dict=sharedPreferences.getString("recent_songs_string","").split("\\|OUTERDIVIDE\\|");
        int p=-1;
        for(int i=0;i<dict.length;i++){
            if(msg.equals(dict[i])){
                p=i;
                String re=msg;
                for(int u=0;u<dict.length;u++){
                    if(u!=p&&u!=dict.length-1){
                        re+=dict[u]+"|OUTERDIVIDE|";
                    }else if(u!=p&&u==dict.length-1){
                        re+=dict[u];
                    }else{
                        continue;
                    }
                }
                editor.putString("recent_songs_string",re);
                editor.apply();
                return true;
            }
        }
        return false;
    }

    //SongItem class

    public class SongItem extends LinearLayout {
        public String name="null",author="null",url="http://music.163.com/song/media/outer/url?id=2061978961",which="";
        private TextView name_view,author_view;
        private ImageButton download_button,play_button,star_button,add_to_list_button;
        public SongItem(Context context) {
            super(context);
            init();
        }
        public SongItem(Context context, AttributeSet attrs) {
            super(context, attrs);
            init();
        }
        public SongItem(Context context,String name,String author,String url,String which) {
            super(context);
            this.which=which;
            this.name=name;
            this.author=author;
            this.url=url;
            init();
        }
        private void init() {
            LayoutInflater.from(getContext()).inflate(R.layout.song_item, this, true);
            // 这里可以对布局文件中的子 View 进行操作
            name_view=findViewById(R.id.song_item_song_name);
            add_to_list_button=findViewById(R.id.song_item_add_to_play_list_button);
            author_view=findViewById(R.id.song_item_song_author);
            download_button=findViewById(R.id.song_item_download_button);
            play_button=findViewById(R.id.song_item_play_button);
            star_button=findViewById(R.id.song_item_star_button);
            name_view.setText(name);
            author_view.setText(author);
            if(which.equals("play_list")){
                download_button.setVisibility(View.GONE);
                add_to_list_button.setImageResource(R.drawable.remove_from_list_icon);
                star_button.setVisibility(View.GONE);
            }
            //判定是否stared
            if(if_stared(name+"|INNERDIVIDE|"+url+"|INNERDIVIDE|"+author)){
                star_button.setImageResource(R.drawable.song_item_stared);
            }else{
                //
            }
            download_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
                    builder.setTitle(">_<");
                    builder.setMessage("该功能仍在开发中");
                    builder.show();
                }
            });
            play_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    add_to_string_unique(name+"|INNERDIVIDE|"+url+"|INNERDIVIDE|"+author,"play_list_string");
                    if(is_prepared){
                        is_prepared=false;
                        play(name,author,url);
                    }else{
                        play(name,author,url);
                    }
                }
            });
            star_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(if_stared(name+"|INNERDIVIDE|"+url+"|INNERDIVIDE|"+author)){
                        delete_from_list(name,author,url,"stared_songs_string");
                        star_button.setImageResource(R.drawable.song_item_star);
                    }else{
                        add_to_list(name,author,url,"stared_songs_string");
                        star_button.setImageResource(R.drawable.song_item_stared);
                    }
                }
            });
            add_to_list_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(SongItem.this.which.equals("play_list")){
                        final String tmp_name=name;
                        delete_from_list(name,author,url,"play_list_string");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                deal_msg_from_server(sharedPreferences.getString("play_list_string",""),"play_list");//重新加载列表
                                Toast.makeText(MainPageActivity.this,"已从播放列表删除"+tmp_name,Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        final String tmp_name=name;
                        add_to_list(name,author,url,"play_list_string");
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainPageActivity.this,"已添加"+tmp_name+"至播放列表",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });
            download_button.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    new DownloadTask().execute(url);
                }
            });
        }
        public boolean if_stared(String msg){//msg使用|INNERDIVIDE|隔开
            String[] dict=sharedPreferences.getString("stared_songs_string","").split("\\|OUTERDIVIDE\\|");
            for(int i=0;i<dict.length;i++){
                if(msg.equals(dict[i])){
                    return true;
                }
            }
            return false;
        }
        public class DownloadTask extends AsyncTask<String, Integer, Void> {

            private ProgressDialog progressDialog;

            @Override
            protected void onPreExecute() {
                try{
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:" + getPackageName()));
                        }
                    });
                }catch (Exception e){

                }
                super.onPreExecute();
                progressDialog = new ProgressDialog(MainPageActivity.this);
                progressDialog.setMessage("Downloading Music...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCancelable(false);
                progressDialog.show();

            }

            @Override
            protected Void doInBackground(String... params) {
                String fileName = params[0].split("=")[1] + ".mp3";
                File file = new File(Environment.getExternalStorageDirectory(), fileName);

                try {
                    URL url = new URL(params[0]);
                    try (InputStream in = url.openStream();
                         FileOutputStream out = new FileOutputStream(file)) {
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        int totalBytesRead = 0;
                        int fileSize = url.openConnection().getContentLength();
                        while ((bytesRead = in.read(buffer)) != -1) {
                            out.write(buffer, 0, bytesRead);
                            totalBytesRead += bytesRead;
                            publishProgress((int) ((totalBytesRead * 100) / fileSize));
                        }
                    }
                    String savedFilePath = file.getAbsolutePath();
                    Log.e("path",savedFilePath);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            add_to_string_unique(name+"|INNERDIVIDE|"+savedFilePath+"|INNERDIVIDE|"+author,"downloaded_songs_string");
                        }
                    });
                    // 现在你可以将savedFilePath用于后续的操作，比如显示给用户或者进行其他处理
                } catch (IOException e) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder=new AlertDialog.Builder(MainPageActivity.this);
                            builder.setMessage("无权限，请手动配置存储权限");
                            builder.show();
                        }
                    });
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                progressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                progressDialog.dismiss();
            }
        }
    }

    //a class to connect the server and get the search result

    public class ServerConnect {
        private String re;
        public ServerConnect(){
        }
        public class WebSocketClient {
            private OkHttpClient client;
            private String msg;
            private WebSocket webSocket;
            WebSocketClient(String message){
                msg=message;
            }
            public String connect(String url) {
                try{
                    Request request = new Request.Builder().url(url).build();
                    client = new OkHttpClient();
                    webSocket = client.newWebSocket(request, new WebSocketListener() {
                        @Override
                        public void onOpen(WebSocket webSocket, Response response) {
                            // 连接成功后发送消息
                            webSocket.send(msg);
                            System.out.println("Sent message: " + msg);
                        }
                        @Override
                        public void onMessage(WebSocket webSocket, String text) {
                            // 接收到服务器的msg
                            try {
                                Handler handler = new Handler(Looper.getMainLooper());
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        // 将结果传递给UI线程
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {//在这里对传入的字符串进行处理并且添加控件
                                                Log.e("Server return ",text);
                                                deal_msg_from_server(text,"search");
                                            }
                                        });
                                    }
                                }).start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("Received message: " + text);
                            re=text;
                            disconnect();
                        }
                        @Override
                        public void onClosed(WebSocket webSocket, int code, String reason) {
                            // 连接关闭
                            System.out.println("Connection closed");
                        }

                        @Override
                        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
                            // 连接失败
                            System.out.println("Connection failed");
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainPageActivity.this,"无法连接到服务器",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }catch (Exception e){
                    Log.e("t",e.getMessage().toString());
                }
                return re;
            }

            public void disconnect() {
                if (webSocket != null) {
                    webSocket.close(1000, null);
                }
            }
        }
        public void search(String message){
            new ServerConnect.WebSocketClient(message).connect("ws://116.204.83.200:8629");
        }
    }
    //用于解析服务器传回的字符串的函数
    void deal_msg_from_server(String msg, @Nullable String which){
        String name,author,url;
        music_item_container.removeAllViews();
        String[] songs_msg=msg.split("\\|OUTERDIVIDE\\|");
        for(int i=0;i<songs_msg.length;i++){
            try{
                String[] song_msg=songs_msg[i].split("\\|INNERDIVIDE\\|");
                name=song_msg[0];
                author=song_msg[2];
                url=song_msg[1];
                music_item_container.addView(new SongItem(MainPageActivity.this,name,author,url,which));
            }catch (Exception e){

            }
        }
    }
    void init_search_page(){
        seekBar=findViewById(R.id.music_play_progress);
        search_one_song_circle_button=findViewById(R.id.search_page_song_circle_button);
        search_song_list_circle_button=findViewById(R.id.search_page_list_circle_button);
        search_song_play_list_button=findViewById(R.id.search_page_play_list_button);
        music_item_container=findViewById(R.id.song_item_container_linear);
        player_bar_title_text_view=findViewById(R.id.search_list_page_play_bar_song_msg_view);
        search_button=findViewById(R.id.list_search_search_button);
        search_input=findViewById(R.id.list_search_search_content_input);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(search_input.getVisibility()==View.INVISIBLE){
                    search_input.setVisibility(View.VISIBLE);
                }else{
                    String content=search_input.getText().toString();
                    if(content.equals("")){
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainPageActivity.this,"请输入内容",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }else{
                        serverConnect.search(content);
                    }
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //haha do nothing
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(is_prepared){
                    mediaPlayer.pause();
                }else{
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(is_prepared){
                    mediaPlayer.seekTo((int)(((double)seekBar.getProgress()/1000)*mediaPlayer.getDuration()));
                    mediaPlayer.start();
                }else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainPageActivity.this,"无可播放的资源",Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        search_one_song_circle_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(circle_mode==Circle_modes.ONE_CIRCLE){
                    //
                }else{

                    search_song_list_circle_button.setImageResource(R.drawable.song_list_circle_icon);
                    search_one_song_circle_button.setImageResource(R.drawable.one_song_circle_purple_icon);
                    circle_mode=Circle_modes.ONE_CIRCLE;
                }
            }
        });
        search_song_list_circle_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(circle_mode==Circle_modes.ONE_CIRCLE){
                    search_song_list_circle_button.setImageResource(R.drawable.song_list_circle_purple_icon);
                    search_one_song_circle_button.setImageResource(R.drawable.one_song_circle_icon);
                    circle_mode=Circle_modes.LIST_CIRCLE;
                }else{

                }
            }
        });
        search_song_play_list_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deal_msg_from_server(sharedPreferences.getString("play_list_string",""),"play_list");
            }
        });
    }
    void init_home_page(){
        home_page_horizontal_linear=findViewById(R.id.home_page_song_list_horizontal_container);
        home_page_vertical_linear=findViewById(R.id.home_page_song_list_vertical_container);
        home_page_search_button=findViewById(R.id.home_page_search_button);
        home_page_main_image=findViewById(R.id.home_page_main_image_view);
        home_page_search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                search_input.callOnClick();
                search_input.callOnClick();
            }
        });
        Glide.with(MainPageActivity.this)
                .load("http://39.101.160.55/ad/kid.jpg")
                .apply(RequestOptions.bitmapTransform(new RoundedCorners(14))) // 设置圆角半径
                .into(home_page_main_image);

    }
    //using to update the song list
    public String add_to_list(String name,String author,String url,String which){
        String msg=sharedPreferences.getString(which,"");
        String re=msg;
        if(msg.length()==0){
            msg+=name+"|INNERDIVIDE|"+url+"|INNERDIVIDE|"+author;
        }else{
            msg+="|OUTERDIVIDE|"+name+"|INNERDIVIDE|"+url+"|INNERDIVIDE|"+author;
        }
        SharedPreferences.Editor editor1=sharedPreferences.edit();
        editor1.putString(which,msg);
        editor1.apply();
        return re;
    }
    public String delete_from_list(String name,String author,String url,String which){
        SharedPreferences sharedPreferences1=getSharedPreferences("MSG",MODE_PRIVATE);
        String msg=sharedPreferences1.getString(which,"");
        String re=msg;
        String song=name+"|INNERDIVIDE|"+url+"|INNERDIVIDE|"+author;
        String[] songs_list=msg.split("\\|OUTERDIVIDE\\|");
        int p=-1;
        for(int i=0;i<songs_list.length;i++){
            if(songs_list[i].equals(song)){
                p=i;
                break;
            }
        }
        re="";
        for(int i=0;i<songs_list.length;i++){
            if(i!=p&&i!=songs_list.length-1){
                re+=songs_list[i]+"|OUTERDIVIDE|";
            }else if(i!=p&&i==songs_list.length-1){
                re+=songs_list[i];
            }else{
                continue;
            }
        }
        SharedPreferences.Editor editor1=sharedPreferences1.edit();
        editor1.putString(which,re);
        editor1.apply();
        return re;
    }
    void init_self_page(){
        self_page_name_text_view=findViewById(R.id.self_page_name_text_view);
        self_page_name_text_view.setText(sharedPreferences.getString("nick_name","Visitor"));
        self_page_downloaded_button=findViewById(R.id.self_page_downloaded_button);
        self_page_song_list_button=findViewById(R.id.self_page_song_list_button);
        self_page_recent_button=findViewById(R.id.self_page_recent_button);
        self_page_stared_button=findViewById(R.id.self_page_stared_button);
        self_settings_button=findViewById(R.id.self_setting_button);
        self_settings_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(MainPageActivity.this,SettingActivity.class);
                startActivity(intent);
            }
        });
        self_page_stared_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                deal_msg_from_server(sharedPreferences.getString("stared_songs_string",""),"stared");
            }
        });
        self_page_downloaded_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                deal_msg_from_server(sharedPreferences.getString("downloaded_songs_string",""),"downloaded");
            }
        });
        self_page_recent_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(1);
                deal_msg_from_server(sharedPreferences.getString("recent_songs_string",""),"recent");
            }
        });
        self_page_song_list_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder builder=new AlertDialog.Builder(MainPageActivity.this);
                        builder.setMessage("该功能仍未开放");
                        builder.show();
                    }
                });
            }
        });
    }
















    //
    void load_song_item(){
        for(int i=0;i<8;i++){
            home_page_horizontal_linear.addView(new SongListItem(Tmp.titles.get(i),Tmp.times.get(i),Tmp.urls.get(i),null));
        }
        for(int i=8;i<Tmp.titles.size();i++){
            home_page_vertical_linear.addView(new SongListItem(Tmp.titles.get(i),Tmp.times.get(i),Tmp.urls.get(i),null),true);
        }
    }
    public class SongListItem extends LinearLayout{
        private ImageView imageView;
        private TextView titletextView;
        private TextView statustextView;
        String image_url;
        String title;
        String more_msg;
        String url;
        public SongListItem(String title1, String more_msg1, String img_url1, String page_url1){
            super(MainPageActivity.this);
            LayoutInflater inflater = LayoutInflater.from(getContext());
            View view = inflater.inflate(R.layout.home_page_song_list_item, this, true);
            imageView = view.findViewById(R.id.item_image_view);
            titletextView = view.findViewById(R.id.item_title);
            statustextView=view.findViewById(R.id.item_status);
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v){

                }
            });
            int i=0;
            title=title1;
            more_msg=more_msg1;
            image_url=img_url1;
            url=page_url1;
            titletextView.setText(title);
            statustextView.setText(more_msg);
            Picasso.get().load(image_url).into(imageView);
        }
    }


    public class CheckUpdate {

        public class DownloadApkAsyncTask extends AsyncTask<String, Integer, String> {
            private static final String TAG = "DownloadApkAsyncTask";

            private Context context;
            private ProgressDialog progressDialog;

            public DownloadApkAsyncTask(Context context) {
                this.context = context;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // 在任务执行前显示进度对话框
                progressDialog = new ProgressDialog(context);
                progressDialog.setMessage("正在下载，请稍候...");
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();
            }

            @Override
            protected String doInBackground(String... params) {
                String apkUrl = params[0];
                try {
                    URL url = new URL(apkUrl);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.connect();

                    int fileLength = connection.getContentLength(); // 获取文件长度
                    InputStream inputStream = connection.getInputStream();
                    File apkDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    if (!apkDirectory.exists()) { // 如果下载目录不存在，则创建
                        apkDirectory.mkdirs();
                    }
                    File outputFile = new File(apkDirectory, "kk_music.apk"); // 保存的文件名为 chatplus.apk
                    FileOutputStream outputStream = new FileOutputStream(outputFile);

                    byte[] buffer = new byte[1024];
                    int len;
                    long total = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        total += len;
                        publishProgress((int) (total * 100 / fileLength)); // 发布进度更新
                        outputStream.write(buffer, 0, len);
                    }

                    outputStream.flush();
                    outputStream.close();
                    inputStream.close();

                    return outputFile.getPath(); // 返回文件路径
                } catch (IOException e) {
                    Log.e(TAG, "下载失败", e);
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                super.onProgressUpdate(values);
                // 更新进度对话框的进度
                progressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(String apkFilePath) {
                super.onPostExecute(apkFilePath);
                // 获取最新下载的文件
                String latestDownloadedFilePath = getLatestDownloadedFilePath(context);

                if (latestDownloadedFilePath != null) {
                    File apkFile = new File(latestDownloadedFilePath);
                    Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", apkFile);


                    //调用安装程序
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    }
                    Intent installIntent = new Intent(Intent.ACTION_INSTALL_PACKAGE);
                    installIntent.setData(apkUri);
                    installIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    try {
                        context.startActivity(installIntent); // 启动安装程序
                    } catch (ActivityNotFoundException e) {
                        // 处理未找到可用安装程序的情况
                        Toast.makeText(context, "找不到安装程序", Toast.LENGTH_SHORT).show();
                    } catch (SecurityException e) {
                        // 处理权限问题
                        Toast.makeText(context, "没有安装权限，请手动安装", Toast.LENGTH_SHORT).show();
                    } catch (NullPointerException e) {
                        // 处理空指针异常
                        Toast.makeText(context, "找不到文件", Toast.LENGTH_SHORT).show();
                    } catch (IllegalArgumentException e) {
                        // 处理非法参数异常
                        Toast.makeText(context, "找不到文件", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "找不到最新下载的文件", Toast.LENGTH_SHORT).show();
                }

                // 关闭进度对话框
                progressDialog.dismiss();
            }

            /**
             * 获取最新下载的文件路径
             *
             * @param context 上下文对象
             * @return 最新下载的文件路径，如果找不到则返回 null
             */
            private String getLatestDownloadedFilePath(Context context) {
                String apkDirectoryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath();
                File apkDirectory = new File(apkDirectoryPath);
                File[] files = apkDirectory.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".apk");
                    }
                });

                if (files != null && files.length > 0) {
                    Arrays.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File file1, File file2) {
                            long lastModified1 = file1.lastModified();
                            long lastModified2 = file2.lastModified();
                            return Long.compare(lastModified2, lastModified1);
                        }
                    });
                    return files[0].getPath();
                }

                return null;
            }
        }

        public int versioncode;
        private String versionname;
        public String new_version;

        public class CheckVersionTask extends AsyncTask<String, Integer, String> {
            @Override
            protected void onPreExecute() {

            }

            @Override
            protected String doInBackground(String... strings) {
                URL versionUrl = null;
                try {
                    versionUrl = new URL(latest_version_url);
                } catch (MalformedURLException e) {
                    throw new RuntimeException(e);
                }
                HttpURLConnection connection = null;
                try {
                    connection = (HttpURLConnection) versionUrl.openConnection();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                try {
                    connection.setRequestMethod("GET");
                    connection.setConnectTimeout(5000);
                    connection.setReadTimeout(5000);
                } catch (ProtocolException e) {
                    throw new RuntimeException(e);
                }
                try {
                    connection.setReadTimeout(3000);
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream inputStream = connection.getInputStream();
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader((inputStream)));
                        StringBuilder stringBuilder = new StringBuilder();
                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            stringBuilder.append(line);
                        }
                        bufferedReader.close();
                        inputStream.close();
                        JSONObject jsonObject = new JSONObject(stringBuilder.toString());
                        new_version = jsonObject.getString("version");
                        Log.e("version_new",new_version);
                        versioncode = BuildConfig.VERSION_CODE;
                        versionname = getString(R.string.version);
                        Log.e("version_local",versionname);
                        if (versionname.equals(new_version)) {
                            return "no new version";
                        } else {
                            return "new version";
                        }
                    }
                } catch (IOException e) {
                    return "time out";
                } catch (JSONException e) {
                    return "version check error";
                }
                return "nothing";
            }

            public void showConnectionErrorDialog() {
                Toast.makeText(MainPageActivity.this, "网络连接出错", Toast.LENGTH_SHORT).show();
            }
            public void showConfirmDialog() {
                Toast.makeText(MainPageActivity.this, "发现新版本", Toast.LENGTH_SHORT).show();
                AlertDialog.Builder builder = new AlertDialog.Builder(MainPageActivity.this);
                builder.setTitle("有新的版本可以更新");
                builder.setMessage("当前版本为" + versionname + ",最新版本为" + new_version);
                builder.setPositiveButton("更新", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        /*MainActivity.CheckUpdate checkUpdate = new MainActivity.CheckUpdate();
                        MainActivity.CheckUpdate.DownloadApkAsyncTask downloadApkAsyncTask = checkUpdate.new DownloadApkAsyncTask(MainActivity.this);
                        downloadApkAsyncTask.execute(download_url);*/
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(download_url));
// 检查是否有应用程序能够处理该 Intent
                        if (intent.resolveActivity(getPackageManager()) != null) {
                            // 启动 Intent，打开浏览器并跳转到指定网页
                            startActivity(intent);
                            finish();
                        } else {
                            // 如果没有应用程序能够处理该 Intent，你可以提供备选方案，比如提示用户安装浏览器应用程序
                            Toast.makeText(MainPageActivity.this, "No application available to handle this request", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                builder.setCancelable(false);
                builder.show();
            }

            public void showDialogNewest() {
                Toast.makeText(MainPageActivity.this, "当前为最新版本", Toast.LENGTH_SHORT).show();
            }

            public void showDialogSourceError() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainPageActivity.this);
                builder.setTitle("检查版本错误，请联系开发者邮箱tor108@outlook.com");
                builder.setPositiveButton("确认", null);
                builder.show();
            }

            @Override
            protected void onPostExecute(String result) {
                if (result.equals("no new version")) {
                    showDialogNewest();
                } else if (result.equals("time out")) {
                    showConnectionErrorDialog();
                } else if (result.equals("version check error")) {
                    showDialogSourceError();
                } else if (result.equals("nothing")) {
                    showDialogSourceError();
                } else if (result.equals("new version")) {
                    showConfirmDialog();
                }
            }
        }
    }
}