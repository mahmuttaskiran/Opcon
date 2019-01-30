package com.opcon.firebaseclient;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opcon.components.Component;

import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class ComponentSender {
  public interface ComponentSentListener {
    void onSuccess(Component component);
    void onFail(Component component);
  }
  private String mPath;
  private ComponentSentListener mListener;
  private Component mComponent;
  private Object[] mExtractParams;
  public ComponentSender(String path, Component component) {
    mPath = path;
    mComponent = component;
  }
  public void setListener(ComponentSentListener listener) {
    mListener = listener;
  }

  public String sent() {

    mComponent.setSender(PresenceManager.uid());
    mComponent.setReceiver(mPath.substring(mPath.lastIndexOf("/") + 1));

    DatabaseReference push = FirebaseDatabase.getInstance().getReference(mPath).push();
    mComponent.setSid(push.getKey());



    Map<String, Object> map = mComponent.toMap();
    if (mExtractParams != null) {
      for (Object mExtractParam : mExtractParams) {
        map.remove(Component.completeParamName(mExtractParam));
      }
    }
    push.updateChildren(map).addOnCompleteListener(mCompleteListener);
    fcm(push.getKey());
    return push.getKey();
  }
  public void update() {
    DatabaseReference ref = FirebaseDatabase.getInstance().getReference(mPath);
    ref.updateChildren(mComponent.toMap());
  }
  private void fcm(String uid) {
    String target = mPath.substring(mPath.lastIndexOf("/") + 1);
    DatabaseReference fcm = FirebaseDatabase.getInstance().getReference("fcm").push();
    Map<String, Object> map = new HashMap<>();
    map.put("target", target);
    map.put("path", mPath);
    map.put("key", uid);
    map.put("root", mPath.substring(0, mPath.indexOf("/")));
    fcm.updateChildren(map);
  }
  public void deleteParams(Object... params) {
    mExtractParams = params;
  }
  private OnCompleteListener mCompleteListener = new OnCompleteListener() {
    @Override
    public void onComplete(@NonNull Task task) {
      if (task.isSuccessful()) {
        if (mListener != null)
          mListener.onSuccess(mComponent);

      } else {
        if (mListener != null)
          mListener.onFail(mComponent);
        Timber.e(task.getException());
      }
    }
  };
}
