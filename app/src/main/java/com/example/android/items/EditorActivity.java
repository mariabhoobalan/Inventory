package com.example.android.items;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.items.data.ItemContract.ItemEntry;

public class EditorActivity extends AppCompatActivity
        implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //identifier for item data loader
    private static final int EXISTING_ITEM_LOADER = 0;
    //Uri for the current item
    private Uri mCurrentItemUri;
    //Views
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mEmailEditText;
    private ImageView mImageView;
    private TextView mUriLockerTextView;
    //To check if the item is changed
    private boolean mItemHasChanged = false;
    //If image pick is successfull
    private static final int PICK_IMAGE_REQUEST = 1;
    //Order message and fields related to email intent
    String orderMessage = "Hello,";
    String finalOrderMessage;
    private String xImgUri;
    public String xName;
    public String endActivity;
    public String xEmail;
    public int xQuantity;
    //Log tag
    private static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //Examine the intent that was used to launch this activity
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        //Set proper title
        if (mCurrentItemUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_item));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_item));
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        //Views
        mNameEditText = (EditText) findViewById(R.id.nameEdit);
        mPriceEditText = (EditText) findViewById(R.id.priceEdit);
        mQuantityEditText = (EditText) findViewById(R.id.quantityEdit);
        mEmailEditText = (EditText) findViewById(R.id.emailEdit);
        mImageView = (ImageView) findViewById(R.id.imagex);
        mUriLockerTextView = (TextView) findViewById(R.id.uriLocker);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mEmailEditText.setOnTouchListener(mTouchListener);

        Button orderButton = (Button) findViewById(R.id.orderbutton);
        Button salesButton = (Button) findViewById(R.id.salebutton);
        Button purchaseButton = (Button) findViewById(R.id.purchasebutton);
        Button photoButton = (Button) findViewById(R.id.photobutton);
        Button deleteButton = (Button) findViewById(R.id.deletebutton);
        Button updateButton = (Button) findViewById(R.id.updatebutton);
        orderMessage = orderMessage + "\nWe would like to order the following item";

        //Send out an email when order button is pressed
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                xName = mNameEditText.getText().toString().trim();
                xEmail = mEmailEditText.getText().toString().trim();
                finalOrderMessage = orderMessage + "\nItem =" + xName
                        + "\nQuantity = 100";
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/html");
                intent.putExtra(Intent.EXTRA_EMAIL, new String[]{xEmail});
                intent.putExtra(Intent.EXTRA_SUBJECT, "Inventory Order");
                intent.putExtra(Intent.EXTRA_TEXT, finalOrderMessage);
                startActivity(Intent.createChooser(intent, "Send Email"));
                ;
            }
        });

        //Decrease the quantity when the item is sold
        salesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String charQty = mQuantityEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(charQty)) {
                    xQuantity = Integer.parseInt(charQty);
                    if (xQuantity > 0) {
                        xQuantity = xQuantity - 1;
                        mQuantityEditText.setText(Integer.toString(xQuantity));
                        saveItem();
                    }
                }
            }
        });

        //Increase the qty when the item is purchased
        purchaseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String charQty = mQuantityEditText.getText().toString().trim();
                if (!TextUtils.isEmpty(charQty)) {
                    xQuantity = Integer.parseInt(charQty);
                    xQuantity = xQuantity + 1;
                    mQuantityEditText.setText(Integer.toString(xQuantity));
                    saveItem();
                }
            }
        });

        //Pick a photo from gallery
        photoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 1);
            }
        });

        //Delete the selected item
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });

        //Update the changes made in the Editor screen
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endActivity = "1";
                saveItem();
                if (endActivity == "1") {
                    finish();
                }
            }
        });

    }

    //Photo selector
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST) {
                // Get the url from data
                Uri selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // Get the path from the Uri
                    String path = getPathFromURI(selectedImageUri);
                    Log.i(LOG_TAG, "Image Path : " + path);
                    // Set the image in ImageView
                    mImageView.setImageURI(selectedImageUri);
                    Log.i(LOG_TAG, "Image URI : " + selectedImageUri);
                    xImgUri = selectedImageUri.toString();
                    mUriLockerTextView.setText(xImgUri);
                }
            }
        }
    }

    /* Get the real path from the URI */
    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }

    //Save the records populated in the editor view into DB
    private void saveItem() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String emailString = mEmailEditText.getText().toString().trim();
        String uriString = mUriLockerTextView.getText().toString().trim();
        endActivity = "1";

        //Throw error if item name, price or quantity is blanks
        if (TextUtils.isEmpty(nameString)||
            TextUtils.isEmpty(priceString)||
            TextUtils.isEmpty(quantityString)){
            Toast.makeText(this, getString(R.string.mandatoryfielderror),
                    Toast.LENGTH_SHORT).show();
            endActivity = "0";
        } else {
            // Create a ContentValues object where column names are the keys,
            // and item attributes from the editor are the values.
            ContentValues values = new ContentValues();
            values.put(ItemEntry.COLUMN_ITEM_NAME, nameString);
            values.put(ItemEntry.COLUMN_VENDOR_EMAIL, emailString);
            values.put(ItemEntry.COLUMN_ITEM_IMAGE, uriString);
            int qnty = 0;
            int price = 0;
            if (!TextUtils.isEmpty(priceString)) {
                price = Integer.parseInt(priceString);
            }

            if (!TextUtils.isEmpty(quantityString)) {
                qnty = Integer.parseInt(quantityString);
            }

            values.put(ItemEntry.COLUMN_ITEM_PRICE, price);
            values.put(ItemEntry.COLUMN_ITEM_QUANTITY, qnty);

            // Determine if this is a new or existing item by checking if mCurrentItemUri is null or not
            if (mCurrentItemUri == null) {
                // This is a NEW item, so insert a new item into the provider,
                // returning the content URI for the new item.
                Uri newUri = getContentResolver().insert(ItemEntry.CONTENT_URI, values);

                // Show a toast message depending on whether or not the insertion was successful.
                if (newUri == null) {
                    // If the new content URI is null, then there was an error with insertion.
                    Toast.makeText(this, getString(R.string.editor_insert_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the insertion was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_insert_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                // Otherwise this is an EXISTING item, so update the item with content URI: mCurrentItemUri
                // and pass in the new ContentValues. Pass in null for the selection and selection args
                // because mCurrentItemUri will already identify the correct row in the database that
                // we want to modify.
                int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);

                // Show a toast message depending on whether or not the update was successful.
                if (rowsAffected == 0) {
                    // If no rows were affected, then there was an error with the update.
                    Toast.makeText(this, getString(R.string.editor_update_item_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    // Otherwise, the update was successful and we can display a toast.
                    Toast.makeText(this, getString(R.string.editor_update_item_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                endActivity = "1";
                // Save item to database
                saveItem();
                if (endActivity == "1") {
                    finish();
                }
                return true;

            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mItemHasChanged) {
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
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mItemHasChanged) {
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

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all item attributes, define a projection that contains
        // all columns from the item table
        String[] projection = {
                ItemEntry._ID,
                ItemEntry.COLUMN_ITEM_NAME,
                ItemEntry.COLUMN_ITEM_PRICE,
                ItemEntry.COLUMN_ITEM_QUANTITY,
                ItemEntry.COLUMN_ITEM_IMAGE,
                ItemEntry.COLUMN_VENDOR_EMAIL};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                mCurrentItemUri,         // Query the content URI for the current item
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of item attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
            int emailColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_VENDOR_EMAIL);
            int imageColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            int price = cursor.getInt(priceColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            String email = cursor.getString(emailColumnIndex);
            String xUri = cursor.getString(imageColumnIndex);
            Uri fnlURI;
            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mEmailEditText.setText(email);
            mUriLockerTextView.setText(xUri);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(Integer.toString(price));
            fnlURI = Uri.parse(xUri);
            if (!TextUtils.isEmpty(xUri)) {
                 mImageView.setImageURI(fnlURI);
                Log.i(LOG_TAG, "Image URI : " + fnlURI);
            }


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("0");
        mQuantityEditText.setText("0");
        mEmailEditText.setText("");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentItemUri
            // content URI already identifies the item that we want.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }
}
