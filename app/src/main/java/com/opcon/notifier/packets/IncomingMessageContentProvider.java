package com.opcon.notifier.packets;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.opcon.components.Message;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.utils.MobileNumberUtils;

/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class IncomingMessageContentProvider implements SpecialPacketProvider {
    @Override
    public SpecialPacket getPacket(Context context) {
        SpecialPacket sp = new SpecialPacket(Packets._IN_MSG);

        ContentResolver contentResolver =
                context.getContentResolver();

        Cursor cursor = contentResolver.query(Uri.parse("content://sms/inbox"),
                null, null, null, "date desc");

        if (cursor == null || cursor.getCount() == 0) {
            if (cursor != null) {
                cursor.close();
            }
            return sp;
        }

        if (cursor.moveToNext()) {
            String receiver = cursor.getString(cursor.getColumnIndex("address"));
            long date = cursor.getLong(cursor.getColumnIndex("date"));
            String body = cursor.getString(cursor.getColumnIndex("body"));

            String uLocale = PresenceManager.getUserLocale(context);
            if (MobileNumberUtils.checkValid(receiver, uLocale.toUpperCase())) {
                receiver = MobileNumberUtils.toInternational(receiver, uLocale, false);
            }

            sp.put(Message.InoutMessage.WHO, receiver);
            sp.put(Message.InoutMessage.WHEN, date);
            sp.put(Message.InoutMessage.WHAT, body);
        }

        cursor.close();

        return sp;
    }

    @Override
    public SpecialPacket getForTest(Context context) {
        return getPacket(context);
    }
}
