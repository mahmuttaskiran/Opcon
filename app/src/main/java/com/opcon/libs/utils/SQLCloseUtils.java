package com.opcon.libs.utils;

import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.Closeable;
import java.io.IOException;

/**
 *
 * Created by Mahmut Taşkiran on 25/12/2016.
 */

public class SQLCloseUtils {

    public static void close(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public static void close(Closeable closeable0, Closeable closeable1) {
        close(closeable0);
        close(closeable1);
    }

    public static void close(SQLiteOpenHelper sqLiteOpenHelper) {
        if (sqLiteOpenHelper != null) {
            sqLiteOpenHelper.close();
        }
    }

    public static void close(SQLiteOpenHelper sqLiteOpenHelper, Closeable db, Closeable cursor) {
        close(cursor);
        close(db);
        close(sqLiteOpenHelper);
    }

    public static void close(SQLiteOpenHelper sqLiteOpenHelper, Closeable db, Cursor cursor) {
        close(cursor);
        close(db);
        close(sqLiteOpenHelper);
    }

    public static void close(SQLiteOpenHelper sqLiteOpenHelper, Closeable cursor) {
        close(cursor);
        close(sqLiteOpenHelper);
    }

  public static boolean isPoolOrCloseOrNull(SQLiteOpenHelper helper) {
      return helper == null || !helper.getReadableDatabase().isOpen() || !helper.getWritableDatabase().isOpen();
  }
}
