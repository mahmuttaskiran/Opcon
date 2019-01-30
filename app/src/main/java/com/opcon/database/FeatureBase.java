package com.opcon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.opcon.R;
import com.opcon.components.Dialog;
import com.opcon.components.Feature;
import com.opcon.ui.activities.FeatureActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmut Taşkiran on 14/04/2017.
 */

public class FeatureBase extends SQLiteOpenHelper {

  public static String OPCON_TEAM = "Opcon Team";

  public FeatureBase(Context context) {
    super(context.getApplicationContext(), "Features.db", null, 3);
  }

  public volatile static FeatureBase singleton;

  public static FeatureBase getInstance(Context context) {
    if (singleton == null) {
      synchronized (FeatureBase.class) {
        if (singleton == null) {
          singleton = new FeatureBase(context);
        }
      }
    }
    return singleton;
  }

  private SQLiteDatabase database;
  private int counter;

  private synchronized SQLiteDatabase openConnection() {
    counter ++;
    if (counter == 1) {
      database = getWritableDatabase();
    }
    return database;
  }

  private synchronized void closeConnection() {
    counter --;
    if (counter == 0) {
      database.close();
    }
  }

  @Override public void onCreate(SQLiteDatabase db) {
    db.execSQL("" +
        "create table features " +
        "(" +
        "uid varchar(50) primary key," +
        "params varchar(1000)," +
        "seen integer," +
        "deleted integer" +
        ");");
  }

  @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("drop table if exists features;");
    onCreate(db);
  }


  // is an new feature? (is don't exists before insert.)
  public boolean newFeature(Feature feature) {
    boolean isExists;
    Cursor n = openConnection().query("features", new String[]{"uid"}, "uid=?", new String[]{feature.getFeatureUID()}, null, null, null);
    isExists = n != null && n.moveToNext(); // isExists
    closeConnection();
    if (n != null ){
      n.close();
    }
    if (!isExists) {
      ContentValues values = new ContentValues();
      values.put("params", feature.toPureJson().toString());
      values.put("uid", feature.getFeatureUID());
      values.put("seen", 0);
      values.put("deleted", 0);
      openConnection().insert("features", null, values);
      closeConnection();
    }
    return !isExists;
  }

  public List<Feature> getFeatures() {
    Cursor features = openConnection().query("features", new String[]{"params"}, "deleted=0", null, null, null, null);
    List<Feature> list = new ArrayList<>();
    if (features != null) {
      while (features.moveToNext()) {
        try {
          list.add(new Feature(new JSONObject(features.getString(0))));
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
    }
    if (features != null ){
      features.close();
    }
    closeConnection();
    return list;
  }

  public void seen(String uid) {
    ContentValues values = new ContentValues();
    values.put("seen", 1);
    openConnection().update("features", values, "uid=?", new String[] {uid});
    closeConnection();
  }

  public Dialog prepareDialog(Context context) {

    Dialog dialog = null;
    Feature feature = null;
    Cursor features = openConnection().query("features", null, "deleted=0", null, null, null, null);


    if (features != null) {
      if (features.moveToLast()) {
        feature = new Feature(features.getString(features.getColumnIndex("params")));
      }
    }

    if (features != null) {
      features.close();
    }

    closeConnection();

    if (feature != null) {
      dialog = new Dialog(FeatureBase.OPCON_TEAM);
      dialog.name = context.getString(R.string.opcon_team);
      dialog.content = feature.getDialogContent();
      Cursor c = openConnection().query("features", new String[]{"uid"}, "seen=0", null, null, null, null);
      dialog.nonSeenMessageLength = c != null ? c.getCount(): 0;
      closeConnection();
      dialog.intent = FeatureActivity.getIntent(context);
      if (c != null) {
        c.close();
      }
    }

    return dialog;
  }

  public void delete(String uid) {
    ContentValues values = new ContentValues();
    values.put("deleted", 1);
    openConnection().update("features", values, "uid=?", new String[] {uid});
    closeConnection();
  }

  public void deleteAll() {
    ContentValues values = new ContentValues();
    values.put("deleted", 1);
    openConnection().update("features", values, "deleted=0", null);
    closeConnection();
  }

  public void removeSelf() {
    openConnection().delete("features", null, null);
    closeConnection();
  }

}
