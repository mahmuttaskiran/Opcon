package com.opcon.firebaseclient.converters;

import android.support.annotation.Nullable;

import com.opcon.components.Ack;

import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 30/01/2017.
 */

public class AckConverter implements Converter<Ack> {
    public static final AckConverter INSTANCE = new AckConverter();
    @Override
    public Ack convertObj(@Nullable String sid, JSONObject t) {
        Ack ack = new Ack();
        ack.put(t);
        if (sid != null) {
            ack.setSid(sid);
        }
        return ack;
    }
}
