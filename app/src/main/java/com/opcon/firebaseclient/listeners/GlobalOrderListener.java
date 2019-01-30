package com.opcon.firebaseclient.listeners;

import android.content.Context;
import android.text.TextUtils;

import com.opcon.components.Order;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;

/**
 * Created by Mahmut Taşkiran on 20/02/2017.
 */

public class GlobalOrderListener implements ComponentListenerManager.ComponentListener, OperationProcessManager.OperationProcessListener {
  Context mContext;
  public GlobalOrderListener(Context context) {
    mContext = context;
  }
  @Override
  public boolean onNewComponent(Object component) {
    Order order = (Order) component;
    String rl = order.getRelationNotifier();
    if (!TextUtils.isEmpty(rl)) {
      Notifier r = NotifierProvider.Utils.get(mContext, rl);
      if (r != null && r.getState() == Notifier.RUNNING && r.isOperationProgressable() && r.getRelationship().equals(order.getSender())) {
        OperationProcessManager.getInstance().processAsync(mContext, r, this);
      }
    }
    return true;
  }

  @Override
  public void onSuccessfulOperated(Notifier r) {
    // ignore
  }

  @Override
  public void onFatalOperation(Notifier r) {
    // ignore
  }
}
