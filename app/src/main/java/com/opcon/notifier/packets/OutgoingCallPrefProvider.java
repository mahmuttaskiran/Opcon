package com.opcon.notifier.packets;

import android.content.Context;

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

public class OutgoingCallPrefProvider implements SpecialPacketProvider {
    @Override
    public SpecialPacket getPacket(Context context) {

        SpecialPacket sp = new SpecialPacket(Packets._OUT_CALL);

        String number =  PreferenceUtils.getSecureString(context,PhoneStateReceiver.LAST_OUTGOING_CALL, "");
        Long timestamp = PreferenceUtils.getLong(context, PhoneStateReceiver.LAST_OUTGOING_CALL_TIMESTAMP, 0L);

        if (number.equals("") || timestamp == 0) {
            return sp;
        }

        String uLocale = PresenceManager.getUserLocale(context);
        if (MobileNumberUtils.checkValid(number, uLocale.toUpperCase())) {
            number = MobileNumberUtils.toInternational(number, uLocale, false);
        }

        if (System.currentTimeMillis() > timestamp + 20000) {
            return sp;
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
