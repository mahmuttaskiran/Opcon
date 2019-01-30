package com.opcon.libs.permission;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.NotifierEventSentUtils;
import com.opcon.notifier.components.Notifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * Created by Mahmut Taşkiran on 24/10/2016.
 *
 */

public class PermissionManagement {

    protected static final String PERMISSION_PREF = "Permissions";

    public static final int GRANTED = 0;
    public static final int DENIED = 1;
    public static final int PERSISTENTLY_DENIED = 2;

    private Activity mActivity;
    private Fragment mFragment;
    private PermissionRequest mPermissionRequest;
    private PermissionEventListener mPermissionEventListener;
    private int mPermissionRequestCode;
    private boolean mSaveStates;

    private PermissionManagement() {
        // default value is true.
        saveState(true);
    }

    private PermissionManagement(Activity activity) {
        this();
        this.mActivity = activity;
    }

    private PermissionManagement(Fragment fragment) {
        this();
        this.mFragment = fragment;
    }

    public static PermissionManagement with(Activity activity) {
        return new PermissionManagement(activity);
    }

    public static PermissionManagement with(Fragment fragment) {
        return new PermissionManagement(fragment);
    }

    private PermissionManagement saveState(boolean saveState) {
        mSaveStates = saveState;
        return this;
    }

    public PermissionManagement observer(PermissionEventListener permissionEventListener) {
        this.mPermissionEventListener = permissionEventListener;
        return this;
    }

    public PermissionManagement builtRequest(Integer requestCode, String... permissions) {
        mPermissionRequest = new PermissionRequest(permissions);
        this.mPermissionRequestCode = requestCode;
        return this;
    }

    public PermissionManagement builtRequest(int rc, PermissionRequest pr) {
        mPermissionRequest = pr;
        mPermissionRequestCode = rc;
        return this;
    }


    public void request() {
        if (ifWithActivity()) {
            ActivityCompat.requestPermissions(mActivity, mPermissionRequest.permissions, mPermissionRequestCode);
        } else {
            mFragment.requestPermissions(mPermissionRequest.permissions, mPermissionRequestCode);
        }
    }

    public PermissionRequest dispatchEvent(int requestCode, int[] grantResults) {

        if (mPermissionRequest == null || mPermissionRequestCode != requestCode) {
            return null;
        }

        mPermissionRequest.grantResults = grantResults;
        setAfterRationale(mPermissionRequest);
        mPermissionRequest.granted = check(grantResults);
        if (mPermissionEventListener != null) {
            if (!mPermissionRequest.granted) {
                mPermissionEventListener.onAnyPermissionsDenied(requestCode, mPermissionRequest);
            } else {
                mPermissionEventListener.onAllPermissionsGranted(requestCode, mPermissionRequest);
            }
        }
        if (mSaveStates) {
            saveOnDisk(mPermissionRequest);
        }
        return mPermissionRequest;
    }

    private Context getContext() {
        if (ifWithActivity()) {
            return mActivity.getApplicationContext();
        } else {
            return mFragment.getContext().getApplicationContext();
        }
    }

