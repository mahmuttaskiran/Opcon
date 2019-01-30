package com.opcon.libs.registration.libs;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

/**
 * Created by Mahmut Taşkiran on 23/03/2017.
 */

public class RegistrationEndpoint {

  public static RegistrationEndpoint instance;

  private String address;
  private int port;

  private RegistrationEndpoint(String address, int port) {
    this.address = address;
    this.port = port;
  }

  public String getAddress() {
    return address;
  }

  public int getPort() {
    return port;
  }

  @Override
  public String toString() {
    return "RegistrationEndpoint{" +
        "address='" + address + '\'' +
        ", port=" + port +
        '}';
  }

  public static class Builder {
    public interface RegistrationEndpointGetter {
      void onGet(RegistrationEndpoint endpoint);
      void onError();
    }

    private static ValueEventListener mValueListener = new ValueEventListener() {
      @Override public void onDataChange(DataSnapshot dataSnapshot) {
        try {
          Object data = dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {});
          Map<String, Object> map = (Map<String, Object>) data;
          RegistrationEndpoint endpoint = new RegistrationEndpoint(map.get("address").toString(), Integer.parseInt(map.get("port").toString()));
          instance = endpoint;
          if (mGetter != null) {
            mGetter.onGet(endpoint);
          }
        } catch (Exception e) {
          if (mGetter != null) {
            mGetter.onError();
          }
          e.printStackTrace();
        }
        leave();
      }
      @Override public void onCancelled(DatabaseError databaseError) {
        leave();
      }
      void leave() {
        mGetter = null;
        FirebaseDatabase.getInstance().getReference("constants/recognize_server").removeEventListener(mValueListener);
      }
    };

    private static RegistrationEndpointGetter mGetter;

    public static void takeEndpoint(@NonNull final RegistrationEndpointGetter getter) {
      mGetter = getter;

      if (instance != null) {
        mGetter.onGet(instance);
        return;
      }

      FirebaseDatabase.getInstance().getReference("constants/recognize_server").addValueEventListener(mValueListener);
    }
  }



}
