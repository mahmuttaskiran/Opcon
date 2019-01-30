package com.opcon.notifier.environment.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.opcon.database.NotifierProvider;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.ConditionChecker;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.utils.PreferenceUtils;

import java.util.List;

public class PhoneStateReceiver extends BroadcastReceiver implements OperationProcessManager.OperationProcessListener {
    private static final String TAG = "PhoneStateReceiver";

    public static final String LAST_OUTGOING_CALL = "lastOutgoingCall";
    public static final String LAST_OUTGOING_CALL_TIMESTAMP = "lastOutgoingCallTimestamp";
    public static final String LAST_INCOMING_CALL = "lastIncomingCall";
    public static final String LAST_INCOMING_CALL_TIMESTAMP = "lastIncomingCallTimestamp";

    private OperationProcessManager operationProcessManager = OperationProcessManager.getInstance();
    
    public PhoneStateReceiver() {}

    @Override public void onSuccessfulOperated(Notifier r) {}
    @Override public void onFatalOperation(Notifier r) {}

    @Override public void onReceive(Context context, Intent intent) {

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (intent.getAction().equals("android.intent.action.PHONE_STATE")) {
            telephonyManager.listen(new IncomingCallListener(context), PhoneStateListener.LISTEN_CALL_STATE);

        } else if (intent.getAction().equals(Intent.ACTION_NEW_OUTGOING_CALL)) {



            String number = intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER);

            PreferenceUtils.putSecureString(context, LAST_OUTGOING_CALL, number);
            PreferenceUtils.putLong(context, LAST_OUTGOING_CALL_TIMESTAMP, System.currentTimeMillis());

            ConditionChecker mConditionChecker = new ConditionChecker(context.getApplicationContext());
            mConditionChecker.putParam("triggerPhone", number);
            List<Notifier> notifiers = NotifierProvider.Utils.getProgressableNotifiersWithConditionType(context.getApplicationContext(), Conditions._OUT_CALL, Conditions.__OUT_CALL);
            for (Notifier r: notifiers) {
                if (mConditionChecker.check(r)) {
                    operationProcessManager.processAsync(context.getApplicationContext(), r, this);
                }
            }
        }

    }


    private static class IncomingCallListener extends PhoneStateListener implements OperationProcessManager.OperationProcessListener {

        OperationProcessManager operationProcessManager = OperationProcessManager.getInstance();
        Context context;

        private IncomingCallListener (Context context) {
            this.context = context;
        }

        @Override
        public void onCallStateChanged(int state, String number) {
            super.onCallStateChanged(state, number);
            if (state == TelephonyManager.CALL_STATE_RINGING) {
                PreferenceUtils.putSecureString(context, LAST_INCOMING_CALL, number);
                PreferenceUtils.putLong(context, LAST_INCOMING_CALL_TIMESTAMP, System.currentTimeMillis());
                ConditionChecker mConditionChecker = new ConditionChecker(context.getApplicationContext());
                mConditionChecker.backoff(1000).putParam("triggerPhone", number);
                List<Notifier> notifiers = NotifierProvider.Utils.getProgressableNotifiersWithConditionType(context.getApplicationContext(), Conditions._IN_CALL, Conditions.__IN_CALL);
                for (Notifier r: notifiers) {
                    if (mConditionChecker.check(r)) {
                        operationProcessManager.processAsync(context.getApplicationContext(), r, this);
                    }
                }
            }
        }

        @Override public void onSuccessfulOperated(Notifier r) {}
        @Override public void onFatalOperation(Notifier r) {}
    }
    
    
}
