package com.opcon.firebaseclient;

import android.content.Context;

import com.opcon.components.Ack;
import com.opcon.components.Component;
import com.opcon.components.Feature;
import com.opcon.components.Message;
import com.opcon.components.Order;
import com.opcon.components.NotifierLog;
import com.opcon.firebaseclient.listeners.GlobalAckListener;
import com.opcon.firebaseclient.listeners.GlobalFeatureListener;
import com.opcon.firebaseclient.listeners.GlobalMessageListener;
import com.opcon.firebaseclient.listeners.GlobalOrderListener;
import com.opcon.firebaseclient.listeners.GlobalNotifierListener;
import com.opcon.firebaseclient.listeners.GlobalNotifierLogListener;
import com.opcon.notifier.components.Notifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mahmut Taşkiran on 17/02/2017.
 */

public class ComponentListenerManager {
  private volatile static ComponentListenerManager singleton;
  public interface ComponentListener {boolean onNewComponent(Object component);}
  private Map<Class<?>, List<ComponentListener>> mListeners;
  private PacketRegister mPacketRegister;

  private ComponentListenerManager(Context context) {
    mListeners = new HashMap<>();
    mPacketRegister = PacketRegister.getInstance(context);
    setupGlobalListeners(context);
  }

  private void setupGlobalListeners(Context context) {
    addComponentListener(Message.class, new GlobalMessageListener(context.getApplicationContext()));
    addComponentListener(Ack.class, new GlobalAckListener(context.getApplicationContext()));
    addComponentListener(Notifier.class, new GlobalNotifierListener(context.getApplicationContext()));
    addComponentListener(NotifierLog.class, new GlobalNotifierLogListener(context.getApplicationContext()));
    addComponentListener(Order.class, new GlobalOrderListener(context.getApplicationContext()));
    addComponentListener(Feature.class, new GlobalFeatureListener(context.getApplicationContext()));
  }

  public static ComponentListenerManager getInstance(Context context) {
    if (singleton == null) {
      synchronized (ComponentListenerManager.class) {
        if (singleton == null) {
          singleton = new ComponentListenerManager(context);
        }
      }
    }
    return singleton;
  }

  public void addComponentListener(Class<?>klass, ComponentListener listener, int index) {
    List<ComponentListener> componentListeners = mListeners.get(klass);
    if (componentListeners == null) {
      componentListeners = new ArrayList<>();
      componentListeners.add(listener);
      mListeners.put(klass, componentListeners);
    } else {
      if (!componentListeners.contains(listener)) {
        if (index == -1) {
          index = componentListeners.size();
        }
        componentListeners.add(index, listener);
      }
    }
  }

  public void addComponentListener(Class<?> klass, ComponentListener listener) {
    addComponentListener(klass, listener, -1);
  }

  public void removeComponentListener(Class<?> klass, ComponentListener l) {
    List<ComponentListener> componentListeners = mListeners.get(klass);
    componentListeners.remove(l);
  }

  public void notifyNewComponent(String sid, Object component) {
    if (component != null && component instanceof Component) {
      ((Component) component).delete(Component.LID);
      if (!mPacketRegister.isWrapped(sid)) {
        List<ComponentListener> componentListeners = mListeners.get(component.getClass());
        for (ComponentListener componentListener : componentListeners) {
          if (!componentListener.onNewComponent(component))
            break;
        }
      }
    }
  }
}
