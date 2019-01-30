package com.opcon.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.opcon.notifier.environment.EnvironmentManager;

public class BootBroadcastReceiver extends BroadcastReceiver {

    @Override public void onReceive(Context context, Intent intent) {
        if (intent.getAction() != null && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            EnvironmentManager.init().builtEnvironment(context);
        }
    }

}
