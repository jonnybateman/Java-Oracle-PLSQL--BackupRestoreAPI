package com.development.smartlist;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

class DBAdapter {

    private Context context;
    private DBHelper dbHelper;
    private SQLiteDatabase db;

    static final String DB_NAME = "smartlist_db";
    static final int DB_VERSION = 2;

    // Setup the table 'category'.
    static final String CATEGORY_TABLE = "categories";
    static final String C_CAT_ID = "cat_id";
    private static final String C_CATEGORY_NAME = "category_name";

    // String constant for the SQL syntax to create 'category' table.
    private static final String CREATE_CATEGORY_TABLE_SQL = "create table " + CATEGORY_TABLE + "(" +
            C_CAT_ID + " integer primary key autoincrement, " + C_CATEGORY_NAME +
            " text not null unique)";

    // Setup the table 'shop'.
    static final String SHOP_TABLE = "shops";
    private static final String C_SHOP_ID = "shop_id";
    private static final String C_SHOP_NAME = "shop_name";

    // String constant for the SQL syntax to create 'shop' table.
    private static final String CREATE_SHOP_TABLE_SQL = "create table " + SHOP_TABLE + "(" +
            C_SHOP_ID + " integer primary key autoincrement, " + C_SHOP_NAME +
            " text not null unique)";

    // Setup the table 'brand'.
    static final String BRAND_TABLE = "brands";
    private static final String C_BRAND_ID = "brand_id";
    private static final String C_BRAND_NAME = "brand_name";

    // String constant for the SQL syntax to create 'brand' table.
    private static final String CREATE_BRAND_TABLE_SQL = "create table " + BRAND_TABLE + "(" +
            C_BRAND_ID + " integer primary key autoincrement, " + C_BRAND_NAME +
            " text not null unique)";

    // Setup the table 'items'.
    static final String ITEMS_TABLE = "items";
    static final String C_ITEM_ID = "item_id";
    static final String C_ITEM_NAME = "item_name";
    static final String C_QUANTITY_UNIT = "quantity_unit";
    static final String C_BARCODE = "barcode";

    // String constant for the SQL syntax to create 'items' table.
    private static final String CREATE_ITEMS_TABLE_SQL = "create table " + ITEMS_TABLE + "(" +
            C_ITEM_ID + " integer primary key autoincrement, " + C_ITEM_NAME +
            " text not null unique, " + C_CAT_ID + " integer not null, " + C_QUANTITY_UNIT +
            " text not null, " +
            C_BARCODE + " text unique, foreign key (" +
            C_CAT_ID + ") references " + CATEGORY_TABLE + "(" + C_CAT_ID + ") on delete cascade)";

    // Setup the table 'item_info'
    static final String ITEM_INFO_TABLE = "item_info";
    private static final String C_LOCATION = "location";
    private static final String C_QUANTITY = "quantity";
    private static final String C_PRICE = "price";

    // String constant for the SQL syntax to create the 'item_info' table.
    private static final String CREATE_ITEM_INFO_TABLE_SQL = "create table " + ITEM_INFO_TABLE +
            "(" + C_ITEM_ID + " integer," + C_PRICE + " real," + C_SHOP_ID + " integer," + C_BRAND_ID +
            " integer," + C_LOCATION + " text," + C_QUANTITY + " text, primary key (" + C_ITEM_ID +
            "," + C_SHOP_ID + "," + C_LOCATION + "))";

    // Setup the table 'lists'.
    static final String LISTS_TABLE = "lists";
    private static final String C_LIST_ID = "list_id";
    private static final String C_LIST_NAME = "list_name";

    // String constant for the SQL syntax to create the 'lists' table.
    private static final String CREATE_LISTS_TABLE_SQL = "create table " + LISTS_TABLE + "(" +
            C_LIST_ID + " integer primary key autoincrement, " + C_LIST_NAME +
            " text not null unique)";

    // Setup the table 'list_items'
    static final String LIST_ITEMS_TABLE = "list_items";
    private static final String C_ITEM_CHECKED = "checked";

    // String constant for the SQL syntax to create the 'list_items' table.
    private static final String CREATE_LIST_ITEMS_TABLE_SQL = "create table " + LIST_ITEMS_TABLE +
            "(" + C_LIST_ID + " integer, " + C_ITEM_ID + " integer, " + C_ITEM_NAME + " text not null, " +
            C_CATEGORY_NAME + " text not null, " + C_QUANTITY + " text not null," +
            C_ITEM_CHECKED + " integer not null, primary key ("
            + C_LIST_ID + ", " + C_ITEM_ID + "))";

    // Setup the table 'locale'.
    private static final String LOCALE_TABLE = "locale";

    // String constant for the SQL syntax to create the 'locale' table.
    private static final String CREATE_LOCALE_TABLE_SQL = "create table " + LOCALE_TABLE +
            "(" + C_LOCATION + " text not null)";

    // Setup the table 'user'.
    static final String USER_TABLE = "user";
    static final String C_USER_ID = "user_id";
    static final String C_USER_NAME = "user_name";

    // String constant for the SQL syntax to create the 'user' table.
    private static final String CREATE_USER_TABLE_SQL = "create table " + USER_TABLE +
            "(" + C_USER_ID + " integer not null," + C_USER_NAME + " text not null)";

    // Initiate new instance of DBHelper class within constructor of DBAdapter class and save that
    // instance in a member variable. DBHelper instance used for opening and closing of database.
    DBAdapter(Context context) {
        this.context = context;
        dbHelper = new DBHelper();
    }

    // Private inner class named DBHelper which is a subclass of SQLiteOpenHelper class.
    // Helps us to manage the tasks of database creation and version management.
    public class DBHelper extends SQLiteOpenHelper {

