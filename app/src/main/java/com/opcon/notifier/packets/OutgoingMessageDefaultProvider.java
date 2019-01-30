package com.opcon.notifier.packets;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;

import com.opcon.components.Message;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.notifier.environment.triggers.OutgoingSmsReceiver;

/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class OutgoingMessageDefaultProvider implements SpecialPacketProvider {



    @Override
    public SpecialPacket getForTest(Context context) {
        return getPacket(context);
    }

    @Override
    public SpecialPacket getPacket(Context context) {
        SpecialPacket sp = new SpecialPacket(Packets._OUT_MSG);

        ContentResolver contentResolver = context.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://sms"), null, null, null, "date desc");

        long timestamp;

        while (cursor != null && cursor.moveToNext()) {
            int __type;
            __type = cursor.getInt(cursor.getColumnIndex("type"));
            System.out.println(String.format("TEST! %d", __type));
            if (__type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_OUTBOX ||
                __type == Telephony.TextBasedSmsColumns.MESSAGE_TYPE_SENT) {
                timestamp = cursor.getLong(cursor.getColumnIndex("date"));
                String address = cursor.getString(cursor.getColumnIndex("address"));
                String body = cursor.getString(cursor.getColumnIndex("body"));
                context.sendBroadcast(OutgoingSmsReceiver.createIntent(address, body, timestamp));
                sp.put(Message.InoutMessage.WHAT, body);
                sp.put(Message.InoutMessage.WHEN, timestamp);
                sp.put(Message.InoutMessage.WHO, address);
                break;
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        return sp;
    }
}
