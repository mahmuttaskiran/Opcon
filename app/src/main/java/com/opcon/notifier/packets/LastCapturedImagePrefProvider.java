package com.opcon.notifier.packets;

import android.content.Context;
import android.text.TextUtils;

import com.opcon.components.Message;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.environment.triggers.NewPictureReceiver;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.utils.PreferenceUtils;


/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class LastCapturedImagePrefProvider implements SpecialPacketProvider {
    @Override
    public SpecialPacket getPacket(Context context) {
        SpecialPacket sp = new SpecialPacket(Packets._LAST_IMAGE);
        String path =  PreferenceUtils.getString(context, NewPictureReceiver.PREF_LAST_CAPTURED_IMAGE, "");
        if (TextUtils.isEmpty(path)) return null;
        sp.put((Message.Picture.FILE), path);
        return sp;
    }

    @Override
    public SpecialPacket getForTest(Context context) {
        return getPacket(context);
    }
}
