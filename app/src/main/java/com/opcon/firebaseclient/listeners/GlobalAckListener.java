package com.opcon.firebaseclient.listeners;

import android.content.Context;

import com.opcon.components.Ack;
import com.opcon.database.MessageProvider;
import com.opcon.firebaseclient.ComponentListenerManager;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class GlobalAckListener implements ComponentListenerManager.ComponentListener {
  private Context mContext;
  public GlobalAckListener(Context context) {
    mContext = context.getApplicationContext();
  }
  @Override
  public boolean onNewComponent(Object component) {
    Ack ack = (Ack) component;
    String column = getColumn(ack.getState());
    if (column != null){
      MessageProvider.Utils.setSingleLong(mContext, ack.getMessageSid(),
          column, ack.getTimestamp());
    }
    return true;
  }
  private String getColumn(int state) {
    switch (state) {
      case Ack.RECEIVED:
        return MessageProvider.RECEIVE_TIMESTAMP;
      case Ack.SEEN:
        return MessageProvider.SEEN_TIMESTAMP;
      case Ack.SENT:
        return MessageProvider.SENT_TIMESTAMP;
      default:return null;
    }
  }
}
