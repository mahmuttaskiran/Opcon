package com.opcon.firebaseclient.listeners;

import android.content.Context;

import com.opcon.components.Ack;
import com.opcon.components.Message;
import com.opcon.database.MessageProvider;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.ComponentSender;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.notification.MessageNotificator;
import com.opcon.libs.MessageDispatcher;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class GlobalMessageListener implements ComponentListenerManager.ComponentListener {
  private Context mContext;
  public GlobalMessageListener(Context context) {
    mContext = context.getApplicationContext();
  }
  @Override
  public boolean onNewComponent(Object component) {
    Message msg = (Message) component;

    String sid = ((Message) component).getSid();
    if (sid != null) {
      if (MessageProvider.Utils.sidDoesNotExists(mContext, sid)) {
        int msgId = MessageProvider.Utils.newMessage(mContext, msg);
        msg.setId(msgId);
        new ComponentSender("acks/" + msg.getSender(), new Ack(msg.getSid(), System.currentTimeMillis(), Ack.RECEIVED)).sent();
        MessageDispatcher.getInstance().notifyNewMessage(msg, true);
        if (PresenceManager.getInstance(mContext).isNotActive()) {
          Timber.d("message will notify: %s", msg.toString());
          MessageNotificator.getInstance(mContext).forThat(msg);
        }
        return true;
      }
    }

    return false;
  }
}
