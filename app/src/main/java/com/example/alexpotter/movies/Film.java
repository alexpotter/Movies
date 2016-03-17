package com.example.alexpotter.movies;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class Film {
    protected String id;
    protected String title;
    protected String plot;
    protected String releaseDate;
    protected String posterUrl;
    protected SQLiteDatabase db;

    /**
     * @param filmTitle
     * @param filmPlot
     * @param filmReleaseDate
     * @param filmPosterUrl
     */
    Film (String filmId, String filmTitle, String filmPlot, String filmReleaseDate, String filmPosterUrl) {
        this.id = filmId;
        this.title = filmTitle;
        this.plot = filmPlot;
        this.releaseDate = filmReleaseDate;
        this.posterUrl = filmPosterUrl;
    }

    public void setDbConnection(SQLiteDatabase database) {
        this.db = database;
    }

    public View buildFilm(Activity activity, Context applicationContext) {
        View filmItem = LayoutInflater.from(activity).inflate(R.layout.display_film, null, false);

        TextView title = (TextView) filmItem.findViewById(R.id.filmTitle);
        title.setText(this.title);

        TextView plot = (TextView) filmItem.findViewById(R.id.filmPlot);
        plot.setText(this.plot);

        TextView year = (TextView) filmItem.findViewById(R.id.filmYear);
        year.setText(this.releaseDate);

        ImageView imgPoster = (ImageView) filmItem.findViewById(R.id.filmPoster);
        Picasso.with(applicationContext).load("http://image.tmdb.org/t/p/w185/" + this.posterUrl).into(imgPoster);

        final ImageView imgLike = (ImageView) filmItem.findViewById(R.id.filmLike);

        // Check film is favourite
        Cursor cursor = db.query(
                FavouritesSchema.Favourite.TABLE_NAME,
                new String[]{FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID},
                FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID + " = " + this.id,
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

        imgLike.setId(Integer.parseInt(this.id));

        // Set event listener
        imgLike.setOnClickListener(new myFavouriteEventListener(this, imgLike));

        return filmItem;
    }

    public void storeFavourite() {
        ContentValues values = new ContentValues();
        values.put(FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID, id);
        values.put(FavouritesSchema.Favourite.COLUMN_NAME_FILM_TITLE, title);
        values.put(FavouritesSchema.Favourite.COLUMN_NAME_IMAGE_URL, "http://image.tmdb.org/t/p/w185/" + posterUrl);

        db.insert(
                FavouritesSchema.Favourite.TABLE_NAME,
                null,
                values
        );
    }

    public class myFavouriteEventListener implements View.OnClickListener {
        protected Film film;
        protected ImageView imgLike;

        myFavouriteEventListener(Film filmIn, ImageView image) {
            this.film = filmIn;
            this.imgLike = image;
        }

        @Override
        public void onClick(View v)
        {
            Cursor c = db.query(
                    FavouritesSchema.Favourite.TABLE_NAME,
                    new String[]{ FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID },
                    FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID + " =? ",
                    new String[]{String.valueOf(Integer.toString(v.getId()))},
                    null,
                    null,
                    null
            );

            if (c.getCount() == 0) {
                this.film.storeFavourite();
                this.imgLike.setImageResource(R.drawable.full_heart);
            } else {
                db.delete(
                        FavouritesSchema.Favourite.TABLE_NAME,
                        FavouritesSchema.Favourite.COLUMN_NAME_FILM_ID + " =? ",
                        new String[]{String.valueOf(Integer.toString(Integer.parseInt(id)))}
                );
                this.imgLike.setImageResource(R.drawable.empty_heart);
            }
        }

    }
}
