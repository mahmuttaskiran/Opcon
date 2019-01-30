package com.opcon.notifier.environment;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import com.opcon.database.NotifierProvider;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.environment.triggers.LocationAlertReceiver;
import com.opcon.notifier.environment.triggers.TimeNotifierBroadcastReceiver;
import com.opcon.notifier.utils.NotifierUtils;

import java.util.List;

/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 */

public class EnvironmentManager {

    private static EnvironmentManager singleton = new EnvironmentManager();

    public static EnvironmentManager init() {
        if (singleton == null) {
            synchronized (EnvironmentManager.class) {
                if (singleton==null) {
                    singleton = new EnvironmentManager();
                }
            }
        }
        return singleton;
    }

    public void builtEnvironment(Context context) {
        Environment mEnvironment = getEnvironment(context);
        List<Notifier> mNotifiers = NotifierProvider.Utils.getProgressableNotifiers(context);
        mNotifiers = NotifierUtils.permissionFilter(context, mNotifiers);
        State mState = State.capture(mNotifiers);
        mEnvironment.builtComponents(mState, mNotifiers);
    }

    public void removeLocationalNotifierEnvironment(Context context, int id) {
        Intent mIntent = new Intent(context, LocationAlertReceiver.class);
        mIntent.setAction(LocationAlertReceiver.ACTION);
        mIntent.putExtra("notifierId", id);
        PendingIntent mPIntent = PendingIntent.getBroadcast(context,
                id, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        LocationManager mLocationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        mLocationManager.removeProximityAlert(mPIntent);
    }

    public void removeTimelyNotifierEnvironment(Context context, int id) {
        TimeNotifierBroadcastReceiver.cancelAlarmFor(context, id);
    }

    private Environment getEnvironment(Context context) {
        return new EnvironmentProxy14(context);
    }

}
