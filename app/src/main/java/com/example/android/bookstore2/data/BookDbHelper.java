package com.example.android.bookstore2.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.android.bookstore2.data.BookContract.BookEntry;

public class BookDbHelper extends SQLiteOpenHelper {

    public static final String SQL_CREATE_BOOKS_TABLE = "CREATE TABLE " + BookEntry.TABLE_NAME + " (" +
            BookEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            BookEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
            BookEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL DEFAULT 0, " +
            BookEntry.COLUMN_PRODUCT_QUANTITY + " INTEGER NOT NULL DEFAULT 0, " +
            BookEntry.COLUMN_SUPPLIER_NAME + " TEXT, " +
            BookEntry.COLUMN_SUPPLIER_PHONE + " INTEGER" +
            ");";

    // Database Version. If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;
    /* Database Name */
    private static final String DATABASE_NAME = "bookstore.db";
    // DELETE ENTRIES
    private static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + BookEntry.TABLE_NAME;
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_BOOKS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public BookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
}