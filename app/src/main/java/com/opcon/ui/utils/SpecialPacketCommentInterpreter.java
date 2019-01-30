package com.opcon.ui.utils;

import android.content.Context;

import com.opcon.R;
import com.opcon.notifier.components.constants.Packets;

/**
 * Created by Mahmut Taşkiran on 19/10/2016.
 */

public class SpecialPacketCommentInterpreter {

    public static String interpret(Context context, int UID, boolean forSender) {

        String result = null;

        switch (UID) {
            case Packets._BATTERY_LEVEL:
                result = batteryLevel(context, forSender);
                break;
            case Packets._IN_CALL:
                result = lastIncomingCall(context, forSender);
                break;
            case Packets._IN_MSG:
                result = lastIncomingMessage(context, forSender);
                break;
            case Packets._OUT_CALL:
                result = lastOutgoingCall(context, forSender);
                break;
            case Packets._OUT_MSG:
                result = lastOutgoingMessage(context, forSender);
                break;
            case Packets._LOCATION:
                result = location(context, forSender);
                break;

            case Packets._LAST_IMAGE:
                result = capturedImage(context, forSender);
                break;
        }

        return result;
    }


    private static String capturedImage(Context context, boolean forSender) {
        if (forSender) {
            return context.getString(R.string.interpreter_special_sender_captured_image);
        } else {
            return context.getString(R.string.interpreter_special_receiver_captured_image);
        }
    }


    private static String location(Context context, boolean forSender) {
        if (forSender) {
            return context.getString(R.string.interpreter_special_sender_location);
        } else {
            return context.getString(R.string.interpreter_special_receiver_location);
        }
    }

    private static String connectionState(Context context, boolean forSender) {
        if (forSender) {
            return context.getString(R.string.interpreter_special_sender_connection_state);
        } else {
            return context.getString(R.string.interpreter_special_receiver_connection_state);
        }
    }

    private static String motionSpeed(Context context, boolean forSender) {
        if (forSender)
            return context.getString(R.string.interpreter_special_receiver_motion_speed);

        else
            return context.getString(R.string.interpreter_special_sender_motion_speed);
    }


    private static String lastIncomingCall(Context context, boolean forSender) {
        if (forSender)
            return context.getString(R.string.interpreter_special_receiver_call_inf);

        else
            return context.getString(R.string.interpreter_special_sender_call_inf);

    }

    private static String lastOutgoingCall(Context context, boolean forSender) {
        if (forSender) {
            return context.getString(R.string.interpreter_special_receiver_outgoing_call_inf);

        } else {
            return context.getString(R.string.interpreter_special_sender_outgoing_call_inf);
        }
    }

    private static String lastOutgoingMessage(Context context, boolean forSender) {
        if (forSender) {
            return context.getString(R.string.interpreter_special_receiver_outgoing_message_inf);

        } else {
            return context.getString(R.string.interpreter_special_sender_outgoing_message_inf);
        }
    }

    private static String lastIncomingMessage(Context context, boolean forSender) {
        if (forSender) {
            return context.getString(R.string.interpreter_special_receiver_message_inf);
        } else {
            return context.getString(R.string.interpreter_special_sender_message_inf);
        }
    }

    private static String batteryLevel(Context context, boolean forSender) {
        if (forSender) {
            return context.getString(R.string.interpreter_special_receiver_battery_level);
        } else {
            return context.getString(R.string.interpreter_special_sender_battery_level);
        }

    }

}
