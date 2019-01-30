package com.opcon.libs.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.opcon.R;
import com.opcon.components.Component;
import com.opcon.components.Message;
import com.opcon.database.ComponentSettings;
import com.opcon.database.ContactBase;
import com.opcon.database.MessageProvider;
import com.opcon.libs.settings.SettingsUtils;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.activities.ChatActivity;
import com.opcon.ui.activities.MainActivity;
import com.opcon.utils.PreferenceUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 21/02/2017.
 */

public class MessageNotificator extends Notificator<Component> {

  private volatile static MessageNotificator singleton;

  private static final int NOTIFICATION_ID = 2;

  private List<String> mSenders;
  private String mLastPhone;
  private NotificationCompat.InboxStyle mStyle;

  private int isMessageExists = 0;
  private int isNotifierExists = 0;

  private MessageNotificator (Context context, int nid) {
    super(context, nid);
    mSenders = new ArrayList<>();
    mStyle = new NotificationCompat.InboxStyle();
    initFromDisk();
    if (!TextUtils.isEmpty(getContent())) {
      mStyle.addLine(getContent());
    }
    notifyState();
  }

  public static MessageNotificator getInstance(Context context) {
    if (singleton == null) {
      synchronized (MessageNotificator.class) {
        if (singleton == null) {
          singleton = new MessageNotificator(context, NOTIFICATION_ID);
        }
      }
    }
    return singleton;
  }

  @Override public void snapshot() {
    super.snapshot();
    PreferenceUtils.putString(getContext(), getClass().getSimpleName() + "_senders", new JSONArray(mSenders).toString());
    PreferenceUtils.putInt(getContext(), getClass().getSimpleName() + "_notifierCount", isNotifierExists);
    PreferenceUtils.putInt(getContext(), getClass().getSimpleName() + "_messageCount", isMessageExists);
  }

 @Override public void cancel() {
   super.cancel();
   mSenders.clear();
   isMessageExists = 0;
   isNotifierExists = 0;
   PreferenceUtils.putString(getContext(), getClass().getSimpleName() + "_senders", null);
   PreferenceUtils.putInt(getContext(), getClass().getSimpleName() + "_notifierCount", 0);
   PreferenceUtils.putInt(getContext(), getClass().getSimpleName() + "_messageCount", 0);
 }

  @Override
  public void initFromDisk() {
    super.initFromDisk();
    String senders = PreferenceUtils.getString(getContext(), getClass().getSimpleName() + "_senders", null);
    isNotifierExists = PreferenceUtils.getInt(getContext(), getClass().getSimpleName() + "_notifierCount", 0);
    isMessageExists = PreferenceUtils.getInt(getContext(), getClass().getSimpleName() + "_messageCount", 0);
    if (senders != null) {
      try {
        JSONArray ja = new JSONArray(senders);
        for (int i = 0; i < ja.length(); i++) {
          this.mSenders.add(ja.get(i).toString());
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
  }

  private boolean isNotificationSoundOn(String sender) {
    return ComponentSettings.isNotificationSoundOn(getContext(), sender)
        && SettingsUtils.Dialog.isEnabledNotifySound(getContext());
  }

  private boolean isSenderNotificationEnabled(String sender) {
    return ComponentSettings.getBoolean(getContext(), sender, ComponentSettings.NOTIFICATION, true);
  }

  @Override public void forThat(Component component) {
    super.forThat(component);

    if (component instanceof Message) {
      Message message = (Message) component;
      mLastPhone = message.getSender();
      isMessageExists++;
    } else if (component instanceof Notifier) {
      mLastPhone = component.getSender();
      isNotifierExists++;
    } else {
      throw new IllegalArgumentException("Look at the class name!");
    }

    if (!isSenderNotificationEnabled(mLastPhone)) {
      return;
    }

    putSenderName(ContactBase.Utils.getName(getContext(), mLastPhone));
    prepareContent(component);
    setAvatar(ContactBase.Utils.getValidAvatar(getContext(), mLastPhone));
    setTitle(prepareTitle());
    notifyState();

    Timber.d("state will be notify");

  }

  private void prepareContent(Component component) {
    setSummaryText();
    String c = null;
    if (component instanceof Message) {
      c = MessageProvider.DialogUtils.lastMessageToString(getContext(), (Message) component);
    } else if (component instanceof Notifier) {
      c = getContext().getString(R.string.added_new_notifier_x_man);
    }
    if (c != null) {
      mStyle.addLine(mSenders.get(mSenders.size() -1) + ":" + c);
      appendToContent(c + "\n");
    }
  }

  private void setSummaryText() {
    String st = null;

    if (isMessageExists > 0 && isNotifierExists > 0) {
      st = String.format(getContext().getString(R.string.there_are_x_message_and_x_notifier), isMessageExists, isNotifierExists);
    } else if (isMessageExists > 0) {
      st = String.format(getContext().getString(R.string.there_are_x_unreaded_message), isMessageExists);
    } else if (isNotifierExists > 0) {
      st = String.format(getContext().getString(R.string.there_are_x_notifier), isNotifierExists);
    }

    if (st != null) {
      mStyle.setSummaryText(st);
    }
  }

  private void putSenderName(String sender) {
    if (!mSenders.contains(sender)) {
      mSenders.add(sender);
    }
  }

  public String prepareTitle() {
    if (mSenders.size() == 1) {
      return mSenders.get(0);
    } else if (mSenders.size() == 2) {
      return mSenders.get(0) + getContext().getString(R.string.and) + mSenders.get(1);
    } else {
      return mSenders.get(mSenders.size() -1) + ", " +
          mSenders.get(mSenders.size() -2) +
          getContext().getString(R.string.and) + " " +
          String.format(getContext().getString(R.string.orher_x_man), mSenders.size() -2);
    }
  }

  @Override public boolean isEmpty() {
    return super.isEmpty() || mSenders.isEmpty();
  }

  @Override
  public void notifyState() {
    if (isEmpty()) return;

    NotificationCompat.Builder b = NotificationUtils.defaultBuilder(getContext(), prepareTitle(), getContent().toString(), getPendingIntent());
    b.setStyle(mStyle);

    if (isNotificationSoundOn(mLastPhone)) {
      b.setSound(Uri.parse(SettingsUtils.Dialog.getRingtone(getContext())));
    } else {
      b.setSound(null);
    }


    int degree = SettingsUtils.Dialog.getVibratorDegree(getContext());
    if (degree != 1) {
      b.setVibrate(SettingsUtils.getVibratePattern(degree));
    }

    NotificationUtils.addAvatar(getContext(), b, getAvatar(), new NotificationUtils.Event() {
      @Override
      public void onEvent(NotificationCompat.Builder b) {
        MessageNotificator.this.notify(b.build());
      }
    });
  }

  private PendingIntent getPendingIntent() {
    Intent i;
    if (mSenders.size() == 1) {
      i = new Intent(getContext(), ChatActivity.class);
      i.putExtra(ChatActivity.DESTINATION, mLastPhone);
    } else {
      i = new Intent(getContext(), MainActivity.class);
    }
    i.setAction(Intent.ACTION_MAIN);
    i.addCategory(Intent.CATEGORY_LAUNCHER);
    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

    return PendingIntent.getActivity(getContext(), 1, i, PendingIntent.FLAG_UPDATE_CURRENT);
  }

}
