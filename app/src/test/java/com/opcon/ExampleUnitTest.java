package com.opcon;

import com.opcon.utils.MobileNumberUtils;

import org.junit.Test;

import timber.log.Timber;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    private static final String TAG = "ExampleUnitTest";
    @Test
    public void addition_isCorrect() throws Exception {
        assertEquals(4, 2 + 2);
        String s = MobileNumberUtils.toInternational("+++ 90 546 227 25 5 0", null, true);
        String ks = MobileNumberUtils.toInternational("+++ 90 546 227 25 5 1", null, true);
        System.out.println(s);
        Timber.d(s);

        System.out.println(ks);
        Timber.d(ks);
    }
}