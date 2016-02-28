package com.example.alexpotter.movies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        new MyAsyncTask().execute();
    }

    class MyAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            HttpUrl url = new HttpUrl.Builder()
                    .scheme("http")
                    .host("www.omdbapi.com")
                    .addQueryParameter("t", "Inception")
                    .build();

            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Log.i("DEBUG", url.toString());

            try {
                Response response = client.newCall(request).execute();
                String responseData = response.body().string();

                return responseData;
            }
            catch (IOException e) {

            }
            catch (Exception e) {

            }
            return null;
        }

        @Override
        protected void onPostExecute(String results) {
            if (results != null) {
                try {
                    JSONObject json = new JSONObject(results);
                    final String title = json.getString("Title");
                    final String year = json.getString("Year");
                    final String plot = json.getString("Plot");

                    Log.d("DEBUG", title);
                    Log.d("DEBUG", year);
                    Log.d("DEBUG", plot);
                }
                catch (JSONException e) {

                }
            }
            else
            {
                // Catch no movies found;
            }
        }
    }
}
