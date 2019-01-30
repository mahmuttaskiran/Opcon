package com.opcon.notifier.environment.triggers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.OperationProcessManager;

import java.util.concurrent.TimeUnit;

public class BatteryLevelReceiver extends BroadcastReceiver implements OperationProcessManager.OperationProcessListener {

    public BatteryLevelReceiver() {}

    @Override public void onReceive(Context context, Intent intent) {
        BatteryEventReceiver.check(context.getApplicationContext());
    }

    @Override
    public void onSuccessfulOperated(Notifier r) {}

    @Override
    public void onFatalOperation(Notifier r) {}

    public static void start(Context context) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(context, BatteryLevelReceiver.class);
        PendingIntent mPIntent = PendingIntent.getBroadcast(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60 * 1000, TimeUnit.MINUTES.toMillis(15), mPIntent);
    }

    public static void stop(Context context) {
        AlarmManager mAlarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent mIntent = new Intent(context, BatteryLevelReceiver.class);
        PendingIntent mPIntent = PendingIntent.getBroadcast(context, 1, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mAlarmManager.cancel(mPIntent);
    }

}
