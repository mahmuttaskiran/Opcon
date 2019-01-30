package com.opcon.notifier.environment;

import android.Manifest;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.LocationManager;

import com.opcon.libs.permission.PermissionUtils;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.environment.triggers.BatteryEventReceiver;
import com.opcon.notifier.utils.NotifierUtils;
import com.opcon.notifier.environment.triggers.BatteryLevelReceiver;
import com.opcon.notifier.environment.triggers.IncomingMessageReceiver;
import com.opcon.notifier.environment.triggers.LocationAlertReceiver;
import com.opcon.notifier.environment.triggers.NewPictureReceiver;
import com.opcon.notifier.environment.triggers.PhoneStateReceiver;
import com.opcon.notifier.environment.triggers.TimeNotifierBroadcastReceiver;
import com.opcon.ui.fragments.occs.ConditionLocation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;


/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 */

public abstract class DefaultEnvironmentImpl extends Environment {

    public DefaultEnvironmentImpl(Context context) {
        super(context);
    }

    @Override public void builtComponents(State state, List<Notifier> processableNotifiers) {

        // builtComponents timely conditions
        if (state.isTimeConditionExists()) {
            List<Notifier> timelyNotifiers = NotifierUtils.filterWithConditionType(processableNotifiers, Conditions._TIMELY, Conditions.__TIMELY);
            for (Notifier timelyNotifier : timelyNotifiers)
                TimeNotifierBroadcastReceiver.setAlarm(getContext(), timelyNotifier);
        }

        // builtComponents locational conditions
        if (state.isLocationalConditionExists()) {
            requestLocations();
            List<Notifier> locationalNotifiers = NotifierUtils.filterWithConditionType(processableNotifiers, Conditions._LOCATION, Conditions.__LOCATION);
            for (Notifier locationalNotifier : locationalNotifiers)
                addProximityAlertFor(locationalNotifier);
        } else {
            removeRequestLocations();
        }

        if (state.isBatteryConditionExists()) {
            BatteryLevelReceiver.start(getContext());
        } else {
            BatteryLevelReceiver.stop(getContext());
        }

        /*
        setComponentState(IncomingMessageReceiver.class, state.isIncomingSmsConditionExists());
        setComponentState(PhoneStateReceiver.class, state.isIncomingCallExists() || state.isOutgoingCallExists());
        setComponentState(NewPictureReceiver.class, state.isCameraConditionExists());
        setComponentState(BatteryEventReceiver.class, state.isBatteryConditionExists());
         */

    }

    private void requestLocations() {
        LocationManager mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingAccuracy(Criteria.NO_REQUIREMENT);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setSpeedRequired(false);

        Intent intent = new Intent(getContext(), LocationAlertReceiver.class);
        intent.setAction(LocationAlertReceiver.ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (PermissionUtils.check(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)) {
            try {
                mLocationManager.requestLocationUpdates(TimeUnit.MINUTES.toMillis(10), 10, criteria, pi);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

    }

    private void removeRequestLocations() {
        LocationManager mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        Intent intent = new Intent(getContext(), LocationAlertReceiver.class);
        intent.setAction(LocationAlertReceiver.ACTION);
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), 1000, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mLocationManager.removeUpdates(pi);
    }

    private void setComponentState(Class<?> klass, boolean state) {
        if (state) {
            enableComponent(klass);
        } else {
            disableComponent(klass);
        }
    }

    private void disableComponent(Class<?> klass) {
      ComponentName cn = new ComponentName(getContext(), klass);
      PackageManager pm = getContext().getPackageManager();
      pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
          PackageManager.DONT_KILL_APP);
    }

    private void enableComponent(Class<?> klass) {
      ComponentName cn = new ComponentName(getContext(), klass);
      PackageManager pm = getContext().getPackageManager();
      pm.setComponentEnabledSetting(cn, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    private void addProximityAlertFor(Notifier notifier) {
        LocationManager mLocationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        double latitude = notifier.getCondition().getDouble(ConditionLocation.LATITUDE);
        double longitude = notifier.getCondition().getDouble(ConditionLocation.LONGITUDE);
        int radius = notifier.getCondition().getInt(ConditionLocation.NEAR);
        Intent intent = new Intent(getContext(), LocationAlertReceiver.class);
        intent.setAction(LocationAlertReceiver.ACTION);
        intent.putExtra("notifierId", notifier.getId());
        PendingIntent pi = PendingIntent.getBroadcast(getContext(), notifier.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (PermissionUtils.isLocationalPermissionsGranted(getContext())) {
            try {
                mLocationManager.addProximityAlert(latitude, longitude, radius, -1, pi);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

}
