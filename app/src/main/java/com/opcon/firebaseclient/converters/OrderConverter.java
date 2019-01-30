package com.opcon.firebaseclient.converters;

import com.opcon.components.Order;

import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 20/02/2017.
 */

public class OrderConverter implements Converter<Order> {
  public static OrderConverter INSTANCE = new OrderConverter();
  @Override
  public Order convertObj(String sid, JSONObject t) {
    return new Order(t);
  }
}
