package de.everytap.broteinheiten_datenbank.database.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by randombyte on 20.12.2014.
 */
public class BeDatabase extends SQLiteOpenHelper{

    public static final String TABLE_BE = "table_be";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FOOD = "_food";
    public static final String COLUMN_BE = "_be";
    public static final String COLUMN_USER_CREATED = "_user_created";

    private static final String DATABASE_NAME = "food_be.db";
    private static final int DATABASE_VERSION = 2; //2

    private static final String DATABASE_CREATE = "CREATE TABLE " + TABLE_BE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_FOOD + " TEXT NOT NULL, " + COLUMN_BE + " TEXT NOT NULL, " + COLUMN_USER_CREATED + " INTEGER NON NULL);";

    public BeDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(BeDatabase.class.getName(), "Upgrading database from version " + oldVersion + " to " + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BE);
        onCreate(db);
    }
}
