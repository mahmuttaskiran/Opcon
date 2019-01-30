package com.opcon.libs.settings;

import android.content.Context;
import android.preference.PreferenceManager;


/**
 * Created by Mahmut Taşkiran on 21/02/2017.
 */

public class SettingsUtils {
  public static class Dialog {
    public static boolean enterToSend(Context c) {
      return PreferenceManager.getDefaultSharedPreferences(c).getBoolean("dialog_enter_to_send", false);
    }
    public static boolean isEnabledNotifySound(Context c) {
      return PreferenceManager.getDefaultSharedPreferences(c)
          .getBoolean("dialog_ringtone_state", true);
    }
    public static String getRingtone(Context c) {
      return PreferenceManager.getDefaultSharedPreferences(c)
          .getString("dialog_ringtone", "");
    }
    public static int getVibratorDegree(Context c) {
      try {

        String a = PreferenceManager.getDefaultSharedPreferences(c).getString("dialog_vibrate", null);
        if (a == null) {
          return 0;
        }
        return Integer.parseInt(a);

      } catch (Exception e) {
        e.printStackTrace();
      }
      return 0;
    }

  }
  public static class NotifierNotification {
    public static boolean isEnabled(Context c) {
      return PreferenceManager.getDefaultSharedPreferences(c)
          .getBoolean("notifier_ringtone_state", true);
    }
    public static String getRingtone(Context c) {
      return PreferenceManager.getDefaultSharedPreferences(c)
          .getString("notifier_ringtone", "");
    }
    public static int getVibratorDegree(Context c) {
      try {
        return Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(c)
            .getString("notifier_vibrate", "0"));
      } catch (Exception e) {
        e.printStackTrace();
      }
      return 0;
    }
  }
  public static long[] getVibratePattern(int degree) {
    if (degree == 0) {
      return new long[] {1000L, 1000L};
    } else if (degree == 1) {
      return new long[] {0L,0L};
    } else if (degree == 2) {
      return new long[] {500L, 500L};
    } else {
      return new long[] {2500, 2500};
    }
  }
}
