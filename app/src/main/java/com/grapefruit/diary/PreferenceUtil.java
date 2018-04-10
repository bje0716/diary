package com.grapefruit.diary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceUtil {

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;
    private Context context;

    public PreferenceUtil(Context context) {
        this.context = context;
        sp = PreferenceManager.getDefaultSharedPreferences(context);
        editor = sp.edit();
    }

    public boolean getBoolean(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

    public int getInt(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public String getString(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public void putBoolean(String key, boolean value) {
        editor.putBoolean(key, value).commit();
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value).commit();
    }

    public void putString(String key, String value) {
        editor.putString(key, value).commit();
    }

    public void clear() {
        editor.clear().commit();
    }

    public void remove(String key) {
        editor.remove(key).commit();
    }
}