        // Constructor of DBHelper class
        DBHelper() {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try {
                db.execSQL(CREATE_CATEGORY_TABLE_SQL);
                db.execSQL(CREATE_SHOP_TABLE_SQL);
                db.execSQL(CREATE_BRAND_TABLE_SQL);
                db.execSQL(CREATE_ITEMS_TABLE_SQL);
                db.execSQL(CREATE_LISTS_TABLE_SQL);
                db.execSQL(CREATE_LIST_ITEMS_TABLE_SQL);
                db.execSQL(CREATE_ITEM_INFO_TABLE_SQL);
                db.execSQL(CREATE_LOCALE_TABLE_SQL);
                db.execSQL("insert into " + LOCALE_TABLE + " values('GBP')");
                db.execSQL(CREATE_USER_TABLE_SQL);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        // Called when the DB_VERSION number is different from the stored one.
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            int upgradeTo = oldVersion + 1;
            // Loop through each version to ensure user has all the latest updates.
            while (upgradeTo <= newVersion) {
                switch(upgradeTo) {
                    case 2:
                        db.execSQL(CREATE_CATEGORY_TABLE_SQL);
                        db.execSQL(CREATE_SHOP_TABLE_SQL);
                        db.execSQL(CREATE_BRAND_TABLE_SQL);
                        db.execSQL(CREATE_ITEMS_TABLE_SQL);
                        db.execSQL(CREATE_LISTS_TABLE_SQL);
                        db.execSQL(CREATE_LIST_ITEMS_TABLE_SQL);
                        db.execSQL(CREATE_ITEM_INFO_TABLE_SQL);
                        db.execSQL(CREATE_LOCALE_TABLE_SQL);
                        db.execSQL("insert into " + LOCALE_TABLE + " values('GBP')");
                        db.execSQL(CREATE_USER_TABLE_SQL);
                    case 3:
                        db.execSQL("drop table if exists " + LIST_ITEMS_TABLE);
                        db.execSQL(CREATE_LIST_ITEMS_TABLE_SQL);
                        break;
                }
                upgradeTo++;
            }
        }

        // Enable foreign key constraints in database
        @Override
        public void onConfigure(SQLiteDatabase db) {
            super.onConfigure(db);
            if (!db.isReadOnly()) {
                db.setForeignKeyConstraintsEnabled(true);
            }
        }
    }

    // Open Database
    void open() {
        db = dbHelper.getWritableDatabase();
    }

    // Close Database
    void close() {
        db.close();
    }

    // Start transaction.
    void beginTransaction() {
        db.beginTransaction();
    }

    // Set the transaction() {
    void setTransaction() {
        db.setTransactionSuccessful();
    }

    // End transaction.
    void endTransaction() {
        db.endTransaction();
    }
  
    ...
      
    // Insert record into USER table. Declared package private
    // Returns -1 if error occurred, otherwise returns rowid of new record.
    long insertUser(int userId, String userName) {
        ContentValues values = new ContentValues();
        values.put(C_USER_ID, userId);
        values.put(C_USER_NAME, userName);
        return db.insert(USER_TABLE, null, values);
    }

    // Update the user record with new user name.
    long updateUserName(String userName) {
        ContentValues data = new ContentValues();
        data.put(C_USER_NAME, userName);
        return db.update(USER_TABLE, data, null, null);
    }

    int getUserId() {
        int userId = 0;
        Cursor cursor = db.rawQuery("select user_id from " + USER_TABLE, null);

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }

        cursor.close();
        return userId;
    }

    // Get the stored username for the database.
    String getUserName() {
        String userName = "";
        try (Cursor cursor = db.rawQuery("select user_name from " + USER_TABLE, null)) {

            if (cursor.moveToFirst()) {
                userName = cursor.getString(0);
            }

            return userName;
        }
    }
  
    ...
      
    /*
     * Retrieve data for the passed table. Used for returning data to build backup XML file.
     */
    List<Object[]> getTableData(String tableName) {

        try (Cursor cursor = db.rawQuery("select * from " + tableName, null)){

            // Define the list of record objects.
            List<Object[]> records = new ArrayList<>();

            while (cursor.moveToNext()) {

                // Create record object to store the cursor's current row.
                Object[] record = new Object[cursor.getColumnCount()];

                for (int i=0; i<cursor.getColumnCount(); i++) {

                    // Determine the data type of the current column and add the value to the record object.
                    switch (cursor.getType(i)) {
                        case Cursor.FIELD_TYPE_INTEGER:
                            record[i] = cursor.getInt(i);
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                            // Replace any escape characters from the string.
                            String s = cursor.getString(i).replace("&", "&amp");
                            record[i] = s;
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            record[i] = cursor.getFloat(i);
                            break;
                    }
                }
                // Add the record to the record list.
                records.add(record);
            }

            return records;

        } catch (Exception e) {
            Log.d("getTableData","Exception:" + e.toString());
            return null;
        }
    }

    /*
     * Retrieve column names for a given table.
     */
    LinkedHashMap<String, String> getTableColumns(String table) {

        try (Cursor cursor = db.rawQuery("Pragma table_info(" + table + ")", null)) {

            LinkedHashMap<String, String> columns = new LinkedHashMap<>(); // <column_name, column_type>

            if (cursor.moveToFirst()) {

                do {
                    Log.d("getTableColumns","column:" + cursor.getString(cursor.getColumnIndex("name")));
                    columns.put(cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("type")));
                } while (cursor.moveToNext());
            }

            return columns;

        } catch (Exception e) {
            Log.d("getTableColumns","Exception:" + e.toString());
            return null;
        }
    }
}
