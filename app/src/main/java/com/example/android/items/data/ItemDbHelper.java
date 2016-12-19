package com.example.android.items.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.items.data.ItemContract.ItemEntry;

public class ItemDbHelper extends SQLiteOpenHelper {
    //db name
    private static final String DATABASE_NAME = "store.db";
    //db version. change the version if the db is modified
    private static final int DATABASE_VERSION = 1;

    public ItemDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    //create the table.This will be called only for the first time the app is invoked
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_ITEMS_TABLE = "CREATE TABLE " + ItemEntry.TABLE_NAME + " ("
                + ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + ItemEntry.COLUMN_ITEM_NAME + " TEXT NOT NULL, "
                + ItemEntry.COLUMN_ITEM_PRICE + " INTEGER NOT NULL DEFAULT 0,"
                + ItemEntry.COLUMN_ITEM_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
                + ItemEntry.COLUMN_ITEM_IMAGE + " TEXT, "
                + ItemEntry.COLUMN_VENDOR_EMAIL + " TEXT);";

        // Execute the SQL statement
        db.execSQL(SQL_CREATE_ITEMS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // The database is still at version 1, so there's nothing to do be done here.
    }


}
