package com.opcon.firebaseclient.listeners;

import android.content.Context;

import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.notification.MessageNotificator;
import com.opcon.notifier.NotifierEventDispatcher;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.management.DialogStoreManagement;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class GlobalNotifierListener implements ComponentListenerManager.ComponentListener {
  private Context mContext;
  public GlobalNotifierListener(Context context) {
    mContext = context;
  }
  @Override
  public boolean onNewComponent(Object component) {
    Notifier notifier = (Notifier) component;
    String sid = notifier.getSid();
    if (sid != null) {
      if (!NotifierProvider.Utils.sidExists(mContext,sid)) {
        notifier.setState(Notifier.NOT_DETERMINED);
        int notifierId = NotifierProvider.Utils.newNotifier(mContext, notifier);
        notifier.setId(notifierId);
        DialogStoreManagement.getInstance(mContext).onNewComponent(notifier);
        NotifierEventDispatcher.getInstance().dispatchAdded(notifierId);
        if (PresenceManager.getInstance(mContext).isNotActive()) {
          MessageNotificator.getInstance(mContext).forThat(notifier);
        }
        return true;
      }
    }
    return false;
  }
}
