package com.opcon.firebaseclient;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.opcon.components.Feature;
import com.opcon.database.FeatureBase;
import com.opcon.database.MessageProvider;
import com.opcon.database.PostBase;
import com.opcon.libs.notification.MessageNotificator;
import com.opcon.libs.notification.NotifierLogNotificator;
import com.opcon.libs.notification.WaitingMessageNotificator;
import com.opcon.libs.registration.RegistrationManagement;
import com.opcon.libs.registration.activities.RequestTokenActivity;
import com.opcon.ui.activities.ChatActivity;
import com.opcon.ui.activities.MainActivity;
import com.opcon.ui.activities.WaitingPostActivity;
import com.opcon.utils.PreferenceUtils;

import org.json.JSONObject;

import java.io.File;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class PresenceManager implements Application.ActivityLifecycleCallbacks {


  private static String AVATAR;

  private volatile static PresenceManager singleton;

  private boolean isConnected;
  private boolean isActive; // is in ui or not
  private boolean isJoined;

  private Context mContext;

  private static String SAVED_UID;

  private long lastSing;

  private PresenceManager(Context c) {
    mContext = c.getApplicationContext();
    AVATAR = getAvatar(c);
  }

  public static String getUserLocale(Context context) {
      return PreferenceUtils.getString(context,
              RequestTokenActivity.SAVED_LOCALE,
              Locale.getDefault().getCountry().toUpperCase());
  }

  public static String getAvatar(Context context) {
      ContextWrapper contextWrapper = new ContextWrapper(context);
      String avatarFileName = getAvatarFileName(context);
      if (TextUtils.isEmpty(avatarFileName)) {
        return null;
      }
      File parentDir = contextWrapper.getDir("images", MODE_PRIVATE);
      File file = new File(parentDir, avatarFileName);
      return file.exists() ? "file://" + file.getAbsolutePath() : null;
  }

  public static void setAvatarFileName(Context context, String avatarFileName) {
    PreferenceUtils.putString(context, "avatarFileName", avatarFileName);
  }

  public static String getAvatarFileName(Context context) {
    return PreferenceUtils.getString(context, "avatarFileName", null);
  }

  public static void removeAvatar(Context context) {
    ContextWrapper contextWrapper = new ContextWrapper(context);
    File parentDir = contextWrapper.getDir("images", MODE_PRIVATE);
    File file = new File(parentDir, "mAvatar.jpeg");
    if (file.exists()) {
      file.delete();
    }
  }


  void putVersion() {
    long timestamp = PreferenceUtils.getLong(mContext, "lastVersionUpdate", 0);
    if (timestamp < (System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1))) {
      if (isJoined && !RegistrationManagement.getInstance().isRegistrationRequires(mContext)) {
        FirebaseDatabase.getInstance().getReference("users/" + uid()).child("applicationVersion").setValue(com.opcon.Build.VERSION);
        PreferenceUtils.putLong(mContext, "lastVersionUpdate", System.currentTimeMillis());
      }
    }
  }

  public void init() {
    FirebaseDatabase.getInstance().getReference(".info/connected").addValueEventListener(mConnectionListener);
    FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener);
    FirebaseDatabase.getInstance().getReference("presence/" + uid()).child("status")
        .onDisconnect()
        .setValue(false);
  }

  private FirebaseAuth.AuthStateListener mAuthStateListener = new FirebaseAuth.AuthStateListener() {
    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
      isJoined = firebaseAuth.getCurrentUser() != null;
      if (isJoined && !RegistrationManagement.getInstance().isRegistrationRequires(mContext)) {
        updateFcmToken();
        putVersion();
        SAVED_UID = PreferenceUtils.getString(mContext, "user", null);
      } else {
        if (lastSing < System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(5))
        {
          String vp = RegistrationManagement.getInstance().getVolatilePassword(mContext);
          if (vp != null) {
            sing(RegistrationManagement.getInstance().getEmail(mContext), vp);
          }
        }
      }
      if (isFirstInitialize()) {
        loadWelcomeFeatures();
      }
      lastSing = System.currentTimeMillis();
    }
  };

  public static PresenceManager getInstance(Context context) {
    checkNameAndAvatar(context);
    if (singleton == null) {
      synchronized (PresenceManager.class) {
        if (singleton == null) {
          singleton = new PresenceManager(context);
        }
      }
    }
    return singleton;
  }

  private synchronized static void checkNameAndAvatar(Context context) {
    if (context != null) {
      if (TextUtils.isEmpty(AVATAR)) AVATAR = getAvatar(context);
    }
  }

  public void goOnline() {
    setPresenceStatus(true);
    updateLastTime();
  }

  private void goOffline() {
      setPresenceStatus(false);
      updateLastTime();
  }

  public void updateFcmToken() {
    FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid()).child("fcm_token")
        .setValue(FirebaseInstanceId.getInstance().getToken());
    String avatar = getAvatarDownloadUrl(mContext);
    if (TextUtils.isEmpty(avatar)) avatar = "false";
    FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid()).child("avatar").setValue(avatar);

    FirebaseMessaging.getInstance().subscribeToTopic("Opcon_Android");
    FirebaseMessaging.getInstance().subscribeToTopic("Opcon_Android_" + Build.VERSION.SDK_INT);
    FirebaseMessaging.getInstance().subscribeToTopic("Opcon_Android_" + Locale.getDefault().getCountry().toUpperCase());
    FirebaseMessaging.getInstance().subscribeToTopic("Opcon_Android_" + Locale.getDefault().getLanguage().toUpperCase());

    FirebaseMessaging.getInstance().subscribeToTopic("Opcon_Android_" + com.opcon.Build.VERSION);

  }

  public void updateLastTime() {
    if (!TextUtils.isEmpty(uid()))
      FirebaseDatabase.getInstance().getReference().child("presence")
          .child(uid())
          .child("lastActivity")
          .setValue(ServerValue.TIMESTAMP);
  }

  public boolean isFirstInitialize() {
    boolean firstQuery = PreferenceUtils.getBoolean(mContext, "welcomeFeaturesLoad", true);
    return !RegistrationManagement.getInstance().isRegistrationRequires(mContext) && firstQuery;
  }

  public void loadWelcomeFeatures() {
    FirebaseDatabase.getInstance().getReference("welcome_features").addChildEventListener(new ChildEventListener() {
      @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        JSONObject json = new JSONObject(
            dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {})
        );
        Feature feature = new Feature(json);
        FeatureBase.getInstance(mContext).newFeature(feature);
        PreferenceUtils.putBoolean(mContext, "welcomeFeaturesLoad", false);
        ComponentListenerManager.getInstance(mContext).notifyNewComponent(dataSnapshot.getKey(), feature);
      }
      @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
      @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
      @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
      @Override public void onCancelled(DatabaseError databaseError) {}
    });

  }



  public boolean isOnline() {
    return isActive && isConnected;
  }

  public boolean isConnected() {
    return isConnected;
  }

  public boolean isActive() {
    return isActive;
  }

  public boolean isNotActive() {return !isActive;}
  public boolean isJoined() {
    return isJoined;
  }

  private void setPresenceStatus(boolean status) {
    FirebaseDatabase.getInstance()
        .getReference("presence/" + uid())
        .child("status").setValue(status);
  }

  public void sing(String username, String password) {
    FirebaseAuth.getInstance()
        .signInWithEmailAndPassword(username, password);
  }

  public void login() {
    String username = RegistrationManagement.getInstance().getEmail(mContext);
    String password = RegistrationManagement.getInstance().getPassword(mContext);
    sing(username, password);
  }

  public void hiddenUi() {
    isActive = false;
    if (!RegistrationManagement.getInstance().isRegistrationRequires(mContext)) {
      goOffline();
    }
  }

  private ValueEventListener mConnectionListener = new ValueEventListener() {
    @Override public void onDataChange(DataSnapshot dataSnapshot) {
      isConnected = dataSnapshot.getValue(Boolean.class);
      ConnectionDispatcher.getInstance().notify(isConnected);
    }
    @Override public void onCancelled(DatabaseError databaseError) {
      FirebaseDatabase.getInstance().getReference(".info/connected")
          .removeEventListener(mConnectionListener);
    }
  };
  @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

  }
  @Override public void onActivityStarted(Activity activity) {}
  @Override public void onActivityResumed(Activity activity) {
    if (!RegistrationManagement.getInstance().isRegistrationRequires(mContext)) {
      if (activity instanceof MainActivity || activity instanceof ChatActivity || activity instanceof WaitingPostActivity) {
        MessageProvider.Utils.deleteWaitingMessagesThatDoesNotExists(mContext);
        PostBase.Utils.deletePostThatRelationWithRemovedImages(mContext);
      }
      isActive = true;
      cancelNotifications();
      goOnline();
    }
  }
  @Override public void onActivityPaused(Activity activity) {}
  @Override public void onActivityStopped(Activity activity) {}
  @Override public void onActivitySaveInstanceState(Activity activity,Bundle outState) {}

  @Override public void onActivityDestroyed(Activity activity) {
    if (!RegistrationManagement.getInstance().isRegistrationRequires(mContext)) {
      updateLastTime();
    }
  }

  private void cancelNotifications() {
    MessageNotificator.getInstance(mContext).cancel();
    NotifierLogNotificator.getInstance(mContext).cancel();
    WaitingMessageNotificator.getInstance(mContext).cancel();
  }

  public static String uid() {
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    if (currentUser != null) {
      if (!TextUtils.isEmpty(currentUser.getUid())) {
        return currentUser.getUid();
      }
    }
    return SAVED_UID;
  }

  public static void setAvatarDownloadUrl(Context context, String url) {
    PreferenceUtils.putString(context, "avatarDownloadUrl", url);
  }

  public static String getAvatarDownloadUrl(Context context) {
    return PreferenceUtils.getString(context, "avatarDownloadUrl", null);
  }

}
