package com.opcon.notifier;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Mahmut Taşkiran on 25/10/2016.
 */

public class NotifierEventDispatcher {

    private volatile static NotifierEventDispatcher instance;

    private Set<NotifierEventListener> listeners;

    private NotifierEventDispatcher() {
        listeners = new HashSet<>();
    }

    public static NotifierEventDispatcher getInstance() {
        if (instance == null) {
            synchronized (NotifierEventListener.class) {
                if (instance == null) {
                    instance = new NotifierEventDispatcher();
                }
            }
        }
        return instance;
    }

    public interface NotifierEventListener {
        void onStateChanged(int LID, int state);
        void onDeleted(int LID);
        void onAdded(int LID);
        void onEdited(int LID);
    }

    public void addEventListener(NotifierEventListener notifierEvent) {
        if (!listeners.contains(notifierEvent))
            this.listeners.add(notifierEvent);
    }

    public void removeEventListener(NotifierEventListener notifierEvent) {
        this.listeners.remove(notifierEvent);
    }

    public synchronized void dispatchDelete(int LID) {
        for (NotifierEventListener listener : listeners) {
            listener.onDeleted(LID);
        }
    }

    public synchronized void dispatchAdded(int LID) {
        for (NotifierEventListener listener : listeners) {
            listener.onAdded(LID);
        }
    }

    public synchronized void dispatchStateChanged(int LID, int state) {
        for (NotifierEventListener listener : listeners) {
            listener.onStateChanged(LID, state);
        }
    }

    public synchronized void dispatchUpdate(int lid) {
        for (NotifierEventListener listener: listeners) {
            listener.onEdited(lid);
        }
    }

}
