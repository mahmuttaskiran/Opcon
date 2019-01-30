package com.opcon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.opcon.components.Component;
import com.opcon.components.Dialog;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.utils.SQLCloseUtils;
import com.opcon.notifier.components.Notifier;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Mahmut Taşkiran on 17/10/2016.
 *
 */

public class NotifierProvider {

    private static final String DESCRIPTION = "description";
    public static  final String OWNER = "owner";
    public static  final String TARGET = "target";
    public static  final String STATE = "state";
    public static  final String IS_GLOBAL = "is_global";
    private static final String SID = "sid";
    public static  final String LID = "_id";
    private static final String CONDITION_PARAMS = "condition_params";
    private static final String CONDITION_DESC = "condition_desc";
    private static final String CONDITION_TYPE = "condition_type";
    private static final String OPERATION_PARAMS = "operation_params";
    private static final String PACKET_TYPE = "packetType";
    private static final String OPERATION_TYPE = "operation_type";
    private static final String OPERATION_DESC = "operation_desc";
    private static final String TIMESTAMP = "timestamp";
    private static final String LAST_CHECK_TIME = "lastCheckTimestamp";
    private static final String REVERSED = "reversed";
    private static final String CONDITION_PROGRESSABLE = "progressable";
    public static  final String RELATIONSHIP_STATE = "relationshipState";
    private static final String LAST_APPLY_TIMESTAMP = "lastApplyTimestamp";
    private static final String APPLY_LENGTH = "applyLength";
    private static final String TABLE = "notifiers";



    public static class Utils {

        public static synchronized Notifier parse(Cursor c) {
            if (c == null || c.getCount() < 1) return null;
            Notifier.Builder builder = new Notifier.Builder();
            builder.setUID(c.getInt(c.getColumnIndex(NotifierProvider.LID)))
                .setState(c.getInt(c.getColumnIndex(NotifierProvider.STATE)))
                .setRelationshipState(c.getInt(c.getColumnIndex(NotifierProvider.RELATIONSHIP_STATE)))
                .setDescription(c.getString(c.getColumnIndex(NotifierProvider.DESCRIPTION)))
                .setCondition(c.getString(c.getColumnIndex(NotifierProvider.CONDITION_PARAMS)))
                .setOperation(c.getString(c.getColumnIndex(NotifierProvider.OPERATION_PARAMS)))
                .setOwner(c.getString(c.getColumnIndex(NotifierProvider.OWNER)))
                .setTarget(c.getString(c.getColumnIndex(NotifierProvider.TARGET)))
                .setTimestamp(c.getLong(c.getColumnIndex(NotifierProvider.TIMESTAMP)))
                .setProfileUpdater(c.getInt(c.getColumnIndex("profileUpdater")) == 1)
                .setSID(c.getString(c.getColumnIndex(NotifierProvider.SID)));
            return builder.built();
        }

        public static boolean hasActiveCameraConditionFor(Context c, String relationship) {
            Cursor notifiers = NotifierBase.getInstance(c).openConnection().query("notifiers", new String[]{LID},
                "progressable = 1 and state = 3 and (condition_type = 14 or condition_type = 8) and " +
                    "(owner = ? or target = ?);",
                new String[]{relationship, relationship}, null, null, null);
            boolean exists = notifiers != null && notifiers.moveToNext();
            if (notifiers != null) {
                notifiers.close();
            }
            NotifierBase.getInstance(c).closeConnection();
            return exists;
        }

        public static int isExistsProfileUpdater(Context context, String conditionParams, String operationParams, int packetType)
        {
            Cursor notifiers = NotifierBase.getInstance(context).openConnection().query(TABLE, new String[]{LID}, "condition_params = ? and operation_params = ? and packetType = ? and target =?", new String[]{String.valueOf(conditionParams), String.valueOf(operationParams), String.valueOf(packetType), "PROFILE_UPDATER"}, null, null, null);
            int id = -1;
            if (notifiers != null) {
                if (notifiers.moveToNext()) {
                    id = notifiers.getInt(0);
                }
                notifiers.close();
            }
            return id;
        }

