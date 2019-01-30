package com.opcon.notifier.components.constants;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Mahmut Taşkiran on 18/01/2017.
 *
 */

public class Operations {
    // on the target
    public static final int _SENT_MSG = 0;
    public static final int _PLAY_SOUND = 3;


    // on the owner
    public static final int __PLAY_SOUND = 2;
    public static final int __SENT_MSG = 5;
    public static final int __POST = 6;

    // public static final int TEST = 573;

    public static boolean isOnTheTarget(int i) {
        return i == _SENT_MSG || i == _PLAY_SOUND;
    }

    public static boolean isOnTheOwner(int i) {
        return i == __SENT_MSG|| i == __PLAY_SOUND || i == __POST;
    }

    public static boolean canHoldPacket(int i) {
        return i == _SENT_MSG || i == __SENT_MSG || i == __POST;
    }

    public static boolean isNotification(int id) {
        return id == _PLAY_SOUND || id == __PLAY_SOUND;
    }

    public static boolean isSendMessage(int id) {
        return id == _SENT_MSG || id == __SENT_MSG;
    }

    public static boolean isPost(int id) {
        return id == __POST;
    }

    public static String getOperationAlias(int operationType) {
        Field[] fields = Operations.class
            .getFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && field.getType().getSimpleName().equals("int")) {
                try {
                    if (field.getInt(null) == operationType) {
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
