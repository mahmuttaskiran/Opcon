package com.opcon.ui.utils;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.opcon.R;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.constants.Operations;
import com.opcon.notifier.components.constants.Packets;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by Mahmut Taşkiran on 16/10/2016.
 *
 */

public class NotifierConstantUtils {

    public static final String ON_THE_TARGET = "_";
    public static final String ON_THE_OWNER = "__";

    public static String getPacketTitleForSent(Context context, int uid) {
        if (uid == Packets._IN_CALL) {
            return context.getString(R.string.incall_packet_for_sent);
        } else if (uid == Packets._LOCATION) {
            return context.getString(R.string.location_packet_for_sent);
        } else if (uid == Packets._BATTERY_LEVEL) {
            return context.getString(R.string.battery_level_packet_for_sent);
        }
        return getPacketTitle(context, uid, true);
    }

    public static class Component {
        public int uid;
        public String avatar;
        public String title;
        public int icon;
        public Component(int uid, String avatar, String title, int icon) {
            this.uid = uid;
            this.avatar = avatar;
            this.title = title;
            this.icon = icon;
        }
    }

    public static int getPacketColor(int i) {

        if (i > -1) {
            return Color.parseColor("#FFBE06");
        }

        int c = 0x9e9e9e;
        switch (i) {
            case Packets._BATTERY_LEVEL:
                c = Color.parseColor("#f44336");
                break;
            case Packets._IN_CALL:
                c = Color.parseColor("#673ab7");
                break;
            case Packets._IN_MSG:
                c = Color.parseColor("#3f51b5");
                break;
            case Packets._OUT_MSG:
                c = Color.parseColor("#ffc107");
                break;
            case Packets._OUT_CALL:
                c = Color.parseColor("#2196f3");
                break;
            case Packets._LOCATION:
                c = Color.parseColor("#009688");
                break;
            case Packets._LAST_IMAGE:
                c = Color.parseColor("#4caf50");
                break;
        }
        return c;
    }

    public static int getConditionColor(int i) {

        if (i > -1) {
            return Color.parseColor("#4FC3F7");
        }

        int c = 0x9e9e9e;
        switch (i) {
            case Conditions.__BATTERY:
            case Conditions._BATTERY:
                c = Color.parseColor("#f44336");
                break;
            case Conditions.__IN_CALL:
            case Conditions._IN_CALL:
                c = Color.parseColor("#673ab7");
                break;
            case Conditions.__IN_MSG:
            case Conditions._IN_MSG:
                c = Color.parseColor("#3f51b5");
                break;
            case Conditions.__OUT_MSG:
            case Conditions._OUT_MSG:
                c = Color.parseColor("#ffc107");
                break;
            case Conditions.__OUT_CALL:
            case Conditions._OUT_CALL:
                c = Color.parseColor("#2196f3");
                break;
            case Conditions._LOCATION:
            case Conditions.__LOCATION:
                c = Color.parseColor("#009688");
                break;
            case Conditions.__NEW_PICTURE:
            case Conditions._NEW_PICTURE:
                c = Color.parseColor("#4caf50");
                break;
            case Conditions._TIMELY:
            case Conditions.__TIMELY:
                c = Color.parseColor("#ffeb3b");
                break;
        }
        return c;
    }

    public static int getOperationColor(int i) {

        if (i > -1) {
            return Color.parseColor("#FFBE06");
        }

        int c = 0x9e9e9e;
        switch (i) {
            case Operations.__SENT_MSG:
            case Operations._SENT_MSG:
                c = Color.parseColor("#673ab7");
                break;
            case Operations._PLAY_SOUND:
            case Operations.__PLAY_SOUND:
                c = Color.parseColor("#3f51b5");
                break;
            case Operations.__POST:
                c = Color.parseColor("#ff1744");
        }
        return c;
    }

    public static int getConditionIcon18dp(int conditionId) {
        switch (conditionId) {
            case Conditions._OUT_CALL:
            case Conditions.__OUT_CALL:
                return R.drawable.condition_ic_call_made_black_18dp;
            case Conditions._IN_CALL:
            case Conditions.__IN_CALL:
                return R.drawable.condition_ic_call_received_black_18dp;
            case Conditions._OUT_MSG:
            case Conditions.__OUT_MSG:
                return R.drawable.condition_ic_send_black_18dp;
            case Conditions._IN_MSG:
            case Conditions.__IN_MSG:
                return R.drawable.condition_ic_chat_black_18dp;
            case Conditions._TIMELY:
            case Conditions.__TIMELY:
                return R.drawable.condition_ic_access_time_black_18dp;
            case Conditions._BATTERY:
            case Conditions.__BATTERY:
                return R.drawable.condition_ic_battery_charging_90_black_18dp;
            case Conditions._LOCATION:
            case Conditions.__LOCATION:
                return R.drawable.condition_ic_location_on_black_18dp;
            case Conditions._NEW_PICTURE:
            case Conditions.__NEW_PICTURE:
                return R.drawable.condition_ic_camera_alt_black_24dp;
            default:
                return R.drawable.ic_help_white_24dp;

        }
    }


