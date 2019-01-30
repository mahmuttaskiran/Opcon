package com.opcon.notifier;

import com.opcon.components.NotifierLog;

import java.util.ArrayList;

/**
 * Created by Mahmut Taşkiran on 15/12/2016.
 */

public class NotifierLogEventDispatcher {

    private volatile static NotifierLogEventDispatcher singleton;

    public interface NotifierLogEventListener {
        void onNewNotifierLog(NotifierLog notifierLog, boolean receivedLog);
        void onDeletedLogs(int id);
    }

    private ArrayList<NotifierLogEventListener> listeners;

    private NotifierLogEventDispatcher(){
        listeners = new ArrayList<>();
    }

    public static NotifierLogEventDispatcher getInstance() {
        if (singleton == null) {
            synchronized (NotifierLogEventDispatcher.class) {
                if (singleton == null) {
                    singleton = new NotifierLogEventDispatcher();
                }
            }
        }
        return singleton;
    }

    public void addEventListener(NotifierLogEventListener listener)  {
        if (!this.listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(NotifierLogEventListener listener) {
        this.listeners.remove(listener);
    }

    public void dispatchDeletedAll(int id) {
        for (NotifierLogEventListener listener : listeners) {
            listener.onDeletedLogs(id);
        }
    }

}
