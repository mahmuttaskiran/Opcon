package com.opcon.notifier.environment.triggers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.opcon.components.Component;
import com.opcon.database.NotifierProvider;
import com.opcon.notifier.ConditionChecker;
import com.opcon.notifier.components.Condition;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.ui.fragments.occs.ConditionTime;
import com.opcon.ui.utils.Restrict;

import java.util.Calendar;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 03/11/2016.
 */

public class TimeNotifierBroadcastReceiver extends BroadcastReceiver implements OperationProcessManager.OperationProcessListener{

    public static final String NOTIFIER_ID = "notifierId";

    public TimeNotifierBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Timber.d("TimeNotifierBroadcastReceiver: " + "yep!");
        int id = intent.getExtras().getInt(NOTIFIER_ID);
        OperationProcessManager opm = OperationProcessManager.getInstance();
        Notifier notifier = NotifierProvider.Utils.get(context, id);
        if (notifier == null) return;

        ConditionChecker conditionChecker = new ConditionChecker(context);
        boolean check = conditionChecker.backoff(1).defaultResult(true)
            .check(notifier);
        if (check) {
            opm.processAsync(context, notifier, this);
        }
    }

    @Override public void onSuccessfulOperated(Notifier r) {}
    @Override public void onFatalOperation(Notifier r) {}

    public static void setAlarm(Context context, Notifier notifier) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Condition condition = notifier.getCondition();
        Calendar time = Calendar.getInstance();

        long timestamp;

        if (condition.getDateRestrictParams() != null) {
            Component restrict = condition.getDateRestrictParams();
            int year = restrict.getInt(Restrict.YEAR);
            int month = restrict.getInt(Restrict.MONTH);
            int day = restrict.getInt(Restrict.DAY);
            time.set(Calendar.YEAR, year);
            time.set(Calendar.MONTH, month);
            time.set(Calendar.DAY_OF_MONTH, day);
        }

        int hours = condition.getInt(ConditionTime.HOUR);
        int minutes = condition.getInt(ConditionTime.MINUTES);

        long current = System.currentTimeMillis();

        time.set(Calendar.HOUR_OF_DAY, hours);
        time.set(Calendar.MINUTE, minutes);
        time.set(Calendar.SECOND, 0);


        timestamp = time.getTimeInMillis();

        if (current > timestamp) {
            // pass
            return;
        }
        Intent intent = new Intent(context, TimeNotifierBroadcastReceiver.class);
        intent.putExtra(TimeNotifierBroadcastReceiver.NOTIFIER_ID, notifier.getId());
        PendingIntent pi = PendingIntent.getBroadcast(context, notifier.getId(), intent, 0);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timestamp, pi);
    }

    public static void cancelAlarmFor(Context context, int id) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, TimeNotifierBroadcastReceiver.class);
        intent.putExtra(TimeNotifierBroadcastReceiver.NOTIFIER_ID, id);
        PendingIntent pi = PendingIntent.getBroadcast(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.cancel(pi);
    }

}
