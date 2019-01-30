package com.opcon.ui.fragments.occs;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.notifier.operations.PlaySoundLocaleProcessor;
import com.opcon.ui.dialogs.SoundListDialog;
import com.opcon.ui.views.NewChatInput;
import com.opcon.ui.views.TitleView;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by Mahmut Taşkiran on 18/10/2016.
 */

public class OperationNotification extends OPCFragment {

    public static final int SOUND = 100;
    public static final int SOUND_RAW_INDEX = 101;
    public static final int TEXT = 102;
    public static final int VIBRATE = 103;

    private String selectedSoundName = null;

    @BindView(R.id.ringtone)
    CardView mSelectRingtone;
    @BindView(R.id.chatInput)
    NewChatInput chatInput;
    @BindView(R.id.vibrate)
    CheckBox checkBoxVibrate;
    @BindView(R.id.selectSoundIndicator)
    TextView mSoundIndicator;


    private boolean noneNotification;

    private int fieldIndex = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.operation_notification_sound, container, false);
        ButterKnife.bind(this, inflate);


        chatInput.setActivityReference(getActivity());
        chatInput.setHint(getString(R.string.content_of_notification));
        chatInput.setVisibilityOfSendButton(false);

        if (noneNotification) {
            noneNotification();
        } else {
            showNotification();
        }

        setSound(-1, null);

        Animation animation = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.heart);

        mSelectRingtone.startAnimation(animation);

        return inflate;
    }

    @Override
    public void review() {

        String sound_name = (String) getParam(String.valueOf(SOUND));
        if (sound_name != null) {
            setSound((Integer) getParam(String.valueOf(SOUND_RAW_INDEX)), sound_name);
        }

        Object objVibrate = getParam(VIBRATE);
        boolean vibrate = false;
        if (objVibrate != null ){
            vibrate = (Boolean) objVibrate;
        }

        checkBoxVibrate.setChecked(vibrate);

        Object param = getParam(TEXT);
        if (param != null){
            if (!TextUtils.isEmpty(param.toString())) {
                chatInput.setText(param.toString());
            }
        }

    }

    @OnClick({R.id.ringtone, R.id.selectSoundIndicator})
    public void onClickSelect(View v) {
        if (v.getId() == R.id.selectSoundIndicator) {

          if (TextUtils.isEmpty(selectedSoundName)) {
            pick();
          } else {
            PlaySoundLocaleProcessor.play(getContext(), fieldIndex);
          }

        } else {
          pick();
        }
    }

    void pick() {
      SoundListDialog soundListDialog = new SoundListDialog(getContext(), new SoundListDialog.OnClickListener() {
        @Override
        public void onClick(int fieldID) {
          // ignore. created for test.
        }
        @Override
        public void onSelected(int fieldID, String fieldName) {
          setSound(fieldID, fieldName);
        }
      });
      soundListDialog.show();
    }

    private void setSound(int fieldIndex, String soundName) {
        this.selectedSoundName = soundName;
        this.fieldIndex = fieldIndex;

      if (TextUtils.isEmpty(soundName)) {

        mSoundIndicator.setText(R.string.to_select_notification_sound_indicator);
        mSoundIndicator.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);

      } else {
        mSoundIndicator.setText(soundName);
        mSoundIndicator.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_play_arrow_black_18dp,0,0,0);
      }

    }

    @Override
    public boolean checkForms() {

        if (noneNotification && TextUtils.isEmpty(chatInput.getText())) {
            setAlert(R.string.you_have_to_enter_notification_title);
            return false;
        } else if (!noneNotification && selectedSoundName == null) {
            setAlert(R.string.notification_sound_please_select_a_sound);
            return false;
        }


        putParam(String.valueOf(SOUND), selectedSoundName);
        putParam(String.valueOf(SOUND_RAW_INDEX), fieldIndex);
        putParam(VIBRATE, checkBoxVibrate.isChecked());
        if (!TextUtils.isEmpty(chatInput.getText())) {
            putParam(TEXT, chatInput.getText());
        }
        return true;
    }

    public void noneNotification() {
        noneNotification = true;
    }

    public void showNotification() {
        noneNotification = false;
    }
}
