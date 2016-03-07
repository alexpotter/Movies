package com.example.alexpotter.movies;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class FavouritesSchema {
    public FavouritesSchema() {}

    public static abstract class Favourite implements BaseColumns {
        public static final String TABLE_NAME = "favourites";
        public static final String COLUMN_NAME_FILM_ID = "id";
        public static final String COLUMN_NAME_FILE_TITLE = "title";
        public static final String COLUMN_NAME_IMAGE_URL = "poster_url";

        public static final String TEXT_TYPE = " TEXT";
        private static final String COMMA_SEP = ",";

        private static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + Favourite.TABLE_NAME + " (" +
                        Favourite.COLUMN_NAME_FILM_ID + " INTEGER PRIMARY KEY," +
                        Favourite.COLUMN_NAME_FILE_TITLE + TEXT_TYPE + COMMA_SEP +
                        Favourite.COLUMN_NAME_IMAGE_URL + TEXT_TYPE +
                " )";

        private static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + Favourite.TABLE_NAME;

        public static class FavouriteDbHelper extends SQLiteOpenHelper {
            // If you change the database schema, you must increment the database version.
            public static final int DATABASE_VERSION = 1;
            public static final String DATABASE_NAME = "Favourites.db";

            public FavouriteDbHelper(Context context) {
                super(context, DATABASE_NAME, null, DATABASE_VERSION);
            }
            public void onCreate(SQLiteDatabase db) {
                db.execSQL(SQL_CREATE_ENTRIES);
            }
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                // This database is only a cache for online data, so its upgrade policy is
                // to simply to discard the data and start over
                db.execSQL(SQL_DELETE_ENTRIES);
                onCreate(db);
            }
            public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                onUpgrade(db, oldVersion, newVersion);
            }
        }
    }
}
