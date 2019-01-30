package com.opcon.notifier.environment.triggers;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.annotation.Nullable;

import com.opcon.database.KeyBackoff;
import com.opcon.utils.PreferenceUtils;

/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 *
 */

public class OutgoingSmsServiceProxy14 extends Service {

    private OutgoingSmsObserver mOutgoingSmsObserver;

    public static final String PREF_LAST_CAPTURED_SMS_TIMESTAMP = "lastTimeStampForOutSms";

  @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onDestroy() {
        getContentResolver().unregisterContentObserver(mOutgoingSmsObserver);
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        catchNonCatchMessages(getApplicationContext());
        if (mOutgoingSmsObserver != null) {
            getContentResolver().unregisterContentObserver(mOutgoingSmsObserver);
            mOutgoingSmsObserver = null;
        }
        mOutgoingSmsObserver = new OutgoingSmsObserver(new Handler(), getApplicationContext());
        getContentResolver().registerContentObserver(Uri.parse("content://sms"), true, mOutgoingSmsObserver);
        return START_STICKY;
    }

    public static void catchNonCatchMessages(Context context) {

        long lastTimestamp = PreferenceUtils.getLong(context, PREF_LAST_CAPTURED_SMS_TIMESTAMP, 0L);

        if (lastTimestamp == 0) {
            return;
        }

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, "date desc");

        long timestamp = 0;

        while (cursor != null && cursor.moveToNext()) {
            int __type;
            __type = cursor.getInt(cursor.getColumnIndex("type"));
            if (__type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX ||
                    __type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
                timestamp = cursor.getLong(cursor.getColumnIndex("date"));
                if (timestamp > lastTimestamp) {
                    String address = cursor.getString(cursor.getColumnIndex("address"));
                    String body = cursor.getString(cursor.getColumnIndex("body"));
                    context.sendBroadcast(OutgoingSmsReceiver.createIntent(address, body, timestamp));
                } else {
                    break;
                }
            }
        }

        if (timestamp != 0) {
            updateLastCapturesOutgoingSmsTimestamp(context, timestamp);
        }

        if (cursor != null)
            cursor.close();

    }

    private static void updateLastCapturesOutgoingSmsTimestamp(Context context, long timestamp) {
        PreferenceUtils.putLong(context, PREF_LAST_CAPTURED_SMS_TIMESTAMP, timestamp);
    }

    private static class OutgoingSmsObserver extends ContentObserver {

        Context mContext;

        private OutgoingSmsObserver(Handler handler, Context context) {
            super(handler);
            this.mContext = context.getApplicationContext();
        }
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            take(uri);
        }

        void take(Uri uri) {
            ContentResolver contentResolver = mContext.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, "date desc");

            String address = null;
            String body = null;
            long timestamp = 0;

            if (cursor != null && cursor.moveToNext()) {
                int type;
                int index = cursor.getColumnIndex("type");

                if (index == -1) {
                    cursor.close();
                    return;
                }

                type = cursor.getInt(index);
                if (type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT ||
                    type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX)
                {
                    address = cursor.getString(cursor.getColumnIndex("address"));
                    body = cursor.getString(cursor.getColumnIndex("body"));
                    timestamp = cursor.getLong(cursor.getColumnIndex("date"));


                    String key = address + timestamp;

                    if (KeyBackoff.getInstance(mContext).keyIsExactlyProcessed(key)) {
                        cursor.close();
                        return;
                    }

                }
            }

            if (cursor != null)
                cursor.close();

            if (address == null) {
                return;
            }

            updateLastCapturesOutgoingSmsTimestamp(mContext, timestamp);
            mContext.sendBroadcast(OutgoingSmsReceiver.createIntent(address, body, timestamp));
        }

        @Override
        public void onChange(boolean selfChange) {
            take(Uri.parse("content://sms"));
        }
    }

}
