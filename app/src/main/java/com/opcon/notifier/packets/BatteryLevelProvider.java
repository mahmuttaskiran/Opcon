package com.opcon.notifier.packets;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.opcon.components.Message;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.components.SpecialPacket;

/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class BatteryLevelProvider implements SpecialPacketProvider {
    @Override
    public SpecialPacket getPacket(Context context) {
        SpecialPacket sp = new SpecialPacket(Packets._BATTERY_LEVEL);
        IntentFilter filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.getApplicationContext().registerReceiver(null, filter);
        if (batteryStatus != null) {
            int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            sp.put(Message.Battery.PERCENT, level);
            return sp;
        }
        return null;
    }

    @Override
    public SpecialPacket getForTest(Context context) {
        return getPacket(context);
    }
}
