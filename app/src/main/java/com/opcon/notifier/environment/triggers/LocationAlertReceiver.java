package com.opcon.notifier.environment.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;

import com.opcon.database.NotifierProvider;
import com.opcon.notifier.ConditionChecker;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.packets.LocationPrefProvider;
import com.opcon.ui.fragments.occs.ConditionLocation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;


/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 *
 */

public class LocationAlertReceiver extends BroadcastReceiver implements OperationProcessManager.OperationProcessListener
{
    public static final String ACTION = "com.opcon.LOCATION_ALERT";

    @Override
    public void onReceive(Context context, Intent intent) {
        int mNotifierId = intent.getExtras().getInt("notifiers", -1);

        log("---------------- RECEIVED AN LOCATIONAL CONDITION ----------------");

        if (mNotifierId == -1) {
            log("FOR_ALL");
            Object o = intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
            if (o != null) {
                Location currentLocation = (Location) o;
                log(String.format("lt: %s, ln: %s", String.valueOf(currentLocation.getLatitude()), String.valueOf(currentLocation.getLongitude())));
                LocationPrefProvider.setLocation(context, currentLocation);

                List<Notifier> notifiers = NotifierProvider.Utils.getProgressableNotifiersWithConditionType(context, Conditions._LOCATION, Conditions.__LOCATION);

                log("notifiers size: " + notifiers.size());

                ConditionChecker conditionChecker = new ConditionChecker(context);
                conditionChecker.backoff(TimeUnit.MINUTES.toMillis(15));
                conditionChecker.requiresReverse(true);

                for (Notifier notifier : notifiers) {
                    Location exceptedLocation = toLocation(notifier);
                    float distance = exceptedLocation.distanceTo(currentLocation);
                    float exceptedDistance = getNear(notifier);
                    log(String.format("exceptedLocation(lt: %s, ln: %s: near: %s)", String.valueOf(exceptedLocation.getLatitude()), String.valueOf(exceptedLocation.getLongitude()), String.valueOf(exceptedDistance)));
                    conditionChecker.defaultResult(distance <= exceptedDistance);
                    log(String.format("defaultResult(distance(%s) <= exceptedDistance(%s))", String.valueOf(distance), String.valueOf(exceptedDistance)));
                    boolean check = conditionChecker.check(notifier);

                    log("check: %s" + String.valueOf(check));

                    if (check) {
                        OperationProcessManager.getInstance().processAsync(context, notifier, this);
                    }
                }

            } else {
                log("KEY_LOCATION_CHANGED: Location value returned null.");
            }

        } else {
            log(String.format("FOR_SINGLE %d", mNotifierId));
            boolean mEntering = intent.getExtras().getBoolean(LocationManager.KEY_PROXIMITY_ENTERING);
            log("entering: %s" + String.valueOf(mEntering));
            ConditionChecker mConditionChecker = new ConditionChecker(context);
            Notifier mNotifier = NotifierProvider.Utils.get(context, mNotifierId);

            log(String.format("notifier is null: %s", String.valueOf(mNotifier == null)));

            OperationProcessManager mProcessor = OperationProcessManager.getInstance();

            if (mNotifier != null) {
                boolean check = mConditionChecker
                    .defaultResult(mEntering)
                    .backoff(TimeUnit.MINUTES.toMillis(15))
                    .requiresReverse(true)
                    .check(mNotifier);

                log("check: %s"+ String.valueOf(check));

                if (check) {
                    mProcessor.processAsync(context, mNotifier, this);
                }
            }
        }

    }

    private void log(String log) {
        Timber.d("LocationAlertReceiver: %s", log);
    }

    Location toLocation(Notifier r) {
        Location l = new Location("NO_PROVIDER");
        l.setLatitude(r.getCondition().getDouble(ConditionLocation.LATITUDE));
        l.setLongitude(r.getCondition().getDouble(ConditionLocation.LONGITUDE));
        return l;
    }

    float getNear(Notifier r) {
        return r.getCondition().getInt(ConditionLocation.NEAR);
    }

    @Override public void onSuccessfulOperated(Notifier r) {}
    @Override public void onFatalOperation(Notifier r) {}
}
