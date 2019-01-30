package com.opcon.firebaseclient.converters;

import com.opcon.components.Feature;

import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 14/04/2017.
 */

public class FeatureConverter implements Converter<Feature> {
  public static FeatureConverter instance = new FeatureConverter();
  @Override public Feature convertObj(String sid, JSONObject t) {
    return new Feature(t);
  }
}
