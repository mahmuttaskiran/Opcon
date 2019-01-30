package com.opcon.notifier.packets;

import android.content.Context;
import android.text.TextUtils;

import com.opcon.components.Message;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.environment.triggers.PhoneStateReceiver;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.utils.MobileNumberUtils;
import com.opcon.utils.PreferenceUtils;

/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class IncomingCallPrefProvider implements SpecialPacketProvider {
    @Override
    public SpecialPacket getPacket(Context context) {
        SpecialPacket sp = new SpecialPacket(Packets._IN_CALL);

        String number = PreferenceUtils.getSecureString(context, PhoneStateReceiver.LAST_INCOMING_CALL, null);
        long timestamp =  PreferenceUtils.getLong(context, PhoneStateReceiver.LAST_INCOMING_CALL_TIMESTAMP, 0);

        if (TextUtils.isEmpty(number) || timestamp == 0) {
            return sp;
        }

        if (System.currentTimeMillis() > timestamp + 20000) {
            return sp;
        }

        String uLocale = PresenceManager.getUserLocale(context);
        if (MobileNumberUtils.checkValid(number, uLocale.toUpperCase())) {
            number = MobileNumberUtils.toInternational(number, uLocale, false);
        }


        sp.put(Message.InoutCall.WHO, number);
        sp.put(Message.InoutCall.WHEN, timestamp);
        return sp;
    }

    @Override
    public SpecialPacket getForTest(Context context) {
        return getPacket(context);
    }
}
