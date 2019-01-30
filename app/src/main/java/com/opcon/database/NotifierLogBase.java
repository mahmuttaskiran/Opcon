package com.opcon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import com.opcon.components.Component;
import com.opcon.components.NotifierLog;
import com.opcon.libs.utils.SQLCloseUtils;
import com.opcon.notifier.components.Notifier;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;



/**
 *
 * Created by Mahmut Taşkiran on 29/11/2016.
 *
 */

public class NotifierLogBase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "NotifierLogBase.db";
    private static final int VERSION = 9;

    private static final String TYPE = "type";
    private static final String TIMESTAMP = "timestamp";
    private static final String SEEN = "seen";
    private static final String NOTIFIER_ID = "notifierId";
    private static final String NOTIFIER_SID = "notifierSid";
    private static final String CREATE_TABLE = "CREATE TABLE logs (" +
            "_id integer primary key autoincrement not null," +
            "type integer," +
            "seen integer," +
            "timestamp integer, " +
            "notifierId integer," +
            "notifierSid varchar(21)," +
            "params varchar(1000)," +
            "sender varchar(30)" +
            ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists logs;");
        onCreate(db);
    }

    private NotifierLogBase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private volatile static NotifierLogBase instance;
    private SQLiteDatabase database;
    private int connectionCount;

    public static NotifierLogBase getInstance(Context context) {
        if (SQLCloseUtils.isPoolOrCloseOrNull(instance)) {
            synchronized (NotifierLogBase.class) {
                if (SQLCloseUtils.isPoolOrCloseOrNull(instance)) {
                    if (instance != null) {
                        instance.close();
                    }
                    instance = new NotifierLogBase(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public synchronized SQLiteDatabase openConnection() {
        connectionCount ++;
        if (connectionCount == 1) {
            database = getWritableDatabase();
        }
        return database;
    }

    public synchronized void closeConnection() {
        connectionCount --;
        if (connectionCount == 0) {
            database.close();
        }
    }



    public static class Utils {


        public static boolean sidExists(Context context, String sid) {
            if (sid != null) {
                Cursor query = getInstance(context).openConnection().query("logs", new String[]{"_id"}, NOTIFIER_SID + "=?", new String[]{sid}, null, null, null);
                boolean exists = query.getCount() > 0;
                query.close();
                getInstance(context).closeConnection();
                return exists;
            }
            return false;
        }


        public static List<Notifier> getDistinctNotifiers(Context context) {
            List<Notifier> list = new ArrayList<>();
            Cursor cr = getInstance(context).openConnection().query(true, "logs", new String[]{NOTIFIER_ID}, "type<>-100", null, null, null, null, null);
            if (cr != null) {
                while(cr.moveToNext()) {
                    int id = cr.getInt(cr.getColumnIndex(NOTIFIER_ID));
                    Notifier r = NotifierProvider.Utils.get(context, id);
                    if (r != null) {
                        list.add(r);
                    }
                }
                cr.close();
            }
            getInstance(context).closeConnection();
            return list;
        }

        public static List<NotifierLog> getNonSeenLogs(Context context, int id) {
            List<NotifierLog> logs = new ArrayList<>();
            Notifier notifier = NotifierProvider.Utils.get(context, id);

            if (notifier == null) {
                return logs;
            }

            String avatar = ContactBase.Utils.getValidAvatar(context, notifier.getRelationship());


            Cursor cursor = getInstance(context).openConnection().query("logs", null, NOTIFIER_ID + "= ? and seen = 0",
                    new String[] {String.valueOf(id)}, null, null, null);


            final int INDEX_TIMESTAMP = cursor.getColumnIndex(TIMESTAMP);
            final int INDEX_TYPE = cursor.getColumnIndex(TYPE);
            final int INDEX_ID = cursor.getColumnIndex("_id");
            final int INDEX_PARAMS = cursor.getColumnIndex("params");
            final int INDEX_SID = cursor.getColumnIndex(NOTIFIER_SID);
            final int INDEX_OF_SENDER = cursor.getColumnIndex("sender");

            NotifierLog notifierLog;
            while (cursor.moveToNext()) {
                notifierLog = new NotifierLog();

                notifierLog.setId(cursor.getInt(INDEX_ID));
                notifierLog.setType(cursor.getInt(INDEX_TYPE));
                notifierLog.setTimestamp(cursor.getLong(INDEX_TIMESTAMP));
                notifierLog.setNotifierSid(cursor.getString(INDEX_SID));
                notifierLog.setSender(cursor.getString(INDEX_OF_SENDER));

                String sParams = cursor.getString(INDEX_PARAMS);
                if (!TextUtils.isEmpty(sParams)) {
                    Component externalParams = new Component();
                    try {
                        externalParams.put(new JSONObject(sParams));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    notifierLog.setExternalParams(externalParams);
                }

                notifierLog.setAvatar(avatar);

                notifierLog.setNotifierId(cursor.getInt(cursor.getColumnIndex(NOTIFIER_ID)));
                notifierLog.setSeen(true);
                logs.add(notifierLog);
            }

            cursor.close();
            getInstance(context).closeConnection();
            return logs;
        }

        public static List<NotifierLog> getSeenLogs(Context context, int id) {

            String relationship = null;

            Notifier notifier = NotifierProvider.Utils.get(context, id);
            if (notifier != null) {
                relationship = notifier.getRelationship();
            }

            String avatar = ContactBase.Utils.getValidAvatar(context, relationship);


            Cursor cursor = getInstance(context).openConnection().query("logs", null, NOTIFIER_ID + "=? and seen = 1",
                    new String[] {String.valueOf(id)}, null, null, null);

            List<NotifierLog> logs = new ArrayList<>();

            final int INDEX_TIMESTAMP = cursor.getColumnIndex(TIMESTAMP);

            final int INDEX_TYPE = cursor.getColumnIndex(TYPE);
            final int INDEX_ID = cursor.getColumnIndex("_id");
            final int INDEX_PARAMS = cursor.getColumnIndex("params");
            final int INDEX_SID = cursor.getColumnIndex(NOTIFIER_SID);
            final int INDEX_OF_SENDER = cursor.getColumnIndex("sender");

            NotifierLog notifierLog;
            while (cursor.moveToNext()) {
                notifierLog = new NotifierLog();

                notifierLog.setId(cursor.getInt(INDEX_ID));
                notifierLog.setType(cursor.getInt(INDEX_TYPE));
                notifierLog.setTimestamp(cursor.getLong(INDEX_TIMESTAMP));
                notifierLog.setNotifierSid(cursor.getString(INDEX_SID));
                notifierLog.setSender(cursor.getString(INDEX_OF_SENDER));

                String sParams = cursor.getString(INDEX_PARAMS);
                if (!TextUtils.isEmpty(sParams)) {
                    Component externalParams = new Component();
                    try {
                        externalParams.put(new JSONObject(sParams));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    notifierLog.setExternalParams(externalParams);
                }


                notifierLog.setNotifierId(cursor.getInt(cursor.getColumnIndex(NOTIFIER_ID)));
                logs.add(notifierLog);
                notifierLog.setSeen( false );
                notifierLog.setAvatar(avatar);
            }

            cursor.close();
            getInstance(context).closeConnection();
            return logs;
        }

        public static int getNonSeenLogCount(Context context, int id) {
            int count = (int) DatabaseUtils.queryNumEntries(getInstance(context).openConnection(), "logs", "notifierId = ? and seen = 0",
                    new String[] {String.valueOf(id)});
            getInstance(context).closeConnection();
            return count;
        }

        public static int getLogCount(Context context, int id){
            int count = (int) DatabaseUtils.queryNumEntries(getInstance(context).openConnection(), "logs", "notifierId = ?",
                new String[] {String.valueOf(id)});
            getInstance(context).closeConnection();
            return count;
        }

        public static void setSeen(Context context, List<NotifierLog> logs) {
            if (logs != null) {
                ContentValues values = new ContentValues();
                values.put(SEEN, 1);
                SQLiteDatabase readableDatabase = getInstance(context).openConnection();
                for (NotifierLog log: logs) {
                    readableDatabase.update("logs", values, "_id = ?", new String[]{String.valueOf(log.getId())});
                }
            }
            getInstance(context).closeConnection();
        }

        public static void delete(Context context, int id) {
            getInstance(context).openConnection().delete("logs", NOTIFIER_ID + "=?", new String[]{String.valueOf(id)});
            getInstance(context).closeConnection();
        }

        public static void delete(Context context, String sid) {
            getInstance(context).openConnection().delete("logs", NOTIFIER_SID + "=?", new String[]{String.valueOf(sid)});
            getInstance(context).closeConnection();
        }

        public static int newLog(Context context, NotifierLog log) {

            ContentValues values = new ContentValues();

            values.put(TYPE, log.getType());
            values.put(TIMESTAMP, log.getTimestamp());
            values.put(SEEN, log.isSeen() ? 1: 0);
            values.put(NOTIFIER_ID, log.getNotifierId());
            values.put(NOTIFIER_SID, log.getNotifierSid());
            values.put("sender", log.getSender());

            values.put("params", log.getExternalParams() != null ? log.getExternalParams().toJson().toString() : null );
            int insertedId = (int) getInstance(context).openConnection().insert("logs", null, values);
            getInstance(context).closeConnection();
            return insertedId;
        }
    }

}
