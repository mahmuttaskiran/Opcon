package com.opcon.notifier.environment.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.opcon.LocaleUncaughtExceptionHandler;
import com.opcon.database.NotifierProvider;
import com.opcon.notifier.ConditionChecker;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.utils.PreferenceUtils;

import java.util.List;

public class IncomingMessageReceiver extends BroadcastReceiver implements OperationProcessManager.OperationProcessListener {

    public static final String PREF_FROM = "pref_im_from";
    public static final String PREF_BODY = "pref_im_body";
    public static final String PREF_TIME = "pref_im_time";

    public IncomingMessageReceiver() {}

    @Override public void onReceive(Context context, Intent intent) {

        LocaleUncaughtExceptionHandler.writeToLog(String.format("inComingMessageReceived: %s", intent.getAction()));

        List<Notifier> notifiers = NotifierProvider.Utils.getProgressableNotifiersWithConditionType(context, Conditions._IN_MSG, Conditions.__IN_MSG);

        LocaleUncaughtExceptionHandler.writeToLog(String.format("incomingMessageReceived: %d", notifiers.size()));

        String from = null;
        String msg = null;

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] messages;
            if (bundle != null) {
                try {
                    Object[] pdus = (Object[]) bundle.get("pdus");
                    if (pdus != null) {
                        messages = new SmsMessage[pdus.length];

                        for (int i = 0; i < messages.length; i++) {
                            messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            from = messages[i].getOriginatingAddress();
                            msg = messages[i].getMessageBody();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            PreferenceUtils.putSecureString(context, PREF_FROM, from);
            PreferenceUtils.putSecureString(context, PREF_BODY, msg);
            PreferenceUtils.putLong(context, PREF_TIME, System.currentTimeMillis());
            OperationProcessManager operationProcessManager = OperationProcessManager.getInstance();
            ConditionChecker mConditionChecker = new ConditionChecker(context);
            mConditionChecker.putParam("triggerPhone", from);
            for (Notifier notifier : notifiers) {
                boolean check = mConditionChecker.backoff(300).check(notifier);
                if (check) {
                    operationProcessManager.processAsync(context, notifier, this);
                }
            }
        }
    }

    @Override public void onSuccessfulOperated(Notifier r) {}
    @Override public void onFatalOperation(Notifier r) {}

}
