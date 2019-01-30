package com.opcon.libs.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 23/12/2016.
 */

public class JSONObjectUtils {

    public static boolean isEmpty(JSONObject jsonObject) {
        return  (jsonObject == null || !jsonObject.keys().hasNext());
    }

    public static boolean isEmpty(JSONArray jsonArray) {
        return jsonArray == null || jsonArray.length() == 0;
    }

    public static JSONObject toJson(String willJson) {
        try {
            return new JSONObject(willJson);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static boolean has(JSONObject jo, int param) {
        return jo != null && jo.has(String.valueOf(param));
    }

    public static boolean has(JSONObject jo, int ...p) {
        boolean has = true;
        for (int i : p) {
            has = (jo != null && jo.has(String.valueOf(i)));
            if (!has)
                break;
        }
        return has;
    }

    public static Object get(JSONObject jo,  int param) {
        try {
            return jo.get(String.valueOf(param));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

}
