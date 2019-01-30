package com.opcon.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.opcon.R;
import com.opcon.libs.SecurePreferences;

/**
 *
 * Created by Mahmut Taşkiran on 11/10/2016.
 */

public class PreferenceUtils {

    static SharedPreferences sRef;

    public static void removeSelf(Context c) {
        SharedPreferences.Editor editorFor = getEditorFor(getPreferences(c));
        editorFor.clear();
        editorFor.commit();
    }

    public static void putString(Context context, String def, String value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editorFor = getEditorFor(preferences);
        editorFor.putString(def, value);
        editorFor.apply();
    }

    public static void putBoolean(Context context, String def, boolean value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editorFor = getEditorFor(preferences);
        editorFor.putBoolean(def, value);
        editorFor.apply();
    }

    public static void putLong(Context context, String def, long value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editorFor = getEditorFor(preferences);
        editorFor.putLong(def, value);
        editorFor.apply();
    }

    public static void putInt(Context context, String def, int value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editorFor = getEditorFor(preferences);
        editorFor.putInt(def, value);
        editorFor.apply();
    }

    public static void putFloat(Context context, String def, float value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editorFor = getEditorFor(preferences);
        editorFor.putFloat(def, value);
        editorFor.apply();
    }

    public static void putDouble(Context context, String def, double value) {
        SharedPreferences preferences = getPreferences(context);
        SharedPreferences.Editor editorFor = getEditorFor(preferences);
        editorFor.putFloat(def, (float) value);
        editorFor.apply();
    }

    public static int getInt(Context context, String def, int defaultVal) {
        return  getPreferences(context).getInt(def, defaultVal);
    }

    public static long getLong(Context context, String def, long defaultVal)
    {
        return  getPreferences(context).getLong(def, defaultVal);
    }

    public static String getString(Context context, String def, String defaultVal) {
        return  getPreferences(context).getString(def, defaultVal);
    }

    public static boolean getBoolean(Context context, String def, boolean defaultVal) {
        return  getPreferences(context).getBoolean(def, defaultVal);
    }

    public static float getFloat(Context context, String def, float defaultVal) {
        return  getPreferences(context).getFloat(def, defaultVal);
    }

    public static String getSecureString(Context context, String key, String defaultValue) {
        SecurePreferences securePreferences = new SecurePreferences(context);
        return securePreferences.getString(key, defaultValue);
    }

    public static void putSecureString(Context context, String key, String value) {
        SecurePreferences securePreferences = new SecurePreferences(context);
        SecurePreferences.Editor edit = securePreferences.edit();
        edit.putString(key, value);
        edit.apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        if (sRef == null) {
            sRef = PreferenceManager.getDefaultSharedPreferences(context);
        }
        return sRef;
    }

    private static SharedPreferences.Editor getEditorFor(SharedPreferences Preferences) {
        return Preferences.edit();
    }

}
