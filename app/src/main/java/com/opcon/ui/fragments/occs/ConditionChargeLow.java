package com.opcon.ui.fragments.occs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.firebase.storage.FirebaseStorage;
import com.opcon.R;
import com.opcon.ui.utils.Restrict;
import com.opcon.ui.views.DateRestrictView;
import com.opcon.ui.views.TimeRangeRestrictView;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Mahmut Taşkiran on 18/10/2016.
 */

public class ConditionChargeLow extends OPCFragment {

    public static final int PARAM_INT_PERCENT = 0;

    Restrict timeRestrictUIManagement;

    @BindView(R.id.chargelow_date_restrict)
    DateRestrictView dateRestrictView;

    @BindView(R.id.chargelow_time_restrict)
    TimeRangeRestrictView mTimeRangeRestrict;

    @BindView(R.id.chargelow_percent)
    EditText et_mm;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.condition_charge_low, container, false);
        ButterKnife.bind(this, mainView);
        this.timeRestrictUIManagement = new Restrict(mTimeRangeRestrict, dateRestrictView);
        return mainView;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        this.timeRestrictUIManagement = null;
        et_mm.destroyDrawingCache();
        et_mm = null;
        dateRestrictView.destroyDrawingCache();
        dateRestrictView = null;
    }

    @Override public void review() {
        Integer percent = (Integer) getParam(String.valueOf(PARAM_INT_PERCENT));
        if (percent != null) {
            et_mm.setText(String.format("%%%d", percent));
        }
        timeRestrictUIManagement.init(Restrict.getTimeRestrictParamsFromConditionParams( mParams ));
    }

    @OnClick(R.id.chargelow_add) public void add() {

        int percent = getMM();
        if (percent == 10) {
            percent = 15;
        } else {
            percent += 5;
        }

        if (percent>95) {
            percent = 15;
        }

        et_mm.setText(String.valueOf("%" + percent));

    }

    @OnClick(R.id.chargelow_subtract)
    public void subtrack() {
        int percent = getMM();
        if (percent < 15) {
            percent = 15;
        } else {
            percent -= 5;
        }
        et_mm.setText(String.valueOf("%" + percent));
    }

    @Override
    public boolean checkForms() {
        if (getMM() == 0) {
            setAlert(getString(R.string.charge_low_alert_select_a_percent));
            return false;
        }
        putParam(String.valueOf(PARAM_INT_PERCENT), getMM());
        String params = timeRestrictUIManagement.getParams();
        Restrict.putTimeRestrictToConditionParams(super.mParams, params);
        return true;
    }

    private int getMM() {
        String s = et_mm.getText().toString();
        if (s.isEmpty()) {
            return 0;
        } else {
            return Integer.parseInt(s.substring(1));
        }
    }

}
