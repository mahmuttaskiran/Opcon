package com.opcon.components;

import com.opcon.firebaseclient.PresenceManager;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Mahmut Taşkiran on 20/02/2017.
 */

public class Order extends Component {

  public static Order newOrderForNotifierSid(String notifierSid) {
    Order order = new Order();
    order.setRelationalNotifier(notifierSid);
    order.setSender(PresenceManager.uid());
    return order;
  }

  private Order() {}

  public Order(JSONObject json) {
    put(json);
  }

  public void setExternal(Map<String, Object> external){
    put(4, external);
  }

  public Component getExternal() {
    return getComponent(4);
  }

  public void setRelationalNotifier(String sid) {
    put(3, sid);
  }

  public String getRelationNotifier() {
    return getString(3);
  }

}
