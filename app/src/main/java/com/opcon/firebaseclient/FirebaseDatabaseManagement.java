package com.opcon.firebaseclient;

import android.content.Context;

import com.google.firebase.database.FirebaseDatabase;
import com.opcon.firebaseclient.converters.AckConverter;
import com.opcon.firebaseclient.converters.MessageConverter;
import com.opcon.firebaseclient.converters.OrderConverter;
import com.opcon.firebaseclient.converters.NotifierConverter;
import com.opcon.firebaseclient.converters.NotifierLogConverter;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class FirebaseDatabaseManagement {
  private volatile static FirebaseDatabaseManagement singleton;
  public static FirebaseDatabaseManagement getInstance(Context context) {
    if (singleton == null) {
      synchronized (FirebaseDatabaseManagement.class) {
        if (singleton == null) {
          singleton = new FirebaseDatabaseManagement(context);
        }
      }
    }
    return singleton;
  }
  FirebaseDatabase mDatabase;
  private FirebaseDatabaseManagement(Context context) {
    mDatabase = FirebaseDatabase.getInstance();
    listenPaths(context);
  }
  private void listenPaths(Context context) {

    mDatabase.getReference("msgs/" + PresenceManager.uid())
        .addChildEventListener(new FirebasePathListener(context, MessageConverter.INSTANCE));

    mDatabase.getReference("acks/" + PresenceManager.uid())
        .addChildEventListener(new FirebasePathListener(context, AckConverter.INSTANCE));

    mDatabase.getReference("notifiers/" + PresenceManager.uid())
        .addChildEventListener(new FirebasePathListener(context, NotifierConverter.INSTANCE));

    mDatabase.getReference("notifier_logs/" + PresenceManager.uid())
        .addChildEventListener(new FirebasePathListener(context, NotifierLogConverter.INSTANCE));

    mDatabase.getReference("order/" + PresenceManager.uid()).addChildEventListener(new FirebasePathListener(context, OrderConverter.INSTANCE));
  }
}
