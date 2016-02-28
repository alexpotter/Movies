package com.example.alexpotter.movies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONObject;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        OkHttpClient client = new OkHttpClient();

        HttpUrl url = new HttpUrl.Builder()
                .scheme("http")
                .host("www.omdbapi.com")
                .addQueryParameter("t", "Frozen")
                .build();

        Request request = new Request.Builder()
                .url(url.toString())
                .build();

        Log.i("DEBUG", url.toString());

        try {
            Response response = client.newCall(request).execute();
            String responseData = response.body().string();
            JSONObject json = new JSONObject(responseData);
            final String title = json.getString("Title");

            Log.i("DEBUG", title);
        }
        catch (Exception e) {
            Log.i("DEBUG", e.getMessage());
        }
    }
}
