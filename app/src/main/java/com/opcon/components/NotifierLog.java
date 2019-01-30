package com.opcon.components;

import android.content.Context;

import com.opcon.R;
import com.opcon.database.ContactBase;

import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 29/11/2016.
 *
 */

public class NotifierLog extends Component {

    // events
    public static final int DELETED = 1;
    public static final int STOPPED = 2;
    public static final int EDITED = 3;
    public static final int RERUN = 6;
    public static final int PACKET_DO_NOT_SENT = 7;
    // end of events

    public NotifierLog() {}

    public NotifierLog(JSONObject json) {
        put(json);
    }

    public void setType(int type) {
        put(2, type);
    }

    public void setTimestamp(long timestamp) {
        put(4, timestamp);
    }

    public void setNotifierSid(String sid) {
        put(1, sid);
    }

    public String getNotifierSid() {
        return getString(1);
    }

    public void setExternalParams(Component params) {
        put(12, params);
    }

    public void setNotifierId(int id) {
        put(5, id);
    }

    public int getNotifierId() {
        return getInt(5);
    }

    public int getType() {
        return getInt(2);
    }

    public String   getDescription() {
        return getString(3);
    }

    public long getTimestamp() {
        return getLong(4);
    }

    public boolean isSeen() {
        return getBoolean(6);
    }

    @SuppressWarnings("unchecked")
    public Component getExternalParams() {
       return getComponent(12);
    }

    public void setSeen(boolean seen) {
        put(6, seen);
    }

    public void setAvatar(String avatar) {
        put(11, avatar);
    }

    public String getAvatar() {
        return getString(11);
    }

    public String getTitle(Context context) {
        switch (getType()) {
            case DELETED:
                return context.getString(R.string.notifier_log_deleted);
            case STOPPED:
                return context.getString(R.string.notifier_log_stopped);
            case EDITED:
                return context.getString(R.string.notifier_log_edited);
            case RERUN:
                return context.getString(R.string.notifier_log_rerun);
            case PACKET_DO_NOT_SENT:
                return context.getString(R.string.notifier_packet_do_not_sent);
            default:
                return null;
        }
    }

    public int getIcon() {
        switch (getType()) {
            case DELETED:
                return R.drawable.ic_delete_white_18dp;
            case PACKET_DO_NOT_SENT:
                return R.drawable.ic_close_black_18dp;
            case STOPPED:
                return R.drawable.ic_stop_black_18dp;
            case RERUN:
                return R.drawable.ic_play_arrow_black_18dp;
            case EDITED:
                return R.drawable.ic_border_color_grey_600_18dp;
        }
        return R.drawable.ic_help_black_18dp;
    }


    public String getSenderName(Context context) {
        String name = getString(112);
        if (name == null) {
            name = ContactBase.Utils.getName(context, getSender());
            put(112, name);
        }
        return name;
    }
}
