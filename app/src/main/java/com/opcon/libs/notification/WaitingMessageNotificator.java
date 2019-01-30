package com.opcon.libs.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;

import com.opcon.R;
import com.opcon.components.Message;
import com.opcon.database.ContactBase;
import com.opcon.database.MessageProvider;
import com.opcon.libs.settings.SettingsUtils;
import com.opcon.ui.activities.MainActivity;
import com.opcon.ui.activities.WaitingPostActivity;

/**
 * Created by Mahmut Taşkiran on 22/02/2017.
 */

public class WaitingMessageNotificator extends Notificator<Message>{

  private volatile static WaitingMessageNotificator singleton;
  private static final int NOTIFICATION_ID = WaitingPostActivity.NOTIFICATION_ID;


  private WaitingMessageNotificator (Context context, int nid) {
    super(context, nid);
    initFromDisk();
    notifyState();
  }

  public static WaitingMessageNotificator getInstance(Context context) {
    if (singleton == null) {
      synchronized (WaitingMessageNotificator.class) {
        if (singleton == null) {
          singleton = new WaitingMessageNotificator(context, NOTIFICATION_ID);
        }
      }
    }
    return singleton;
  }

  @Override
  public void forThat(Message log) {
    super.forThat(log);


    setTitle(String.format(getContext().getString(R.string.thereiswaiting_message), getNotificationCount()));
    setContent(MessageProvider.DialogUtils.lastMessageToString(getContext(), log));
    setAvatar(ContactBase.Utils.getValidAvatar(getContext(), log.getReceiver()));
    notifyState();
  }

  @Override
  public void notifyState() {

    if (isEmpty())
      return;

    NotificationCompat.Builder builder = NotificationUtils.defaultBuilder(getContext(),getTitle() ,
        getContent().toString(), getPendingIntent());

    boolean mute = !SettingsUtils.NotifierNotification.isEnabled(getContext());

    if (!mute) {
      builder.setSound(Uri.parse(SettingsUtils.NotifierNotification.getRingtone(getContext())));
    } else {
      builder.setSound(null);
    }

    builder.setVibrate(SettingsUtils.getVibratePattern(SettingsUtils.NotifierNotification.getVibratorDegree(getContext())));

    NotificationUtils.addAvatar(getContext(), builder, getAvatar(), new NotificationUtils.Event() {
      @Override public void onEvent(NotificationCompat.Builder b) {
        WaitingMessageNotificator.this.notify(b.build());
      }
    });

  }

  private PendingIntent getPendingIntent() {
    Intent intent = new Intent(getContext(), MainActivity.class);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);
    intent.setAction(Intent.ACTION_MAIN);
    intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
    return PendingIntent.getActivity(getContext(), 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
  }

}
