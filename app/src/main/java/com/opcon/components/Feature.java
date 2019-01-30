package com.opcon.components;

import com.opcon.libs.utils.JSONObjectUtils;

import org.json.JSONObject;

import java.util.Comparator;
import java.util.Locale;

/**
 * Created by Mahmut Taşkiran on 14/04/2017.
 */

public class Feature extends LanguageSensitiveComponent {

  public static Comparator<Feature> COMPARATOR = new Comparator<Feature>() {
    @Override public int compare(Feature o1, Feature o2) {
      return (int) (o2.getTimestamp() - o1.getTimestamp());
    }
  };


  public Feature(JSONObject json) {
    super(json);
    if (getTimestamp() == 0 || getTimestamp() == -1) {
      put(12, System.currentTimeMillis());
    }
  }

  public Feature(String json) {
    this(JSONObjectUtils.toJson(json));
  }

  public String getImage() {
    return getLanguageSensitiveString("imageUrl");
  }

  public String getNotificationTitle() {
    return getLanguageSensitiveString("notificationTitle");
  }

  public String getNotificationContent() {
    return getLanguageSensitiveString("notificationContent");
  }

  public String getContentTitle() {
    return getLanguageSensitiveString("contentTitle");
  }

  public String getContent() {
    return getLanguageSensitiveString("content");
  }

  public String getExtraAction() {
    return getString("extraAction");
  }

  public String getExtraActionTitle() {
    return getLanguageSensitiveString("extraActionTitle");
  }

  public String getDialogContent() {
    String dialogContent = getLanguageSensitiveString("dialogContent");
    if (dialogContent == null ){
      dialogContent = getNotificationTitle();
    }
    if (dialogContent == null) {
      dialogContent = getContentTitle();
    }
    return dialogContent;
  }

  public long getTimestamp() {
    return getLong(12);
  }

  public boolean hasNotification() {
    return getNotificationContent() != null;
  }

  public boolean hasImage() {
    return getImage() != null;
  }

  public boolean hasExtraAction() {
    return getExtraAction() != null;
  }

  public String getFeatureUID() {
    return getString("uid");
  }

  public boolean isDone() {
    return getContent() != null && getContentTitle() != null;
  }

  @Override public int hashCode() {
    if (getFeatureUID() != null ){
      return getFeatureUID().hashCode();
    } else {
      return super.hashCode();
    }
  }

  @Override public boolean equals(Object o) {
    return o instanceof Feature && getFeatureUID() != null && getFeatureUID().equals(((Feature) o).getFeatureUID());
  }

}
