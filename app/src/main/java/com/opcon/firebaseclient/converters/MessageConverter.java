package com.opcon.firebaseclient.converters;

import com.opcon.components.Message;

import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 28/01/2017.
 */

public class MessageConverter implements Converter<Message> {
    public static final MessageConverter INSTANCE = new MessageConverter();
    @Override
    public Message convertObj(String sid, JSONObject t) {
        Message msg = new Message();
        msg.put(t);
        if (sid != null) {
            msg.setSid(sid);
        }
        return msg;
    }
}
