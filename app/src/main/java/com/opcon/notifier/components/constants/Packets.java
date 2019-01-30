package com.opcon.notifier.components.constants;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Mahmut Taşkiran on 18/01/2017.
 *
 */

public class Packets {
    public static final int _IN_MSG = 1;
    public static final int _OUT_MSG = 2;
    public static final int _IN_CALL = 3;
    public static final int _OUT_CALL = 4;
    // public static final int _MOTION_SPEED = 5;
    public static final int _BATTERY_LEVEL = 6;
    // public static final int _CONNECTION_STATE = 7;
    public static final int _LAST_IMAGE = 8;
    public static final int _LOCATION = 9;


    public static String getPacketAlias(int packetType) {

        if (packetType == 0 || packetType == -1) {
            return "NoPacket";
        }

        Field[] fields = Packets.class
            .getFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && field.getType().getSimpleName().equals("int")) {
                try {
                    if (field.getInt(null) == packetType) {
                        return field.getName();
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

}
