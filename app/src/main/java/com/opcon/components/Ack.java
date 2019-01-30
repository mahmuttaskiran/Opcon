package com.opcon.components;

import android.support.annotation.NonNull;

/**
 *
 * Created by Mahmut Taşkiran on 30/01/2017.
 */

public class Ack extends Component {

    public static final int SENT = 1;
    public static final int RECEIVED = 2;
    public static final int SEEN = 3;

    public Ack(String messageSid, long timestamp, int state) {
        put(1, messageSid);
        put(2, timestamp);
        put(3, state);
    }

    public Ack() {}

    public String getMessageSid() {
        return getString(1);
    }
    public long getTimestamp() {
        return getLong(2);
    }
    public int getState() {
        return getInt(3);
    }

    public void putToMessage(@NonNull Message msg) {
        int state = getState();
        long timestamp = getTimestamp();
        if (state == SENT) {
            msg.setSentTimestamp(timestamp);
        } else if (state == RECEIVED) {
            msg.setReceiveTimestamp(timestamp);
        } else if (state == SEEN) {
            msg.setSeenTimestamp(timestamp);
        }
    }

}
