package com.opcon.notifier.packets;

import android.content.Context;
import android.location.Location;

import com.opcon.components.Message;
import com.opcon.libs.post.PostPoster;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.utils.PreferenceUtils;

/**
 * Created by Mahmut Taşkiran on 06/04/2017.
 */

public class LocationPrefProvider implements SpecialPacketProvider {

  @Override
  public SpecialPacket getPacket(Context context) {
    SpecialPacket sp = new SpecialPacket(Packets._LOCATION);


    double latitude = getLatitude(context);
    double longitude = getLongitude(context);

    if (latitude != 0 && longitude != 0) {
      sp.put(Message.Location.LATITUDE, latitude);
      sp.put(Message.Location.LONGITUDE, longitude);
      sp.put(Message.Location.TIME, getTimestamp(context));
      sp.put(Message.Location.ADDRESS, PostPoster.completeAddressOf(context, latitude, longitude));
    }

    return sp;
  }

  @Override
  public SpecialPacket getForTest(Context context) {
    return null;
  }

  public static void setLocation(Context context, Location location) {
    setLocation(context, location.getLongitude(), location.getLatitude());
  }

  public static void setLocation(Context context, double latitude,double longitude) {
    PreferenceUtils.putString(context, "locationPrefProviderLongitude", "_" + Double.valueOf(longitude).toString());
    PreferenceUtils.putString(context, "locationPrefProviderLatitude", "_" + Double.valueOf(latitude).toString());
    PreferenceUtils.putLong(context, "locationPrefProviderTimestamp", System.currentTimeMillis());
  }

  private static double getLongitude(Context context) {
    String l = PreferenceUtils.getString(context, "locationPrefProviderLongitude", null);
    double ll = 0;
    if (l != null) {
      String substring = l.substring(1);
      ll = Double.valueOf(substring);
    }
    return ll;
  }

  private static double getLatitude(Context context) {
    String l = PreferenceUtils.getString(context, "locationPrefProviderLatitude", null);
    double ll = 0;
    if (l != null) {
      String substring = l.substring(1);
      ll = Double.valueOf(substring);
    }
    return ll;
  }

  private static long getTimestamp(Context context) {
    return PreferenceUtils.getLong(context, "locationPrefProviderTimestamp", 0);
  }

}
