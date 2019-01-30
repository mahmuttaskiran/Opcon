package com.opcon.libs.utils;

import org.ocpsoft.prettytime.PrettyTime;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by aslitaskiran on 19/05/2017.
 */

public class TimeUtils {

  public static String shortTime(long time, long is) {
    long now = System.currentTimeMillis();
    if (now - time > is) {
      return DateFormat.getTimeInstance(DateFormat.SHORT).format(new Date(time));
    } else {
      return new PrettyTime().format(new Date(time));
    }
  }

  public static String shortDate(long time, long is) {
    long now = System.currentTimeMillis();
    if (now - time > is) {
      return DateFormat.getDateInstance(DateFormat.SHORT).format(new Date(time));
    } else {
      return new PrettyTime().format(new Date(time));
    }
  }

  public static String shortDateAndTime(long time, long is) {
    long now = System.currentTimeMillis();
    if (now - time > is) {
      return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(time));
    } else {
      return new PrettyTime().format(new Date(time));
    }
  }

  public static String justTimeIsSomeDay(long time, long is) {
    if (isSameDay(time)) {
      return shortTime(time, is);
    } else {
      return shortDateAndTime(time, is);
    }
  }

  public static boolean isSameDay(long time) {
    Calendar now = Calendar.getInstance();
    Calendar at = Calendar.getInstance();

    now.setTimeInMillis(System.currentTimeMillis());
    at.setTimeInMillis(time);

    return (now.get(Calendar.DAY_OF_YEAR) == at.get(Calendar.DAY_OF_YEAR));
  }

}