        public static int isNotifierExistsForTarget(Context context, String relationship,
                                                    String conditionParams, String operationParams,
                                                    int packetType)
        {
            Cursor notifiers = NotifierBase.getInstance(context).openConnection().query(TABLE, new String[]{LID}, "condition_params = ? and operation_params = ? and packetType = ? and (target =?)", new String[]{String.valueOf(conditionParams), String.valueOf(operationParams), String.valueOf(packetType), relationship}, null, null, null);
            int id = -1;
            if (notifiers != null) {
                if (notifiers.moveToNext()) {
                    id = notifiers.getInt(0);
                }
                notifiers.close();
            }
            return id;
        }

        public static Notifier getLastNotifier(Context context, String relationship) {
            Cursor query = NotifierBase.getInstance(context).openConnection().query(TABLE, null, "target = ? or owner = ?", new String[]{relationship, relationship}, null, null, "_id desc", "1");
            if (query == null) {
                NotifierBase.getInstance(context).closeConnection();
                return null;
            }
            query.moveToFirst();
            Notifier parsed = parse(query);
            NotifierBase.getInstance(context).closeConnection();
            return parsed;
        }

        private static List<String> getRelationships(Context context) {
            Cursor query = NotifierBase.getInstance(context).openConnection().query(TABLE, new String[]{TARGET, OWNER}, null, null, null, null, null);
            List<String> relationships = new ArrayList<>();
            if (query == null) {
                NotifierBase.getInstance(context).closeConnection();
                return relationships;
            }

            while (query.moveToNext()) {
                String r1 = query.getString(0);
                String r2 = query.getString(1);

                String f = r1;

                if (PresenceManager.uid().equals(f)) {
                    f = r2;
                }
                if (!relationships.contains(f)) {
                    relationships.add(f);
                }
            }

            query.close();
            return relationships;
        }

        public static synchronized void getAndPutDialogs(Context context, @NonNull List<Dialog> dialogs) {
            List<String> relationships = getRelationships(context);
            for (String relationship : relationships) {
                if (relationship.equals("PROFILE_UPDATER"))
                    continue;
                Dialog dialog = new Dialog(relationship);
                if (!dialogs.contains(dialog)) {
                    dialog.avatarPath = ContactBase.Utils.getValidAvatar(context, relationship);
                    dialog.name = ContactBase.Utils.getName(context, relationship);
                    dialog.lastNotifier = getLastNotifier(context, relationship);
                    dialogs.add(dialog);
                }
            }
        }



        public static synchronized List<Notifier> get(Context context, String query, String[] queryParams) {
            Cursor cursor = NotifierBase.getInstance(context).openConnection().query(TABLE,
                null,
                query,
                queryParams,
                null,
                null,
                null);

            List<Notifier> notifiers = new ArrayList<>();
            if (cursor == null) {
                NotifierBase.getInstance(context).closeConnection();
                return notifiers; // as empty!
            }
            while (cursor.moveToNext()) {
                Notifier parsed = parse(cursor);
                if (parsed != null) {
                    notifiers.add(parsed);
                }
            }

            cursor.close();
            NotifierBase.getInstance(context).closeConnection();
            return notifiers;
        }

        public static synchronized Notifier get(Context context, int id) {
            Cursor cursor = NotifierBase.getInstance(context).openConnection().query(TABLE,
                null,
                LID + "=" + id,
                null,
                null,
                null,
                null);

            if (cursor == null) {
                NotifierBase.getInstance(context).closeConnection();
                return null;
            }

            cursor.moveToNext();

            Notifier r = parse(cursor);
            NotifierBase.getInstance(context).closeConnection();
            return r;
        }

        public static synchronized Notifier get(Context context, String sid) {
            Cursor cursor = NotifierBase.getInstance(context).openConnection().query(TABLE,
                null,
                SID + "=?",
                new String[]{sid},
                null,
                null,
                null);

            if (cursor == null) {
                NotifierBase.getInstance(context).closeConnection();
                return null;
            }

            cursor.moveToNext();

            Notifier r = parse(cursor);
            NotifierBase.getInstance(context).closeConnection();
            return r;
        }

        public static synchronized List<Notifier> getProfileUpdaters(Context context) {
            return get(context, "profileUpdater = 1 and target = 'PROFILE_UPDATER'", null);
        }

