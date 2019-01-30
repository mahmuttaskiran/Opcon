package com.opcon.ui.utils;

import android.content.Context;

import com.opcon.R;
import com.opcon.database.ContactBase;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.ui.fragments.occs.ConditionChargeLow;
import com.opcon.ui.fragments.occs.ConditionInOut;
import com.opcon.ui.fragments.occs.ConditionLocation;
import com.opcon.ui.fragments.occs.ConditionTime;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 19/10/2016.
 *
 */

public class ConditionCommentInterpreter {

    public static String interpret(Context context, int conditionType, JSONObject params, boolean forSender, boolean amIRelation)
    {
        return interprett(context, conditionType, (params), forSender, amIRelation);
    }

    private static String interprett(Context context, int conditionType, JSONObject params, boolean forSender, boolean amIRelation) {
        String result = null;
        switch (conditionType) {
            case Conditions._BATTERY:
                result = _battery(context, params, forSender, amIRelation);
                break;
            case Conditions.__BATTERY:
                result = __battery(context, params, forSender, amIRelation);
                break;
            case Conditions._IN_CALL:
                result = _inCall(context, params, forSender, amIRelation);
                break;
            case Conditions.__IN_CALL:
                result = __inCall(context, params, forSender, amIRelation);
                break;
            case Conditions._IN_MSG:
                result = _inMsg(context, params, forSender, amIRelation);
                break;
            case Conditions.__IN_MSG:
                result = __inMsg(context, params, forSender, amIRelation);
                break;
            case Conditions._OUT_MSG:
                result = _outMsg(context, params, forSender, amIRelation);
                break;
            case Conditions.__OUT_MSG:
                result = __outMsg(context, params, forSender, amIRelation);
                break;
            case Conditions._OUT_CALL:
                result = _outCall(context, params, forSender, amIRelation);
                break;
            case Conditions.__OUT_CALL:
                result = __outCall(context, params, forSender, amIRelation);
                break;
            case Conditions._LOCATION:
                result = _location(context, params, forSender, amIRelation);
                break;
            case Conditions.__LOCATION:
                result = __location(context, params, forSender, amIRelation);
                break;
            case Conditions._TIMELY:
            case Conditions.__TIMELY:
                result = ___time(context, params);
                break;
            case Conditions._NEW_PICTURE:
                result = _newPicture(context, forSender, amIRelation);
                break;
            case Conditions.__NEW_PICTURE:
                result = __newPicture(context, forSender, amIRelation);
                break;

        }
        return result;
    }

    private static String comment(String string, Object... args) {
        return String.format(string, args);
    }

    private static String gs(Context c, int str) {
        return c.getString(str);
    }

    private static  boolean has(JSONObject j, int p) {
        return j.has(String.valueOf(p));
    }

