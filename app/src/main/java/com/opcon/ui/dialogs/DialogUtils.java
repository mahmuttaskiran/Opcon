package com.opcon.ui.dialogs;

import android.content.Context;
import android.support.v7.app.AlertDialog;

/**
 * Created by Mahmut Taşkiran on 17/10/2016.
 */

public class DialogUtils {

    public static void alertOnlyOk(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", null);
        if (title != null) {
            builder.setTitle(title);
        }
        if (message != null) {
            builder.setMessage(message);
        }
        builder.show();
    }

}
