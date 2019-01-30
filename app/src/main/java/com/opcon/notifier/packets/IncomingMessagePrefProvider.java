package com.opcon.notifier.packets;

import android.content.Context;

import com.opcon.components.Message;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.environment.triggers.IncomingMessageReceiver;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.utils.MobileNumberUtils;
import com.opcon.utils.PreferenceUtils;



/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class IncomingMessagePrefProvider implements SpecialPacketProvider {
    @Override
    public SpecialPacket getPacket(Context context) {

        SpecialPacket sp = new SpecialPacket(Packets._IN_MSG);

        if (isValidity(context)) {
            String receiver =  PreferenceUtils.getSecureString(context, IncomingMessageReceiver.PREF_FROM, "");
            String uLocale = PresenceManager.getUserLocale(context);
            if (MobileNumberUtils.checkValid(receiver, uLocale.toUpperCase())) {
                receiver = MobileNumberUtils.toInternational(receiver, uLocale, false);
            }
            String body =  PreferenceUtils.getSecureString(context, IncomingMessageReceiver.PREF_BODY, "");
            Long date =  PreferenceUtils.getLong(context,  IncomingMessageReceiver.PREF_TIME, 0L);

            sp.put(Message.InoutMessage.WHO, receiver);
            sp.put(Message.InoutMessage.WHEN, date);
            sp.put(Message.InoutMessage.WHAT, body);
        }

        return sp;
    }

    private boolean isValidity(Context context) {
        long t =  PreferenceUtils.getLong(context, IncomingMessageReceiver.PREF_TIME, 0L);
        return System.currentTimeMillis() - t < 10000;
    }

    @Override
    public SpecialPacket getForTest(Context context) {
        return getPacket(context);
    }
}
