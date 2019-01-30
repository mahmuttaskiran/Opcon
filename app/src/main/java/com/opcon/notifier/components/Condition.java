package com.opcon.notifier.components;

import com.opcon.components.Component;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.utils.RestrictChecker;
import com.opcon.ui.utils.Restrict;

import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 31/10/2016.
 *
 */

public final class Condition extends Component {

    public Condition(JSONObject json) {
        put(json);
    }

    public boolean isValidTime() {

        Timber.d("timeFilter: isRestricted: %s", isRestricted());
        Timber.d("timeFilter: isValidDate: %s", RestrictChecker.isValidDate(getDateRestrictParams()));
        Timber.d("timeFilter: isValidTimeRange: %s", RestrictChecker.isValidTimeRange(getTimeRangeRestrictParams()));

        return !isRestricted() || (RestrictChecker.isValidDate(getDateRestrictParams()) &&
            RestrictChecker.isValidTimeRange(getTimeRangeRestrictParams()));
    }

    public boolean isRestricted() {
      return getRestrictParams() != null;
    }

    public Component getRestrictParams() {
        return getComponent(Restrict.RESTRICT);
    }

    public Component getTimeRangeRestrictParams() {
        Component restrictParams = getRestrictParams();
        if (restrictParams !=null) {
            return restrictParams.getComponent(Restrict.TIME_RESTRICT);
        } else {
            return null;
        }
    }

    public String getFromAsString() {
        Component trp = getTimeRangeRestrictParams();
        if (trp != null) {
            return String.format("%02d:%02d", trp.getInt(Restrict.FROM_HOURS), trp.getInt(Restrict.FROM_MINUTES));
        }
        return null;
    }

    public String getToAsString() {
        Component trp = getTimeRangeRestrictParams();
        if (trp != null) {
            return String.format("%02d:%02d", trp.getInt(Restrict.TO_HOURS), getTimeRangeRestrictParams().getInt(Restrict.TO_MINUTES));
        }
        return null;
    }

    public String getDateAsString() {
        Component drp = getDateRestrictParams();
        if (drp != null) {
            return String.format("%02d/%02d/%04d", drp.getInt(Restrict.DAY), drp.getInt(Restrict.MONTH), drp.getInt(Restrict.YEAR));
        }
        return null;
    }

    public Component getDateRestrictParams() {
        Component restrictParams = getRestrictParams();
        if (restrictParams != null)
            return restrictParams.getComponent(Restrict.DATE_RESTRICT);
        else
            return null;
    }

    public String alias() {
        return Conditions.getConditionAlias(getId());
    }

}
