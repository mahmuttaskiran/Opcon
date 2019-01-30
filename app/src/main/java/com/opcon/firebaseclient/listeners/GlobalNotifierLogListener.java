package com.opcon.firebaseclient.listeners;

import android.content.Context;

import com.opcon.components.NotifierLog;
import com.opcon.database.NotifierLogBase;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.notification.NotifierLogNotificator;
import com.opcon.notifier.components.Notifier;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class GlobalNotifierLogListener implements ComponentListenerManager.ComponentListener{
  private Context mContext;
  public GlobalNotifierLogListener(Context context) {
    mContext = context;
  }
  @Override
  public boolean onNewComponent(Object component) {
      NotifierLog log = (NotifierLog) component;

      if (log.getSid() != null) {
        if (!NotifierLogBase.Utils.sidExists(mContext, log.getSid())) {
          Notifier r = NotifierProvider.Utils.get(mContext, log.getNotifierSid());
          if (r == null || !r.getRelationship().equals(log.getSender())) {
            return false;
          }
          int notifierId = r.getId();
          log.setNotifierId(notifierId);
          int id = NotifierLogBase.Utils.newLog(mContext, log);
          log.setId(id);

          if (log.getType() == NotifierLog.DELETED) {
            r.setRelationshipState(Notifier.DELETED);
            r.update(mContext);
          } else if (log.getType() == NotifierLog.STOPPED) {
            r.setRelationshipState(Notifier.STOPPED);
            r.update(mContext);
          } else if (log.getType() == NotifierLog.RERUN) {
            r.setRelationshipState(Notifier.RUNNING);
            r.update(mContext);
          } else if (log.getType() == NotifierLog.EDITED) {
            Notifier notifier = new Notifier(log.getExternalParams());
            notifier.setId(r.getId());
            if (notifier.anyProgressable()) {
              notifier.setState(Notifier.STOPPED);
            }
            NotifierProvider.Utils.updateNotifier(mContext, notifier);
          }

          if (!PresenceManager.getInstance(mContext).isActive()) {
            NotifierLogNotificator.getInstance(mContext).forThat(log);
          }
          return true;
        }
      }
    return false;
  }
}
