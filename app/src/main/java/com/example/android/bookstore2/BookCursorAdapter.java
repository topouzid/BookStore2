package com.example.android.bookstore2;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstore2.data.BookContract.BookEntry;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
public class BookCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the pet data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current pet can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView nameTextView  = (TextView) view.findViewById(R.id.name);
        final TextView summaryTextView = (TextView) view.findViewById(R.id.summary);
        View itemView = (View) view.findViewById(R.id.item_view);
        Button sellButton = (Button) view.findViewById(R.id.sell);

        // Extract properties from cursor
        final int bookId = cursor.getInt(cursor.getColumnIndex(BookEntry._ID));
        String bookName = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME));
        final float price = cursor.getFloat(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PRICE));
        final int stock = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_QUANTITY));

        String summary = context.getString(R.string.label_rrp) + price + " - " + stock + context.getString(R.string.label_book_in_stock);
        // Populate fields with extracted properties
        nameTextView.setText(bookName);
        summaryTextView.setText(summary);

        // Set up item click listener for the view
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Create a new intent to go to {@link EditorActivity
                Intent editorIntent = new Intent(context, EditorActivity.class);
                /**
                 * From the Content URI that represents the specific book that was clicked on
                 * by appending the ID (passed as input to this method) onto the
                 * {@link BookEntry#CONTENT_URI}.
                 * For example the URI would be content://com.example.android.bookstore2/books/2
                 * if the book with ID 2 was clicked on.
                 */
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookId);
                //Set the URI on the data field of the intent
                editorIntent.setData(currentBookUri);
                //Launch the editor activity for the current book that was clicked on
                context.startActivity(editorIntent);
            }
        });

        // Set up item click listener for the "sell" button
        sellButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity = stock;
                // Change the quantity value of the item removing one from the stock
                if (quantity == 0) {
                    Toast.makeText(context, context.getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                } else if (quantity > 0) {
                    quantity = quantity -1;
                    // Create a new map of values, where column names are the keys
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, quantity);
                    /**
                     * From the Content URI that represents the specific book that we are inside
                     * by appending the ID (passed as input to this method) onto the
                     * {@link BookEntry#CONTENT_URI}.
                     * For example the URI would be content://com.example.android.bookstore2/books/2
                     * if the book with ID 2 was clicked on.
                     */
                    Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, bookId);
                    // This is an EXISTING book, so update the book with content URI: mCurrentBookUri
                    // and pass in the new ContentValues. Pass in null for the selection and selection args
                    // because mCurrentBookUri will already identify the correct row in the database that
                    // we want to modify.
                    int rowsAffected = context.getContentResolver().update(currentBookUri, values, null, null);

                    // Show a toast message depending on whether or not the update was successful.
                    if (rowsAffected == 0) {
                        // If no rows were affected, then there was an error with the update.
                        Toast.makeText(context, context.getString(R.string.editor_update_book_failed),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        // Otherwise, the update was successful and we can display a toast and refresh the summary with the new stock value.
                        String summary = context.getString(R.string.label_rrp) + price + " - " + quantity + context.getString(R.string.label_book_in_stock);
                        summaryTextView.setText(summary);
                        // Display a toast.
                        Toast.makeText(context, context.getString(R.string.editor_update_book_successful),
                                Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
    }
}
