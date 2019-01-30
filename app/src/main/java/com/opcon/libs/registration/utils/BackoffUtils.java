package com.opcon.libs.registration.utils;

import android.content.Context;
import android.text.TextUtils;

import com.opcon.libs.utils.JSONObjectUtils;
import com.opcon.utils.PreferenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * Created by Mahmut Taşkiran on 1/24/17.
 *
 */

public class BackoffUtils {

    public static class BO {
        private BO(long backoff, long timestamp) {
            this.backoff = backoff;
            this.timestamp = timestamp;
        }
        public long backoff, timestamp;
    }
    public static final String SMS_BACKOFF = "sms";
    public static final String CALL_BACKOFF = "call";
    public static final String VERIFY_BACKOFF = "verification";
    public static final String IP_BACKOFF = "request";

    public static void save(Context context, JSONObject resultBody) {
        if (JSONObjectUtils.isEmpty(resultBody)) {
            return;
        }

        String pref_name = null;

        if (resultBody.has(VERIFY_BACKOFF)) {
            pref_name = VERIFY_BACKOFF;
        } else if (resultBody.has(IP_BACKOFF)) {
            pref_name = IP_BACKOFF;
        } else if (resultBody.has("method")) {
            try {
                if (resultBody.getString("method").equals(SMS_BACKOFF)) {
                    pref_name = SMS_BACKOFF;
                } else {
                    pref_name = CALL_BACKOFF;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (pref_name == null) {
            return;
        }

        try {
            resultBody.put("__timestamp", System.currentTimeMillis());
            PreferenceUtils.putString(context, pref_name, resultBody.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void setBackoff(Context c, String component, long backoff) {
        PreferenceUtils.putLong(c, component, backoff);
    }

    public static BO getBackoffTime(Context context, String method) {

        String backoff = PreferenceUtils.getString(context, method, null);

        if (TextUtils.isEmpty(backoff)) return null;


        JSONObject joBackoff = JSONObjectUtils.toJson(backoff);
        if (JSONObjectUtils.isEmpty(joBackoff)) return null;

        try {
            long timestamp = joBackoff.getLong("__timestamp");
            if (joBackoff.has("verification")) {
                long remain = joBackoff.getLong("verification");
                return new BO(remain, timestamp);
            } else if (joBackoff.has("method")) {
                long remain = joBackoff.getLong("backoff");
                return new BO(remain, timestamp);
            } else if (joBackoff.has("request")) {
                long remain = joBackoff.getLong("request");
                return new BO(remain, timestamp);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void resetBackoff(Context c, String n) {
        PreferenceUtils.putString(c, n, null);
    }

    public static long getRemainBackoffTime(Context context, String method) {
        BO backoff = getBackoffTime(context, method);
        if (backoff == null) return 0;
        long now = System.currentTimeMillis();
        if (backoff.backoff < 1 || backoff.timestamp < 1) return 0;
        if (now - backoff.timestamp > backoff.backoff) return 0;
        else return backoff.backoff - (now - backoff.timestamp);
    }


}
