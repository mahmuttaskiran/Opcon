package com.opcon.libs.permission;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;

import java.util.ArrayList;

/**
 *
 * Created by Mahmut Taşkiran on 31/12/2016.
 */

public class PermissionUtils {

    public static boolean check(Context c, String... vars) {
        if (vars == null || vars.length < 1) {
            return true;
        }
        return check(c, new PermissionRequest(vars));
    }

    public static boolean check(Context context, @NonNull PermissionRequest permissionRequest) {
        boolean ret = true;
        for (String permission: permissionRequest.permissions) {
            int state = ActivityCompat.checkSelfPermission(
                    context,
                    permission
            );
            if (state != PackageManager.PERMISSION_GRANTED) {
                ret = false;
            }
        }
        return ret;
    }

    public static boolean checkReadWriteExternalStorage(Context context){
        PermissionRequest permissionRequest;
        if (Build.VERSION.SDK_INT >= 16) {
            permissionRequest = new PermissionRequest(Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        } else {
            permissionRequest = new PermissionRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        return check(context, permissionRequest);
    }

    public static String[] checkAndGetDeniedPermissions(Context contexts, @NonNull PermissionRequest permissionRequest) {
        ArrayList<String> deniedPermission = new ArrayList<>();
        for (String permission: permissionRequest.permissions) {
            int state = ActivityCompat.checkSelfPermission(
                    contexts,
                    permission
            );
            if (state != PackageManager.PERMISSION_GRANTED) {
                deniedPermission.add(permission);
            }
        }
        if (deniedPermission.isEmpty())
            return null;
        return deniedPermission.toArray(new String[0]);
    }

    public static boolean check(Context context, String permission) {
        int state = ActivityCompat.checkSelfPermission(
                context,
                permission
        );
        return state == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isAnyPermissionPersistentlyDenied(Context context, @NonNull PermissionRequest request) {
        SharedPreferences mPreferences = context
                .getSharedPreferences(PermissionManagement.PERMISSION_PREF,
                        Context.MODE_PRIVATE);
        if (request.permissions == null || request.permissions.length == 0) {
            return false;
        }
        for (String permission: request.permissions) {
            if (mPreferences.getInt(permission, PermissionManagement.DENIED)
                    == PermissionManagement.PERSISTENTLY_DENIED) {
                return true;
            }
        }
        return false;
    }

    public static boolean isReadCalLogPermissionGranted(Context context) {
        String permission = Build.VERSION.SDK_INT <= 15 ? Manifest.permission.READ_CONTACTS: Manifest.permission.READ_CALL_LOG;
        return (ActivityCompat.checkSelfPermission(context, permission)) == PackageManager.PERMISSION_GRANTED;
    }


    public static boolean isLocationalPermissionsGranted(Context c) {
        return check(c, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION);
    }

}
