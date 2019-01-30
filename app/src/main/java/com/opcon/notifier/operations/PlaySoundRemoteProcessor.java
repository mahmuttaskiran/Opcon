package com.opcon.notifier.operations;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.opcon.components.Order;
import com.opcon.firebaseclient.ComponentSender;
import com.opcon.firebaseclient.FirebaseDatabaseManagement;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.ui.fragments.occs.OperationNotification;

/**
 * Created by Mahmut Taşkiran on 20/02/2017.
 * Relation operation is Operations.__PLAY_SOUND.
 * When condition triggered on target's device then
 * this processor will send signal to owner to play sound
 * if conditions is right. Otherwise, if condition triggered
 * on owner target then this processor will play ordered sound.
 */

public class PlaySoundRemoteProcessor implements OperationProcessor {

  private volatile static PlaySoundRemoteProcessor INSTANCE = new PlaySoundRemoteProcessor();

  public static PlaySoundRemoteProcessor getInstance() {
    if (INSTANCE == null) {
      synchronized (OperationProcessor.class) {
        if (INSTANCE == null){
          INSTANCE = new PlaySoundRemoteProcessor();
        }
      }
    }
    return INSTANCE;
  }

  @Override
  public void process(Context context, Notifier notifier, @Nullable SpecialPacket sp, @Nullable OperationProcessManager.OperationProcessListener listener) {
    if (notifier.isTargetAmI()) {
      Order order = Order.newOrderForNotifierSid(notifier.getSid());
      PresenceManager pm = PresenceManager.getInstance(context.getApplicationContext());
      boolean connected = pm
          .isConnected();
      if (!connected) {
        pm.login();
        FirebaseDatabaseManagement.getInstance(context.getApplicationContext());
      }
      ComponentSender componentSender = new ComponentSender("order/" + notifier.getSender(), order);
      componentSender.sent();
    } else {
      if (notifier.getOperation().getBoolean(OperationNotification.VIBRATE)) {
        PlaySoundLocaleProcessor.vibrate(context);
      }
      PlaySoundLocaleProcessor.play(context, notifier.getOperation().getInt(OperationNotification.SOUND_RAW_INDEX));
      String notify = notifier.getOperation().getString(OperationNotification.TEXT);
      if (!TextUtils.isEmpty(notify)) {
        PlaySoundLocaleProcessor.notify(context, notifier);
      }
    }
  }

}
