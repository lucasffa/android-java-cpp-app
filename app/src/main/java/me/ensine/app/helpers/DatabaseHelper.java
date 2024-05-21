package me.ensine.app.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user.db";
    private static final int DATABASE_VERSION = 2; // Updated version

    public static final String TABLE_USER = "user";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_TOKEN = "token";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_LAST_LOGIN = "last_login";
    public static final String COLUMN_UUID = "uuid";
    public static final String COLUMN_ROLE = "role";
    public static final String COLUMN_IS_LOGGED_OFF = "is_logged_off"; // New column

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_USER + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TOKEN + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_LAST_LOGIN + " TEXT, " +
                    COLUMN_UUID + " TEXT, " +
                    COLUMN_ROLE + " TEXT, " +
                    COLUMN_IS_LOGGED_OFF + " INTEGER DEFAULT 0);"; // Default is not logged off

    private static final String TAG = "DatabaseHelper";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "Creating database table");
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_USER + " ADD COLUMN " + COLUMN_IS_LOGGED_OFF + " INTEGER DEFAULT 0;");
        }
    }
}
