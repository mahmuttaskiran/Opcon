package com.opcon.utils;

import android.support.annotation.Nullable;

import com.google.i18n.phonenumbers.PhoneNumberUtil;

import org.json.JSONException;
import org.junit.Test;

import java.util.Locale;

/**
 * Created by Mahmut Taşkiran on 22/12/2016.
 */
public class PhoneMatcherTest {


    @Test
    public void test1() throws JSONException {


        System.out.println(Locale.getDefault().getCountry().toUpperCase());

        is("905462272550", "TR");
        isRegion("5462272550", "US");


    }

    boolean equal(String pn1, String pn2, PhoneNumberUtil.MatchType matchType) {
        PhoneNumberUtil instance = PhoneNumberUtil.getInstance();
        return instance.isNumberMatch(pn1,pn2) == matchType;
    }

    void isRegion(String phone, String region) {
        System.out.println(MobileNumberUtils.checkIsValidAndGetValidLocale(phone, region));
    }

    void is(String phone, @Nullable String region) {
        System.out.println(MobileNumberUtils.checkIsValidAndGetValidLocale(phone, region));
    }


}