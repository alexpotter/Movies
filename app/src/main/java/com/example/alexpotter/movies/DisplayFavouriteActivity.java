package com.example.alexpotter.movies;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class DisplayFavouriteActivity extends AppCompatActivity {
    protected int filmId;
    protected SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Gets the data repository in write mode
        FavouritesSchema.Favourite.FavouriteDbHelper mDbHelper = new FavouritesSchema.Favourite.FavouriteDbHelper(this);
        db = mDbHelper.getWritableDatabase();

        Intent intent = getIntent();
        filmId = intent.getIntExtra("filmId", 0);

        setContentView(R.layout.display_films);
        new viewSelectedFilm().execute();
    }

    class viewSelectedFilm extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... Params)  {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("http://api.themoviedb.org/3/movie/" + filmId + "?api_key=1888c83e4f4bbe98ecf4973b7db0f7c4")
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
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    setContentView(R.layout.display_films);

                    Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar1);
                    setSupportActionBar(myToolbar);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                    myToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent main = new Intent(DisplayFavouriteActivity.this, MainActivity.class);
                            DisplayFavouriteActivity.this.startActivity(main);
                        }
                    });

                    LinearLayout filmList = (LinearLayout)findViewById(R.id.tableLayout);
                    JSONObject jsonFilmObject = new JSONObject(result);

                    Film film = new Film(
                            jsonFilmObject.getString("id"),
                            jsonFilmObject.getString("title"),
                            jsonFilmObject.getString("overview"),
                            jsonFilmObject.getString("release_date"),
                            jsonFilmObject.getString("poster_path")
                    );

                    film.setDbConnection(db);

                    filmList.addView(film.buildFilm(DisplayFavouriteActivity.this, getApplicationContext()));
                }
                catch (JSONException e) {

                }
            }
        }
    }
}
