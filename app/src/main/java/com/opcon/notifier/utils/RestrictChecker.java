package com.opcon.notifier.utils;

import com.opcon.components.Component;
import com.opcon.ui.utils.Restrict;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 31/10/2016.
 *
 */

public final class RestrictChecker  {

    public static boolean isValidDate(Component restrict) {
        if (restrict == null || restrict.isEmpty()) return true;
        long c = System.currentTimeMillis();
        Calendar n = Calendar.getInstance();
        n.setTimeInMillis(c);
        int cy = n.get(Calendar.YEAR);
        int cm = n.get(Calendar.MONTH);
        int cd = n.get(Calendar.DAY_OF_MONTH);
        int ey = restrict.getInt(Restrict.YEAR);
        int em = restrict.getInt(Restrict.MONTH);
        int ed = restrict.getInt(Restrict.DAY);
        return (cy == ey && cm == em && cd == ed);
    }

    public static boolean isValidTimeRange(Component r) {
      if (r == null || r.isEmpty()) return true;
        long c = System.currentTimeMillis();
        Calendar n = Calendar.getInstance();
        n.setTimeInMillis(c);

        // fix OP-00007

        int efh = r.getInt(Restrict.FROM_HOURS);
        int efm = r.getInt(Restrict.FROM_MINUTES);
        int eth = r.getInt(Restrict.TO_HOURS);
        int etm = r.getInt(Restrict.TO_MINUTES);

        Calendar from = Calendar.getInstance();
        from.set(Calendar.HOUR, efh);
        from.set(Calendar.MINUTE, efm);

        Calendar to = Calendar.getInstance();
        to.set(Calendar.HOUR, eth);
        to.set(Calendar.MINUTE, etm);

        return n.after(from) && n.before(to);
    }
}
