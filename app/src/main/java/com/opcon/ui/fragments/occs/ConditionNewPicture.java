package com.opcon.ui.fragments.occs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.opcon.R;
import com.opcon.ui.utils.Restrict;
import com.opcon.ui.views.DateRestrictView;
import com.opcon.ui.views.TimeRangeRestrictView;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by Mahmut Taşkiran on 20/10/2016.
 */

public class ConditionNewPicture extends OPCFragment {

    private int resumeRegister = 0;

    @BindView(R.id.new_picture_date_restrict)
    DateRestrictView dateRestrictView;

    @BindView(R.id.new_picture_time_restrict)
    TimeRangeRestrictView timeRangeRestrictView;

    Restrict management;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.condition_new_picture, container, false);
        ButterKnife.bind(this, view);
        this.management = new Restrict(timeRangeRestrictView, dateRestrictView);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (resumeRegister == 0) {
            if (getInitType() == InitType.INIT) {
                review();
            }
        }
        resumeRegister ++;
    }

    @Override
    public void review() {
        management.init(Restrict.getTimeRestrictParamsFromConditionParams(mParams));

    }

    @Override
    public boolean checkForms() {
        String params = management.getParams();
        Restrict.putTimeRestrictToConditionParams(super.mParams, params);
        return true;
    }

}
