package com.example.android.items.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class ItemContract {

    // To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    private ItemContract() {
    }
    //The "Content authority" is a name for the entire content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.items";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //setup the path
    public static final String PATH_ITEMS = "items";

    public static final class ItemEntry implements BaseColumns {
        //create the Uri to contact the content provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;
        //define database field names
        public final static String TABLE_NAME = "items";
        public final static String _ID = BaseColumns._ID;
        public final static String COLUMN_ITEM_NAME = "name";
        public final static String COLUMN_ITEM_PRICE = "price";
        public final static String COLUMN_ITEM_QUANTITY = "quantity";
        public final static String COLUMN_VENDOR_EMAIL = "email";
        public final static String COLUMN_ITEM_IMAGE = "image";

    }
}
