package com.opcon.ui.views;

import android.content.Context;
import android.text.Spanned;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.utils.MobileNumberUtils;

import agency.tango.android.avatarview.views.AvatarView;
import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by Mahmut Taşkiran on 10/10/2016.
 */

public class ContactView extends LinearLayout {

    @BindView(R.id.contact_letter) TextView tvLetter;
    @BindView(R.id.contact_name)   TextView tvName;
    public @BindView(R.id.contact_notifier_icon) ImageView bNotifierIcon;
    @BindView(R.id.contact_avatar)
    AvatarView civ_avatar;
    @BindView(R.id.contact_root) RelativeLayout llRootView;
    @BindView(R.id.contactCircle) CircleRelativeLayout mCircle;
    @BindView(R.id.number)
    TextView mPhoneNumber;


    public ContactView(Context context) {
        super(context);
        inject();
    }

    public ContactView(Context context, AttributeSet attrs) {
        super(context, attrs);
        inject();
    }

    private void inject() {
        View mainview = LayoutInflater.from(getContext()).inflate(R.layout.cview_contact, this, true);
        ButterKnife.bind(this, mainview);
    }

    public void setContactName(Spanned spanned) {
        tvName.setText(spanned);
    }

    public void setContactName(String contactName) {
        tvName.setText(contactName);
    }

    public void setPhoneNumber(String phonenumber) {
        mPhoneNumber.setText(MobileNumberUtils.toInternational(phonenumber, null, true));
    }

    public void setLetterVisible(boolean bool, char c) {
        if (bool) {
            tvLetter.setText(String.valueOf(c));
            tvLetter.setVisibility(VISIBLE);
        } else {
            tvLetter.setVisibility(INVISIBLE);
        }
    }

    public AvatarView getReferenceOfImageView() {
        return civ_avatar;
    }

    public void setSelected(boolean bool) {
        if (!bool) {
            TypedValue outValue = new TypedValue();
            getContext().getTheme().resolveAttribute(android.R.attr.selectableItemBackground, outValue, true);
            llRootView.setBackgroundResource(outValue.resourceId);
        } else {
            llRootView.setBackgroundResource(R.drawable.contact_non_select);
        }
    }

    public RelativeLayout getRootReference() {
        return llRootView;
    }

    public CircleRelativeLayout getGavelButtonRef() {
        return mCircle;
    }

}
