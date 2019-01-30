package com.opcon.ui.fragments.occs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.opcon.R;
import com.opcon.ui.utils.NotifierConstantUtils;
import com.opcon.ui.views.NewChatInput;
import com.opcon.ui.views.TitleView;
import com.opcon.ui.views.VerticalPacketPickerView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 *
 * Created by Mahmut Taşkiran on 18/10/2016.
 *
 */

public class OperationInOutMessage extends OPCFragment {
    public static final int TEXT = 100;

    @BindView(R.id.packetPicker)
    VerticalPacketPickerView mPacketPicker;

    @BindView(R.id.chatinput)
    NewChatInput mChatInput;

    @BindView(R.id.selectAnSpecialPacket)
    TitleView mTitleView;

    private boolean forOwner;

    private int selectedSpecialPacketType = -1;

    public void setForOwner(boolean forOwner) {
        this.forOwner = forOwner;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mainView = inflater.inflate(R.layout.operation_notification, container, false);
        ButterKnife.bind(this, mainView);
        mChatInput.setActivityReference(getActivity());
        mChatInput.setVisibilityOfSendButton(false);
        mChatInput.setHint(getString(R.string.write));
        mPacketPicker.setForSender(forOwner);

        if (forTarget) {
            mPacketPicker.showTitles(false);
        }

        mPacketPicker.setListener(new VerticalPacketPickerView.SpecialPacketSelectListener() {
            @Override
            public void onSpecialPacketSelected(int id, String title) {
                setSpecialPacket(id);
            }
        });
        mTitleView.setRightIconClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {

                if (selectedSpecialPacketType == -1) {


                    mPacketPicker.setPacketFilter(null);
                    setPacketFilter(null);

                } else {


                    setSpecialPacket(-1);

                }

            }
        });


        if (getPacketFilter() != null) {
            mPacketPicker.setPacketFilter(getPacketFilter());
        }

        rightSide();

        return mainView;
    }

    @Override
    public void review() {
        Integer special_packet_type =  (Integer) getParam(String.valueOf(PACKET));
        if (special_packet_type != null) {
            setSpecialPacket(special_packet_type);
        }
        String message = (String) getParam(String.valueOf(TEXT));
        if (message != null) {
            setMessage(message);
        }
    }

    private void setSpecialPacket(int specialPacketType) {

        this.selectedSpecialPacketType = specialPacketType;
        this.mPacketPicker.setPacket(specialPacketType);

        rightSide();
    }

    void rightSide() {

        if (mTitleView == null) {
            return;
        }

        if (selectedSpecialPacketType == -1) {
            if (getPacketFilter() == null){
                mTitleView.hideRightSide();
            } else {
                mTitleView.showRightSide();
                mTitleView.setRightIcon(R.drawable.ic_more_horiz);
                mTitleView.setRightIconColor(getContext().getResources().getColor(R.color.materialGrey));
                mTitleView.setRightIconStrokeColor(getContext().getResources().getColor(R.color.materialGrey));
            }
        } else {
            int pc = NotifierConstantUtils.getPacketColor(selectedSpecialPacketType);
            mTitleView.showRightSide();
            mTitleView.setRightIcon(R.drawable.ic_close_black_18dp);
            mTitleView.setRightIconStrokeColor(pc);
            mTitleView.setRightIconColor(pc);
        }
    }

    private void setMessage(String message) {
        this.mChatInput.setText(message);
    }


    @Override
    public boolean checkForms() {
        if (selectedSpecialPacketType == -1 && TextUtils.isEmpty(mChatInput.getText())) {
            setAlert(getString(R.string.notification_alert_please_enter_any_special_packet_or_text));
            return false;
        }

        if (selectedSpecialPacketType != -1) {
            putParam(String.valueOf(PACKET), selectedSpecialPacketType);

        }

        if (!TextUtils.isEmpty(mChatInput.getText())){
            putParam(TEXT, mChatInput.getText());
        }
        return true;
    }

    @Override
    public void setPacketFilter(@Nullable List<Integer> packets) {
        super.setPacketFilter(packets);
        if (packets != null && mPacketPicker != null) {
            mPacketPicker.setPacketFilter(packets);
        }
        rightSide();
    }
}
