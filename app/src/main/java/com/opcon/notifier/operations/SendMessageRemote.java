package com.opcon.notifier.operations;

import android.content.Context;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.opcon.components.Message;
import com.opcon.components.Order;
import com.opcon.database.MessageProvider;
import com.opcon.firebaseclient.ComponentSender;
import com.opcon.firebaseclient.FirebaseDatabaseManagement;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.notification.WaitingMessageNotificator;
import com.opcon.libs.utils.SpecialPacketUtils;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.libs.MessageDispatcher;
import com.opcon.ui.fragments.occs.OperationInOutMessage;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 27/02/2017.
 */

public class SendMessageRemote implements OperationProcessor {

  public volatile static SendMessageRemote INSTANCE = new SendMessageRemote();

  public static SendMessageRemote getInstance() {
    if (INSTANCE == null) {
      synchronized (SendMessageRemote.class) {
        if (INSTANCE == null){
          INSTANCE = new SendMessageRemote();
        }
      }
    }
    return INSTANCE;
  }

  @Override
  public void process(Context context, Notifier notifier, @Nullable SpecialPacket sp, @Nullable OperationProcessManager.OperationProcessListener listener) {

    if (notifier.isTargetAmI()) {

      boolean connected = PresenceManager.getInstance(context).isConnected();

      if (!connected) {
        PresenceManager.getInstance(context).login();
        FirebaseDatabaseManagement.getInstance(context.getApplicationContext());
      }

      if (notifier.getSid() != null) {
        Order order = Order.newOrderForNotifierSid(notifier.getSid());
        ComponentSender componentSender = new ComponentSender("order/" + notifier.getSender(), order);
        componentSender.sent();
      }

    } else {

      if (notifier.getOperation().isPacketExists() && (sp == null || sp.isEmpty())) {
        if (listener !=null) listener.onFatalOperation(notifier);
        return;
      }

      Message.Builder mBuilder = new Message.Builder();
      mBuilder.setReceiver(notifier.getReceiver())
          .setSentTimestamp(System.currentTimeMillis())
          .setSender(PresenceManager.uid())
          .setRelationNotifier(notifier.getSid())
          .setTriedForServer(false)
          .setWaiting(true);

      if (notifier.getOperation().isPacketExists() && sp !=null) {
        mBuilder.setType(SpecialPacketUtils.packetTypeToMessageType(sp.getId()));
        mBuilder.putSpecialPacket(sp);

        if (!TextUtils.isEmpty(notifier.getOperation().getString(OperationInOutMessage.TEXT))) {
          mBuilder.setSpecialParam(Message.SpecialPacket.ADDITION_TEXT, notifier.getOperation().getString(OperationInOutMessage.TEXT));
        }

      } else {
        mBuilder.setType(Message.TEXT);
        mBuilder.setSpecialParam(Message.Text.BODY, notifier.getOperation().getString(OperationInOutMessage.TEXT));
      }

      Message msg = mBuilder.built();

      int uid = MessageProvider.Utils.newMessage(context, msg);
      msg.setId(uid);

      if (notifier.isPacketAutomated(context)) {
        msg.send(context);
      } else {
        if (!PresenceManager.getInstance(context).isActive() && notifier.isNotificationOn(context)) {
          WaitingMessageNotificator.getInstance(context).forThat(msg);
        }
      }

      MessageDispatcher.getInstance().notifyNewMessage(msg, false);

    }

  }


}
