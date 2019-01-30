package com.opcon.libs.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.opcon.R;
import com.opcon.components.NotifierLog;
import com.opcon.database.NotifierProvider;
import com.opcon.libs.settings.SettingsUtils;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.activities.NotifierLogActivity;

/**
 * Created by Mahmut Taşkiran on 22/02/2017.
 */

public class NotifierLogNotificator extends Notificator<NotifierLog> {

  private volatile static NotifierLogNotificator singleton;
  private static final int NOTIFICATION_ID = 5;

  private NotificationCompat.InboxStyle style;

  private NotifierLogNotificator(Context context, int nid) {
    super(context, nid);
    style = new NotificationCompat.InboxStyle();
    initFromDisk();
    if (!TextUtils.isEmpty(getContent())) style.addLine(getContent());
    notifyState();
  }

  public static NotifierLogNotificator getInstance(Context context) {
    if (singleton == null) {
      synchronized (NotifierLogNotificator.class) {
        if (singleton == null) {
          singleton = new NotifierLogNotificator(context, NOTIFICATION_ID);
        }
      }
    }
    return singleton;
  }

  @Override
  public void forThat(NotifierLog log) {
    super.forThat(log);
    String sid = log.getNotifierSid();
    Notifier r_notifier = NotifierProvider.Utils.get(getContext(), sid);
    if (r_notifier == null || !r_notifier.isNotificationOn(getContext())) {
      return;
    }
    setTitle(getContext().getString(R.string.notifier_notification_someting_has_been_change));
    setAvatar(r_notifier.getTargetAvatar(getContext()));
    String targetName = r_notifier.getReceiverName(getContext());
    String line = targetName + ": " + log.getTitle(getContext());
    style.addLine(line);
    appendToContent(line);
    notifyState();
  }

  @Override
  public void notifyState() {
    if (isEmpty())
      return;

    NotificationCompat.Builder builder = NotificationUtils.defaultBuilder(getContext(),getTitle() ,
        getContent().toString(), getPendingIntent());

    builder.setStyle(style);

    boolean mute = !SettingsUtils.NotifierNotification.isEnabled(getContext());

    if (!mute) {
      builder.setSound(Uri.parse(SettingsUtils.NotifierNotification.getRingtone(getContext())));
    } else {
      builder.setSound(null);
    }

    int vibratorDegree = SettingsUtils.NotifierNotification.getVibratorDegree(getContext());
    if (vibratorDegree != 1) {
      builder.setVibrate(SettingsUtils.getVibratePattern(vibratorDegree));
    }

    NotificationUtils.addAvatar(getContext(), builder, getAvatar(), new NotificationUtils.Event() {
      @Override
      public void onEvent(NotificationCompat.Builder b) {
        NotifierLogNotificator.this.notify(b.build());
      }
    });

  }

  private PendingIntent getPendingIntent() {
    Intent intent = new Intent(getContext(), NotifierLogActivity.class);
    intent.putExtra(NotifierLogActivity.SHOW_ALL, true);
    intent.setAction(Intent.ACTION_MAIN);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    return PendingIntent.getActivity(getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

}