        public static synchronized List<Notifier> getProgressableNotifiersWithConditionType(Context context, int conditionType, int conditionType2) {
            return get(context, "(" + NotifierProvider.CONDITION_TYPE + "=? or " + NotifierProvider.CONDITION_TYPE + "=?) and " + NotifierProvider.STATE + "=? and " + NotifierProvider.CONDITION_PROGRESSABLE + "=?",
                new String[] {
                    String.valueOf(conditionType), String.valueOf(conditionType2), String.valueOf(Notifier.RUNNING), String.valueOf(1)
                });
        }

        public static synchronized List<Notifier> getRelationalNotifiers(Context context, String destination) {
            return get(context, OWNER + "=? or " + TARGET + "=?", new String[]{destination, destination});
        }

        public static synchronized List<Notifier> getProgressableNotifiers(Context context) {
            return get(context, NotifierProvider.CONDITION_PROGRESSABLE
                + "=1 and " + NotifierProvider.STATE + "=" + Notifier.RUNNING, null);
        }

        public static void removeSelf(Context c) {
            NotifierBase.getInstance(c).openConnection().delete(TABLE, null, null);
            NotifierBase.getInstance(c).closeConnection();
        }

        public synchronized static boolean isReversed(Context context, int id) {
            Cursor query = NotifierBase.getInstance(context).openConnection().query(TABLE,
                new String[]{REVERSED},
                LID + "=" + id,
                null,
                null,
                null,
                null);
            if (query != null) {
                if (query.moveToNext()) {
                    int reversed = query.isNull(0) ? 1: query.getInt(0);
                    query.close();
                    return reversed == 1;
                }
            }
            NotifierBase.getInstance(context).closeConnection();
            return true;
        }

        public synchronized static void setReversed(Context context, int id, boolean reversed) {
            ContentValues values = new ContentValues();
            values.put(REVERSED, reversed ? 1: 0);
            NotifierBase.getInstance(context).openConnection().update(TABLE, values, LID + "=?", new String[] {String.valueOf(id)});
            NotifierBase.getInstance(context).closeConnection();
        }

        public static ArrayList<Object> getNotifiers(Context context, @Nullable String u1, @Nullable String u2) {

            ArrayList<Object> list = new ArrayList<>();

            Cursor cursor;


            if (u1 != null && u2 != null) {
                String filter = String.format("(%s = ? and %s = ?) or (%s = ? and %s = ?)",
                        TARGET, OWNER, OWNER, TARGET);
                cursor = NotifierBase.getInstance(context).openConnection().query(TABLE, new String[]{"_id"}, filter, new String[]{u1, u2, u1, u2}, null, null, null);
            } else {
                cursor = NotifierBase.getInstance(context).openConnection().query(TABLE, new String[]{"_id"}, null, null, null, null, null);
            }

            if(cursor != null && cursor.getCount() > 0) {
                int indexOfId = cursor.getColumnIndex("_id");
                while (cursor.moveToNext()) {
                    int id = cursor.getInt(indexOfId);
                    list.add(id);
                }
            }

            if (cursor != null)
                cursor.close();

            NotifierBase.getInstance(context).closeConnection();
            return list;

        }

        public synchronized static void updateLastApplyTime(Context context, int lid, long timestamp) {

            ContentValues values = new ContentValues();
            values.put(LAST_APPLY_TIMESTAMP, timestamp);
            NotifierBase.getInstance(context).openConnection().update(TABLE, values,
                    NotifierProvider.LID + "=?", new String[] {String.valueOf(lid)});
            NotifierBase.getInstance(context).closeConnection();
        }

        private synchronized static int getApplyLength(Context context, int lid) {
            return (Integer) getSingleColumnRow(context,
                    Integer.class, NotifierProvider.APPLY_LENGTH,
                    NotifierProvider.LID + "=?", new String[]{String.valueOf(lid)});
        }

        private synchronized static void setApplyLength(Context context, int lid, int length) {
            ContentValues values = new ContentValues();
            values.put(APPLY_LENGTH, length);
            NotifierBase.getInstance(context).openConnection().update(TABLE,
                    values, LID + "=?", new String[]{String.valueOf(lid)});
            NotifierBase.getInstance(context).closeConnection();
        }

