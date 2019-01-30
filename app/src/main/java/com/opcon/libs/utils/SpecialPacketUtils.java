package com.opcon.libs.utils;

import com.opcon.components.Message;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.components.SpecialPacket;

/**
 * Created by Mahmut Taşkiran on 25/12/2016.
 */

public class SpecialPacketUtils {

    public static int packetTypeToMessageType(int uid) {
        switch (uid) {
            case Packets._BATTERY_LEVEL:
                return Message.BATTERY_STATE;
            case Packets._IN_CALL:
                return Message.INCOMING_CALL;
            case Packets._IN_MSG:
                return Message.INCOMING_MESSAGE;
            case Packets._LOCATION:
                return Message.LOCATION;
            case Packets._OUT_CALL:
                return Message.OUTGOING_CALL;
            case Packets._OUT_MSG:
                return Message.OUTGOING_MESSAGE;
            case Packets._LAST_IMAGE:
                return Message.LAST_CAPTURED_IMAGE;
            default:
                return -1;
        }
    }

    public static int getPacketTypeOfMessageType(int messageType) {
        switch (messageType) {
            case Message.BATTERY_STATE:
                return Packets._BATTERY_LEVEL;
            case Message.INCOMING_CALL:
                return Packets._IN_CALL;
            case Message.INCOMING_MESSAGE:
                return Packets._IN_MSG;
            case Message.LOCATION:
                return Packets._LOCATION;
            case Message.OUTGOING_CALL:
                return Packets._OUT_CALL;
            case Message.OUTGOING_MESSAGE:
                return Packets._OUT_MSG;
            case Message.LAST_CAPTURED_IMAGE:
                return Packets._LAST_IMAGE;
            default:
                return -1;
        }
    }

    public static boolean isEmpty(SpecialPacket sp) {
        return sp == null || sp.isEmpty();
    }
}
