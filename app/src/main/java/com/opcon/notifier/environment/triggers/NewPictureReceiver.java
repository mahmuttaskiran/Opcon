package com.opcon.notifier.environment.triggers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.firebase.crash.FirebaseCrash;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.opcon.database.KeyBackoff;
import com.opcon.database.NotifierProvider;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.notifier.ConditionChecker;
import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.ui.fragments.ChatFragment;
import com.opcon.utils.PreferenceUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

public class NewPictureReceiver extends BroadcastReceiver implements OperationProcessManager.OperationProcessListener{
    public static final String PREF_LAST_CAPTURED_IMAGE = "lastCapturedImage";
    public NewPictureReceiver() {}
    @Override public void onReceive(Context context, Intent intent) {

        if (!PermissionUtils.checkReadWriteExternalStorage(context))
            return;

        Uri data = intent.getData();
        if (data != null) {
            String path = FileUtils.getFile(context, data).getAbsolutePath();

            if (!KeyBackoff.getInstance(context).keyIsExactlyProcessed(path)) {
                PreferenceUtils.putString(context, PREF_LAST_CAPTURED_IMAGE, path);
                List<Notifier> mNotifiers = NotifierProvider.Utils.getProgressableNotifiersWithConditionType(context.getApplicationContext(), Conditions._NEW_PICTURE, Conditions.__NEW_PICTURE);
                ConditionChecker mConditionChecker = new ConditionChecker(context.getApplicationContext());
                for (Notifier notifier : mNotifiers) {
                    if (mConditionChecker.defaultResult(true)
                        .backoff(TimeUnit.SECONDS.toMillis(1))
                        .check(notifier))

                    {

                        Timber.d("%d::: relationship: %s, packetAlias: %s", notifier.getId(), notifier.getRelationshipName(context), Packets.getPacketAlias(notifier.getOperation().getPacketType()));

                        if (isValidForChatActivityState(notifier)) {
                            OperationProcessManager.getInstance().processAsync(context.getApplicationContext(), notifier, this);

                            Timber.d("%d :::operated!", notifier.getId());

                        }



                    }
                }
            }
        } else {
            FirebaseCrash.log("NewPictureReceiver:onReceive(): intent.getData() returned null.");
        }
    }

    public static boolean isValidForChatActivityState(Notifier notifier) {
        return !(ChatFragment.isThereAreCameraRequestFor(notifier.getRelationship()) && notifier.getOperation().getPacketType() == Packets._LAST_IMAGE);

    }

    @Override public void onFatalOperation(Notifier r) {}
    @Override public void onSuccessfulOperated(Notifier r) {}
}
