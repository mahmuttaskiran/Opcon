package com.opcon.firebaseclient.converters;

import com.opcon.notifier.components.Notifier;

import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class NotifierConverter implements Converter<Notifier> {
  public static NotifierConverter INSTANCE = new NotifierConverter();
  @Override
  public Notifier convertObj(String sid, JSONObject t) {
    Notifier notifier = new Notifier(t);
    if (sid != null) {
      notifier.setSid(sid);
    }
    return notifier;
  }
}