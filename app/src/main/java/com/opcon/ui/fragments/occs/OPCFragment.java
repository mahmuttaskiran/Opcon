package com.opcon.ui.fragments.occs;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

import com.opcon.components.Component;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.constants.Operations;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by Mahmut Taşkiran on 15/10/2016.
 */

public abstract class OPCFragment extends Fragment {

    public static final int PACKET = 0;
    public boolean forTarget; // in target show.
    private int mResumeRegister;
    public JSONObject mParams;
    private InitType mInitType;
    private String alert;

    private List<Integer> mPacketFilter;

    public OPCFragment() {
        setInitType(InitType.BUILD);
    }

    public void setParams(JSONObject params) {
        setInitType(InitType.INIT);
        this.mParams = params;
    }

    public JSONObject getParams() {
        return mParams;
    }

    public void putParam(String name, Object value) {
        if (this.mParams == null) {
            mParams = new JSONObject();
        }
        try {
            mParams.put(name, value);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public void putParam(int param, Object value) {
        putParam(String.valueOf(param), value);
    }

    public boolean hasParam(int param) {
        return mParams != null && mParams.has(String.valueOf(param));
    }

    public void forTarget() {
        forTarget = true;
    }

    public Object getParam(String name) {
        if (mParams != null && mParams.has(name)) {
            try {
                return mParams.get(name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public int getType() {
        if (mParams != null) {
            if (mParams.has(String.valueOf(Component.LID))) {
                try {
                    return mParams.getInt(String.valueOf(Component.LID));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        return -1;
    }

    public void removeParam(String paramname) {
        if (this.mParams != null) {
            if (mParams.has(paramname))
                mParams.remove(paramname);
        }
    }

    public void setType(int uid) {
        putParam(String.valueOf(Component.LID), uid);
    }

    public abstract void review();

    public abstract boolean checkForms();

    public String getAlert() {
        return this.alert;
    }


    public void setAlert(String alert) {
        this.alert = alert;
    }

    public InitType getInitType() {
        return mInitType;
    }

    public void setInitType(InitType initType) {
        this.mInitType = initType;
    }
    public enum InitType {
        INIT,
        BUILD
    }

    public void setAlert(@StringRes int res) {
        setAlert(getString(res));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mResumeRegister == 0 && getInitType() == InitType.INIT) {
            review();
        }
        mResumeRegister++;
    }

    public static OPCFragment getConditionFragment(int uid, JSONObject withParams) {

        OPCFragment cf = null;

        switch (uid) {
            case Conditions._BATTERY:
            case Conditions.__BATTERY:
                cf = new ConditionChargeLow();
                break;

            case Conditions._IN_CALL:
            case Conditions.__IN_CALL:
            case Conditions._OUT_CALL:
            case Conditions.__OUT_CALL:
            case Conditions._IN_MSG:
            case Conditions.__IN_MSG:
            case Conditions._OUT_MSG:
            case Conditions.__OUT_MSG:
                cf = new ConditionInOut();
                break;
            case Conditions._TIMELY:
            case Conditions.__TIMELY:
                cf = new ConditionTime();
                break;
            case Conditions._LOCATION:
            case Conditions.__LOCATION:
                cf = new ConditionLocation();
                break;
            case Conditions._NEW_PICTURE:
            case Conditions.__NEW_PICTURE:
                cf = new ConditionNewPicture();
                break;
        }


        if (withParams != null && cf != null) {
            cf.setParams(withParams);
            cf.setInitType(InitType.INIT);
        } else {
            if (cf != null) {
                cf.setInitType(InitType.BUILD);
            }
        }

        if (cf != null)
            cf.setType(uid);

        return cf;
    }

    public static OPCFragment getOperationFragment(int uid, JSONObject withParams) {
        OPCFragment of = null;
        switch (uid) {
            case Operations._SENT_MSG:
                OperationInOutMessage offf = new OperationInOutMessage();
                offf.setForOwner(false);
                of = offf;
                break;
            case Operations.__SENT_MSG:
                OperationInOutMessage off = new OperationInOutMessage();
                off.setForOwner(true);
                of = off;
                break;
            case Operations.__PLAY_SOUND:
            case Operations._PLAY_SOUND:
                of = new OperationNotification();
                break;
            case Operations.__POST:
                of = new OperationPost();
                break;
        }

        if (withParams != null && of != null) {
            of.setParams(withParams);
            of.setInitType(InitType.INIT);
        } else {
            if (of != null)
                of.setInitType(InitType.BUILD);
        }

        if (of!=null)
            of.setType(uid);
        return of;
    }

    public Object getParam(int p) {
        if (mParams == null) {
            return null;
        }
        try {
            return mParams.get(String.valueOf(p));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void setPacketFilter(@Nullable List<Integer> packets) {
        mPacketFilter = packets;
    }

    public List<Integer> getPacketFilter() {
        return mPacketFilter;
    }

}
