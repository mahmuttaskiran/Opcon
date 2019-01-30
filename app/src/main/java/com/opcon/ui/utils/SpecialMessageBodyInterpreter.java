package com.opcon.ui.utils;

import android.content.Context;
import android.text.TextUtils;

import com.opcon.R;
import com.opcon.components.Message;
import com.opcon.database.ContactBase;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by Mahmut Taşkiran on 24/11/2016.
 */

public class SpecialMessageBodyInterpreter {

    private static final String FORMAT_OF_LOCATION = "%s";
    private static final String FORMAT_OF_INOUT_MESSAGE = "<font>%s</font><br>" +
            "%s<br><font color='blue'><i>%s</i></font>";
    private static final String FORMAT_OF_INOUT_CALL = "<font>%s</font><br>" +
            "<font color='blue'><i>%s</i></font>";
    private static final String FORMAT_OF_BATTERY_STATE = "<font size='17'>%s</font>";

    private static final String ADDITION_TEXT = "<br><br>%s";

    public static String interpret(Message msg, Context context) {
        String __ret = null;
        int type = msg.getType();
        if (type == Message.LOCATION){
            String address = msg.getString(Message.Location.ADDRESS);
            String m = TextUtils.isEmpty(address) ? context.getString(R.string.click_to_seen_location_details_of_packet): address;
            __ret = String.format(FORMAT_OF_LOCATION, m);
        } else if (type == Message.INCOMING_CALL || type == Message.OUTGOING_CALL) {
            __ret = String.format(FORMAT_OF_INOUT_CALL, getWho(msg.getString(Message.InoutCall.WHO), context),
                    time(msg.getLong(Message.InoutCall.WHEN)));
        } else if (type == Message.INCOMING_MESSAGE || type == Message.OUTGOING_MESSAGE) {
            __ret = String.format(FORMAT_OF_INOUT_MESSAGE,
                    getWho(msg.getString(Message.InoutMessage.WHO), context),
                    msg.getString(Message.InoutMessage.WHAT),
                    time(msg.getLong(Message.InoutMessage.WHEN)));
        } else if (type == Message.BATTERY_STATE) {
            __ret = String.format(FORMAT_OF_BATTERY_STATE, "%" + msg.getInt(Message.Battery.PERCENT));
        }

        if (!TextUtils.isEmpty(msg.getString(Message.SpecialPacket.ADDITION_TEXT))) {
            __ret += String.format(ADDITION_TEXT, msg.getString(Message.SpecialPacket.ADDITION_TEXT));
        }

        return __ret;
    }

    private static String getWho(String pn, Context context) {
        if (TextUtils.isEmpty(pn)) {
            return "Unknowns";
        }
        return ContactBase.Utils.getName(context, pn);
    }

    private static String time(long time) {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(time));
    }

}