    public static int getOperationIcon18dp(int id) {
        switch (id) {
            case Operations._SENT_MSG:
            case Operations.__SENT_MSG:
                return R.drawable.condition_ic_chat_black_18dp;
            case Operations._PLAY_SOUND:
            case Operations.__PLAY_SOUND:
                return R.drawable.operation_ic_music_note_black_18dp;
            case Operations.__POST:
                return R.drawable.ic_heart_no_outline;
            default:
                return R.drawable.ic_help_white_24dp;
        }

    }

    public static List<Component> getConditions(Context c, String WHAT) {
        if (TextUtils.isEmpty(WHAT) || !((WHAT.equals(ON_THE_OWNER) || WHAT.equals(ON_THE_TARGET)))) {
            throw new IllegalArgumentException("What you desired for components?");
        }
        List<Component> components = new ArrayList<>();

        boolean remote = WHAT.equals(ON_THE_TARGET);
        boolean locale = WHAT.equals(ON_THE_OWNER);

        Field[] fields = Conditions.class.getFields();
        try {
            for (Field field: fields) {
                if (field.getName().matches("^_[A-Z].*?") && remote) {
                    components.add(new Component(field.getInt(null), null, getConditionTitle(c, field.getInt(null)), getConditionIcon18dp(field.getInt(null))));
                } else if (field.getName().matches("^__[A-Z].*?") && locale) {
                    components.add(new Component(field.getInt(null), null, getConditionTitle(c, field.getInt(null)), getConditionIcon18dp(field.getInt(null))));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        return components;
    }

    public static List<Component> getOperations(Context c, String WHAT) {
        if (TextUtils.isEmpty(WHAT) || !((WHAT.equals(ON_THE_OWNER) || WHAT.equals(ON_THE_TARGET)))) {
            throw new IllegalArgumentException("What you desired for components?");
        }
        List<Component> components = new ArrayList<>();

        // tersle!

        boolean locale = WHAT.equals(ON_THE_TARGET);
        boolean remote = WHAT.equals(ON_THE_OWNER);

        Field[] fields = Operations.class.getFields();
        try {
            for (Field f: fields) {

                if (f.getName().matches("^_[A-Z].*?") && locale) {
                    components.add(new Component(f.getInt(null), null, getOperationTitle(c, f.getInt(null)), getOperationIcon18dp(f.getInt(null))));
                } else if (f.getName().matches("^__[A-Z].*?") && remote) {
                    components.add(new Component(f.getInt(null), null, getOperationTitle(c, f.getInt(null)), getOperationIcon18dp(f.getInt(null))));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        return components;
    }

    public static List<Component> getCutePackets() {
        List<Component> components = new ArrayList<>();
        Component battery = new Component(Packets._BATTERY_LEVEL, null, null, getSpecialPacketGreyIcon(Packets._BATTERY_LEVEL));
        Component location = new Component(Packets._LOCATION, null, null, getSpecialPacketGreyIcon(Packets._LOCATION));

        components.add(battery);
        components.add(location);
        return components;
    }


    public static List<Component> getPackets() {
        Field[] fields = Packets.class.getFields();
        List<Component> packets = new ArrayList<>();
        try {
            for (Field f: fields) {
                if (Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()) &&
                    Modifier.isPublic(f.getModifiers()) && f.getName().startsWith("_")) {
                    int id = f.getInt(null);
                    packets.add(new Component(id, null, null, getSpecialPacketGreyIcon(id)));
                }
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return packets;
    }

    public static List<Integer> getRelationPackets (int conditionType) {

        switch (conditionType) {

            case Conditions.__BATTERY:
            case Conditions._BATTERY:
                return Arrays.asList(Packets._BATTERY_LEVEL);
            case Conditions.__IN_CALL:
            case Conditions._IN_CALL:
                return Arrays.asList(Packets._IN_CALL);
            case Conditions._IN_MSG:
            case Conditions.__IN_MSG:
                return Arrays.asList(Packets._IN_MSG);
            case Conditions.__OUT_CALL:
            case Conditions._OUT_CALL:
                return Arrays.asList(Packets._OUT_CALL);
            case Conditions.__OUT_MSG:
            case Conditions._OUT_MSG:
                return Arrays.asList(Packets._OUT_MSG);
            case Conditions.__LOCATION:
            case Conditions._LOCATION:
                return Arrays.asList(Packets._LOCATION);
            case Conditions.__NEW_PICTURE:
            case Conditions._NEW_PICTURE:
                return Arrays.asList(Packets._LAST_IMAGE);
            default:
                return null;

        }

    }

    public static String getConditionTitle(Context c, int uid) {
        switch (uid) {
            case Conditions._OUT_CALL:
                return c.getString(R.string._condition_out_call);
            case Conditions._IN_CALL:
                return c.getString(R.string._condition_in_call);
            case Conditions._OUT_MSG:
                return c.getString(R.string._condition_out_msg);
            case Conditions._IN_MSG:
                return c.getString(R.string._condition_in_msg);
            case Conditions._TIMELY:
            case Conditions.__TIMELY:
                return c.getString(R.string._condition_is_time);
            case Conditions._BATTERY:
                return c.getString(R.string._condition_battery);
            case Conditions._LOCATION:
                return c.getString(R.string._condition_is_location);
            case Conditions._NEW_PICTURE:
                return c.getString(R.string._condition_new_picture);
            case Conditions.__NEW_PICTURE:
                return gs(c, R.string.__condition_new_picture);
            case Conditions.__IN_CALL:
                return gs(c, R.string.__condition_in_call);
            case Conditions.__IN_MSG:
                return gs(c, R.string.__condition_in_msg);
            case Conditions.__OUT_CALL:
                return gs(c, R.string.__condition_out_call);
            case Conditions.__OUT_MSG:
                return gs(c, R.string.__condition_out_msg);
            case Conditions.__BATTERY:
                return gs(c, R.string.__condition_battery);
            case Conditions.__LOCATION:
                return gs(c, R.string.__condition_location);
        }
        return "";
    }

    private static String gs(Context c, int i) {
        return c.getString(i);
    }

    public static String getOperationTitle(Context context, int uid) {
        switch (uid) {
            case Operations._SENT_MSG:
                return context.getString(R.string._operation_sent_msg);
            case Operations._PLAY_SOUND:
                return context.getString(R.string._operation_play_sound);
            case Operations.__PLAY_SOUND:
                return context.getString(R.string.__operation_play_sound);
            case Operations.__SENT_MSG:
                return gs(context, R.string.__operation_sent_msg);
            case Operations.__POST:
                return gs(context, R.string.operaiton_post);
        }
        return "";
    }

     public static String getPacketTitle(Context c, int uid, boolean o) {
         switch (uid) {
             case Packets._BATTERY_LEVEL:
                 return o ? gs(c, R.string.__packet_battery) : c.getString(R.string.packet_battery_state);
             case Packets._IN_CALL:
                 return o ? gs(c, R.string.__packet_in_call) : c.getString(R.string.packet_in_call);
             case Packets._IN_MSG:
                 return o ? gs(c, R.string.__packet_in_message): c.getString(R.string.packet_in_message);
             case Packets._OUT_MSG:
                 return o?gs(c, R.string.__packet_out_msg):c.getString(R.string.packet_out_message);
             case Packets._OUT_CALL:
                 return o?gs(c, R.string.__packet_out_call):c.getString(R.string.packet_out_call);
             case Packets._LOCATION:
                 return o?gs(c, R.string.__packet_location):c.getString(R.string.packet_location);
             case Packets._LAST_IMAGE:
                 return o?gs(c, R.string.__packet_last_captured_image):c.getString(R.string.packet_last_captured_image);
             default:
                 return "";
         }
    }

    public static int getSpecialPacketGreyIcon(int anInt) {
        switch (anInt) {
            case Packets._BATTERY_LEVEL:
                return R.drawable.ic_battery_charging_90_grey_600_24dp;
            case Packets._IN_CALL:
                return R.drawable.ic_call_received_grey_700_24dp;
            case Packets._IN_MSG:
                return R.drawable.ic_message_grey_600_24dp;
            case Packets._OUT_MSG:
                return R.drawable.ic_send_grey_600_24dp;
            case Packets._OUT_CALL:
                return R.drawable.ic_call_made_grey_600_24dp;
            case Packets._LOCATION:
                return R.drawable.ic_location_on_grey_600_24dp;
            case Packets._LAST_IMAGE:
                return R.drawable.ic_camera_grey_600_24dp;
            default:
                return R.drawable.ic_help_white_24dp;
        }
    }
}
