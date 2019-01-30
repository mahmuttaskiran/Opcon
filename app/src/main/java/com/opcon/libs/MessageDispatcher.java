package com.opcon.libs;

import com.opcon.components.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmut Taşkiran on 04/12/2016.
 */

public class MessageDispatcher {

    public volatile static MessageDispatcher singleton;
    public List<MessageEventListener> listeners;

    private MessageDispatcher() {
        listeners = new ArrayList<>();
    }

    public static MessageDispatcher getInstance() {
        if (singleton == null) {
            synchronized (MessageDispatcher.class) {
                singleton = new MessageDispatcher();
            }
        }
        return singleton;
    }

    public synchronized void addMessageEventListener(MessageEventListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeMessageEventListener(MessageEventListener listener) {
        listeners.remove(listener);
    }

    public synchronized void notifyNewMessage(Message msg, boolean received) {
        for (MessageEventListener listener: listeners)
            if (listener != null)
                listener.onNewMessage(msg, received);
    }

    public synchronized void notifyDoesNotExists(int id) {
        for (MessageEventListener listener: listeners)
            if (listener != null)
                listener.detectedDoesNotExists(id);
    }

    public interface MessageEventListener {
        void onNewMessage(Message message, boolean received);
        void detectedDoesNotExists(int id);
    }

}
