package com.opcon.ui.utils;

import android.support.annotation.Nullable;

import com.opcon.ui.views.DateRestrictView;
import com.opcon.ui.views.TimeRangeRestrictView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Mahmut Taşkiran on 17/10/2016.
 */

public class Restrict {

    public static final int RESTRICT = 9;

    public static final int TIME_RESTRICT = 0;
    public static final int FROM_HOURS = 1;
    public static final int TO_HOURS = 2;
    public static final int FROM_MINUTES = 3;
    public static final int TO_MINUTES = 4;

    public static final int DATE_RESTRICT = 5;
    public static final int YEAR = 6;
    public static final int MONTH = 7;
    public static final int DAY = 8;


    TimeRangeRestrictView mTr;
    DateRestrictView mDr;

    public Restrict(@Nullable TimeRangeRestrictView tr, @Nullable DateRestrictView dr) {
        this.mTr = tr;
        this.mDr = dr;
    }

    public String getParams() {

        JSONObject restrict = new JSONObject();

        if (mTr != null) {
            if (mTr.getFrom() != null && mTr.getTo() != null) {
                JSONObject joTr = new JSONObject();
                try {
                    joTr.put(String.valueOf(FROM_HOURS), mTr.getFrom().getHours());
                    joTr.put(String.valueOf(FROM_MINUTES), mTr.getFrom().getMinutes());
                    joTr.put(String.valueOf(TO_HOURS), mTr.getTo().getHours());
                    joTr.put(String.valueOf(TO_MINUTES), mTr.getTo().getMinutes());
                    restrict.put(String.valueOf(TIME_RESTRICT), joTr);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        if (mDr != null) {
            if (mDr.getYear() != -1 && mDr.getMonth() != -1 && mDr.getDay() != -1) {
                JSONObject joDr = new JSONObject();
                try {
                    joDr.put(String.valueOf(YEAR), mDr.getYear());
                    joDr.put(String.valueOf(MONTH), mDr.getMonth());
                    joDr.put(String.valueOf(DAY), mDr.getDay());
                    restrict.put(String.valueOf(DATE_RESTRICT), joDr);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return restrict.toString();
    }

    public void init(String params) {
        if (params == null) return;
        try {

            JSONObject jsonObject = new JSONObject(params);
            if (jsonObject.has(String.valueOf(TIME_RESTRICT)) && mTr != null) {
                JSONObject time_restrict = jsonObject.getJSONObject(String.valueOf(TIME_RESTRICT));
                Date from, to;

                int from_hour = time_restrict.getInt(String.valueOf(FROM_HOURS));
                int to_hour = time_restrict.getInt(String.valueOf(TO_HOURS));
                int from_minutes = time_restrict.getInt(String.valueOf(FROM_MINUTES));
                int to_minutes = time_restrict.getInt(String.valueOf(TO_MINUTES));

                from = new Date();
                from.setHours(from_hour);
                from.setMinutes(from_minutes);

                to = new Date();
                to.setHours(to_hour);
                to.setMinutes(to_minutes);

                mTr.setTime(from, to);

            } else {

                if (mTr != null)
                    mTr.setTime(null, null);

            }

            if (jsonObject.has(String.valueOf(DATE_RESTRICT)) && mDr != null)  {
                JSONObject date_restrict = jsonObject.getJSONObject(String.valueOf(DATE_RESTRICT));
                int year, month, day;
                year = date_restrict.getInt(String.valueOf(YEAR));
                month = date_restrict.getInt(String.valueOf(MONTH));
                day = date_restrict.getInt(String.valueOf(DAY));
                mDr.setDate(year, month, day);
            } else {
                if (mDr != null)
                    mDr.setDate(-1, -1, -1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void putTimeRestrictToConditionParams(JSONObject conditionParams, String restrictParams) {
        if (restrictParams == null || restrictParams.equals("") || restrictParams.equals("{}")) {
            if (conditionParams.has(String.valueOf(RESTRICT))) conditionParams.remove(String.valueOf(RESTRICT));
        } else {
            try {
                conditionParams.put(String.valueOf(RESTRICT), new JSONObject(restrictParams));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static String getTimeRestrictParamsFromConditionParams(JSONObject conditionParams) {
        String ret = null;
        if (conditionParams.has(String.valueOf(RESTRICT)))
            try {
                JSONObject restrict = conditionParams.getJSONObject(String.valueOf(RESTRICT));
                ret = restrict.toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        return ret;
    }

}
