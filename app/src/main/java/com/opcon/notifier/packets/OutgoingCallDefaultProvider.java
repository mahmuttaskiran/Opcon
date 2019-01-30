package com.opcon.notifier.packets;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.CallLog;

import com.opcon.components.Message;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.utils.MobileNumberUtils;


/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class OutgoingCallDefaultProvider implements SpecialPacketProvider {

    @Override
    public SpecialPacket getPacket(Context context) {
        SpecialPacket sp = new SpecialPacket(Packets._OUT_CALL);

        ContentResolver contentResolver =
                context.getContentResolver();

        if (PermissionUtils.isReadCalLogPermissionGranted(context)) {
            try {
              Cursor cursor = contentResolver.query(
                  CallLog.Calls.CONTENT_URI,
                  new String[] {
                      CallLog.Calls._ID,
                      CallLog.Calls.TYPE,
                      CallLog.Calls.NUMBER,
                      CallLog.Calls.DATE,
                      CallLog.Calls.DURATION,
                  }, null, null, CallLog.Calls.DATE + " desc"
              );

              while (cursor != null && cursor.moveToNext()) {

                int __type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                if (__type == CallLog.Calls.OUTGOING_TYPE) {
                  String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                  long duration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));

                  String uLocale = PresenceManager.getUserLocale(context);
                  if (MobileNumberUtils.checkValid(number, uLocale.toUpperCase())) {
                    number = MobileNumberUtils.toInternational(number, uLocale, false);
                  }

                  sp.put(Message.InoutCall.WHO, number);
                  sp.put(Message.InoutCall.WHEN, duration);
                  break;
                }

              }
              if (cursor != null)
                cursor.close();
            } catch (SecurityException e) {
            e.printStackTrace();
            }
        }

        return sp;
    }

    @Override
    public SpecialPacket getForTest(Context context) {
        return getPacket(context);
    }
}
