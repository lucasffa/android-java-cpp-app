package me.ensine.app;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class UserRepository {
    private static UserRepository instance;
    private SQLiteDatabase database;

    private static final String TAG = "UserRepository";

    private UserRepository(Context context) {
        DatabaseHelper dbHelper = DatabaseHelper.getInstance(context);
        database = dbHelper.getWritableDatabase();
    }

    public static synchronized UserRepository getInstance(Context context) {
        if (instance == null) {
            instance = new UserRepository(context);
        }
        return instance;
    }

    public void saveUserDetails(String token, String name, String lastLogin, String uuid, String role) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_TOKEN, token);
        values.put(DatabaseHelper.COLUMN_NAME, name);
        values.put(DatabaseHelper.COLUMN_LAST_LOGIN, lastLogin);
        values.put(DatabaseHelper.COLUMN_UUID, uuid);
        values.put(DatabaseHelper.COLUMN_ROLE, role);
        values.put(DatabaseHelper.COLUMN_IS_LOGGED_OFF, 0); // Mark as not logged off

        long result = database.insertWithOnConflict(DatabaseHelper.TABLE_USER, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        Log.d(TAG, "saveUserDetails: result=" + result + ", UUID=" + uuid + ", Token=" + token + ", LastLogin=" + lastLogin);
    }

    public User getUserDetails() {
        String query = "SELECT * FROM " + DatabaseHelper.TABLE_USER + " WHERE " + DatabaseHelper.COLUMN_IS_LOGGED_OFF + " = 0 ORDER BY " + DatabaseHelper.COLUMN_LAST_LOGIN + " DESC LIMIT 1";
        Cursor cursor = database.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            String token = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_TOKEN));
            String name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NAME));
            String lastLogin = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LAST_LOGIN));
            String uuid = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_UUID));
            String role = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_ROLE));
            cursor.close();
            Log.d(TAG, "getUserDetails: User found - UUID: " + uuid + ", Token: " + token + ", LastLogin: " + lastLogin);
            return new User(token, name, lastLogin, uuid, role);
        }
        Log.d(TAG, "getUserDetails: No user found");
        return null;
    }

    public void logOffUser() {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_IS_LOGGED_OFF, 1);

        int rowsUpdated = database.update(DatabaseHelper.TABLE_USER, values, DatabaseHelper.COLUMN_IS_LOGGED_OFF + " = 0", null);
        Log.d(TAG, "logOffUser: rowsUpdated=" + rowsUpdated);
    }

    public void clear() {
        int rowsDeleted = database.delete(DatabaseHelper.TABLE_USER, null, null);
        Log.d(TAG, "clear: rowsDeleted=" + rowsDeleted);
    }
}
