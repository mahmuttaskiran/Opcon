package com.opcon.notifier.components.constants;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Created by Mahmut Taşkiran on 18/01/2017.
 *
 */

public class Conditions {
    // remote conditions
    public static final int _IN_MSG = 2;
    public static final int _IN_CALL = 0;
    public static final int _OUT_CALL = 1;
    public static final int _OUT_MSG = 3;
    public static final int _BATTERY = 5;
    // public static final int CONDITION_CONNECTION_STATE = 6;
    // public static final int EMERGENCY = 7;
    public static final int _NEW_PICTURE = 8;
    public static final int _LOCATION = 9;
    // public static final int CONDITION_LOCATION_OUT = 10;
    // locale conditions
    public static final int __IN_MSG = 11;
    public static final int __IN_CALL = 12;
    public static final int __BATTERY = 13;
    public static final int __NEW_PICTURE = 14;
    public static final int __LOCATION = 15;
    public static final int __OUT_MSG = 16;
    public static final int __OUT_CALL = 17;
    // share conditions
    public static final int _TIMELY = 4;
    public static final int __TIMELY = 19;


    public static boolean isOnTheTarget(int u) {
        return u == _IN_MSG  || u == _IN_CALL                       || u == _OUT_CALL ||
               u == _OUT_MSG ||
               u == _BATTERY || u == _NEW_PICTURE                   || u == _LOCATION ||
               u == _TIMELY;
    }

    public static boolean isOnTheOwner(int u){
        return u == __IN_MSG || u == __IN_CALL || u == __OUT_CALL || u == __OUT_MSG ||
            u == __BATTERY || u == __NEW_PICTURE || u == __LOCATION || u == __TIMELY;
    }

    public static boolean isTimely(int id){
        return id == _TIMELY || id == __TIMELY;
    }

    public static boolean isLocational(int id){
        return id == _LOCATION || id == __LOCATION;
    }

    public static boolean isInCall(int id) {
        return id == __IN_CALL || id == _IN_CALL;
    }

    public static boolean isOutCall(int id) {
        return id == _OUT_CALL || id == __OUT_CALL;
    }

    public static boolean isCall(int id) {
        return isInCall(id) || isOutCall(id);
    }

    public static boolean isInMsg(int id) {
        return id == _IN_MSG || id == __IN_MSG;
    }

    public static boolean isOutMsg(int id) {
        return id == _OUT_MSG || id == __OUT_MSG;
    }

    public static boolean isMsg(int id) {
        return isInMsg(id) || isOutMsg(id);
    }

    public static boolean isInOut(int id) {
        return isCall(id) || isMsg(id);
    }

    public static boolean isBattery(int id) {
        return id == __BATTERY || id == _BATTERY;
    }

    public static String getConditionAlias(int conditionType) {
        Field[] fields = Conditions.class
            .getFields();
        for (Field field : fields) {
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) && Modifier.isPublic(modifiers) && Modifier.isFinal(modifiers) && field.getType().getSimpleName().equals("int")) {
                try {
                    if (field.getInt(null) == conditionType) {
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
