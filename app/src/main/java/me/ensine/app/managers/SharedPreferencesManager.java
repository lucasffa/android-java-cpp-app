package me.ensine.app.managers;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesManager {
    private static final String PREF_NAME = "user_prefs";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_NAME = "name";
    private static final String KEY_LAST_LOGIN = "last_login";
    private static final String KEY_UUID = "uuid";
    private static final String KEY_ROLE = "role";

    private static SharedPreferencesManager instance;
    private SharedPreferences sharedPreferences;

    private SharedPreferencesManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPreferencesManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferencesManager(context);
        }
        return instance;
    }

    public void saveUserDetails(String token, String name, String lastLogin, String uuid, String role) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_TOKEN, token);
        editor.putString(KEY_NAME, name);
        editor.putString(KEY_LAST_LOGIN, lastLogin);
        editor.putString(KEY_UUID, uuid);
        editor.putString(KEY_ROLE, role);
        editor.apply();
    }

    public String getToken() {
        return sharedPreferences.getString(KEY_TOKEN, null);
    }

    public String getName() {
        return sharedPreferences.getString(KEY_NAME, null);
    }

    public String getLastLogin() {
        return sharedPreferences.getString(KEY_LAST_LOGIN, null);
    }

    public String getUuid() {
        return sharedPreferences.getString(KEY_UUID, null);
    }

    public String getRole() {
        return sharedPreferences.getString(KEY_ROLE, null);
    }

    public void clear() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
    }
}
