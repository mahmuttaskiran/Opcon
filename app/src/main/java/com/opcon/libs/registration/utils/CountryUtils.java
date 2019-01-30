package com.opcon.libs.registration.utils;

import android.content.Context;
import android.util.Base64;

import com.opcon.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mahmut Taşkiran on 19/01/2017.
 *
 */

public class CountryUtils {

    public static class Country {
        String name;
        String dialCode;

        private Country(String name, String dialCode) {
            this.name = name;
            this.dialCode = dialCode;
        }

        public String getName() {
            return name;
        }

        public String getDialCode() {
            return dialCode;
        }
    }

    private static String readEncodedJsonString(Context context) {
        byte[] data = Base64.decode(context.getString(R.string.countries), Base64.DEFAULT);
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }


    public static List<Country> getCountries(Context context) {
        List<Country> countries = new ArrayList<>();
        try {
            JSONArray mArr = new JSONArray(readEncodedJsonString(context));
            for (int i = 0; i < mArr.length(); i++){
                JSONObject jsonObj = mArr.getJSONObject(i);
                if (jsonObj!= null) {
                    countries.add(
                            new Country(jsonObj.getString("name"),
                                    (jsonObj.getString("dial_code").replaceAll("[^0-9]", "")))
                    );
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return countries;
    }

}
