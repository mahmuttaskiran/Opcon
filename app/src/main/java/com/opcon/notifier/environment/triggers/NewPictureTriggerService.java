package com.opcon.notifier.environment.triggers;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.opcon.database.KeyBackoff;
import com.opcon.database.NotifierProvider;
import com.opcon.notifier.ConditionChecker;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.utils.PreferenceUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class NewPictureTriggerService extends Service {
  private CameraObserver mExternalObserver;
  private CameraObserver mInternalObserver;
  public NewPictureTriggerService() {
  }

  @Override
  public IBinder onBind(Intent intent) {
    // TODO: Return the communication channel to the service.
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void onCreate() {
    super.onCreate();
    mExternalObserver = new CameraObserver(getApplicationContext(),new Handler(), MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    mInternalObserver = new CameraObserver(getApplicationContext(),new Handler(), MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, true, mExternalObserver);
    getContentResolver().registerContentObserver(MediaStore.Images.Media.INTERNAL_CONTENT_URI, true, mInternalObserver);
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    getContentResolver().unregisterContentObserver(mExternalObserver);
    getContentResolver().unregisterContentObserver(mInternalObserver);
  }

  private static class CameraObserver extends ContentObserver {
    Context mContext;
    Uri URI;
    CameraObserver(Context context, Handler handler, Uri uri) {
      super(handler);
      mContext = context.getApplicationContext();
      URI = uri;
    }
    @Override public boolean deliverSelfNotifications() {
      return super.deliverSelfNotifications();
    }
    @Override public void onChange(boolean selfChange) {
      try {
        take(URI);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
    private void take(Uri uri) {
      ContentResolver resolver= mContext.getContentResolver();
      Cursor query = resolver.query(uri, null, null, null, MediaStore.Images.ImageColumns.DATE_TAKEN + " desc");
      if (query!= null && query.moveToNext()) {
        long aLong = query.getLong(query.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
        if (aLong > System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1)) {
          String path = query.getString(query.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
          PreferenceUtils.putString(mContext, NewPictureReceiver.PREF_LAST_CAPTURED_IMAGE, path);
          boolean keyBackoff=KeyBackoff.getInstance(mContext).keyIsExactlyProcessed(path);
          if (!TextUtils.isEmpty(path) && !keyBackoff) {
            List<Notifier> mNotifiers = NotifierProvider.Utils.getProgressableNotifiersWithConditionType(mContext,
                Conditions._NEW_PICTURE, Conditions.__NEW_PICTURE);
            ConditionChecker mConditionChecker = new ConditionChecker(mContext);
            for (Notifier notifier : mNotifiers)
              if (mConditionChecker.defaultResult(true).backoff(TimeUnit.SECONDS.toMillis(1)).check(notifier)) {
                if (NewPictureReceiver.isValidForChatActivityState(notifier)) {
                  OperationProcessManager.getInstance().processAsync(mContext, notifier, null);
                }
              }
          }
        }
      }
      if (query != null) query.close();
    }
    @Override public void onChange(boolean selfChange, Uri uri) {
      try {
        take(uri);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
