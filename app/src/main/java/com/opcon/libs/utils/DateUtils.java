package com.opcon.libs.utils;

import java.util.Calendar;

/**
 * Created by Mahmut Taşkiran on 18/01/2017.
 */

public class DateUtils {
    public static boolean isDifferentDays(long day1, long day2) {

        if (day1 == 0 || day2 == 0) {
            return true;
        }

        Calendar calendarDay1 = Calendar.getInstance();
        calendarDay1.setTimeInMillis(day1);

        Calendar calendarDat2 = Calendar.getInstance();
        calendarDat2.setTimeInMillis(day2);

        return calendarDay1.get(Calendar.YEAR) != calendarDat2.get(Calendar.YEAR) ||
                calendarDay1.get(Calendar.MONTH) != calendarDat2.get(Calendar.MONTH) ||
                calendarDay1.get(Calendar.DAY_OF_MONTH) != calendarDat2.get(Calendar.DAY_OF_MONTH);

    }

    public static boolean isTimeRangeLargerThan(long time1, long time2, long when) {
        return time1 == 0 || time2 == 0 || (time1 > (time2 + when));
    }
}
