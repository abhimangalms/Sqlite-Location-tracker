package com.abhimangalms.trackmylocation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.abhimangalms.trackmylocation.MessageContract.MessageEntry;



public class MessageDBHelper extends SQLiteOpenHelper {

//    private final static String DATABASE_NAME = "messages.db";
    private final static int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME="register.db";
    public static final String TABLE_NAME="registration";
    public static final String COL_1="ID";
    public static final String COL_2="Name";
    public static final String COL_3="Phone";
    public static final String COL_4="Gmail";
    public static final String COL_5="Password";


    public MessageDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {




        final String CREATE_DATABASE = "CREATE TABLE " +
                MessageEntry.TABLE_NAME + " (" +
                MessageEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MessageEntry.COLUMN_SENDER + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_MESSAGE + " TEXT NOT NULL, " +
                MessageEntry.COLUMN_TIMESTAMP + " TEXT NOT NULL" +
                ");";

        db.execSQL(CREATE_DATABASE);
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT,Name TEXT,Phone TEXT,Gmail TEXT,Password TEXT)");


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // delete the old table and recreate a new
        db.execSQL("DROP TABLE IF EXISTS " + MessageEntry.TABLE_NAME);
        onCreate(db);
    }
}
