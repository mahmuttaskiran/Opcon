package com.opcon.notifier.utils;

import android.content.Context;

import com.opcon.libs.permission.NotifierPermissionDetective;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.fragments.occs.OPCFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 */

public class NotifierUtils {

    public static boolean isSpecialPacketExists(Notifier notifier) {
        if (notifier == null) {
            throw new NullPointerException();
        }
        return notifier.getOperation().getInt(OPCFragment.PACKET) != -1;
    }

    public static int getSpecialPacketType(Notifier r) {
        if (isSpecialPacketExists(r)) {
            return r.getOperation().getInt(OPCFragment.PACKET);
        }
        throw new IllegalArgumentException("notifier have not a special packet.");
    }

    public static boolean isAnyConditionExists(List<Notifier> notifiers, int... conditionTypes) {
        for (Notifier notifier : notifiers) {
            int c_id = notifier.getCondition().getId();
            for (int conditionType : conditionTypes) {
                if (conditionType == c_id)
                    return true;
            }
        }
        return false;
    }

    public static List<Notifier> filterWithConditionType(List<Notifier> notifiers, int c1) {
        List<Notifier> filtered = new ArrayList<>();
        for (Notifier notifier : notifiers) {
            if (notifier.getCondition().getId() == c1) {
                filtered.add(notifier);
            }
        }
        return filtered;
    }

    public static List<Notifier> filterWithConditionType(List<Notifier> notifiers, int c1, int c2) {
        List<Notifier> filtered = new ArrayList<>();
        for (Notifier notifier : notifiers) {
            int c_id= notifier.getCondition().getId();
            if (c_id == c1 || c_id == c2) {
                filtered.add(notifier);
            }
        }
        return filtered;
    }

    public static List<Notifier> permissionFilter(Context c, List<Notifier> notifiers){
        List<Notifier> result = new ArrayList<>();
        for (Notifier notifier : notifiers) {
            if (PermissionUtils.check(c,NotifierPermissionDetective.detect(notifier))) {
                result.add(notifier);
            }
        }
        return result;
    }

}
