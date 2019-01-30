package com.opcon.libs.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 *
 * Created by Mahmut Taşkiran on 23/12/2016.
 */

public class AndroidEnvironmentsUtils {

    public static boolean hasActiveInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
        return activeNetworkInfo != null &&
                activeNetworkInfo.isAvailable() &&
                activeNetworkInfo.isConnected();
    }

}
