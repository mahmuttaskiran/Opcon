package com.opcon.components;

import android.content.Context;
import android.text.TextUtils;

import com.opcon.database.ContactBase;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Packets;


/**
 * Created by Mahmut Taşkiran on 18/03/2017.
 *
 */

public class Post extends Component {

  private boolean accepted;

  public boolean isAccepted() {
    return accepted;
  }

  public void setAccepted(boolean accepted) {
    this.accepted = accepted;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public String getText() {
    return getString(8);
  }

  public void setText(String text) {
    put(8, text);
  }

  public void setRelationNotifier(Notifier notifier) {
    put(112, notifier);
  }

  public Notifier getRelationNotifier() {
    Component component = getComponent(112);
    Notifier r = null;
    if (component != null) {
      r = new Notifier(component);
    }
    return r;
  }

  boolean isHaveRelationNotifier() {
    return getComponent(112) != null;
  }

  public int getPrivacy() {
    return getInt(1);
  }

  public void setPrivacy(int p) {
    put(1, p);
  }

  public int getPacketType() {
    return getInt(2);
  }

  public String getRelationNotifierSid() {
    return getString(3);
  }

  public boolean isRcs() {
    return getBoolean(4);
  }

  public void prepareForPost() {
    delete(6,12,-5,-6);
  }

  public String getImageDownloadUrl() {
    return getString(7);
  }

  public String getImageLocalePath() {
    return getString(6);
  }


  public String getOwner(){
    return getString(10);
  }

  public void setDownloadUrl(String url) {
    put(7, url);
  }

  public String getUploadedFilename() {
    return getString(12);
  }

  public long getTimestamp() {
    return getLong(9);
  }

  public void setLatitude(double l) {
    put(15, l);
  }

  public double getLatitude() {
    return getDouble(15);
  }

  public void setLongitude(double l) {
    put(16, l);
  }

  public double getLongitude() {
    return getDouble(16);
  }

  public void setAddress(String a) {
    put(17, a);
  }

  public String getAddress() {
    return getString(17);
  }

  public void setBatteryDegree(int d) {
    put(18, d);
  }

  public boolean isHaveBody() {
    return getPacketType() != -1;
  }

  public int getBatteryDegree() {
    return getInt(18);
  }

  public boolean isMine() {
    return getOwner().equals(PresenceManager.uid());
  }

  public String getOwnerName(Context context) {
    String name = getString(-5);
    if (TextUtils.isEmpty(name)) {
      name = ContactBase.Utils.getName(context, getOwner());
      put(-5, name);
    }
    return name;
  }

  public String getOwnerAvatar(Context context) {
    String avatar = getString(-6);
    if (TextUtils.isEmpty(avatar)) {
      avatar = ContactBase.Utils.getValidAvatar(context, getOwner());
      put(-6, avatar);
    }
    return avatar;
  }

  public String createKey() {
    StringBuilder builder = new StringBuilder();
    String text = getText();
    if (text != null) {
      builder.append(text.hashCode());
    }
    if (isLocationPost()) {
      builder.append(getLatitude()).append(",").append(getLongitude());
    } else if (isPicturePost()) {
      builder.append(getImageLocalePath());
    } else if (isChargePost()) {
      builder.append(getBatteryDegree());
    }
    return builder.toString();
  }



  public boolean isChargePost() {
    return getPacketType() == Packets._BATTERY_LEVEL;
  }

  public boolean isPicturePost() {
    return getPacketType() == Packets._LAST_IMAGE;
  }

  public boolean isLocationPost(){
    return getPacketType() == Packets._LOCATION && getLongitude() == 0 && getLatitude() == 0;
  }

  public static class Builder {
    private Post post = new Post();

    public Builder setRelationalNotifier(String sid) {
      post.put(3, sid);
      return this;
    }

    public Builder setPacketType(int pt) {
      post.put(2, pt);
      return this;
    }

    public Builder setPrivacy(int pr) {
      post.put(1, pr);
      return this;
    }

    public Builder rcs(boolean ask) {
      post.put(4, ask);
      return this;
    }

    public Builder setImageLocalePath(String path) {
      post.put(6, path);
      return this;
    }

    public Builder setOwner(String owner) {
      post.put(10, owner);
      return this;
    }

    public Builder setText(String text) {
      post.put(8, text);
      return this;
    }

    public Builder setTimestamp(long timestamp) {
      post.put(9, timestamp);
      return this;
    }

    public Builder setUploadedFilename(String s) {
      post.put(12, s);
      return this;
    }

    public Post built() {
      return post;
    }

  }

  @Override public int hashCode() {
    return getSid() != null ? getSid().hashCode(): getId();
  }

}
