package com.example.alexpotter.movies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    protected String movieTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get a reference to the AutoCompleteTextView in the layout
        final AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.autocomplete_movie);
        // Get the string array
        // Change to sql lite db or api if poss
        String[] movies = getResources().getStringArray(R.array.movies_array);
        // Create the adapter and set it to the AutoCompleteTextView
        ArrayAdapter<String> adapter =
                new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, movies);
        textView.setAdapter(adapter);

        Button button = (Button) findViewById(R.id.button_search);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                movieTitle = textView.getText().toString();
                new MyAsyncTask().execute();
            }
        });
    }

    class MyAsyncTask extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://api.themoviedb.org/3/search/movie?api_key=1888c83e4f4bbe98ecf4973b7db0f7c4&query=" + movieTitle)
                    .build();

            try {
                Response response = client.newCall(request).execute();
                return response.body().string();
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
                    JSONObject jsonObject = new JSONObject(results);

                    //extracting data array from json string
                    JSONArray films = jsonObject.getJSONArray("results");
                    int length = jsonObject.length();

                    //loop to get all json objects from data json array
                    for(int count=0; count<length; count++)
                    {
                        JSONObject film = films.getJSONObject(count);

                        Log.d("DEBUG", film.getString("title"));
                        Log.d("DEBUG", film.getString("release_date"));
                        Log.d("DEBUG", film.getString("id"));
                    }
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
