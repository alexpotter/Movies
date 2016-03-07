package com.example.alexpotter.movies;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
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
                    setContentView(R.layout.display_films);
                    LinearLayout filmList = (LinearLayout)findViewById(R.id.tableLayout);

                    JSONObject jsonObject = new JSONObject(results);

                    //extracting data array from json string
                    JSONArray films = jsonObject.getJSONArray("results");
                    int length = jsonObject.length();

                    //loop to get all json objects from data json array
                    for(int count=0; count<length; count++)
                    {
                        JSONObject film = films.getJSONObject(count);

                        View filmItem = LayoutInflater.from(MainActivity.this).inflate(R.layout.display_film, null, false);

                        TextView title  = (TextView) filmItem.findViewById(R.id.filmTitle);
                        title.setText(film.getString("title"));

                        TextView plot  = (TextView) filmItem.findViewById(R.id.filmPlot);
                        plot.setText(film.getString("overview"));

                        TextView year  = (TextView) filmItem.findViewById(R.id.filmYear);
                        year.setText("Released: " + film.getString("release_date"));

                        ImageView img = (ImageView) filmItem.findViewById(R.id.filmPoster);
                        Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/w185/" + film.getString("poster_path")).into(img);

                        filmList.addView(filmItem);

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
