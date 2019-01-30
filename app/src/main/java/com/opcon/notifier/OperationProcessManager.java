package com.opcon.notifier;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

import com.opcon.database.KeyBackoff;
import com.opcon.database.NotifierProvider;
import com.opcon.libs.permission.NotifierPermissionDetective;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.notifier.components.constants.Operations;
import com.opcon.notifier.utils.NotifierUtils;

import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 *
 * Created by Mahmut Taşkiran on 05/11/2016.
 */

public class OperationProcessManager {

    private volatile static OperationProcessManager singleton;


    private OperationProcessManager() {
        // ignore
    }

    public static OperationProcessManager getInstance() {
        if (singleton == null) {
            synchronized (OperationProcessManager.class) {
                if (singleton == null) {
                    singleton = new OperationProcessManager();
                }
            }
        }
        return singleton;
    }

    // for async process.
    public void processAsync(final Context c, final Notifier r, final OperationProcessListener listener) {


        NotifierProvider.Utils.updateLastApplyTime(c, r.getId(), System.currentTimeMillis());
        NotifierProvider.Utils.upgradeApplyLength(c, r.getId());

        if (NotifierUtils.isSpecialPacketExists(r)) {
            final int specialPacketType = NotifierUtils.getSpecialPacketType(r);
            boolean checkPermissionForSpecialPacket = checkSpecialPacketPermission(c, specialPacketType);
            if (checkPermissionForSpecialPacket) {

                SpecialPacket sp = SpecialPacketBuilderFactory
                            .instance()
                            .getPacket(c.getApplicationContext(), specialPacketType);


                AsyncOperator.operateAsync(c.getApplicationContext(), r, sp, listener);
            } else {
                AsyncOperator.operateAsync(c.getApplicationContext(), r, null, listener);
            }
        } else {
            AsyncOperator.operateAsync(c.getApplicationContext(), r, null, listener);
        }
    }

    private boolean checkSpecialPacketPermission(Context context, int type) {
        boolean ret = true;
        String[] permissions = NotifierPermissionDetective.SpecialPacketPermissionDetective.detect(type);
        if (permissions == null || permissions.length == 0) {
            return true;
        }
        for (String permission: permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                ret = false;
            }
        }
        return ret;
    }

    public interface OperationProcessListener {
        void onSuccessfulOperated(Notifier r);
        void onFatalOperation(Notifier r);
    }

    private static class AsyncOperator extends Thread implements Runnable {

        Context context;
        Notifier notifier;
        OperationProcessListener operationProcessListener;
        SpecialPacket specialPacket;

        private AsyncOperator(Context context,
                              SpecialPacket specialPacket,
                              Notifier r,
                              OperationProcessListener listener) {

            this.context = context.getApplicationContext();
            this.notifier = r;
            this.operationProcessListener = listener;
            this.specialPacket = specialPacket;
        }

        @Override
        public void run() {

            /*

            if operation type is Operations.__PLAY_SOUND or Operations._PLAY_SOUND
            then process it one time in 1 minute. (same newNotifier, same id)

            if operation has special packets process it exactly one.

             */

            boolean process = true;
            if (specialPacket != null && !specialPacket.isEmpty()) {
                String key = "packet_" + notifier.getId() + "_" + specialPacket.toString();
                process = !KeyBackoff.getInstance(context).isKeyProcessedInDuration(key, TimeUnit.HOURS.toMillis(4));
            }
            if (process) {
                notifier.getOperation().getProcessor().process(context, notifier, specialPacket, operationProcessListener);
            }

        }

        private void operate() {
            new Thread(this).start();
        }

        public static void operateAsync(Context context,
                                        Notifier r,
                                        SpecialPacket specialPacket,
                                        OperationProcessListener listener) {
            AsyncOperator asyncOperator = new AsyncOperator(context, specialPacket, r, listener);
            asyncOperator.operate();
        }

    }

}
