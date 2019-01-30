package com.opcon.services;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.opcon.components.Feature;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.converters.Converter;
import com.opcon.firebaseclient.converters.AckConverter;
import com.opcon.firebaseclient.converters.MessageConverter;
import com.opcon.firebaseclient.converters.OrderConverter;
import com.opcon.firebaseclient.converters.NotifierConverter;
import com.opcon.firebaseclient.converters.NotifierLogConverter;

import org.json.JSONObject;

import timber.log.Timber;

public class FCM extends FirebaseMessagingService {
  public FCM() {
  }

  @Override
  public void onMessageReceived(RemoteMessage remoteMessage) {
    super.onMessageReceived(remoteMessage);

    if (remoteMessage == null || remoteMessage.getData() == null) {
      return;
    }

    if (remoteMessage.getData().get("contentTitle") != null) {
      Feature feature = new Feature(new JSONObject(remoteMessage.getData()));
      ComponentListenerManager.getInstance(getApplicationContext()).notifyNewComponent(feature.getFeatureUID(), feature);
    } else {

        try {
          JSONObject data = new JSONObject(remoteMessage.getData().get("data"));
          String path = remoteMessage.getData().get("path");
          String sid = remoteMessage.getData().get("sid");
          Converter<?> converter = getConverter(path);
          if (converter != null) {
            Object component = converter.convertObj(sid, data);
            ComponentListenerManager.getInstance(getApplicationContext()).notifyNewComponent(sid, component);
          }
        } catch (Exception e) {
          e.printStackTrace();
        }

    }

  }

  private Converter<?> getConverter(String p) {
    if (p.startsWith("msgs")) {
      return MessageConverter.INSTANCE;
    } else if (p.startsWith("notifiers")) {
      return NotifierConverter.INSTANCE;
    } else if (p.startsWith("acks")) {
      return AckConverter.INSTANCE;
    } else if (p.startsWith("notifier_logs")) {
      return NotifierLogConverter.INSTANCE;
    } else if (p.startsWith("order")) {
      return OrderConverter.INSTANCE;
    }
    return null;
  }
}
