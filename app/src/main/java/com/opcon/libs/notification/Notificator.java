package com.opcon.libs.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.text.TextUtils;

import com.opcon.utils.PreferenceUtils;

/**
 * Created by Mahmut Taşkiran on 21/02/2017.
 */

public abstract class Notificator<TriggerObjectType> {

  private Context mContext;

  private StringBuilder content;
  private String title;
  private String avatar;

  private int notificationCount = 0;
  private int mNotificationId;

  private NotificationManager mManager;

  public Notificator(Context context, int nid) {
    mContext = context.getApplicationContext();
    mNotificationId = nid;
    mManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
  }

  public Context getContext() {
    return mContext;
  }

  public int getNotificationId() {
    return mNotificationId;
  }

  public void snapshot() {
    String klass = getClass().getSimpleName();
    PreferenceUtils.putString(mContext, klass + "_" + "title", title);
    PreferenceUtils.putString(mContext, klass + "_" + "content", content.toString());
    PreferenceUtils.putString(mContext, klass + "_" + "avatar", avatar);
    PreferenceUtils.putInt(mContext, klass + "_" + "count", notificationCount);
  }

  public void initFromDisk() {
    String klass = getClass().getSimpleName();
    String content = PreferenceUtils.getString(mContext, klass + "_" + "content", null);
    title = PreferenceUtils.getString(mContext, klass + "_" + "title", null);
    avatar = PreferenceUtils.getString(mContext, klass + "_" + "avatar", null);
    notificationCount = PreferenceUtils.getInt(mContext, klass + "_" + "count", 0);
    if (content == null) {
      this.content = new StringBuilder();
    } else {
      this.content = new StringBuilder(content);
    }
  }

  public void notify(Notification b) {
    mManager.notify(getNotificationId(), b);
  }

  public boolean isEmpty() {
    return TextUtils.isEmpty(content) && TextUtils.isEmpty(title);
  }

  public void cancel() {
    content = new StringBuilder();
    title = "";
    notificationCount = 0;
    avatar = null;

    String klass = getClass().getSimpleName();
    PreferenceUtils.putString(mContext, klass + "_" + "title", null);
    PreferenceUtils.putString(mContext, klass + "_" + "content", null);
    PreferenceUtils.putString(mContext, klass + "_" + "avatar", null);
    PreferenceUtils.putInt(mContext, klass + "_" + "count", 0);
    mManager.cancel(getNotificationId());
  }

  public void setContext(Context context) {
    this.mContext = context;
  }

  public String prepareTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public int getNotificationCount() {
    return notificationCount;
  }

  public StringBuilder getContent() {
    return content;
  }

  public void appendToContent(String content) {
    if (this.content == null) {
      this.content = new StringBuilder();
    }
    this.content.append(content);
  }

  public String getTitle() {
    return title;
  }
  public String getAvatar() {
    return avatar;
  }
  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }
  public void setContent(String s) {
    content = new StringBuilder(s);
  }
  public void forThat(TriggerObjectType obj) {
    notificationCount++;
  }
  public abstract void notifyState();

}
