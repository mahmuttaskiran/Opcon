package com.opcon.components;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.opcon.database.FeatureBase;
import com.opcon.notifier.components.Notifier;


/**
 *
 * Created by Mahmut Taşkiran on 28/11/2016.
 */

public class Dialog implements Comparable<Dialog> {
    public String destination;
    public String name;
    public String avatarPath;
    public Message lastMessage;
    public Notifier lastNotifier;
    public int nonSeenMessageLength;
    public String content;
    public Intent intent;

    public Dialog(String destination) {
        this.destination = destination;
    }
    @Override public int hashCode() {
        return destination.hashCode();
    }
    @Override public boolean equals(Object obj) {
        if (!(obj instanceof Dialog))
            return false;
        Dialog d = (Dialog) obj;
        return (this.destination.equals(d.destination));
    }
    @Override public String toString() {
        return name + ":: " +  destination + ", " + avatarPath;
    }

    @Override public int compareTo(@NonNull Dialog o) {
        long x = getLastTime(), y = o.getLastTime();
        return (x < y) ? 1 : ((x == y) ? 0 : -1);
    }
    public long getLastTime() {
        if (isAssistant()) {
            return System.currentTimeMillis();
        }
        long x = 0, y = 0;
        if (lastMessage != null)
            x = lastMessage.getSentTimestamp();
        if (lastNotifier != null)
            y = lastNotifier.getTimestamp();
        return x > y ? x : y;
    }

    public boolean isAssistant() {
        return destination.equals(FeatureBase.OPCON_TEAM);
    }

    public void setIntent(Intent in) {
        intent = in;
    }
    public Intent getIntent() {
        return intent;
    }

  public boolean isNotifierBiggerThanMessage() {
    return lastNotifier != null && (lastNotifier.getTimestamp() > (lastMessage != null ? lastMessage.getSentTimestamp() : 0));
  }
}