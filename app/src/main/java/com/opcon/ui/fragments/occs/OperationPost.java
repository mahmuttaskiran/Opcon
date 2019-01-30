package com.opcon.ui.fragments.occs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.opcon.R;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.ui.utils.NotifierConstantUtils;
import com.opcon.ui.views.NewChatInput;
import com.opcon.ui.views.TitleView;
import com.opcon.ui.views.VerticalPacketPickerView;

import java.util.Arrays;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by Mahmut Taşkiran on 17/03/2017.
 */

public class OperationPost extends OPCFragment {

  public static final int PRIVACY = 100;
  public static final int TEXT = 101;

  public static final int RCS = 102;

  public static final int CONTACTS = 0;

  @BindView(R.id.chatinput)
  NewChatInput mChatInput;
  @BindView(R.id.packetPicker)
  VerticalPacketPickerView mPacketPicker;

  @BindView(R.id.selectAnSpecialPacket)
  TitleView mTitleView;


  private int mSelectedPacket = -1;

  private boolean forProfileUpdaters = false;

  @Override
  public void review() {
    if (hasParam(TEXT)) {
      String text = (String) getParam(TEXT);
      mChatInput.setText(text);
    }

    if (hasParam(PACKET)) {
      int packet = (Integer) getParam(PACKET);
      setSpecialPacket(packet);
    }
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view;
    view = inflater.inflate(R.layout.operation_post, container, false);
    ButterKnife.bind(this, view);
    mChatInput.setActivityReference(getActivity());
    mChatInput.setVisibilityOfSendButton(false);
    mPacketPicker.setListener(new VerticalPacketPickerView.SpecialPacketSelectListener() {
      @Override
      public void onSpecialPacketSelected(int id, String title) {
        setSpecialPacket(id);
      }
    });

    mChatInput.setHint(getString(R.string.write));
    mPacketPicker.setPacketFilter(Arrays.asList(Packets._BATTERY_LEVEL, Packets._LOCATION, Packets._LAST_IMAGE));
    mPacketPicker.setForSender(true);

    if (forTarget)
      mPacketPicker.showTitles(false);

    if (getPacketFilter() != null ){
      mPacketPicker.setPacketFilter(getPacketFilter());
    }

    mTitleView.setRightIconClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        setSpecialPacket(-1);
      }
    });

    mTitleView.hideRightSide();

    forProfileUpdaters(forProfileUpdaters);

    return view;
  }

  void setSpecialPacket(int packet) {
    if (packet == -1) {
      mTitleView.hideRightSide();
    } else {
      mTitleView.showRightSide();
      int pc = NotifierConstantUtils.getPacketColor(packet);
      mTitleView.setRightIconStrokeColor(pc);
      mTitleView.setRightIconColor(pc);
    }
    this.mSelectedPacket = packet;
    this.mPacketPicker.setPacket(packet);
  }

  @Override
  public boolean checkForms() {
    String text = mChatInput.getText();
    if (TextUtils.isEmpty(text) && mSelectedPacket == -1) {
      setAlert(R.string.please_enter_an_text_or_packet_for_post);
      return false;
    }
    if (!TextUtils.isEmpty(mChatInput.getText()))
      putParam(TEXT, mChatInput.getText());
    putParam(PRIVACY, CONTACTS);
    if (mSelectedPacket != -1)
      putParam(PACKET, mSelectedPacket);
    putParam(RCS, true);
    return true;
  }

  @Override
  public void onDestroyView() {
    super.onDestroyView();
  }

  @Override
  public void setPacketFilter(@Nullable List<Integer> packets) {
    super.setPacketFilter(packets);
    if (packets != null && mPacketPicker != null) {
      mPacketPicker.setPacketFilter(packets);
    }
  }

  public void forProfileUpdaters(boolean bool) {
    forProfileUpdaters = bool;
  }

}
