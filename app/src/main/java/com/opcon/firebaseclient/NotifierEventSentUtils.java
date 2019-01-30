package com.opcon.firebaseclient;

import android.content.Context;

import com.opcon.components.Component;
import com.opcon.components.NotifierLog;
import com.opcon.database.NotifierProvider;
import com.opcon.notifier.components.Notifier;

/**
 * Created by Mahmut Taşkiran on 18/02/2017.
 */

public class NotifierEventSentUtils {

  public static void sendEdited(Notifier notifier) {
    NotifierLog notifierLog = new NotifierLog();
    notifierLog.setType(NotifierLog.EDITED);
    notifierLog.setNotifierSid(notifier.getString(Component.SID));
    notifierLog.setTimestamp(System.currentTimeMillis());
    notifierLog.setExternalParams(notifier);
    notifier.delete(1); // delete relationship state !! it is important. who is relationship?
    sendToRemote(notifierLog, notifier.getRelationship());
  }
  public static void sendDeleted(Notifier notifier) {
    send(notifier, NotifierLog.DELETED);
  }
  private static void sendRunning(Notifier notifier) {
    send(notifier, NotifierLog.RERUN);
  }
  public static void sendStopped(Notifier notifier) {
    send(notifier, NotifierLog.STOPPED);
  }
  private static void sendPacketDoNotSend(Notifier notifier) {
    send(notifier, NotifierLog.PACKET_DO_NOT_SENT);
  }
  private static void sendToRemote(NotifierLog log, String destination) {

    log.setReceiver(destination);
    log.setSender(PresenceManager.uid());

    ComponentSender componentSender = new ComponentSender("notifier_logs/" + destination, log);
    componentSender.sent();
  }
  private static void send(Notifier notifier, int type) {

    if (notifier.isProfileUpdater()) {
      return;
    }

    NotifierLog notifierLog = new NotifierLog();
    notifierLog.setType(type);
    notifierLog.setNotifierSid(notifier.getSid());
    notifierLog.setTimestamp(System.currentTimeMillis());
    sendToRemote(notifierLog, notifier.getRelationship());
  }
  public static void send(Context context, int notifierId, int type) {
    Notifier notifier = NotifierProvider.Utils.get(context, notifierId);
    if (notifier != null && !notifier.isProfileUpdater()) {
      if (type == NotifierLog.DELETED) {
        sendDeleted(notifier);
      } else if (type == NotifierLog.STOPPED) {
        sendStopped(notifier);
      } else if (type == NotifierLog.EDITED){
        sendEdited(notifier);
      } else if (type == NotifierLog.RERUN) {
        sendRunning(notifier);
      } else if (type == NotifierLog.PACKET_DO_NOT_SENT) {
        sendPacketDoNotSend(notifier);
      }
    }
  }
}
