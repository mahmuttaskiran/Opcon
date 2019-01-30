package com.opcon.services;

import android.text.TextUtils;

import com.google.firebase.iid.FirebaseInstanceId;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.utils.PreferenceUtils;

public class FIDS extends com.google.firebase.iid.FirebaseInstanceIdService {

  public static final String PREF_TOKEN = "firebase_token";

  @Override
  public void onTokenRefresh() {
    super.onTokenRefresh();
    String token = FirebaseInstanceId.getInstance().getToken();
    PreferenceUtils.putString(getApplicationContext(), PREF_TOKEN, token);
    if (!TextUtils.isEmpty(PresenceManager.uid())) {
      PresenceManager.getInstance(getApplicationContext())
          .updateFcmToken();
    }

  }
}
