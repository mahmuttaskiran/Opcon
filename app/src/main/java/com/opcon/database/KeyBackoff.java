package com.opcon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.TimeUnit;

/**
 * Created by Mahmut Taşkiran on 06/04/2017.
 */

public class KeyBackoff extends SQLiteOpenHelper {

  public KeyBackoff(Context context) {
    super(context, "keys", null, 5);
  }

  private volatile static KeyBackoff instance;

  public static KeyBackoff getInstance(Context context) {
    if (instance ==null) {
      synchronized (KeyBackoff.class) {
        if (instance == null) {
          instance = new KeyBackoff(context);
          removeOldKeys(context, System.currentTimeMillis() - TimeUnit.DAYS.toMillis(10));
        }
      }
    }
    return instance;
  }

  private int counter;
  private SQLiteDatabase database;

  private synchronized SQLiteDatabase openConnection(){
    counter ++;
    if (counter == 1) {
      database= getWritableDatabase();
    }

    return database;
  }

  private synchronized void closeConnection(){
    counter --;
    if (counter==0) {
      database.close();
    }
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL("create table keys ('key' varchar(255) primary key not null, timestamp integer);");
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("drop table if exists keys;");
  }

  public synchronized boolean keyIsExactlyProcessed(String key, boolean updateTimestampIfKeyProcessed) {

    long timestamp;

    SQLiteDatabase connection = openConnection();
    Cursor keys = connection.query("keys", new String[]{"timestamp"}, "key = ?", new String[]{key}, null, null, null);
    boolean exists =false;
    if (keys!=null&&keys.moveToNext()) {
      timestamp = keys.getLong(0);
      exists = timestamp > 0;
      keys.close();
    }

    if (!exists) {
      ContentValues values = new ContentValues();
      values.put("key", key);
      values.put("timestamp",System.currentTimeMillis());
      connection.insert("keys", null, values);
    } else {
      if (updateTimestampIfKeyProcessed) {
        ContentValues values = new ContentValues();
        values.put("timestamp",System.currentTimeMillis());
        connection.update("keys", values, "key = ?", new String[]{key});
      }
    }


    closeConnection();
    return exists;
  }

  public boolean keyIsExactlyProcessed(String key) {
    return keyIsExactlyProcessed(key, false);
  }

  public synchronized boolean isKeyProcessedInDuration(String key, long duration, boolean updateTimestampIfKeyProcessed) {
    long now = System.currentTimeMillis();
    long timestamp = now;

    SQLiteDatabase connection = openConnection();
    Cursor keys = connection.query("keys", new String[]{"timestamp"}, "key = ?", new String[]{key}, null, null, null);
    boolean exists = false;
    if (keys!=null) {
      if (keys.moveToNext()) {
        timestamp = keys.getLong(0);
        exists = timestamp > 0;
      }
      keys.close();
    }

    if (!exists) {
      ContentValues values = new ContentValues();
      values.put("key", key);
      values.put("timestamp",now);
      connection.insert("keys", null, values);
    } else {
      if (updateTimestampIfKeyProcessed) {
        ContentValues values = new ContentValues();
        values.put("timestamp", now);
        connection.update("keys", values, "key = ?", new String[]{key});
      }
    }

    closeConnection();
    return exists && (timestamp > now - duration);
  }

  public boolean isKeyProcessedInDuration(String key, long duration) {
    return isKeyProcessedInDuration(key, duration, false);
  }

  public static void removeOldKeys(Context c, long than) {
    getInstance(c).openConnection().delete("keys", "timestamp < " + than, null);
    getInstance(c).closeConnection();
  }

}
