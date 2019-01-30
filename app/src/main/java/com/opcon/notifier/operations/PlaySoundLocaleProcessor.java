package com.opcon.notifier.operations;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.opcon.components.Order;
import com.opcon.database.KeyBackoff;
import com.opcon.firebaseclient.ComponentSender;
import com.opcon.firebaseclient.FirebaseDatabaseManagement;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.notification.NotificationUtils;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.ui.activities.NotifierBuilderActivity;
import com.opcon.ui.fragments.occs.OperationNotification;
import com.opcon.utils.PreferenceUtils;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 20/02/2017.
 */

public class PlaySoundLocaleProcessor implements OperationProcessor {

  private volatile static PlaySoundLocaleProcessor INSTANCE = new PlaySoundLocaleProcessor();

  public static PlaySoundLocaleProcessor getInstance() {
    if (INSTANCE == null) {
      synchronized (OperationProcessor.class) {
        if (INSTANCE == null){
          INSTANCE = new PlaySoundLocaleProcessor();
        }
      }
    }
    return INSTANCE;
  }

  @Override
  public void process(Context context, Notifier notifier,
                      @Nullable SpecialPacket sp,
                      @Nullable OperationProcessManager.OperationProcessListener listener)
  {


    if (notifier.isOwnerAmI()) {
      Order order = Order.newOrderForNotifierSid(notifier.getSid());
      PresenceManager pm = PresenceManager.getInstance(context.getApplicationContext());
      boolean connected = pm
          .isConnected();
      if (!connected) {
        pm.login();
        FirebaseDatabaseManagement.getInstance(context.getApplicationContext());
      }
      ComponentSender componentSender = new ComponentSender("order/" + notifier.getReceiver(), order);
      componentSender.sent();
    } else {
      if (notifier.getOperation().getBoolean(OperationNotification.VIBRATE)) {
        vibrate(context);
      }
      play(context, notifier.getOperation().getInt(OperationNotification.SOUND_RAW_INDEX));
      String notify = notifier.getOperation().getString(OperationNotification.TEXT);
      if (!TextUtils.isEmpty(notify)) {
        notify(context, notifier);
      }
    }

  }

  public static void vibrate(Context context) {
    long now = System.currentTimeMillis();
    long lastTime = PreferenceUtils.getLong(context, "lastVibrateTimestamp", 0);
    boolean vibrate = lastTime < (now - TimeUnit.SECONDS.toMillis(1));
    if (vibrate) {
      Vibrator systemService = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
      systemService.vibrate(400);
    }
    PreferenceUtils.putLong(context, "lastVibrateTimestamp", now);
  }

  public static void play(Context context, @RawRes int resource) {


    if (KeyBackoff.getInstance(context).isKeyProcessedInDuration("playingSound", TimeUnit.SECONDS.toMillis(2))) {
      return;
    }

    try {
      MediaPlayer mediaPlayer = MediaPlayer.create(context, resource);
      if (mediaPlayer  != null) {
        if (mediaPlayer.isPlaying()) {
          mediaPlayer.reset();
        }
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
          @Override
          public void onCompletion(MediaPlayer mp) {
            mp.release();
          }
        });
        mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
          @Override
          public boolean onError(MediaPlayer mp, int what, int extra) {
            mp.release();
            return false;
          }
        });
        mediaPlayer.start();
      } else {
        // ignore
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void notify(final Context context, final Notifier r) {
    String text = r.getOperation().getString(OperationNotification.TEXT);
    Intent in = new Intent(context, NotifierBuilderActivity.class);
    in.putExtra(NotifierBuilderActivity.NOTIFIER_ID, r.getId());
    in.putExtra(NotifierBuilderActivity.BOOL_ONLY_SEEN, true);
    final NotificationCompat.Builder builder = NotificationUtils.defaultBuilder(context, r.getRelationshipName(context), text, PendingIntent.getActivity(context, 0, in, 0));
    NotificationUtils.addAvatar(context, builder, r.getRelationshipAvatar(context), new NotificationUtils.Event() {
      @Override
      public void onEvent(NotificationCompat.Builder b) {
        Notification notification = builder.build();
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(r.getId(), notification);
      }
    });
  }

}
