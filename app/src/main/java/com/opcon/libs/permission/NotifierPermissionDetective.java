package com.opcon.libs.permission;

import android.Manifest;
import android.content.Context;
import android.os.Build;
import android.support.annotation.StringRes;

import com.opcon.R;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.constants.Operations;
import com.opcon.notifier.components.constants.Packets;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * Created by Mahmut Taşkiran on 25/10/2016.
 *
 */

public class NotifierPermissionDetective {

    public static String[] detect(Notifier notifier) {
        int conditionType = -1;
        int operationType = -1;
        int packetType = -1;

        if (notifier.isConditionProgressable()) {
            conditionType = notifier.getCondition().getId();
        }

        if (notifier.isOperationProgressable()) {
            operationType = notifier.getOperation().getId();
            if (notifier.getOperation().isPacketExists()) {
                packetType = notifier.getOperation().getPacketType();
            }
        }

        return detect(conditionType, operationType, packetType);
    }

    public static String[] detect(int conditionType,
                                  int operationType,
                                  int specialPacket /* -1: null value */) {

        ArrayList<String> permissions = new ArrayList<>();
        String[] conditionPermissions = conditionType != -1 ? ConditionPermissionDetective.detect(conditionType): null;
        String[] operationPermissions = operationType != -1 ? OperationPermissionDetective.detect(operationType): null;
        String[] specialPacketPermissions = SpecialPacketPermissionDetective.detect(specialPacket);


        if (conditionPermissions != null) {
            // add all
            Collections.addAll(permissions, conditionPermissions);
        }

        if (operationPermissions != null) {
            // add all, don't forget, maybe same value returned.
            for (String operationPermission : operationPermissions) {
                if (!permissions.contains(operationPermission))
                    permissions.add(operationPermission);
            }
        }

        if (specialPacketPermissions != null) {
            // all all, don't forget, maybe same value return.
            for (String specialPacketPermission : specialPacketPermissions) {
                if (!permissions.contains(specialPacketPermission))
                    permissions.add(specialPacketPermission);
            }
        }

        return permissions.toArray(new String[0]);
    }

    /**
     * Created by Mahmut Taşkiran on 25/10/2016.
     */

    public static class SpecialPacketPermissionDetective {

        public static String[] detect(int specialPacketType) {




            switch (specialPacketType) {
                case Packets._BATTERY_LEVEL:
                    return null;
                case Packets._IN_CALL:
                case Packets._OUT_CALL:
                    if (Build.VERSION.SDK_INT >= 16) {
                        return new String[] {
                                Manifest.permission.READ_CALL_LOG,
                        };
                    } else {
                        return new String[] {
                                Manifest.permission.READ_CONTACTS
                        };
                    }
                case Packets._IN_MSG:
                case Packets._OUT_MSG:
                    return new String[] {
                            Manifest.permission.READ_SMS,
                    };
                case Packets._LOCATION:
                    return new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                    };
                case Packets._LAST_IMAGE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        return new String[]
                                {
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                };
                    } else {
                        return new String[]
                                {
                                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                                };
                    }
            }
            return null;
        }

        public static String getPermissionDetailsForRuntimePermissions(Context context, int uid) {
            StringBuilder strBuilder = new StringBuilder();
            switch (uid) {
                case Packets._BATTERY_LEVEL:
                    return null;
                case Packets._IN_CALL:
                case Packets._OUT_CALL:
                    if (Build.VERSION.SDK_INT > 15) {
                        strBuilder.append(getStr(context, R.string.permission_readcallog_title))
                        .append(getStr(context, R.string.permission_specialpacket_readcallog));
                    } else {
                        strBuilder.append(getStr(context, R.string.permission_reaccontacts_title))
                                .append(getStr(context, R.string.permission_specialpacket_readcallog));
                    }
                    break;
                case Packets._IN_MSG:
                case Packets._OUT_MSG:
                    strBuilder.append(getStr(context, R.string.permission_readsms_title))
                            .append(getStr(context, R.string.permission_specialpacket_readsms));
                    break;
                case Packets._LOCATION:
                    strBuilder.append(getStr(context, R.string.permission_location_title))
                            .append(getStr(context, R.string.permission_specialpacket_location));
                    break;
                case Packets._LAST_IMAGE:
                    strBuilder.append(getStr(context, R.string.permission_readexternal_title))
                            .append(getStr(context, R.string.permission_specialpacket_readexternal));
                    break;

            }
            return strBuilder.toString();
        }

        private static String getStr(Context context, @StringRes int strResourceId) {
            return context.getString(strResourceId) + "\n\n";
        }

    }

    /**
     * Created by Mahmut Taşkiran on 25/10/2016.
     */

    private static class ConditionPermissionDetective {


        private static String[] detect(int conditionType) {

            switch (conditionType) {
                case Conditions._BATTERY:
                    return null;
                case Conditions._IN_CALL:
                case Conditions.__IN_CALL:
                    return new String[]
                            {
                                    Manifest.permission.READ_PHONE_STATE,
                            };
                case Conditions._OUT_CALL:
                case Conditions.__OUT_CALL:
                    return new String[]
                            {
                                    Manifest.permission.READ_PHONE_STATE,
                                    Manifest.permission.PROCESS_OUTGOING_CALLS
                            };
                case Conditions._IN_MSG:
                case Conditions.__IN_MSG:
                case Conditions._OUT_MSG:
                case Conditions.__OUT_MSG:
                    return new String[]
                        {
                            Manifest.permission.RECEIVE_SMS,
                            Manifest.permission.READ_SMS
                        };
                case Conditions._LOCATION:
                case Conditions.__LOCATION:
                    return new String[]
                        {
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        };
                case Conditions._NEW_PICTURE:
                case Conditions.__NEW_PICTURE:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        return new String[]
                            {
                                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            };
                    } else {
                        return new String[]
                            {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE
                            };
                    }
                case Conditions._TIMELY:
                case Conditions.__TIMELY:
                    return null; // it depends on system time contents and
                                //  normal permissions.
            }

            return null;
        }
    }

    /**
     * Created by Mahmut Taşkiran on 25/10/2016.
     */

    private static class OperationPermissionDetective {

        private static String[] detect(int operationType) {
            switch (operationType) {
                case Operations._SENT_MSG:
                    return null; // it depends on Opcon client.
                case Operations.__PLAY_SOUND:
                case Operations._PLAY_SOUND:
                    return null; // it depends on application component that MediaPlayer.
            }

            return null;
        }

    }
}
