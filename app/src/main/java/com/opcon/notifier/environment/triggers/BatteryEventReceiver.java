package com.opcon.notifier.environment.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.text.TextUtils;

import com.opcon.database.NotifierProvider;
import com.opcon.notifier.ConditionChecker;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mahmut Taşkiran on 06/04/2017.
 */

public class BatteryEventReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    if (!TextUtils.isEmpty(intent.getAction())) {
      check(context.getApplicationContext());
    }
  }

  public static void check(Context context) {
    int batteryLevel =  getBatteryLevel(context.getApplicationContext());
    if (batteryLevel == -1) {
      return;
    }
    List<Notifier> notifiers = NotifierProvider.Utils.getProgressableNotifiersWithConditionType(context, Conditions._BATTERY, Conditions.__BATTERY);
    ConditionChecker mConditionChecker = new ConditionChecker(context);
    mConditionChecker.putParam("currentCharge", batteryLevel);
    for (Notifier notifier : notifiers) {
      boolean check = mConditionChecker
          .requiresReverse(true)
          .backoff(TimeUnit.MINUTES.toMillis(14))
          .check(notifier);
      if (check) {
        OperationProcessManager opm = OperationProcessManager.getInstance();
        opm.processAsync(context, notifier, null);
      }
    }
  }

  public static int getBatteryLevel(Context context) {
    IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    Intent batteryStatus = context.getApplicationContext().registerReceiver(null, iFilter);
    if (batteryStatus == null)
      return -1;
    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
    if (level > 0) {
      return (level * 100) / scale;
    }
    return -1;
  }

}
