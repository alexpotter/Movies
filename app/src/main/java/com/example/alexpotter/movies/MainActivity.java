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

            HttpUrl url = new HttpUrl.Builder()
                    .scheme("http")
                    .host("www.omdbapi.com")
                    .addQueryParameter("t", movieTitle)
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
                    final String imdbId = json.getString("imdbID");

                    if (json.getString("Response").equals("false"))
                    {
                        Log.d("DEBUG", "No movie found");
                        return;
                    }

                    Log.d("DEBUG", title);
                    Log.d("DEBUG", year);
                    Log.d("DEBUG", plot);
                    Log.d("DEBUG", imdbId);

                    setContentView(R.layout.display_film);

                    final TextView movieTitle = (TextView) findViewById(R.id.filmTitle);
                    movieTitle.setText("Film: " + title);

                    final TextView movieYear = (TextView) findViewById(R.id.filmYear);
                    movieYear.setText("Year: " + year);

                    final TextView moviePlot = (TextView) findViewById(R.id.filmPlot);
                    moviePlot.setText("Plot: " + plot);

                    GridLayout movieLayout = (GridLayout) findViewById(R.id.movieContainer);
                    movieLayout.computeScroll();  // replace 100 with your dimensions

                    // NEED TO ALLOW INFINITE SCROLLING
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