    private void saveOnDisk(PermissionRequest permissionRequest) {

        SharedPreferences mSharedPreferences = getContext()
                .getSharedPreferences(PERMISSION_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEdit = mSharedPreferences.edit();

        String[] persistentlyDeniedPermissions = permissionRequest.getPersistentlyDeniedPermissions();
        String[] deniedPermissions = permissionRequest.getDeniedPermissions();
        String[] grantedPermissions = permissionRequest.getGrantedPermissions();

        if (deniedPermissions != null && persistentlyDeniedPermissions != null)
            deniedPermissions = extract(deniedPermissions, persistentlyDeniedPermissions);

        if (persistentlyDeniedPermissions != null) {
            for (String persistentlyDeniedPermission : persistentlyDeniedPermissions) {
                mEdit.putInt(persistentlyDeniedPermission, PERSISTENTLY_DENIED);
            }
        }

        if (deniedPermissions != null) {
            for (String persistentlyDeniedPermission : deniedPermissions) {
                mEdit.putInt(persistentlyDeniedPermission, DENIED);
            }
        }

        if (grantedPermissions != null) {
            for (String persistentlyDeniedPermission : grantedPermissions) {
                mEdit.putInt(persistentlyDeniedPermission, GRANTED);
            }
        }

        mEdit.apply();
    }

    private String[] extract(String[] deniedPermissions, String[] persistentlyDeniedPermissions) {
        ArrayList<String> aDenied = new ArrayList<>(Arrays.asList(deniedPermissions));
        ArrayList<String> aPersistentlyDenied = new ArrayList<>(Arrays.asList(persistentlyDeniedPermissions));
        aDenied.removeAll(aPersistentlyDenied);
        if (aDenied.isEmpty()) {
            return null;
        }
        return aDenied.toArray(new String[0]);
    }

    private boolean check(int[] grantResults) {
        boolean ret = true;
        for (int code: grantResults) {
            if (code != PackageManager.PERMISSION_GRANTED) {
                ret = false;
                break;
            }
        }
        return ret;
    }

    private void setAfterRationale(PermissionRequest permissionRequest) {
        permissionRequest.afterRationale = getPermissionsRationale(permissionRequest.permissions);
    }

    private boolean[] getPermissionsRationale(String[] permissions) {
        int __size = permissions.length;
        boolean[] rationale = new boolean[__size];
        int __i = 0;
        for (String permission: permissions) {
            if (ifWithActivity()) {
                rationale[__i] =
                        ActivityCompat.shouldShowRequestPermissionRationale(this.mActivity, permission);
            } else {
                rationale[__i] =
                        mFragment.shouldShowRequestPermissionRationale(permission);
            }
            __i++;
        }
        return rationale;
    }

    public static void showAppSettingsPageFor(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        intent.setData(uri);
        context.startActivity(intent);
    }

    private static void checkStates(Context context,
                                   @Nullable PermissionStateChangeListener listener)
    {

        SharedPreferences mPreferences = context.getSharedPreferences(PERMISSION_PREF, Context.MODE_PRIVATE);
        SharedPreferences.Editor mEdit = mPreferences.edit();

        Map<String, ?> all = mPreferences.getAll();
        Set<? extends Map.Entry<String, ?>> mSet = all.entrySet();
        for (Map.Entry<String, ?> mEntry: mSet) {
            String permission = mEntry.getKey();
            Integer bState = (Integer) mEntry.getValue();
            int nState;

            if (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) {
                nState = GRANTED;
            } else {
                nState = DENIED;
            }

            if (bState == PERSISTENTLY_DENIED && nState == DENIED)  {
                continue;
            }

            if (bState != nState) {
                mEdit.putInt(permission, nState);
                if (listener != null)
                    listener.onStateChange(permission, bState, nState);
            }
        }

        mEdit.apply();

    }

    private boolean ifWithActivity() {
        return mActivity != null;
    }

    public interface PermissionEventListener {
        void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest);
        void onAnyPermissionsDenied(int requestCode, PermissionRequest permissionRequest);
    }

    private interface PermissionStateChangeListener {
        void onStateChange(String permission, int previousState, int currentState);
    }

    // detect permission changes!
    public static void init(final Context context) {
        checkStates(context, new PermissionStateChangeListener() {
            @Override
            public void onStateChange(String permission, int previousState, int currentState) {
                if (currentState == PermissionManagement.PERSISTENTLY_DENIED ||
                    currentState == PermissionManagement.DENIED) {

                    List<Notifier> notifiers = NotifierProvider.Utils.getProgressableNotifiers(context);
                    for (Notifier notifier : notifiers) {
                        String[] detect = NotifierPermissionDetective.detect(notifier);
                        for (String s : detect) {
                            if (s.equals(permission)) {
                                NotifierEventSentUtils.sendStopped(notifier);
                                NotifierProvider.Utils.updateSingleInt(context,
                                    notifier.getId(), NotifierProvider.STATE, Notifier.STOPPED);
                            }
                        }
                    }
                }
            }
        });
    }
}
