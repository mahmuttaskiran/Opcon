package com.opcon.ui.fragments.occs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.erz.timepicker_library.TimePicker;
import com.opcon.R;
import com.opcon.ui.utils.Restrict;
import com.opcon.ui.views.DateRestrictView;

import java.util.Date;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by Mahmut Taşkiran on 18/10/2016.
 */

public class ConditionTime extends OPCFragment {

    public static final int HOUR = 0;
    public static final int MINUTES = 1;

    Restrict timeRestrictUIManagement;
    @BindView(R.id.time_time_picker)
    TimePicker timePicker;
    @BindView(R.id.time_date_restrict)
    DateRestrictView dateRestrictView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.condition_time, container, false);
        ButterKnife.bind(this, mainView);
        timePicker.enableTwentyFourHour(true);

        this.timeRestrictUIManagement = new Restrict(null, dateRestrictView);
        return mainView;
    }

    @Override
    public void review() {

        if (getInitType() == InitType.INIT) {
            timeRestrictUIManagement.init(Restrict.getTimeRestrictParamsFromConditionParams(mParams));
        }

        Date date = new Date();
        Integer hours = (Integer) getParam(String.valueOf(HOUR));
        Integer minutes = (Integer) getParam(String.valueOf(MINUTES));

        if (hours != null && minutes != null) {
            date.setHours(hours);
            date.setMinutes(minutes);
        }

        setTime(date);

    }

    private void setTime(Date date) {
        timePicker.setTime(date);
    }

    @Override
    public boolean checkForms() {
        putParam(String.valueOf(HOUR), timePicker.getTime().getHours());
        putParam(String.valueOf(MINUTES), timePicker.getTime().getMinutes());
        Restrict.putTimeRestrictToConditionParams(super.mParams, timeRestrictUIManagement.getParams());

        return true;
    }

}
