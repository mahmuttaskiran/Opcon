package com.opcon.libs.permission;

import android.content.pm.PackageManager;

import java.util.ArrayList;

/**
 * Created by Mahmut Taşkiran on 31/12/2016.
 */

public class PermissionRequest {
    public String[] permissions;
    public boolean[] afterRationale;
    protected boolean granted;
    public int[] grantResults;

    public PermissionRequest(String... permissions) {
        this.permissions = permissions;
    }

    public String[] getPersistentlyDeniedPermissions() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < afterRationale.length; i++) {
            if (!afterRationale[i] && grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                list.add(permissions[i]);
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.toArray(new String[0]);
    }

    public String[] getDeniedPermissions() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                list.add(permissions[i]);
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.toArray(new String[0]);
    }

    public String[] getGrantedPermissions() {
        ArrayList<String> list = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                list.add(permissions[i]);
            }
        }
        if (list.isEmpty()) {
            return null;
        }
        return list.toArray(new String[0]);
    }
}
