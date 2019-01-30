package com.opcon.firebaseclient;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.opcon.notifier.components.Condition;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.Operation;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.constants.Operations;
import com.opcon.notifier.components.constants.Packets;

/**
 * Created by aslitaskiran on 26/05/2017.
 */

public class Analytics {

  Context context;
  FirebaseAnalytics mAnalytics;

  private Analytics(Context context) {
    this.context = context;
    mAnalytics = FirebaseAnalytics.getInstance(context);
  }

  public static Analytics instance(Context context) {
    return new Analytics(context);
  }


  public void log(Notifier notifier) {

    if (notifier == null || notifier.getCondition() == null || notifier.getOperation() == null) {
      return;
    }

    FirebaseAnalytics mAnalytics = FirebaseAnalytics.getInstance(context);
    Bundle bundle = new Bundle();
    bundle.putString("condition", Conditions.getConditionAlias(notifier.getCondition().getId()));
    bundle.putString("operation", Operations.getOperationAlias(notifier.getOperation().getId()));
    bundle.putString("packet", Packets.getPacketAlias(notifier.getOperation().getPacketType()));
    if (notifier.isProfileUpdater()){
      mAnalytics.logEvent("profile_updater", bundle);
    } else {
      mAnalytics.logEvent("notifier", bundle);
    }
    String s = eventNameOfNotifier(notifier);
    if (s != null) {
      if (s.length() > 40) {
        s = s.substring(0, 40);
      }
      s = s.toLowerCase();

      mAnalytics.logEvent(s, null);
      mAnalytics.logEvent(Conditions.getConditionAlias(
          notifier.getCondition().getId()
      ), null);
      mAnalytics.logEvent(Operations.getOperationAlias(
          notifier.getOperation().getId()
      ), null);
      mAnalytics.logEvent(Packets.getPacketAlias(
          notifier.getOperation().getPacketType()
      ), null);
    }
  }

  /*
  Burdan dönen değerin 40 karakterden büyük olmadığına çok
  dikkat etmelisin :)

  (hesapladım, burdan dönebilecek maxsimum katar büyüklüğü 40.
  tesadüfe bak sen!)
   */

  private String eventNameOfNotifier(Notifier notifier) {
    if (notifier == null){
      return null;
    }
    StringBuilder b = new StringBuilder();
    if (notifier.isProfileUpdater()) {
      b.append("post_");
    } else {
      b.append("notifier_");
    }


    b.append(notifier.getCondition().getId());

    b.append("_");

    b.append(notifier.getOperation().getId());

    b.append("_");

    b.append(notifier.getOperation().getPacketType());

    return b.toString();
  }

  public void accepted(Notifier notifier) {

    if (notifier == null || notifier.getCondition() == null || notifier.getOperation() == null) {
      return;
    }

    Condition condition = notifier.getCondition();
    Operation operation = notifier.getOperation();
    int packetType = notifier.getOperation().getPacketType();


    acceptedCondition(condition);
    acceptedOperation(operation);
    acceptedPacket(Packets.getPacketAlias(packetType));


    if (notifier.isOwnerAmI()) {

      acceptedOwnerSide(condition, operation, packetType);

    } else {

      acceptedTargetSide(condition, operation, packetType);

    }


  }

  void acceptedCondition(Condition condition) {
    if (condition == null) {
      return;
    }
    mAnalytics.logEvent("accepted_" + Conditions.getConditionAlias(condition.getId()).toLowerCase(), null);
  }

  void acceptedOperation(Operation operation) {
    if (operation == null) {
      return;
    }
    mAnalytics.logEvent("accepted_" + Operations.getOperationAlias(operation.getId()).toLowerCase(), null);
  }

  void acceptedPacket(String p_name) {
    if (p_name == null) {
      return;
    }
    mAnalytics.logEvent(p_name, null);
  }

  void acceptedOwnerSide(Condition c_name, Operation o_name, int p_name){
    if (c_name == null || o_name == null) {
      return;
    }
    mAnalytics.logEvent("accepted_owner_side_" + c_name.getId() + "_" + o_name.getId() + "_" + p_name, null);
  }

  void acceptedTargetSide(Condition c_name, Operation o_name, int p_name){
    if (c_name == null || o_name == null) {
      return;
    }
    mAnalytics.logEvent("accepted_target_side" + c_name.getId() + "_" + o_name.getId() + "_" + p_name, null);
  }

  void recognizeWithAccountKit() {
    mAnalytics.logEvent("recognized_with_account_kit", null);
  }

  void recognizeWithOpconSmsVerificationSystemProvidedByTwilio() {
    mAnalytics.logEvent("recognized_with_opcon", null);
  }

  public void log(String eventName) {
    mAnalytics.logEvent(eventName, null);
  }

}
