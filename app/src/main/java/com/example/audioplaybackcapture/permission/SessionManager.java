package com.example.audioplaybackcapture.permission;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private final String MY_PREFS_NAME = "my_preferences";

    public SessionManager(Context context) {
        sharedPreferences =context.getSharedPreferences(MY_PREFS_NAME, Context.MODE_PRIVATE);
    }

    public void firstTimeAskingPermission(String permission, boolean isFirstTime) {
        doEdit();
        editor.putBoolean(permission, isFirstTime);
        doCommit();
    }

    public boolean isFirstTimeAskingPermission(String permission) {
        return sharedPreferences.getBoolean(permission, true);
    }

    private void doEdit() {
        if (editor == null) {
            editor = sharedPreferences.edit();
        }
    }

    private void doCommit() {
        if (editor != null) {
            editor.commit();
            editor = null;
        }
    }
}
