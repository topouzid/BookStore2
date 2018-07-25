package com.example.android.bookstore2;

import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstore2.data.BookContract.BookEntry;

/**
 * Allows user to create a new book or edit an existing one.
 */
public class EditorActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /** EditText field to enter the book's name */
    private EditText mBookNameEditText;

    /** EditText field to enter the book's price */
    private EditText mBookPriceEditText;

    /** EditText field to enter the book's stock value */
    private EditText mBookStockEditText;

    /** EditText field to enter the supplier's name */
    private EditText mSupplierNameEditText;

    /** EditText field to enter the supplier's phone number */
    private EditText mSupplierNumberEditText;

    /** Button +1 that adds one to the current quantity */
    private Button mPlusOne;

    /** Button -1 that adds one to the current quantity */
    private Button mMinusOne;

    /** Button Call Supplier starts an intent to make a call */
    private Button mCallSupplier;


    /**
     * Constant value for the book loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    /** Content URI for the existing book (null if it's a new book) */
    private Uri mCurrentBookUri;

    /** Track if there are any changes to the data */
    private boolean mBookHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        /**
         * Use getIntent() and getData() to get the associated URI
         */

        // Examine the intent that was used to launch this activity
        // in order to figure out if we are editing a book or adding a new one
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();

        // Find all relevant views that we will need to read user input from
        mBookNameEditText = (EditText) findViewById(R.id.edit_book_name);
        mBookPriceEditText = (EditText) findViewById(R.id.edit_book_price);
        mBookStockEditText = (EditText) findViewById(R.id.edit_book_quantity);
        mSupplierNameEditText = (EditText) findViewById(R.id.edit_supplier_name);
        mSupplierNumberEditText = (EditText) findViewById(R.id.edit_supplier_phone);
        mPlusOne = (Button) findViewById(R.id.button_plus_one);
        mMinusOne = (Button) findViewById(R.id.button_minus_one);
        mCallSupplier = (Button) findViewById(R.id.button_call_order);


        mBookNameEditText.setOnKeyListener(mKeyListener);
        mBookNameEditText.setOnTouchListener(mTouchListener);
        mBookPriceEditText.setOnTouchListener(mTouchListener);
        mBookStockEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierNumberEditText.setOnTouchListener(mTouchListener);
        mPlusOne.setOnTouchListener(mTouchListener);
        mMinusOne.setOnTouchListener(mTouchListener);

        /**
         * Set the title of EditorActivity on which situation we have
         * If the editor activity was opened using the ListView item, then we will
         * have uri of book so change app bar to say "Edit Book"
         * Otherwise if this is a new book, uri is null so change app bar to "Add a Book"
         */
        // If the intent does not contain any Book Content URI,
        // then we know we are creating a new book
        if (mCurrentBookUri == null) {
            // This is a new book, so change the app bar to say "Add a Book"
            setTitle(getString(R.string.editor_activity_title_new_book));
            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu();
            minusClick();
            plusClick();
            callClick();
        } else {
            // Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(getString(R.string.editor_activity_title_edit_book));

            // Start the loader
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    /**
     * This method is called after invalidateOptionsMenu(), so that the
     * menu can be updated (some menu items can be hidden or made visible).
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new book, hide the "Delete" menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    /**
     * Get user input from editor and save book into database.
     */
    public void saveBook() {

        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String bookNameString = mBookNameEditText.getText().toString().trim();
        String bookPriceString = mBookPriceEditText.getText().toString().trim();
        String bookStockString = mBookStockEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneString = mSupplierNumberEditText.getText().toString().trim();
        float price;
        long phone = 0;
        int stock = 0;
        // If the price is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        if (!bookPriceString.equals("")) {
            price = Float.parseFloat(bookPriceString);
        } else {
            price = 0;
        }
        if (!bookStockString.equals("")) {
            stock = Integer.parseInt(bookStockString);
        }
        if (!supplierPhoneString.equals("")) {
            phone = Long.parseLong(supplierPhoneString);
        }


        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(BookEntry.COLUMN_PRODUCT_NAME, bookNameString);
        values.put(BookEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(BookEntry.COLUMN_PRODUCT_QUANTITY, stock);
        values.put(BookEntry.COLUMN_SUPPLIER_NAME, supplierNameString);
        values.put(BookEntry.COLUMN_SUPPLIER_PHONE, phone);

        if (mCurrentBookUri == null) {
            //Adding a New Book
            if (TextUtils.isEmpty(bookNameString) ||
                    TextUtils.isEmpty(bookStockString) ||
                    TextUtils.isEmpty(bookPriceString) ||
                    TextUtils.isEmpty(supplierNameString) ||
                    TextUtils.isEmpty(supplierPhoneString)) {
                //The user didn't input all book data
                Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                        Toast.LENGTH_LONG).show();
            } else {
                // Insert a new pet into the provider, returning the content URI for the new pet.
                Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
                // Show a toast message depending on whether or not the insertion was successful
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
                // If all went good adding a new book, finish this activity
                finish();
            }
        } else {
            //Editing an existing book
            int rowsAffected;
            if (bookNameString.equals("") ||
                    TextUtils.isEmpty(bookStockString) ||
                    TextUtils.isEmpty(bookPriceString) ||
                    TextUtils.isEmpty(supplierNameString) ||
                    TextUtils.isEmpty(supplierPhoneString)) {
                //The user didn't set all fields while editing the book
                rowsAffected = 0;
            } else {
                // Otherwise this is an EXISTING book, so update the book with content URI: mCurrentBookUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentBookUri will already identify the correct row in the database that
                // we want to modify.
                rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
            }
            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_book_successful),
                        Toast.LENGTH_SHORT).show();
                // If all went good editing the book, finish the activity
                finish();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                saveBook();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                /// Pop up confirmation dialog for deletion
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_call:
                //Display message if phone number is missing
                if (TextUtils.isEmpty(mSupplierNumberEditText.getText())) {
                    Toast.makeText(EditorActivity.this, getString(R.string.no_number), Toast.LENGTH_SHORT).show();
                } else {
                    String callUriString = "tel:" + mSupplierNumberEditText.getText().toString().trim();
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse(callUriString));
                    startActivity(callIntent);
                }
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:

                // If the book hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mBookHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the books table
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_PRODUCT_NAME,
                BookEntry.COLUMN_PRODUCT_PRICE,
                BookEntry.COLUMN_PRODUCT_QUANTITY,
                BookEntry.COLUMN_SUPPLIER_NAME,
                BookEntry.COLUMN_SUPPLIER_PHONE
        };
        // This loader will execute the ContentProvider's query method on a background thread
        CursorLoader currentBookCursorLoader = new CursorLoader(
                this,
                mCurrentBookUri,
                projection,
                null,
                null,
                null);
        return currentBookCursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find fields
            TextView bookNameEditText = (TextView) findViewById(R.id.edit_book_name);
            TextView bookPriceEditText = (TextView) findViewById(R.id.edit_book_price);
            TextView bookStockEditText = (TextView) findViewById(R.id.edit_book_quantity);
            TextView supplierNameEditText = (TextView) findViewById(R.id.edit_supplier_name);
            TextView supplierNumberEditText = (TextView) findViewById(R.id.edit_supplier_phone);
            // Extract properties from cursor
            String name = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_NAME));
            String supplierName = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_NAME));
            float price = cursor.getFloat(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_PRICE));
            int stock = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_PRODUCT_QUANTITY));
            long phone = cursor.getLong(cursor.getColumnIndex(BookEntry.COLUMN_SUPPLIER_PHONE));
            //Set data to views
            bookNameEditText.setText(name);
            supplierNameEditText.setText(supplierName);
            if (price != 0) {
                bookPriceEditText.setText(Float.valueOf(price).toString());
            }
            bookStockEditText.setText(Integer.valueOf(stock).toString());
            if (phone != 0) {
                supplierNumberEditText.setText(Long.valueOf(phone).toString());
            }

            // Start the click listeners after the loader has finished
            minusClick();
            plusClick();
            callClick();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    // OnTouchListener that listens for any user touches on a View, implying that they are modifying
    // the view, and we change the mBookHasChanged boolean to true.

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    // OnKeyListener that listens for any user input on a View (on EditText), implying that they are
    // writing something on the selected editText even without touching the view (keyboard, emulator),
    // and we change the mBookHasChanged boolean to true.
    private View.OnKeyListener mKeyListener = new View.OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            mBookHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
                // If the book is deleted, finish this activity and return to CatalogActivity
                finish();
            }
        }
    }

    /**
     * Method to remove one item from the stock (only on display).
     */
    private void minusClick() {
        mMinusOne.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity;
                if (TextUtils.isEmpty(mBookStockEditText.getText())) {
                    quantity = 0;
                } else {
                    //Read the EditText with the current displayed quantity
                    quantity = Integer.parseInt(mBookStockEditText.getText().toString());
                }
                if (quantity < 1) {
                    Toast.makeText(EditorActivity.this, getString(R.string.out_of_stock), Toast.LENGTH_SHORT).show();
                } else {
                    quantity = quantity - 1;
                    //Only update the quantity display without saving to database
                    mBookStockEditText.setText(Integer.valueOf(quantity).toString());
                }
            }
        });
    }

    /**
     * Method to add one item from the stock (only on display).
     */
    private void plusClick() {
        mPlusOne.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                int quantity;
                if (TextUtils.isEmpty(mBookStockEditText.getText())) {
                    quantity = 0;
                } else {
                    //Read the EditText with the current displayed quantity
                    quantity = Integer.parseInt(mBookStockEditText.getText().toString());
                }
                quantity = quantity + 1;
                //Only update the quantity display without saving to database
                mBookStockEditText.setText(Integer.valueOf(quantity).toString());
            }
        });
    }

    /**
     * Method to monitor the Call button and start the call Intent.
     */
    private void callClick() {
        mCallSupplier.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Display message if phone number is missing
                if (TextUtils.isEmpty(mSupplierNumberEditText.getText())) {
                    Toast.makeText(EditorActivity.this, getString(R.string.no_number), Toast.LENGTH_SHORT).show();
                } else {
                    String callUriString = "tel:" + mSupplierNumberEditText.getText().toString().trim();
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse(callUriString));
                    startActivity(callIntent);
                }
            }
        });
    }
}
