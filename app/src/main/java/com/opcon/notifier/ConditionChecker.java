package com.opcon.notifier;

import android.content.Context;
import android.text.TextUtils;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.database.NotifierProvider;
import com.opcon.notifier.components.Condition;
import com.opcon.ui.fragments.occs.ConditionChargeLow;
import com.opcon.ui.fragments.occs.ConditionInOut;

import java.util.HashMap;
import java.util.Locale;

/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 */

public class ConditionChecker {

    // in millisecond
    private static final long DEFAULT_BACKOFF_TIME = 500;

    private Context mContext;
    private HashMap<String, Object> mParams;
    private Condition mCondition;
    private boolean mRequiresReverse;
    private int mNotifierId;
    private boolean mDefaultResult = true;
    private long mBackoffTime = DEFAULT_BACKOFF_TIME;

    public ConditionChecker(Context context) {
        mParams = new HashMap<>();
        mContext = context;
    }

    public ConditionChecker putParam(String key, Object value) {
        mParams.put(key, value);
        return this;
    }

    public ConditionChecker defaultResult(boolean result) {
        mDefaultResult = result;
        return this;
    }

    public ConditionChecker requiresReverse(boolean reverse) {
        mRequiresReverse = reverse;
        return this;
    }

    public ConditionChecker backoff(long backoff) {
        mBackoffTime = backoff;
        return this;
    }

    public boolean check(Notifier notifier) {
        mCondition = notifier.getCondition();
        mNotifierId = notifier.getId();
        // default false
        boolean check = false;
        // if condition is not progressable return false
        if (notifier.isConditionProgressable()) {
            // backoff?
            if (mBackoffTime != 0 && backoff()) return false;
            NotifierProvider.Utils.setLastCheckTime(mContext, mNotifierId, System.currentTimeMillis());
            if (!mCondition.isValidTime()) return false;
            check = __check();
            if (mRequiresReverse) {
                boolean isReversed = isReversed();
                if (!isReversed) {
                    if (!check) {
                        NotifierProvider.Utils.setReversed(mContext, mNotifierId, true);
                    }
                    return false;
                }
                NotifierProvider.Utils.setReversed(mContext, mNotifierId, false);
            }
        }
        return check;
    }

    private boolean backoff() {
        long now = System.currentTimeMillis();
        long lc = NotifierProvider.Utils.getLastCheckTime(mContext, mNotifierId);
        return (now - lc) < mBackoffTime;
    }

    private boolean isReversed() {
        return NotifierProvider.Utils.isReversed(mContext, mNotifierId);
    }

    private boolean __check() {
        if (Conditions.isInOut(mCondition.getId())) {
            return checkInOut();
        } else if (Conditions.isBattery(mCondition.getId())) {
            return checkCharge();
        }
        return mDefaultResult;
    }

    private boolean checkInOut() {
        // i don't want to support in-out call-msg for everyone
        // check it.
        String exceptedPhone = mCondition.getString(ConditionInOut.PHONE);
        if (TextUtils.isEmpty(exceptedPhone))
            return false;
        String triggerPhone = (String) mParams.get("triggerPhone");
        return isMatch(exceptedPhone, triggerPhone);
    }

    public static boolean isMatch(String pn1, String pn2) {
        PhoneNumberUtil.MatchType mt = PhoneNumberUtil.getInstance().isNumberMatch(pn1, pn2);
      boolean result = mt == PhoneNumberUtil.MatchType.EXACT_MATCH || mt == PhoneNumberUtil.MatchType.NSN_MATCH;

      if (!result) {

        // fix OP-00018

        if (pn1 != null && pn2 != null) {
          try {

            String country = Locale.getDefault().getCountry().toUpperCase();
            PhoneNumberUtil util = PhoneNumberUtil.getInstance();

            Phonenumber.PhoneNumber _pn1 = util.parse(pn1, country);
            Phonenumber.PhoneNumber _pn2 = util.parse(pn2, country);

            PhoneNumberUtil.MatchType numberMatch = util.isNumberMatch(util.format(_pn1, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL), util.format(_pn2, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL));

            result = numberMatch == PhoneNumberUtil.MatchType.EXACT_MATCH ||
                numberMatch == PhoneNumberUtil.MatchType.SHORT_NSN_MATCH;

          } catch (Exception e) {
            e.printStackTrace();
          }
        }


      }


      return result;
    }

    private boolean checkCharge() {
        int exceptedCharge = mCondition.getInt(ConditionChargeLow.PARAM_INT_PERCENT);
        int currentCharge = (Integer) mParams.get("currentCharge");
        return  (currentCharge < exceptedCharge);
    }

}
