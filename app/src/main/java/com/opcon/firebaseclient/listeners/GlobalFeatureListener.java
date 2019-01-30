package com.opcon.firebaseclient.listeners;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.NotificationCompat;

import com.opcon.R;
import com.opcon.components.Feature;
import com.opcon.database.FeatureBase;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.ui.activities.FeatureActivity;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 14/04/2017.
 */

public class GlobalFeatureListener implements ComponentListenerManager.ComponentListener{
  private Context mContext;
  public static final int NOTIFICATION_ID = 1994;
  public GlobalFeatureListener(Context context) {
    mContext = context.getApplicationContext();
  }

  @Override public boolean onNewComponent(Object component) {
    Feature feature = (Feature) component;
    Timber.d("feature..: %s", feature.toString());
    boolean isNew = FeatureBase.getInstance(mContext).newFeature(feature);
    if (isNew && feature.hasNotification() && !PresenceManager.getInstance(mContext).isActive()) {
      notifyFeature(feature);
    }
    return true;
  }

  private void notifyFeature(Feature feature) {
    NotificationManager nm = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
    builder.setSmallIcon(R.drawable.opcon_small_icon);
    builder.setContentTitle(feature.getNotificationTitle());
    builder.setContentText(feature.getNotificationContent());
    Intent i = new Intent(mContext, FeatureActivity.class);
    PendingIntent pi = PendingIntent.getActivity(mContext, 0, i, 0);
    builder.setContentIntent(pi);
    nm.notify(NOTIFICATION_ID, builder.build());
  }


}

