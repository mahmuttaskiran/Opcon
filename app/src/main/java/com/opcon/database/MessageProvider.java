package com.opcon.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.opcon.R;
import com.opcon.components.Component;
import com.opcon.components.Dialog;
import com.opcon.components.Message;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.MessageDispatcher;
import com.opcon.libs.utils.DateUtils;
import com.opcon.libs.utils.SQLCloseUtils;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.adapters.MessageAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * Created by Mahmut Taşkiran on 08/11/2016.
 *
 */

public class MessageProvider {

    private static final String WAITING = "waiting";
    private static final String _ID = "_id";
    private static final String SID = "sid";
    private static final String TABLE = "messages";
    private static final String SENDER ="sender";
    private static final String RECEIVER ="receiver";
    public static final String RECEIVE_TIMESTAMP = "receiveTimestamp";
    public static final String SEEN_TIMESTAMP = "seenTimestamp";
    public static final String SENT_TIMESTAMP = "sentTimestamp";
    private static final String SPECIAL_PARAMS = "specialParams";
    private static final String TYPE = "type";
    private static final String TRIED_FOR_SERVER = "triedForServer";

    public MessageProvider() {
    }

    public static class Utils {

        public synchronized static void deleteWaitingMessagesThatDoesNotExists(Context context) {
            Cursor query = MessageBase.getInstance(context).openConnection().query(TABLE, null, WAITING + "=1", null, null, null, null);
            List<Integer> ids = new ArrayList<>();

            try {
                while (query != null && query.moveToNext()) {
                    Message msg = parse(query);
                    if (msg != null && msg.isImageMessage()) {
                        String file = msg.getString(Message.Picture.FILE);

                        if (!TextUtils.isEmpty(file)) {

                            Uri parse = Uri.parse(file);
                            if (parse != null) {
                                file = parse.getPath();
                            }

                            if (!new File(file).exists()) {
                                ids.add(msg.getId());
                            }
                        }
                    }
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }

            if (query != null) {
                query.close();
            }

            for (Integer id : ids) {
                delete(context, id);
                MessageDispatcher.getInstance().notifyDoesNotExists(id);
            }

            MessageBase.getInstance(context).closeConnection();
        }

        public synchronized static int setSingleLong(Context context, String withSid, String column, long newVal) {
            SQLiteDatabase sqLiteDatabase = MessageBase.getInstance(context).openConnection();
            ContentValues values = new ContentValues();
            values.put(column, newVal);
            int update = sqLiteDatabase.update(TABLE, values, "sid = ?", new String[]{withSid});
            MessageBase.getInstance(context).closeConnection();
            return update;
        }

        public synchronized static void deleteAllMessages(Context context, String destination) {
            MessageBase.getInstance(context).openConnection().delete(TABLE,  MessageProvider.SENDER + "= ? or " + MessageProvider.RECEIVER + "=?",
                new String[]{destination, destination});
            MessageBase.getInstance(context).closeConnection();
        }

        public synchronized static List<Integer> getMessages(Context context, String destination) {
            List<Integer> list = new ArrayList<>();

            if (TextUtils.isEmpty(destination)) {
                throw new IllegalArgumentException("Destination cannot be null.");
            }

            Cursor cursor = MessageBase.getInstance(context).openConnection().query(TABLE, new String[]{"_id", SENT_TIMESTAMP}, "(receiver = ? and sender = ?) or (receiver = ? and sender = ?)",
                new String[]{PresenceManager.uid(), destination,
                    destination, PresenceManager.uid()}, null, null, null);


            if (cursor == null || cursor.getCount() == 0 ) {
                if (cursor != null ) {
                    cursor.close();
                }
                MessageBase.getInstance(context).closeConnection();
                return list;
            }

            int count = cursor.getCount();
            if (count > 0) {

                    final int indexOfId = cursor.getColumnIndex("_id");
                    final int indexOfSentTimestamp = cursor.getColumnIndex(SENT_TIMESTAMP);
                    long lastTimestamp = 0;
                    while (cursor.moveToNext()) {
                        long timestamp = cursor.getLong(indexOfSentTimestamp);
                        boolean isDiffDay = DateUtils.isDifferentDays(timestamp, lastTimestamp);
                        boolean isDiffTime = DateUtils.isTimeRangeLargerThan(timestamp, lastTimestamp, 3 * (1000 * 60));

                        if (isDiffDay && isDiffTime) {
                            list.add(MessageAdapter.DATE_DIVIDER);
                            list.add(MessageAdapter.TIME_DIVIDER);
                            lastTimestamp = timestamp;
                        } else if (isDiffDay) {
                            list.add(MessageAdapter.DATE_DIVIDER);
                            lastTimestamp = timestamp;
                        } else if (isDiffTime) {
                            list.add(MessageAdapter.TIME_DIVIDER);
                            lastTimestamp = timestamp;
                        }

                        int id = cursor.getInt(indexOfId);
                        list.add(id);
                    }

            }
            cursor.close();
            MessageBase.getInstance(context).closeConnection();
            return list;
        }

        public synchronized static int newMessage(Context context, Message message) {
            SQLiteDatabase sqLiteDatabase = MessageBase.getInstance(context).openConnection();
            ContentValues values = new ContentValues();
            values.put(SENDER, message.getSender());
            values.put(RECEIVER, message.getReceiver());
            values.put(RECEIVE_TIMESTAMP,  message.getReceiveTimestamp());
            values.put(SEEN_TIMESTAMP,  message.getSeenTimestamp());
            values.put(SENT_TIMESTAMP, message.getSentTimestamp());
            values.put(TYPE, message.getType());
            values.put(TRIED_FOR_SERVER,  message.isTried());
            values.put(WAITING, message.isWaiting() ? 1: 0);
            values.put(SPECIAL_PARAMS, message.toJson().toString());
            values.put(SID, message.getSid());
            int id  = (int) sqLiteDatabase.insert(TABLE, null, values);
            MessageBase.getInstance(context).closeConnection();
            Log.d("MessageProvider", "newMessage: added: " + id+ ", " + message.getSid());
            return id;
        }

        public static synchronized Message getSingleMessage(Context context, String sid) {
            return getSingleMessage(context, "sid=?", new String[]{sid});
        }

        public static synchronized Message getSingleMessage(Context context, int id) {
           return getSingleMessage(context, "_id=" + id, null);
        }

        static synchronized Message parse(Cursor cursor) {
            Message ret = null;

            final int indexOfSender = cursor.getColumnIndex(MessageProvider.SENDER);
            final int indexOfReceiver = cursor.getColumnIndex(MessageProvider.RECEIVER);
            final int indexOfParams = cursor.getColumnIndex(MessageProvider.SPECIAL_PARAMS);
            final int indexOfReceiveTimestamp = cursor.getColumnIndex(MessageProvider.RECEIVE_TIMESTAMP);
            final int indexOfSEETimestamp = cursor.getColumnIndex(MessageProvider.SEEN_TIMESTAMP);
            final int indexOfSENTimestamp = cursor.getColumnIndex(MessageProvider.SENT_TIMESTAMP);
            final int indexOfType = cursor.getColumnIndex(MessageProvider.TYPE);
            final int indexOfTriedForServer = cursor.getColumnIndex(MessageProvider.TRIED_FOR_SERVER);
            final int indexOfId = cursor.getColumnIndex(MessageProvider._ID);
            final int indexOfSid = cursor.getColumnIndex(MessageProvider.SID);
            final int indexOfWaiting = cursor.getColumnIndex(MessageProvider.WAITING);

            Message.Builder builder = new Message.Builder();
            builder.setUID(cursor.getInt(indexOfId))
                .setSender(cursor.getString(indexOfSender))
                .setReceiver(cursor.getString(indexOfReceiver))
                .setType(cursor.getInt(indexOfType))
                .setTriedForServer(cursor.getInt(indexOfTriedForServer) == 1)
                .setReceiveTimestamp(cursor.getLong(indexOfReceiveTimestamp))
                .setSeenTimestamp(cursor.getLong(indexOfSEETimestamp))
                .setSentTimestamp(cursor.getLong(indexOfSENTimestamp))
                .setWaiting(cursor.getInt(indexOfWaiting) == 1)
                .setSpecialParam(Component.SID, cursor.getString(indexOfSid));

            String s_params = cursor.getString(indexOfParams);

            try {
                JSONObject __joParams = new JSONObject(s_params);
                ret = builder.built();
                ret.put(__joParams);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return ret;

        }

        static synchronized Message getSingleMessage(Context context, String projection, String[] args) {
            SQLiteDatabase sqLiteDatabase = MessageBase.getInstance(context).openConnection();
            Message ret = null;
            Cursor cursor = sqLiteDatabase.query(TABLE, null, projection, args, null, null, null);

            if (cursor != null && cursor.moveToNext()) {
                ret = parse(cursor);
            }
            if (cursor!=null)
                cursor.close();

            MessageBase.getInstance(context).closeConnection();

            return ret;
        }

        static synchronized int getNonSeenMessageLength(Context context, @NonNull String destination) {
            SQLiteDatabase sqLiteDatabase = MessageBase.getInstance(context).openConnection();
            Cursor query = sqLiteDatabase.query(TABLE,
                    new String[]{_ID}, SENDER + "=? and " + SEEN_TIMESTAMP + "<1",
                    new String[]{destination},
                    null, null, null);
            if (query == null) {
                MessageBase.getInstance(context).closeConnection();
                return 0;
            }
            int count = query.getCount();
            SQLCloseUtils.close(query);
            MessageBase.getInstance(context).closeConnection();
            return count;
        }

        public synchronized static void update(Context context, Message msg) {
            update(context, msg.getId(), msg);
        }

        public synchronized static void update(Context baseContext, int uid, Message message) {
            SQLiteDatabase sqLiteDatabase = MessageBase.getInstance(baseContext).openConnection();

            ContentValues values = new ContentValues();

            values.put(SENDER, message.getSender());
            values.put(RECEIVER,  message.getReceiver());
            values.put(RECEIVE_TIMESTAMP,  message.getReceiveTimestamp());
            values.put(SEEN_TIMESTAMP,  message.getSeenTimestamp());
            values.put(SENT_TIMESTAMP, message.getSentTimestamp());
            values.put(TYPE,  message.getType());
            values.put(WAITING, message.isWaiting() ? 1: 0);
            values.put(TRIED_FOR_SERVER,  message.isTried());
            values.put(SPECIAL_PARAMS, message.toJson().toString());
            values.put(SID, message.getString(Component.SID));

            sqLiteDatabase.update(TABLE, values, "_id = " + uid, null);
            MessageBase.getInstance(baseContext).closeConnection();
        }

        public static boolean sidExists(Context context, String sid) {
            if (sid != null) {
                Cursor query = MessageBase.getInstance(context).openConnection().query(TABLE, new String[]{_ID}, SID + "=?", new String[]{sid}, null, null, null);
                boolean exists = query.getCount() > 0;
                query.close();
                MessageBase.getInstance(context).closeConnection();
                return exists;
            }
            return false;
        }

        public static boolean sidDoesNotExists(Context context, String sid) {
            return (!sidExists(context, sid));
        }


        public static void removeSelf(Context c) {
            MessageBase.getInstance(c).openConnection().delete(TABLE, null, null);
            MessageBase.getInstance(c).closeConnection();
        }

        public static void delete(Context c, int delId) {
            MessageBase.getInstance(c).openConnection().delete(TABLE, _ID + "=" + delId, null);
            MessageBase.getInstance(c).closeConnection();
        }
    }

    private static class MessageBase extends SQLiteOpenHelper{

        private static final String DATABASE_NAME = "messages.db";
        private static final int DATABASE_VERSION = 14;

        private volatile static MessageBase instance;

        private SQLiteDatabase database;
        private int connectionCount;


        public static MessageBase getInstance(Context context) {
            if (instance == null) {
                synchronized (MessageBase.class) {
                    if (instance == null) {
                        instance = new MessageBase(context.getApplicationContext());
                    }
                }
            }
            return instance;
        }

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

        private static final String CREATE_MESSAGE_TABLE = "" +
                "CREATE TABLE messages (" +
                "_id INTEGER primary key autoincrement not null, " +
                "sid VARCHAR(50)," +
                "receiver VARCHAR(30)," +
                "sender VARCHAR(30)," +
                "receiveTimestamp INTEGER," +
                "seenTimestamp INTEGER," +
                "sentTimestamp INTEGER," +
                "type INTEGER," +
                "triedForServer INTEGER," +
                "specialParams VARCHAR(3000)," +
                "waiting integer, " +
                "dialogId INTEGER" +
                ")" +
                "";

        private MessageBase(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_MESSAGE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table if exists messages;");
            onCreate(db);
        }
    }

    /**
     *
     * Created by Mahmut Taşkiran on 28/11/2016.
     *
     */

    public static class DialogUtils {

        public synchronized static List<Dialog> getFromMessages(Context context) {
            List<Dialog> referenceOfDialogs = new ArrayList<>();
            SQLiteDatabase sqLiteDatabase = MessageBase.getInstance(context).openConnection();


            Cursor cursor = sqLiteDatabase.query(true, TABLE, new String[] {RECEIVER, SENDER},
                    null, null, null, null, null, null);

            while (cursor!=null && cursor.moveToNext()) {

                Dialog dialog;
                String destination;

                String receiver, sender;
                receiver = cursor.getString(cursor.getColumnIndex(RECEIVER));
                sender = cursor.getString(cursor.getColumnIndex(SENDER));


                if (receiver.equals(PresenceManager.uid())) {
                    destination = sender;
                } else {
                    destination = receiver;
                }

                dialog = new Dialog(destination);

                if (!referenceOfDialogs.contains(dialog)) {
                    dialog.name = ContactBase.Utils.getName(context, destination);
                    dialog.avatarPath = ContactBase.Utils.getValidAvatar(context, destination);
                    dialog.lastMessage = getLastMessage(context, destination);
                    dialog.lastNotifier = NotifierProvider.Utils.getLastNotifier(context, destination);
                    dialog.nonSeenMessageLength = MessageProvider.Utils.getNonSeenMessageLength(context, destination);
                    referenceOfDialogs.add(dialog);
                }

            }

            if (cursor!=null)
                cursor.close();

            MessageBase.getInstance(context).closeConnection();
            return referenceOfDialogs;
        }


        public synchronized static Message getLastMessage(Context context, String forWho) {
            Cursor cursor = MessageBase.getInstance(context).openConnection().query(TABLE,
                new String[]{_ID},
                "sender = ? or receiver = ?",
                new String[]{forWho, forWho},
                null,
                null,
                "_id desc",
                "1");

            int id = 0;
            if (cursor!=null && cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex(_ID));
            }
            if (cursor!=null)
                cursor.close();
            if (0 != id) {
                MessageBase.getInstance(context).closeConnection();
                return MessageProvider.Utils.getSingleMessage(context, id);
            }
            MessageBase.getInstance(context).closeConnection();
            return null;
        }

        public synchronized static String lastMessageToString(Context context, Message message) {

            if (message == null) {
                return "";
            }

            int type = message.getType();

            if (type == Message.TEXT) {
                return message.getString(Message.Text.BODY);
            } else if (type == Message.PICTURE) {
                if (message.isSenderAmI()) {
                    return context.getString(R.string.msg_to_string_sent_picture);
                } else {
                    return context.getString(R.string.msg_to_string_reiver_sent_picture);
                }
            } else if (message.isSpecialPacket()) {
                if (message.isSenderAmI()) {
                    return context.getString(R.string.msg_to_string_sent_specialpacket);
                } else {
                    return context.getString(R.string.msg_to_string_receiver_special_packet);
                }
            }

            return "";
        }

        private synchronized static String lastNotifierToString(Context context, Notifier notifierComponent) {
            if (notifierComponent == null) {
                return "";
            }

            if (notifierComponent.isOwnerAmI()) {
                return context.getString(R.string.last_message_as_str_owner_notifier);
            } else {
                return context.getString(R.string.last_message_as_str_target_notifier);
            }
        }


        public synchronized static String getDialogLastStr(Context context, Notifier notifierComponent, Message message) {
            long rt, mt;
            String rs, ms;
            rs = lastNotifierToString(context, notifierComponent);
            ms = lastMessageToString(context, message);
            rt = notifierComponent != null ? notifierComponent.getTimestamp() : 0;
            mt = message != null ? message.getSentTimestamp(): 0;
            if (rt > mt) {
                return rs;
            } else {
                return ms;
            }

        }

    }
}
