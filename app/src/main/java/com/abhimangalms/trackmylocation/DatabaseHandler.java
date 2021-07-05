package com.abhimangalms.trackmylocation;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;


public class DatabaseHandler extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "db_location";
    private static final String TABLE_LOCATIONS = "table_location";
    private static final String KEY_ID = "id";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_LON = "longitude";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        //3rd argument to be passed is CursorFactory instance
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOCATION_TABLE = "CREATE TABLE " + TABLE_LOCATIONS + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_LAT + " TEXT,"
                + KEY_LON + " TEXT" + ")";
        db.execSQL(CREATE_LOCATION_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOCATIONS);

        // Create tables again
        onCreate(db);
    }

    // code to add the new locationModel
    void addLocation(LocationModel locationModel) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_LAT, locationModel.getLatitude());
        values.put(KEY_LON, locationModel.getLongitude());

        // Inserting Row
        db.insert(TABLE_LOCATIONS, null, values);
        //2nd argument is String containing nullColumnHack
        db.close(); // Closing database connection
    }

    // code to get all contacts in a list view
    public List<LocationModel> getAllLocation() {
        List<LocationModel> locationList = new ArrayList<LocationModel>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_LOCATIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                LocationModel locationModel = new LocationModel();
                locationModel.setId(Integer.parseInt(cursor.getString(0)));
                locationModel.setLatitude(cursor.getString(1));
                locationModel.setLongitude(cursor.getString(2));
                // Adding contact to list
                locationList.add(locationModel);
            } while (cursor.moveToNext());
        }

        // return location list
        return locationList;
    }


    // Deleting single contact
    public void deleteContact(LocationModel locationModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_LOCATIONS, KEY_ID + " = ?",
                new String[] { String.valueOf(locationModel.getId()) });
        db.close();
    }

    public void deleteAllLocations() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE_LOCATIONS);
        db.close();
    }


}