        public synchronized static void upgradeApplyLength(Context context, int lid) {
            int length = getApplyLength(context, lid);
            length ++;
            setApplyLength(context, lid, length);
        }

        public synchronized static Object getSingleColumnRow(Context context,
                                                             Class<?> klass,
                                                             String projection,
                                                             String where,
                                                             String[] whereArgs) {


            Cursor query = NotifierBase.getInstance(context).openConnection().query(TABLE,
                    new String[]{projection},
                    where, whereArgs, null, null, null);

            Object retObj = null;

            if(query != null && query.moveToNext()) {
                int INDEX_OF_PROJECTION = query.getColumnIndex(projection);
                if (klass == String.class) {
                    retObj = query.getString(INDEX_OF_PROJECTION);
                } else if (klass == Integer.class) {
                    retObj = query.getInt(INDEX_OF_PROJECTION);
                } else if (klass == Long.class) {
                    retObj = query.getLong(INDEX_OF_PROJECTION);
                }
            }
            if (query != null)
                query.close();

            NotifierBase.getInstance(context).closeConnection();

            return retObj;
        }

        public synchronized static long updateNotifier(Context context, Notifier notifier) {
            ContentValues v = new ContentValues();
            v.put(CONDITION_DESC, notifier.getConditionDescription(context));
            v.put(OPERATION_DESC, notifier.getOperationDescription(context));
            v.put(DESCRIPTION, notifier.getDescription());
            v.put(SID, notifier.getString(Component.SID));
            v.put(CONDITION_TYPE, notifier.getCondition().getId());
            v.put(OPERATION_TYPE, notifier.getOperation().getId());
            if (notifier.getRelationshipState() != -1) {
                v.put(RELATIONSHIP_STATE, notifier.getRelationshipState());
            }
            v.put(STATE, notifier.getState());
            v.put("profileUpdater", notifier.isProfileUpdater() ? 1: 0);
            v.put(CONDITION_PARAMS, notifier.getCondition().toJson().toString());
            v.put(OPERATION_PARAMS, notifier.getOperation().toJson().toString());
            v.put(TARGET, notifier.getReceiver());
            v.put(OWNER, notifier.getSender());
            v.put(CONDITION_PROGRESSABLE, notifier.isConditionProgressable() ? 1: 0);
            v.put(TIMESTAMP, notifier.getTimestamp());
            int id = NotifierBase.getInstance(context).openConnection().update(TABLE, v,  LID + "=" + notifier.getId(), null);
            NotifierBase.getInstance(context).closeConnection();
            return id;
        }

        public static synchronized void deleteIsNecessary(Context context) {
            // TODO !!!
        }

        public synchronized static int newNotifier(Context context, Notifier notifier) {
            ContentValues v = new ContentValues();
            v.put(CONDITION_DESC, notifier.getConditionDescription(context));
            v.put(OPERATION_DESC, notifier.getOperationDescription(context));
            v.put(DESCRIPTION, notifier.getDescription());
            v.put(SID, notifier.getSid());
            v.put(CONDITION_TYPE, notifier.getCondition().getId());
            v.put(OPERATION_TYPE, notifier.getOperation().getId());
            v.put(RELATIONSHIP_STATE, notifier.getRelationshipState());
            v.put("profileUpdater", notifier.isProfileUpdater() ? 1: 0);
            v.put(STATE, notifier.getState());
            v.put(PACKET_TYPE, notifier.getOperation().getPacketType());
            v.put(CONDITION_PARAMS, notifier.getCondition().toJson().toString());
            v.put(OPERATION_PARAMS, notifier.getOperation().toJson().toString());
            v.put(TARGET, notifier.getReceiver());
            v.put(OWNER, notifier.getSender());
            v.put(CONDITION_PROGRESSABLE, notifier.isConditionProgressable() ? 1: 0);
            v.put(TIMESTAMP, notifier.getTimestamp());
            int id = (int) NotifierBase.getInstance(context).openConnection().insert(TABLE, null, v);
            NotifierBase.getInstance(context).closeConnection();
            return id;
        }

