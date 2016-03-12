package com.example.alexpotter.movies;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    protected String movieTitle;
    protected String searchText;
    protected int filmId;
    protected List<String> searchResults = new ArrayList<>();
    protected SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Gets the data repository in write mode
        FavouritesSchema.Favourite.FavouriteDbHelper mDbHelper = new FavouritesSchema.Favourite.FavouriteDbHelper(this);
        db = mDbHelper.getWritableDatabase();

        if (db.isOpen()) {
            Log.d("Debug", "Successfully connected to favourites db");
        }

        myActivity();
    }

    private void myActivity() {
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        final AutoCompleteTextView searchEditText = (AutoCompleteTextView)findViewById(R.id.autocomplete_movie);

        TextView header = (TextView) findViewById(R.id.favouritesHeader);
        header.setText("Favourites");

        LinearLayout favouriteList = (LinearLayout) findViewById(R.id.favourites);
        favouriteList.setClickable(true);

        // Get favourites
        String[] projection = {
                FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID,
                FavouritesSchema.Favourite.COLUMN_NAME_FILM_TITLE,
                FavouritesSchema.Favourite.COLUMN_NAME_IMAGE_URL,
        };

        Cursor c = db.query(
                FavouritesSchema.Favourite.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        while (c.moveToNext()) {
            View filmItem = LayoutInflater.from(MainActivity.this).inflate(R.layout.favourite, favouriteList, false);

            TextView title  = (TextView) filmItem.findViewById(R.id.favouriteTitle);
            title.setText(c.getString(c.getColumnIndex(FavouritesSchema.Favourite.COLUMN_NAME_FILM_TITLE)));

            ImageView imgPoster = (ImageView) filmItem.findViewById(R.id.favouritePoster);
            Picasso.with(getApplicationContext()).load(c.getString(c.getColumnIndex(FavouritesSchema.Favourite.COLUMN_NAME_IMAGE_URL))).into(imgPoster);

            filmItem.setId(c.getColumnIndex(FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID));
            filmItem.setClickable(true);

            filmItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d("ID", "" + v.getId());
                    Log.d("DEBUG", "CLICKED");
                }
            });

            favouriteList.addView(filmItem);
        }

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ArrayAdapter<String> adaptor = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_list_item_1, searchResults);
                searchEditText.setAdapter(adaptor);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                searchText = s.toString();
                new getFilmsWhileTyping().execute();
            }
        });

        Button button = (Button) findViewById(R.id.button_search);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                movieTitle = searchEditText.getText().toString();
                new getFilmsFromSearch().execute();
            }
        });
    }

    private void displayFilm(JSONObject film, LinearLayout view) {
        try {
            View filmItem = LayoutInflater.from(MainActivity.this).inflate(R.layout.display_film, null, false);

            TextView title = (TextView) filmItem.findViewById(R.id.filmTitle);
            title.setText(film.getString("title"));

            TextView plot = (TextView) filmItem.findViewById(R.id.filmPlot);
            plot.setText(film.getString("overview"));

            TextView year = (TextView) filmItem.findViewById(R.id.filmYear);
            year.setText("Released: " + film.getString("release_date"));

            ImageView imgPoster = (ImageView) filmItem.findViewById(R.id.filmPoster);
            Picasso.with(getApplicationContext()).load("http://image.tmdb.org/t/p/w185/" + film.getString("poster_path")).into(imgPoster);

            final ImageView imgLike = (ImageView) filmItem.findViewById(R.id.filmLike);

            // Check film is favourite
            Cursor cursor = db.query(
                    FavouritesSchema.Favourite.TABLE_NAME,
                    new String[]{FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID},
                    FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID + " = " + film.getString("id"),
                    null,
                    null,
                    null,
                    null
            );

            if (cursor.getCount() > 0) {
                imgLike.setImageResource(R.drawable.full_heart);
            } else {
                imgLike.setImageResource(R.drawable.empty_heart);
            }

            imgLike.setId(film.getInt("id"));

            // Set event listener
            imgLike.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Cursor c = db.query(
                            FavouritesSchema.Favourite.TABLE_NAME,
                            new String[]{FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID},
                            FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID + " =? ",
                            new String[]{String.valueOf(Integer.toString(v.getId()))},
                            null,
                            null,
                            null
                    );

                    if (c.getCount() == 0) {
                        filmId = v.getId();
                        new storeFavourite().execute();
                        imgLike.setImageResource(R.drawable.full_heart);
                    } else {
                        filmId = v.getId();
                        db.delete(
                                FavouritesSchema.Favourite.TABLE_NAME,
                                FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID + " =? ",
                                new String[]{String.valueOf(Integer.toString(filmId))}
                        );
                        imgLike.setImageResource(R.drawable.empty_heart);
                    }
                }
            });

            view.addView(filmItem);
        }
        catch (JSONException e) {

        }
        catch (Exception e) {

        }
    }

    class getFilmsWhileTyping extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... Params)  {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url("https://api.themoviedb.org/3/search/movie?api_key=1888c83e4f4bbe98ecf4973b7db0f7c4&query=" + searchText)
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
                    for (int count = 0; count < length; count++) {
                        JSONObject film = films.getJSONObject(count);

                        if(! searchResults.contains(film.getString("title"))) {
                            searchResults.add(count, film.getString("title"));
                        }
                    }

                }
                catch (JSONException e) {

                }
                catch (Exception e) {

                }
            }
        }
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
                            //What to do on back clicked
                            myActivity();
                        }
                    });

                    int length = jsonObject.length();

                    //loop to get all json objects from data json array
                    for(int count=0; count<length; count++)
                    {
                        final JSONObject film = films.getJSONObject(count);

                        displayFilm(film, filmList);
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
                            //What to do on back clicked
                            myActivity();
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
                    LinearLayout filmList = (LinearLayout)findViewById(R.id.tableLayout);
                    JSONObject film = new JSONObject(result);
                    displayFilm(film, filmList);
                }
                catch (JSONException e) {

                }
            }
        }
    }

    class storeFavourite extends AsyncTask<Void, Void, String> {
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
                    JSONObject film = new JSONObject(result);

                    ContentValues values = new ContentValues();
                    values.put(FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID, filmId);
                    values.put(FavouritesSchema.Favourite.COLUMN_NAME_FILM_TITLE, film.getString("title"));
                    values.put(FavouritesSchema.Favourite.COLUMN_NAME_IMAGE_URL, "http://image.tmdb.org/t/p/w185/" + film.getString("poster_path"));

                    db.insert(
                            FavouritesSchema.Favourite.TABLE_NAME,
                            null,
                            values
                    );

                } catch (JSONException e) {
                    
                }
            }
        }
    }
}
