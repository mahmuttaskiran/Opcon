package com.opcon.notifier.packets;

import android.content.Context;

import com.opcon.notifier.components.SpecialPacket;

/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public interface SpecialPacketProvider {
    SpecialPacket getPacket(Context context);
    SpecialPacket getForTest(Context context);
}
