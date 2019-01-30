package com.opcon.firebaseclient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmut Taşkiran on 31/01/2017.
 */

public class ConnectionDispatcher {
    public volatile static ConnectionDispatcher singleton;
    public interface FirebaseConnectionListener {
        void onConnectionChanged(boolean connected);
    }
    public static ConnectionDispatcher getInstance() {
        if (singleton == null) {
            synchronized (ConnectionDispatcher.class ) {
                if (singleton == null) {
                    singleton = new ConnectionDispatcher();
                }
            }
        }
        return singleton;
    }
    private List<FirebaseConnectionListener> listeners;
    public ConnectionDispatcher() {
        this.listeners = new ArrayList<>();
    }
    public void addConnectionListener (FirebaseConnectionListener listener ){
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }
    public void removeListener (FirebaseConnectionListener listener ) {
        listeners.remove(listener);
    }
    public void notify(boolean connected) {
        for (FirebaseConnectionListener listener : listeners) {
            if (listener != null) {
                listener.onConnectionChanged(connected);
            }
        }
    }
}
