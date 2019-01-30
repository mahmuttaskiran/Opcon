package com.opcon.ui.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.database.NotifierLogBase;
import com.opcon.database.NotifierProvider;
import com.opcon.notifier.components.Notifier;

/**
 * Created by Mahmut Taşkiran on 21/10/2016.
 */

public class NotifierChoiceDialog extends Dialog
                              implements View.OnClickListener
{
    public interface OnChoiceListener {
        void onStop(int LID);
        void onStart(int LID);
        void onShowLogs(int LID);
        void onDelete(int LID);
        void onShare(int LID);
        void onEdit(int LID);
        void onSeeDetails(int LID);
    }

    private OnChoiceListener listener;
    private int LID;

    private TextView onToggleWorkState;
    private TextView onShowNotification;
    private TextView onSeenDetails;
    private TextView onEdit;
    private TextView onShare;
    private TextView onDelete;

    private TextView mNotificationState;
    private SwitchCompat mAutomation;

    Notifier notifier;

    private int state;

    public NotifierChoiceDialog(final Context context, final int LID, OnChoiceListener listener) {
        super(context);
        setContentView(R.layout.dialog_notifier_choices);

        this.listener = listener;
        this.LID = LID;


        this.state = (Integer) NotifierProvider.Utils.getSingleColumnRow(getContext(),
                Integer.class, NotifierProvider.STATE, NotifierProvider.LID + "=" + LID,
                null);

        this.onEdit = (TextView) findViewById(R.id.rcEdit);
        this.onShare = (TextView) findViewById(R.id.rcShare);
        this.onSeenDetails = (TextView) findViewById(R.id.rcDetails);
        this.onToggleWorkState = (TextView) findViewById(R.id.rcToggle);
        this.onShowNotification = (TextView) findViewById(R.id.rcGlobal);
        this.onDelete = (TextView) findViewById(R.id.rcDelete);

        this.mAutomation = (SwitchCompat) findViewById(R.id.automation);
        this.mNotificationState = (TextView) findViewById(R.id.rcNotificationState);

        int length = NotifierLogBase.Utils.getLogCount(getContext(), LID);

        if (length > 0) {
            onShowNotification.setVisibility(View.VISIBLE);
        } else {
            onShowNotification.setVisibility(View.GONE);
        }

        this.onEdit.setOnClickListener(this);
        this.onShare.setOnClickListener(this);
        this.onSeenDetails.setOnClickListener(this);
        this.onToggleWorkState.setOnClickListener(this);
        this.onShowNotification.setOnClickListener(this);
        this.onDelete.setOnClickListener(this);

        mNotificationState.setOnClickListener(this);
        if (state == Notifier.STOPPED ||
                state == Notifier.NOT_DETERMINED) {
            onToggleWorkState.setText(getContext().getString(R.string.choice_work_it));
        }

        notifier = NotifierProvider.Utils.get(context, LID);


        if (notifier.isConditionProgressable() || notifier.isOperationProgressable()) {
            onToggleWorkState.setVisibility(View.VISIBLE);
            onEdit.setVisibility(View.VISIBLE);
        } else {
            onToggleWorkState.setVisibility(View.GONE);
        }

        onShare.setVisibility(View.GONE); // ""   ""       ""    "" "" "" ""

        mAutomation.setVisibility(View.GONE);

        /*
        if (notifier.isProfileUpdater()) {
            onShowNotification.setVisibility(View.GONE);
        } else {
            if (notifier.getOperation().getPacketType() != Packets._LAST_IMAGE && notifier.isPacketAutomateable()) {
                mAutomation.setVisibility(View.VISIBLE);
                mAutomation.setChecked(notifier.isPacketAutomated(getContext()));
                mAutomation.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        Notifier.setPacketAutomationState(context, notifier.getId(), isChecked);
                    }
                });
            }
        }

         */


        if (notifier.isNotificationOn(context)) {
            mNotificationState.setText(R.string.notification_off);
        } else {
            mNotificationState.setText(R.string.notification_on);
        }

        onSeenDetails.setVisibility(View.GONE);

    }

    @Override
    public void onClick(View view) {
        if (view.getId() == onToggleWorkState.getId()) {
            if (state == Notifier.RUNNING) {
                listener.onStop(LID);
            } else if (state == Notifier.STOPPED ||
                    state == Notifier.NOT_DETERMINED) {
                listener.onStart(LID);
            }
        } else if (view.getId() == onShowNotification.getId()) {
            listener.onShowLogs(LID);
        } else if (view.getId() == onEdit.getId()) {
            listener.onEdit(LID);
        } else if (view.getId() == onDelete.getId()) {
            listener.onDelete(LID);
        } else if (view.getId() == onShare.getId()) {
            listener.onShare(LID);
        } else if (view.getId() == onSeenDetails.getId()) {
            listener.onSeeDetails(LID);
        } else if (mNotificationState.getId() == view.getId()) {
            boolean state = notifier.isNotificationOn(getContext());
            Notifier.setNotificationState(getContext(), notifier.getId(), !state);
            listener.onShare(notifier.getId());
        }
        dismiss();
    }

}
