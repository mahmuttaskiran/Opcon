package com.opcon.libs.registration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.TextUtils;

import com.opcon.R;
import com.opcon.libs.registration.activities.RequestTokenActivity;
import com.opcon.libs.registration.activities.VerifyTokenActivity;
import com.opcon.libs.registration.activities.WelcomeActivity;
import com.opcon.utils.PreferenceUtils;

import java.util.Locale;

/**
 *
 * Created by Mahmut Taşkiran on 1/26/17.
 */

public class RegistrationManagement {

    private static final String EMAIL = "email";
    private static final String PASSWORD = "password";
    private static final String NAME = "name";
    private static final String LOCALE = "locale";

    public volatile static RegistrationManagement singleton;

    private Class<?> afterRegistration;

    public static RegistrationManagement getInstance() {
        if (singleton == null)
        {
            synchronized (RegistrationManagement.class)
            {
                if (singleton == null)
                {
                    singleton = new RegistrationManagement();
                }
            }
        }
        return singleton;
    }

    private void startRegistration(Activity activityInstance) {
        String savedPhone = PreferenceUtils.getString(activityInstance.getBaseContext(),
            RequestTokenActivity.SAVED_PHONE, null);
        String savedLocale = PreferenceUtils.getString(activityInstance.getBaseContext(),
            RequestTokenActivity.SAVED_PHONE, null);
        String savedDialCode = PreferenceUtils.getString(activityInstance.getBaseContext(),
            RequestTokenActivity.SAVED_PHONE, null);
        if (TextUtils.isEmpty(savedPhone) || TextUtils.isEmpty(savedLocale) || TextUtils.isEmpty(savedDialCode)) {
            activityInstance.startActivity(new Intent(activityInstance, WelcomeActivity.class));
        } else {
            activityInstance.startActivity(new Intent(activityInstance, VerifyTokenActivity.class));
        }
    }

    public String getName(Context context) {
        return PreferenceUtils.getString(context, NAME, context.getString(R.string.no_name));
    }

    public String getName(Context context, @StringRes int defaultValue) {
        return PreferenceUtils.getString(context, NAME, context.getString(defaultValue));
    }

    public String getName(Context context, @Nullable String defaultValue) {
        return PreferenceUtils.getString(context, NAME, defaultValue);
    }

    public void setName(Context context, String name) {
        PreferenceUtils.putString(context, NAME, name);
    }

    public void setEmail(Context context, String token) {
        PreferenceUtils.putSecureString(context, EMAIL, token);
    }

    public String getEmail(Context context) {
        return PreferenceUtils.getSecureString(context, EMAIL, null);
    }

    public void setPassword(Context context, String password) {
        PreferenceUtils.putSecureString(context, PASSWORD, password);
    }

    public String getPassword(Context context) {
        return PreferenceUtils.getSecureString(context, PASSWORD, null);
    }

    public boolean isRegistrationRequires(Context context) {
        return TextUtils.isEmpty(getEmail(context)) ||
                TextUtils.isEmpty(getPassword(context));
    }

    public void startRegistration(Activity context, @Nullable Class<?> activityClass) {
        startRegistration(context);
        this.afterRegistration = activityClass;
    }

    public Class<?> getAfterRegistrationActivity() {
        return this.afterRegistration;
    }

    public String getEmailRef(Context c) {
        return getEmail(c).replaceAll("\\.", "_");
    }

    public String getLocale(Context context) {
        return PreferenceUtils.getString(context, LOCALE, Locale.getDefault().getCountry().toUpperCase());
    }

  public void setLocale(Context context, String locale) {
      PreferenceUtils.putString(context, LOCALE, locale);
  }

    public void setVolatilePassword(Context context, String newPassword) {
        PreferenceUtils.putSecureString(context, "volatile_p", newPassword);
    }

    public String getVolatilePassword(Context context) {
        return PreferenceUtils.getSecureString(context, "volatile_p", null);
    }
}
