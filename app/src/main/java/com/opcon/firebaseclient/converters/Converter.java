package com.opcon.firebaseclient.converters;


import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 28/01/2017.
 */

public interface Converter<R> {
    R convertObj(String sid, JSONObject t);
}