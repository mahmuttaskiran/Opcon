package com.opcon.ui.fragments.occs;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.TextView;


import com.opcon.R;
import com.opcon.database.ContactBase;
import com.opcon.libs.utils.AnimationUtils;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.ui.activities.ContactsActivity;
import com.opcon.ui.utils.Restrict;
import com.opcon.ui.views.CircleRelativeLayout;
import com.opcon.ui.views.DateRestrictView;
import com.opcon.ui.views.TimeRangeRestrictView;

import org.json.JSONException;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Mahmut Taşkiran on 17/10/2016.
 */

public class ConditionInOut extends OPCFragment {

    public static final int PHONE = 0;
    public static final int NAME = 1;

    private String selectedPhone;
    @BindView(R.id.incoming_name)
    TextView tvName;
    @BindView(R.id.incoming_time_restrict)
    TimeRangeRestrictView timeRangeRestrictView;
    @BindView(R.id.incoming_date_restrict)
    DateRestrictView dateRestrictView;
    @BindView(R.id.incoming_contact)
    CircleRelativeLayout avatarView;
    @BindView(R.id.close) CircleRelativeLayout mClose;
    Restrict timeRestrictUIManagement;

    String mName;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = LayoutInflater.from(getContext()).inflate(R.layout.condition_incoming_call, container, false);
        ButterKnife.bind(this, mainView);
        this.timeRestrictUIManagement = new Restrict(timeRangeRestrictView, dateRestrictView);
        tvName.setText(Html.fromHtml(getString(getIndicator())));
        tvName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickContact();
            }
        });
        return mainView;
    }

    void pickContact() {
        if (TextUtils.isEmpty(selectedPhone)) {
            Intent selectAContact = new Intent(getContext(), ContactsActivity.class);
            selectAContact.putExtra(ContactsActivity.NUMBER_ENTERABLE, true);
            startActivityForResult(selectAContact, 0);
        } else {
            setSelectedContact(null, null, null);
        }
    }

    int getIndicator() {
        if (Conditions.isInCall(getType())) {
            return R.string.in_call_indicator_who;
        } else if (Conditions.isOutCall(getType())) {
            return R.string.out_call_indicator_who;
        } else if (Conditions.isInMsg(getType())) {
            return R.string.in_msg_indicator_who;
        } else {
            return R.string.out_msg_indicator_who;
        }
    }

    int getAlertIndicator() {
        if (Conditions.isInCall(getType())) {
            return R.string.in_call_indicator_alert;
        } else if (Conditions.isOutCall(getType())) {
            return R.string.out_call_indicator_alert;
        } else if (Conditions.isInMsg(getType())) {
            return R.string.in_msg_indicator_alert;
        } else {
            return R.string.out_msg_indicator_alert;
        }
    }

    @Override
    public boolean checkForms() {

        if (TextUtils.isEmpty(selectedPhone)) {
            setAlert(getAlertIndicator());
            return false;
        }

        try {
            builtParams();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void review() {
        timeRestrictUIManagement.init(Restrict.getTimeRestrictParamsFromConditionParams(mParams));
        if (super.mParams.has(String.valueOf(PHONE))) {
            try {
                String phone_number = mParams.getString(String.valueOf(PHONE));
                String avatar = ContactBase.Utils.getValidAvatar(getContext(), phone_number);
                setSelectedContact(avatar, ContactBase.Utils.getName(getContext(), phone_number), phone_number);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.incoming_contact)
    public void onClickContact() {
        AnimationUtils.scaleDownScaleUp(avatarView, 0.5f, 1, 100, 100);
        avatarView.postDelayed(new Runnable() {
            @Override
            public void run() {
                pickContact();
            }
        }, 200);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {

            String extra_phone = data.getExtras().getString(ContactsActivity.NUMBER_ENTERABLE);
            if (!TextUtils.isEmpty(extra_phone)) {
                setSelectedContact(null, extra_phone, extra_phone);
            } else {
                String name, phone, avatar;
                name = data.getExtras().getString(ContactsActivity.SELECTED_CONTACT_NAME);
                phone = data.getExtras().getString(ContactsActivity.SELECTED_CONTACT_NUMBER);
                avatar = ContactBase.Utils.getValidAvatar(getContext(), phone);
                setSelectedContact(avatar, name, phone);
            }

        }
    }

    private void builtParams() throws Exception {
        if (selectedPhone != null) {
            putParam(String.valueOf(PHONE), selectedPhone);
            if (!TextUtils.isEmpty(mName)){
                putParam(NAME, mName);
            } else {
                putParam(NAME, selectedPhone);
            }
        } else {
            removeParam(String.valueOf(PHONE));
            removeParam(String.valueOf(NAME));
        }

        Restrict.putTimeRestrictToConditionParams(mParams, timeRestrictUIManagement.getParams());
    }

    private void setSelectedContact(String avatar, String name, String phonenumber) {
        selectedPhone = phonenumber;
        mName = name;

        if (TextUtils.isEmpty(phonenumber)) {

            tvName.setText(Html.fromHtml(getString(getIndicator())));
            mClose.setVisibility(View.GONE);

        } else {

            tvName.setText(TextUtils.isEmpty(name) ? phonenumber : name);
            mClose.setVisibility(View.VISIBLE);

        }

    }


}
