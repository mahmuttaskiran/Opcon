package com.opcon.firebaseclient;

import android.content.Context;
import android.support.annotation.Nullable;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.opcon.firebaseclient.converters.Converter;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Mahmut Taşkiran on 17/02/2017.
 */

public class FirebasePathListener implements ChildEventListener{
  private ComponentListenerManager mClm;
  private Converter<?> mConverter;
  public FirebasePathListener(Context context, @Nullable Converter converter) {
    mClm = ComponentListenerManager.getInstance(context);
    mConverter = converter;
  }
  @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
    Object o;
    if (mConverter == null)
      o = dataSnapshot;
    else{
      o = mConverter.convertObj(dataSnapshot.getKey(),
          new JSONObject(dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {})));
    }
    mClm.notifyNewComponent(dataSnapshot.getKey(), o);
    dataSnapshot.getRef().setValue(null);
  }
  @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
  @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
  @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
  @Override public void onCancelled(DatabaseError databaseError) {}
}
