package com.example.alexpotter.movies;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ListFilmsActivity extends AppCompatActivity {
    protected String movieTitle;
    protected SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        movieTitle = intent.getStringExtra("movieTitle");

        // Gets the data repository in write mode
        FavouritesSchema.Favourite.FavouriteDbHelper mDbHelper = new FavouritesSchema.Favourite.FavouriteDbHelper(this);
        db = mDbHelper.getWritableDatabase();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.display_films);

        new getFilmsFromSearch().execute();
    }

    class getFilmsFromSearch extends AsyncTask<Void, Void, String> {
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
            try {
                JSONObject jsonObject = new JSONObject(results);

                //extracting data array from json string
                JSONArray films = jsonObject.getJSONArray("results");

                if (films.length() > 0) {
                    setContentView(R.layout.display_films);
                    LinearLayout filmList = (LinearLayout)findViewById(R.id.tableLayout);

                    Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar1);
                    setSupportActionBar(myToolbar);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                    myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent main = new Intent(ListFilmsActivity.this, MainActivity.class);
                            ListFilmsActivity.this.startActivity(main);
                        }
                    });

                    int length = jsonObject.length();

                    //loop to get all json objects from data json array
                    for(int count=0; count<length; count++)
                    {
                        final JSONObject jsonFilmObject = films.getJSONObject(count);

                        Film film = new Film(
                                jsonFilmObject.getString("id"),
                                jsonFilmObject.getString("title"),
                                jsonFilmObject.getString("overview"),
                                jsonFilmObject.getString("release_date"),
                                jsonFilmObject.getString("poster_path")
                        );

                        film.setDbConnection(db);

                        filmList.addView(film.buildFilm(ListFilmsActivity.this, getApplicationContext()));
                    }
                }
                else
                {
                    setContentView(R.layout.no_movie_found);
                    TextView noMovieFound = (TextView)findViewById(R.id.noMovieFound);

                    Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar2);
                    setSupportActionBar(myToolbar);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                    myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent main = new Intent(ListFilmsActivity.this, MainActivity.class);
                            ListFilmsActivity.this.startActivity(main);
                        }
                    });

                    String error = "No films could be found.";
                    noMovieFound.setText(error);
                }
            }
            catch(JSONException e) {

            }
            catch(Exception e) {

            }
        }
    }
}
