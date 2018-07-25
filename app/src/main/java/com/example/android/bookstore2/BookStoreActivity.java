package com.example.android.bookstore2;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.bookstore2.data.BookDbHelper;
import com.example.android.bookstore2.data.BookContract.BookEntry;

public class BookStoreActivity extends AppCompatActivity {

    private BookDbHelper mDbHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_store);
        displayDatabaseInfo();
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayDatabaseInfo();
    }

    /**
     * Temporary helper method to display information in the onscreen TextView about the state of
     * the pets database.
     */
    private void displayDatabaseInfo() {
        // To access our database, we instantiate our subclass of SQLiteOpenHelper
        // and pass the context, which is the current activity.
        mDbHelper = new BookDbHelper(this);

        // Create and/or open a database to read from it
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRODUCT_PRICE,
                BookEntry.COLUMN_PRODUCT_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE
        };

        Cursor cursor = db.query(BookEntry.TABLE_NAME, projection, null, null, null, null, null);

        try {
            // Display the number of rows in the Cursor (which reflects the number of rows in the
            // pets table in the database).
            TextView displayView = (TextView) findViewById(R.id.text_view_books);
            displayView.setText("Number of books in our database; " + cursor.getCount() + "\n\n");
            displayView.append(BookEntry._ID + "/" +
                    BookEntry.COLUMN_PRODUCT_NAME + "/" +
                    BookEntry.COLUMN_PRODUCT_QUANTITY + "/" +
                    BookEntry.COLUMN_PRODUCT_PRICE + "\n");
            //Set the index of each column
            int idColumnIndex = cursor.getColumnIndex(BookEntry._ID);
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_QUANTITY);
            //Iterate through all returned rows
            while (cursor.moveToNext()) {
                // Use that index to extract the String or Int value of the word
                // at the current row the cursor is on.
                int currentId = cursor.getInt(idColumnIndex);
                String currentName = cursor.getString(nameColumnIndex);
                int currentPrice = cursor.getInt(priceColumnIndex);
                int currentQuantity = cursor.getInt(quantityColumnIndex);
                //Display the values of each row for the columns we chose
                displayView.append("\n" + currentId + ". " +
                        currentName + " (QTY: " +
                        currentQuantity + ") Pr: " +
                        currentPrice + "");
            }
        } finally {
            // Always close the cursor when you're done reading from it. This releases all its
            // resources and makes it invalid.
            cursor.close();
        }
    }

    public void insertBook() {
        // Gets the data repository in write mode
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, "BookNameTest");
        values.put(BookEntry.COLUMN_PRODUCT_PRICE, 999);
        values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, 9);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, "Supplier Name Test");
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, "Phone number test");

        long newRowId = db.insert(BookEntry.TABLE_NAME, null, values);

        Log.v("BookStoreActivity", "New Row ID: " + newRowId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_bookstore.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_bookstore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertBook();
                displayDatabaseInfo();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
