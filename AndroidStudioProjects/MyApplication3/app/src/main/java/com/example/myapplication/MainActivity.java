package com.example.myapplication;





import static androidx.core.app.ActivityCompat.startActivityForResult;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.view.GestureDetectorCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.squareup.picasso.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

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
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
public class MainActivity extends AppCompatActivity {
    private String latest_version_url = "http://39.101.160.55/download_app_source/kk_music_version.json";
    private String download_url = "http://39.101.160.55/download_app_source/kk_music.apk";
    private static final int INSTALL_APK_REQUEST_CODE = 1;
    private Button login_button;
    private EditText passwd_input;
    private RelativeLayout init_view;
    private String passwd = "bqg1412***";
    private boolean if_updated = false;
    private String nick_name = "visitor";
    private ProgressBar loading_view;
    private EditText nick_name_input;
    SharedPreferences sharedPreferences;
    private Boolean if_first_check = true;
    private String op_type = "login";//设置操作类型
    private Button register_button;

    //检查登陆的状态
    public boolean check_status() {
        String check_result = new DataBaseConnect(MainActivity.this).op_sql("sql " + op_type + " " + nick_name + " " + passwd);
        if (check_result != null && check_result.equals("successed")) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_view=findViewById(R.id.main_page_initial_view);
        sharedPreferences = getSharedPreferences("MSG", MODE_PRIVATE);
        nick_name = sharedPreferences.getString("nick_name", "visitor");
        passwd = sharedPreferences.getString("passwd", "bqg1412***");
        register_button = findViewById(R.id.main_register_button);
        nick_name_input = findViewById(R.id.main_nick_name_text_input);
        login_button = findViewById(R.id.main_login_button);
        passwd_input = findViewById(R.id.main_passwd_text_input);
        loading_view = findViewById(R.id.main_page_progress_bar);
        register_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                op_type = "register";
                loading_view.setVisibility(View.VISIBLE);
                passwd = String.valueOf(passwd_input.getText());
                nick_name = String.valueOf(nick_name_input.getText());
                if (passwd.contains(" ") || passwd.equals("")) {
                    Toast.makeText(MainActivity.this, "密码不可为空或包含空格", Toast.LENGTH_SHORT).show();
                    loading_view.setVisibility(View.GONE);
                    return;
                }
                if (nick_name.contains(" ") || nick_name.equals("")) {
                    Toast.makeText(MainActivity.this, "昵称不可为空或包含空格", Toast.LENGTH_SHORT).show();
                    loading_view.setVisibility(View.GONE);
                    return;
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //进行登陆
                        new LoginAsyncTask().execute();
                        //如果对应的用户名不包含空
                                /*if(!nick_name_input.getText().toString().contains(" ")){
                                        SharedPreferences.Editor editor= sharedPreferences.edit();
                                        editor.putString("nick_name",nick_name_input.getText().toString());
                                        editor.apply();
                                }
                                else{
                                    Toast.makeText(MainActivity.this,"昵称不可以包含空格",Toast.LENGTH_SHORT).show();
                                }*/
                    }
                }, 500);
            }
        });
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                op_type = "login";
                loading_view.setVisibility(View.VISIBLE);
                passwd = String.valueOf(passwd_input.getText());
                nick_name = String.valueOf(nick_name_input.getText());
                if (passwd.contains(" ") || passwd.equals("")) {
                    Toast.makeText(MainActivity.this, "密码不可为空或包含空格", Toast.LENGTH_SHORT).show();
                    loading_view.setVisibility(View.GONE);
                    return;
                }
                if (nick_name.contains(" ") || nick_name.equals("")) {
                    Toast.makeText(MainActivity.this, "昵称不可为空或包含空格", Toast.LENGTH_SHORT).show();
                    loading_view.setVisibility(View.GONE);
                    return;
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //进行登陆
                        new LoginAsyncTask().execute();
                        //如果对应的用户名不包含空
                                /*if(!nick_name_input.getText().toString().contains(" ")){
                                        SharedPreferences.Editor editor= sharedPreferences.edit();
                                        editor.putString("nick_name",nick_name_input.getText().toString());
                                        editor.apply();
                                }
                                else{
                                    Toast.makeText(MainActivity.this,"昵称不可以包含空格",Toast.LENGTH_SHORT).show();
                                }*/
                    }
                }, 1000);
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new LoginAsyncTask().execute();
            }
        }, 1000);
        String saved_version = sharedPreferences.getString("version", "0.0.0");
        String now_version = BuildConfig.VERSION_NAME.toString();
        if (!saved_version.equals(now_version)) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("version", now_version);
            editor.apply();
            if_updated = true;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    check_status();
                }
            });
        }
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

                if (networkInfo != null && networkInfo.isConnected()) {
                    // 设备已连接到网络，可以联网
                } else {
                    Intent intent=new Intent(MainActivity.this,MainPageActivity.class);
                    Toast.makeText(MainActivity.this,"离线进入",Toast.LENGTH_SHORT).show();
                    startActivity(intent);
                }

            }
        },100);
    }

    public class LoginAsyncTask extends AsyncTask<Void, Integer, String> {
        private String re;

        @Override
        protected void onPreExecute() {
            // 在UI线程中执行准备操作，如弹出进度条等
            loading_view.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            // 在后台线程中执行具体的耗时操作，如网络请求、数据库查询等
            // 可以使用publishProgress()来更新进度条等UI控件的状态
            if (check_status()) {
                return "ok";
            } else {
                return "no";
            }
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            // 在UI线程中更新进度条等UI控件的状态
        }

        @Override
        protected void onPostExecute(String s) {
            // 在UI线程中执行一些操作，如更新UI界面、显示结果等
            loading_view.setVisibility(View.GONE);

        }
    }

    public class DataBaseConnect {
        private Context context;
        private String re;

        public DataBaseConnect(Context contex) {
            context = contex;
        }

        public String op_sql(String msg) {
            return new DataBaseConnect.WebSocketClient(msg).connect("ws://116.204.83.200:8627/");
        }

        public class WebSocketClient {
            private OkHttpClient client;
            private String msg;
            private WebSocket webSocket;

            WebSocketClient(String message) {
                msg = message;
            }

            public String connect(String url) {
                try {
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
                                            public void run() {
                                                if (text.equals("successed")) {
                                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                                    //记住对应的用户名与密码
                                                    if (if_first_check) {//如果是对应的检查，不做任何改变
                                                        Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
                                                        startActivity(intent);
                                                    } else {
                                                        editor.putString("nick_name", nick_name_input.getText().toString());
                                                        editor.putString("passwd", passwd_input.getText().toString());
                                                        editor.apply();
                                                        Intent intent = new Intent(MainActivity.this, MainPageActivity.class);
                                                        startActivity(intent);
                                                        Toast.makeText(MainActivity.this, "SAVED", Toast.LENGTH_SHORT).show();
                                                    }
                                                } else if (text.equals("failed")) {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            init_view.setVisibility(View.GONE);
                                                        }
                                                    });
                                                    if_first_check = false;
                                                    Toast.makeText(MainActivity.this, "验证信息错误,请重试", Toast.LENGTH_SHORT).show();
                                                } else if (text.contains("error")) {
                                                    handler.post(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            init_view.setVisibility(View.GONE);
                                                        }
                                                    });
                                                    if_first_check = false;
                                                    Toast.makeText(MainActivity.this, "用户名重复", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                }).start();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            System.out.println("Received message: " + text);
                            re = text;
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
                        }
                    });
                } catch (Exception e) {
                    Log.e("t", e.getMessage().toString());
                }
                return re;
            }

            public void disconnect() {
                if (webSocket != null) {
                    webSocket.close(1000, null);
                }
            }
        }
    }
}



/*
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {

    Handler handler=new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent=new Intent(MainActivity.this,MainPageActivity.class);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                startActivity(intent);
            }
        },1500);
    }
}*/