package com.opcon.notifier.environment.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.opcon.database.NotifierProvider;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.ConditionChecker;
import com.opcon.notifier.OperationProcessManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 */

public class OutgoingSmsReceiver extends BroadcastReceiver implements OperationProcessManager.OperationProcessListener{

    public static final String ACTION = "com.opcon.NEW_OUTGOING_SMS";

    public static final String RECEIVER = "receiver";
    public static final String TIMESTAMP = "date";
    public static final String BODY = "body";

    @Override public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {

            String receiver = extras.getString(RECEIVER);


            List<Notifier> mNotifiers = NotifierProvider.Utils.getProgressableNotifiersWithConditionType(context, Conditions._OUT_MSG, Conditions.__OUT_MSG);

            ConditionChecker mConditionChecker = new ConditionChecker(context);
            mConditionChecker.putParam("triggerPhone", receiver);
            mConditionChecker.backoff(TimeUnit.SECONDS.toMillis(1));

            for (Notifier notifier : mNotifiers) {

                if (mConditionChecker.check(notifier)) {
                    OperationProcessManager.getInstance()
                            .processAsync(context, notifier, this);
                }

            }


        }

    }

    public static Intent createIntent(String receiver, String body, long date) {
        Intent mIntent = new Intent(ACTION);
        mIntent.putExtra(BODY, body);
        mIntent.putExtra(RECEIVER, receiver);
        mIntent.putExtra(TIMESTAMP, date);
        return mIntent;
    }

    @Override public void onSuccessfulOperated(Notifier r) {}
    @Override public void onFatalOperation(Notifier r) {}
}
