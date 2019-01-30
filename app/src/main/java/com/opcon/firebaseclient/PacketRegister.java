package com.opcon.firebaseclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 *
 * Created by Mahmut Taşkiran on 16/02/2017.
 */

public class PacketRegister {

  private volatile static PacketRegister instance;

  private PacketRegister(Context context) {
    mContext = context.getApplicationContext();
    mPackets = new ArrayList<>();
    Thread mShutDownHook = new Thread(new Runnable() {
      @Override
      public void run() {
        snapshot();
      }
    });
    Runtime.getRuntime().addShutdownHook(mShutDownHook);
    loadFromDB();
  }

  private Context mContext;
  private ArrayList<String> mPackets;

  private synchronized void snapshot() {
    removeAllFromDB();
    updateDB();
  }

  private void updateDB() {
    PacketBase pb = new PacketBase(mContext);
    SQLiteDatabase wd = pb.getWritableDatabase();
    ContentValues v = new ContentValues();
    for (String packet: mPackets) {
      if (packet != null) {
        v.put("id", packet);
        wd.insert("packets", null, v);
      }
    }
    wd.close();
    pb.close();
  }

  private void removeAllFromDB() {
    PacketBase pb = new PacketBase(mContext);
    SQLiteDatabase wd = pb.getWritableDatabase();
    wd.delete("packets", null, null);
    wd.close();
    pb.close();
  }

  private void loadFromDB() {
    PacketBase pb = new PacketBase(mContext);
    SQLiteDatabase rb = pb.getReadableDatabase();
    Cursor cursor = rb.query("packets", null, null, null, null, null, null);
    if (cursor != null && cursor.getCount() > 0) {
      int i_id;
      i_id = cursor.getColumnIndex("id");
      while (cursor.moveToNext()) {
        mPackets.add(cursor.getString(i_id));
      }
    }

    if (cursor != null) {
      cursor.close();
    }

    rb.close();
    pb.close();
  }

  /*

  an bug fixed.

     boolean isWrapped(String sid) {
    boolean contains = mPackets.contains(sid);
    if (contains)
      mPackets.remove(contains); !!! what is that !!!
    else
      mPackets.add(sid);
    return contains;
  }
   */

  boolean isWrapped(String sid) {
    boolean contains = mPackets.contains(sid);
    if (contains)
      mPackets.remove(sid);
    else
      mPackets.add(sid);
    return contains;
  }

  public static PacketRegister getInstance(Context context) {
    if (instance == null) {
      synchronized (PacketRegister.class) {
        if (instance == null) {
          instance = new PacketRegister(context);
        }
      }
    }
    return instance;
  }

  private static class PacketBase extends SQLiteOpenHelper {
    private PacketBase(Context context) {
      super(context, "PacketBase.db", null, 2);
    }
    @Override public void onCreate(SQLiteDatabase db) {
      db.execSQL("" +
          "create table packets (" +
          "id varchar(21) primary key not null" +
          ")" +
          "");
    }
    @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      db.execSQL("drop table if exists packets;");
      onCreate(db);
    }
  }

}