        public static void updateSingleInt(Context context, int id, String column, int value) {
            ContentValues values = new ContentValues();
            values.put(column, value);

            NotifierBase.getInstance(context).openConnection().update(TABLE, values,
                NotifierProvider.LID + "=?", new String[]{String.valueOf(id)});
            NotifierBase.getInstance(context).closeConnection();
        }

        public static void delete(Context context, int lid) {
            Notifier notifier = get(context, lid);
            if (notifier != null) {
                NotifierBase.getInstance(context).openConnection().delete(TABLE, LID + "=" + lid, null);
                NotifierBase.getInstance(context).closeConnection();
                NotifierLogBase.Utils.delete(context, lid);
                if (notifier.getSid() != null ){
                    NotifierLogBase.Utils.delete(context, notifier.getSid());
                }
            }
            // delete relation logs...
        }

        public static long getLastCheckTime(Context mContext, int id) {
            Cursor cursor = NotifierBase.getInstance(mContext).openConnection().query(TABLE, new String[]{LAST_CHECK_TIME}, LID + "=" + id, null, null, null, null);
            long lastCheckTime = 0;
            if (cursor != null && cursor.moveToFirst()) {
                lastCheckTime = cursor.getLong(cursor.getColumnIndex(LAST_CHECK_TIME));
                cursor.close();
            }
            NotifierBase.getInstance(mContext).closeConnection();
            return lastCheckTime;
        }

        public static void setLastCheckTime(Context context, int mNotifierId, long timestamp) {
            ContentValues values = new ContentValues();
            values.put(LAST_CHECK_TIME, timestamp);

            NotifierBase.getInstance(context).openConnection().update(TABLE,
                    values, NotifierProvider.LID + "=?", new String[]{String.valueOf(mNotifierId)});
            NotifierBase.getInstance(context).closeConnection();
        }


        public static boolean sidExists(Context context, String sid) {
            if (sid != null) {
                Cursor query = NotifierBase.getInstance(context).openConnection().query(TABLE, new String[]{LID}, SID + "=?", new String[]{sid}, null, null, null);
                boolean exists = query.getCount() > 0;
                query.close();
                NotifierBase.getInstance(context).closeConnection();
                return exists;
            }
            return false;
        }

    }

    private static class NotifierBase extends SQLiteOpenHelper {

        private static final String DATABASE_NAME = "notifiers.db";
        private static final int VERSION = 22;


        private volatile static NotifierBase singleton;

        private SQLiteDatabase database;
        private int connectionCount;

        synchronized SQLiteDatabase openConnection() {
            connectionCount ++;
            if (connectionCount == 1) {
                database = getWritableDatabase();
            }
            return database;
        }

        synchronized void closeConnection() {
            connectionCount --;
            if (connectionCount == 0) {
                database.close();
            }
        }

        private static NotifierBase getInstance(Context context) {
            if (SQLCloseUtils.isPoolOrCloseOrNull(singleton)) {
                synchronized (NotifierBase.class){
                    if (SQLCloseUtils.isPoolOrCloseOrNull(singleton)) {
                        if (singleton != null) {
                            singleton.close();
                        }
                        singleton = new NotifierBase(context.getApplicationContext());
                    }
                }
            }
            return singleton;
        }

        private static final String CREATE_TABLE = "create table notifiers (" +
                "_id integer primary key autoincrement not null," +
                "sid varchar(15)," +
                "description varchar(300)," +
                "condition_desc varchar(100)," +
                "operation_desc varchar(100)," +
                "condition_type integer," +
                "operation_type integer," +
                "condition_params varchar(300)," +
                "operation_params varchar(300)," +
                "owner varchar(50)," +
                "target varchar(50)," +
                "state integer," +
                "relationshipState integer," +
                "progressable integer, " +
                "is_global integer," +
                "timestamp integer," +
                "lastApplyTimestamp integer," +
                "applyLength integer, " +
                "specialParams varchar (3000), " +
                "reversed integer," +
                "lastCheckTimestamp integer," +
                "profileUpdater integer," +
                "packetType integer" +
                ")";

        private NotifierBase(Context context) {
            super(context, DATABASE_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists notifiers;");
            onCreate(db);
        }
    }


}
