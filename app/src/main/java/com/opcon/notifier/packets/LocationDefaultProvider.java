package com.opcon.notifier.packets;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;

import com.opcon.components.Message;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.libs.post.PostPoster;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.notifier.components.constants.Packets;

/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class LocationDefaultProvider implements SpecialPacketProvider {

    @Override
    public SpecialPacket getPacket(Context context) {
        SpecialPacket sp = new SpecialPacket(Packets._LOCATION);
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        Location fl = null, nl = null;

        if (PermissionUtils.isLocationalPermissionsGranted(context)) {
            try {
                nl = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                fl = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
        if (fl == null) {
            fl = nl;
        }

        if (fl != null) {
            sp.put(Message.Location.LATITUDE, fl.getLatitude());
            sp.put(Message.Location.LONGITUDE, fl.getLongitude());
            sp.put(Message.Location.TIME, fl.getTime());
            sp.put(Message.Location.ADDRESS, PostPoster.completeAddressOf(context, fl.getLatitude(), fl.getLongitude()));
        }


        return sp;
    }

    @Override
    public SpecialPacket getForTest(Context context) {
        return getPacket(context);
    }
}
