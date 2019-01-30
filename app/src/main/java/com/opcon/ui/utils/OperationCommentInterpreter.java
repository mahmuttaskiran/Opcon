package com.opcon.ui.utils;

import android.content.Context;
import android.text.TextUtils;

import com.opcon.R;
import com.opcon.libs.utils.JSONObjectUtils;
import com.opcon.notifier.components.constants.Operations;
import com.opcon.ui.fragments.occs.OPCFragment;
import com.opcon.ui.fragments.occs.OperationInOutMessage;
import com.opcon.ui.fragments.occs.OperationNotification;
import com.opcon.ui.fragments.occs.OperationPost;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 19/10/2016.
 */

public class OperationCommentInterpreter {

    public static String interpret(Context context, int operationType, JSONObject params, boolean forSender, boolean mIARelation)
    {
        if (mIARelation) {
            return interprett(context, operationType, params, forSender);
        } else {
            return notRelation(context, operationType, params);
        }
    }

    private static String notRelation(Context context, int ot, JSONObject params) {
        String result = null;
        switch (ot) {
            case Operations.__SENT_MSG:
            case Operations._SENT_MSG:
                result = notRelation_sentMsg(context, params);
                break;
            case Operations.__PLAY_SOUND:
                result = getOperationSoundMe(context, params, false);
                break;
            case Operations._PLAY_SOUND:
                result = getOperationSoundMe(context, params, false);
                break;
            case Operations.__POST:
                result = notRelation_getOperationPost(context, params);
        }
        return result;
    }

    private static String notRelation_getOperationPost(Context context, JSONObject params) {
        return getOperationPost(context, params, false);
    }

    private static String notRelation_sentMsg(Context c, JSONObject p) {
        String str = gs(c, R.string.not_relation_sentMsg_interpret);

        String element;
        if (has(p, OperationInOutMessage.TEXT)) {
            element = gp(p, OperationInOutMessage.TEXT);
        } else {
            int pt = gi(p, OPCFragment.PACKET);
            element = SpecialPacketCommentInterpreter.interpret(c, pt, false);
        }
        return String.format(str, element);
    }

    private static String interprett(Context context, int operationType, JSONObject params, boolean forSender) {
        String result = null;
        switch (operationType) {
            case Operations._SENT_MSG:
                result = _sentMsg(context, params, forSender);
                break;
            case Operations.__SENT_MSG:
                result = __sendMsg(context, params, forSender);
                break;
            case Operations.__PLAY_SOUND:
                result = getOperationSoundMe(context, params, forSender);
                break;
            case Operations._PLAY_SOUND:
                result = getOperationSoundU(context, params, forSender);
                break;
            case Operations.__POST:
                result = getOperationPost(context, params, forSender);
        }
        return result;
    }

    private static String getOperationPost(Context context, JSONObject params, boolean forSender) {

        int packet = -1;
        String text = null;
        String ret;

        if (JSONObjectUtils.has(params, OperationPost.PACKET)) {
            packet = (int) JSONObjectUtils.get(params, OperationPost.PACKET);
        }

        if (JSONObjectUtils.has(params, OperationPost.TEXT)) {
            text = JSONObjectUtils.get(params, OperationPost.TEXT).toString();
        }

        if (packet != -1 && !TextUtils.isEmpty(text)) {
            String p = SpecialPacketCommentInterpreter.interpret(context, packet, forSender);
            ret = String.format(gs(context, forSender ? R.string.operation_post_two_factor:  R.string.operation_post_two_factor_for_target), p, text);
        } else if (packet != -1) {
            String p = SpecialPacketCommentInterpreter.interpret(context, packet, forSender);
            ret = String.format(gs(context,forSender ?  R.string.operation_post_only_special_packet: R.string.operation_post_only_special_packet_for_target), p);
        } else {
            ret = String.format(gs(context, forSender ? R.string.operation_post_only_special_packet: R.string.operation_post_only_special_packet_for_target), text);
        }

        return ret;
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

    private static int gi(JSONObject js, int p) {
        try {
            return js.getInt(String.valueOf(p));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static String __sendMsg(Context c, JSONObject p, boolean fs) {
        String str = !fs ? gs(c, R.string.interpreter_sender_message_to_me):
            gs(c, R.string.interpreter_receiver_message_to_me);
        String element;
        if (has(p, OperationInOutMessage.PACKET)) {
            int pt = gi(p, OPCFragment.PACKET);
            element = SpecialPacketCommentInterpreter.interpret(c, pt, fs);
        } else {
            element = gp(p, OperationInOutMessage.TEXT);
        }
        return String.format(str, element);
    }

    private static String _sentMsg(Context c, JSONObject p, boolean fs) {
        String str = fs ? gs(c, R.string.interpreter_sender_message_to_me):
            gs(c, R.string.interpreter_receiver_message_to_me);
        String element;
        if (has(p, OperationInOutMessage.PACKET)) {
            int pt = gi(p, OPCFragment.PACKET);
            element = SpecialPacketCommentInterpreter.interpret(c, pt, !fs);
        } else {
            element = gp(p, OperationInOutMessage.TEXT);
        }
        return String.format(str, element);
    }

    private static String getOperationSoundMe(Context context, JSONObject p, boolean forSender) {
        String soundname = "";
        try {
            if (p.getInt(String.valueOf(OperationNotification.SOUND_RAW_INDEX)) != -1) {
                soundname = p.getString(String.valueOf(OperationNotification.SOUND));
            }


            if (p.getString(String.valueOf(OperationNotification.TEXT)) != null) {
              String string = p.getString(String.valueOf(OperationNotification.TEXT));
              if (soundname.isEmpty()) {
                soundname += string;
              } else {
                soundname += ", " + string;
              }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String FORMAT = forSender ?
                        context.getString(R.string.interpreter_sender_sound_side_me)
                        :
                        context.getString(R.string.interpreter_receiver_sound_side_me);

        return String.format(FORMAT, soundname);
    }

    private static String getOperationSoundU(Context context, JSONObject p, boolean forSender) {
        String soundname = "";
        try {
            if (p.getInt(String.valueOf(OperationNotification.SOUND_RAW_INDEX)) != -1) {
                soundname = p.getString(String.valueOf(OperationNotification.SOUND));
            }


          if (p.getString(String.valueOf(OperationNotification.TEXT)) != null) {
            String string = p.getString(String.valueOf(OperationNotification.TEXT));
            if (soundname.isEmpty()) {
              soundname += string;
            } else {
              soundname += ", " + string;
            }

          }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String FORMAT =
                forSender ?
                        context.getString(R.string.interpreter_sender_sound_side_u)
                :
                context.getString(R.string.interpreter_receiver_sound_side_u);

        return String.format(FORMAT, soundname);
    }

}
