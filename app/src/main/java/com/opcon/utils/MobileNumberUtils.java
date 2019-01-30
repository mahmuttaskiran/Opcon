package com.opcon.utils;


import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.util.Locale;

/**
 *
 * Created by Mahmut Taşkiran on 06/10/2016.
 */

public class MobileNumberUtils {

    public static String toInternational(String phone, @Nullable String defaultLocale, boolean formatted)
    {
        if (defaultLocale == null) defaultLocale = getDefaultLocale();
        PhoneNumberUtil pnUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber parsed = pnUtil.parse(phone, defaultLocale);
            if (pnUtil.getNumberType(parsed) == PhoneNumberUtil.PhoneNumberType.UNKNOWN) {
                return phone;
            } else {
                String formattedPn = pnUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
                if (formatted) {
                    return formattedPn;
                }
              String s = (formattedPn.replaceAll("[^0-9]", ""));
              return "+" + s;
            }
        } catch (NumberParseException ignore) {}
        return null;
    }

    private static boolean isValid(String phone, @Nullable String region) {
        if (region == null) region = getDefaultLocale();
        PhoneNumberUtil pnUtil = PhoneNumberUtil.getInstance();
        try {
            Phonenumber.PhoneNumber phoneNumber = pnUtil.parse(phone, region);
            if (phoneNumber == null)
                return false;

            if (pnUtil.isValidNumber(phoneNumber))
                return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static String getExampleNumberFor(String region) {
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        Phonenumber.PhoneNumber exampleNumber = instance.getExampleNumber(region);
        return instance.format(exampleNumber, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
    }

    private static String getDefaultLocale() {
        return Locale.getDefault().getCountry().toUpperCase();
    }

    public static String checkIsValidAndGetValidLocale(String phone, String userLocale) {
        String defaultLocale = getDefaultLocale();
        if (TextUtils.isEmpty(userLocale)) {
            userLocale = defaultLocale;
        }
        userLocale = userLocale.toUpperCase();
        if (defaultLocale.equals(userLocale)) {
            return isValid(phone, userLocale) ? userLocale: null;
        } else {
            return isValid(phone, userLocale) ? userLocale : isValid(phone, defaultLocale) ? defaultLocale: null;
        }
    }

    public static boolean checkValid(String phone, @Nullable String localae) {
        return checkIsValidAndGetValidLocale(phone, localae)!=null;
    }

}
