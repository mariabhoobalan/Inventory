package com.example.android.items;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.example.android.items.data.ItemContract.ItemEntry;

public class ItemCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = EditorActivity.class.getSimpleName();

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.listname);
        TextView priceTextView = (TextView) view.findViewById(R.id.listprice);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.listquantity);
        final TextView listIdView = (TextView) view.findViewById(R.id.listid);

        // Find the columns of item attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(ItemEntry._ID);

        // Read the item attributes from the Cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        int itemPrice = cursor.getInt(priceColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);
        int itemID = cursor.getInt(idColumnIndex);

        // Update the TextViews with the attributes for the current pet
        nameTextView.setText(itemName);
        quantityTextView.setText(Integer.toString(itemQuantity));
        priceTextView.setText("$" + Integer.toString(itemPrice));
        listIdView.setText(Integer.toString(itemID));

        Button ListSalesButton = (Button) view.findViewById(R.id.listsalesbutton);
        ListSalesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int quantity = Integer.parseInt(quantityTextView.getText().toString());
                int itemID = Integer.parseInt(listIdView.getText().toString());
                if (quantity > 0) {
                    quantity--;
                    ContentValues values = new ContentValues();
                    values.put(ItemEntry.COLUMN_ITEM_QUANTITY, quantity);
                    final int id = cursor.getInt(cursor.getColumnIndexOrThrow(ItemEntry._ID));
                    Uri currentProductUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, itemID);
                    int rowsAffected = context.getContentResolver().update(currentProductUri, values,
                            null, null);
                    Log.i(LOG_TAG, "id changed : " + id);
                    Log.i(LOG_TAG, "id changed : " + itemID);
                    if (rowsAffected != 0) {
                        // update text view if db update is successful
                        quantityTextView.setText(Integer.toString(quantity));
                    }
                }
            }
        });
    }
}
