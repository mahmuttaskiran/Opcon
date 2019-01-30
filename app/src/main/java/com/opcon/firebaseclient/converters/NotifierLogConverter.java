package com.opcon.firebaseclient.converters;

import com.opcon.components.NotifierLog;

import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class NotifierLogConverter implements Converter<NotifierLog> {
  public static NotifierLogConverter INSTANCE = new NotifierLogConverter();
  @Override
  public NotifierLog convertObj(String sid, JSONObject t) {
    return new NotifierLog(t);
  }
}