    private static String gp(JSONObject js, int p) {
        try {
            return js.getString(String.valueOf(p));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String _newPicture(Context context, boolean forSender, boolean r) {
        if (forSender || !r) {
            return context.getString(R.string.interpreter_sender_new_picture);
        } else {
            return context.getString(R.string.interpreter_receiver_new_picture);
        }
    }

    private static String __newPicture(Context context, boolean forSender, boolean r) {
        return _newPicture(context, !forSender, r);
    }

    private static String ___time(Context context, JSONObject params) {
        String FORMAT = context.getString(R.string.interpreter_sender_time);
        int hours = 0, minutes = 0;
        try {
            hours = params.getInt(String.valueOf(ConditionTime.HOUR));
            minutes = params.getInt(String.valueOf(ConditionTime.MINUTES));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return String.format(FORMAT, hours, minutes);
    }

    private static String _location(Context c, JSONObject p, boolean forSender, boolean r) {
        if (!r || forSender) {
            return comment(gs(c, R.string.interpreter_sender_location), gp(p, ConditionLocation.PARAM_STRING_ADDRESS),
                gp(p, ConditionLocation.NEAR));
        } else {
            return comment(gs(c, R.string.interpreter_receiver_location), gp(p, ConditionLocation.PARAM_STRING_ADDRESS),
                gp(p, ConditionLocation.NEAR));
        }
    }

    private static String __location(Context c, JSONObject p, boolean sender, boolean r){
        return _location(c, p, !sender, r);
    }

    private static String _inMsg(Context c, JSONObject p, boolean forSender, boolean r) {
        String str;
        if (has(p, ConditionInOut.PHONE)) {
            str =  !r || forSender ? gs(c, R.string.interpreter_sender_incoming_message):
                gs(c, R.string.interpreter_receiver_incoming_message);
            return comment(str, r ? gn(c, gp(p, ConditionInOut.PHONE)): gp(p, ConditionInOut.NAME));
        } else {
            str = !r || forSender ? gs(c, R.string.interpreter_sender_incoming_message_all):
                gs(c, R.string.interpreter_receiver_incoming_message_all);
            return comment(str);
        }
    }

    private static String __inMsg(Context c, JSONObject p, boolean forSender, boolean r) {
        return _inMsg(c, p, !forSender, r);
    }

    private static String _outCall(Context c, JSONObject p, boolean forSender, boolean r) {
        String str;
        if (has(p, ConditionInOut.PHONE)) {
            str = !r || forSender ? gs(c, R.string.interpreter_sender_outgoing_call):
                gs(c, R.string.interpreter_receiver_outgoing_call);
            return comment(str, r ? gn(c, gp(p, ConditionInOut.PHONE)): gp(p, ConditionInOut.NAME));
        } else {
            str = !r || forSender ? gs(c, R.string.interpreter_sender_outgoing_call_all):
                gs(c, R.string.interpreter_receiver_outgoing_call_all);
            return comment(str);
        }
    }

    private static String __outCall(Context c, JSONObject p, boolean forSender, boolean r) {
        return _outCall(c,p, !forSender, r);
    }

    private static String _outMsg(Context c, JSONObject p, boolean forSender, boolean r) {
        String str;
        if (has(p, ConditionInOut.PHONE)) {
            str =  !r || forSender ? gs(c, R.string.interpreter_sender_outgoing_message):
                gs(c, R.string.interpreter_receiver_outgoing_message);
            return comment(str, r ? gn(c, gp(p, ConditionInOut.PHONE)): gp(p, ConditionInOut.NAME));
        } else {
            str = !r || forSender ? gs(c, R.string.interpreter_sender_outgoing_message_all):
                gs(c, R.string.interpreter_receiver_outgoing_message_all);
            return comment(str);
        }
    }

    private static String __outMsg(Context c, JSONObject p, boolean forSender, boolean r) {
        return _outMsg(c,p, !forSender, r);
    }

    private static String gn(Context context, String phone) {
        return ContactBase.Utils.getName(context, phone);
    }

    private static String _inCall(Context c, JSONObject p, boolean forSender, boolean r) {
        String str;
        if (has(p, ConditionInOut.PHONE)) {
            str = !r || forSender ? gs(c, R.string.interpreter_sender_incoming_call):
                gs(c, R.string.interpreter_receiver_incoming_call);
            return comment(str, r ? gn(c, gp(p, ConditionInOut.PHONE)): gp(p, ConditionInOut.NAME));
        } else {
            str = !r || forSender ? gs(c, R.string.interpreter_sender_incoming_call_all):
                gs(c, R.string.interpreter_receiver_incoming_call_all);
            return comment(str);
        }
    }

    private static String __inCall(Context c, JSONObject p, boolean forSender, boolean r) {
        return _inCall(c,p,!forSender,r);
    }


    private static String _battery(Context context, JSONObject params, boolean forSender, boolean relation) {
        int percent = 0;
        try {
            percent = params.getInt(String.valueOf(ConditionChargeLow.PARAM_INT_PERCENT));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String FORMAT = !relation ? context.getString(R.string.interpreter_sender_charge) :
            forSender ? context.getString(R.string.interpreter_sender_charge):
                context.getString(R.string.interpreter_receiver_charge);
        return String.format(FORMAT, percent);
    }

    private static String __battery(Context c, JSONObject p, boolean f, boolean relation) {
        return _battery(c, p, !f, relation);
    }

}
