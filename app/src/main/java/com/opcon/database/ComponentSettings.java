package com.opcon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 * Created by Mahmut Taşkiran on 09/12/2016.
 *
 */

public class ComponentSettings extends SQLiteOpenHelper {

    private static final String DB_NAME = "ProfileSettings.db";
    private static final int    VERSION = 2;

    public static final String NOTIFICATION_SOUND = "booleanField1";
    public static final String NOTIFICATION = "booleanField2";

    private ComponentSettings(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table settings (" +
                "destination varchar (50) not null," +
                "booleanField1 integer," +
                "booleanField2 integer, " +
                "booleanField3 integer" +
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists settings;");
        onCreate(db);
    }

    private int counter;
    private SQLiteDatabase database;

    private synchronized SQLiteDatabase openConnection() {
        counter++;
        if (counter == 1) {
            database = getWritableDatabase();
        }
        return database;
    }

    private synchronized void closeConnection() {
        counter--;
        if (counter == 0) {
            database.close();
        }
    }

    private volatile static ComponentSettings instance;

    public static ComponentSettings getInstance(Context context) {
        if (instance == null) {
            synchronized (ComponentSettings.class) {
                if (instance == null) {
                    instance = new ComponentSettings(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public static boolean getBoolean(Context c, String destination, final String fn, boolean d)
    {
        if (destination == null || fn == null) {
            return false;
        }

        Cursor cursor = getInstance(c).openConnection().query("settings", new String[]{fn}, "destination = ?", new String[]{destination}, null, null, null);

        while (cursor != null && cursor.moveToNext()) {
            int indexOfField = cursor.getColumnIndex(fn);
            if (!cursor.isNull(indexOfField)) {
                d = cursor.getInt(indexOfField) == 1;
            }
        }

        if (cursor != null)
            cursor.close();

        getInstance(c).closeConnection();

        return d;
    }

    public static void setBoolean(Context context, String destination, final String fieldName, boolean value) {

        if (destination == null || fieldName == null) {
            return;
        }

        SQLiteDatabase db = getInstance(context).openConnection();
        Cursor q = db.rawQuery("select destination from settings where destination = ?", new String[]{destination});

        ContentValues contentValues = new ContentValues();
        contentValues.put(fieldName, value);
        if (q != null && q.moveToNext()) {
            // update
            db.update("settings", contentValues, "destination = ?", new String[]{destination});

        } else {
            // create
            contentValues.put("destination", destination);
            db.insert("settings", null,  contentValues);
        }
        if (q != null) {
            q.close();
        }
        getInstance(context).closeConnection();
    }

    public static boolean isNotificationSoundOn(Context c, String d) {
        return getBoolean(c, d, NOTIFICATION_SOUND, true);
    }

    public static void setNotificationSound(Context c, String d, boolean state) {
        setBoolean(c, d, NOTIFICATION_SOUND, state);
    }

    public static boolean isNotificationOn(Context c, String d) {
        return getBoolean(c, d, NOTIFICATION, true);
    }

    public static void setNotification(Context c, String d, boolean state) {
        setBoolean(c, d, NOTIFICATION, state);
    }

}
