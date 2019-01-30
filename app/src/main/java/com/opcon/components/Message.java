package com.opcon.components;

import android.content.Context;
import android.graphics.Bitmap;

import com.opcon.database.ContactBase;
import com.opcon.database.MessageProvider;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.ComponentSender;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.utils.SpecialPacketUtils;

import org.json.JSONObject;

/*
 * Created by Mahmut Taşkiran on 08/11/2016.
 */

public class Message extends Component {

  public String getSenderName(Context context) {
    String name = getString(112);
    if (name == null) {
      name= ContactBase.Utils.getName(context, getSender());
      put(112, name);
    }
    return name;
  }

  public static class Text {
        public static final int BODY = 1;
    }

    public static class Picture {
        public static final int URL = 2;
        public static final int FILE = 3;
        // upload or download is done.
        public static final int DONE = 4;
        public static final int FAILED = 5;
        public static final int THUMBNAIL = 6;
    }

    public static class SpecialPacket {
      public static final int ADDITION_TEXT = 1;
    }
    
    public static class Location extends SpecialPacket {
        public static final int LONGITUDE = 2;
        public static final int LATITUDE = 3;
        public static final int ADDRESS = 4;
        public static final int TIME = 5;
    }
    public static class InoutCall extends SpecialPacket {
        public static final int WHO = 2;
        public static final int WHEN = 3;

    }
    public static class InoutMessage extends SpecialPacket {
        public static final int WHO = 2;
        public static final int WHAT = 3;
        public static final int WHEN = 4;

    }

    public static class Battery extends SpecialPacket {
        public static final int PERCENT = 2;
    }
    
    public static final int
            TEXT = 1,
            PICTURE = 2,
            LOCATION = 3,
            INCOMING_CALL = 4,
            OUTGOING_CALL = 5,
            INCOMING_MESSAGE = 7,
            BATTERY_STATE = 8,
            OUTGOING_MESSAGE = 9,
            LAST_CAPTURED_IMAGE = 10,
            CONNECTION_STATE = 11;


    public Message() {
        super();
    }

    public boolean isReceived() {
        return getReceiveTimestamp() > 1;
    }

    public boolean isSeen() {
        return getSeenTimestamp() > 1;
    }

    public boolean isSent() {
        return getSentTimestamp() > 1 && isTried();
    }

    private void setRelationNotifier(String r) {
        put(88, r);
    }
    
    public String getRelationNotifier() {
        return getString(88);
    }


    public boolean isSpecialPacket() {
        return isSpecialPacket(getType());
    }



    public boolean isWaiting() {
        return getBoolean(77);
    }

    public void setWaiting(boolean b) {
        put(77, b);
    }

    public static class Builder {
        Message mMsg;

        public Builder() {
            mMsg = new Message();
        }

        public Builder setUID(int uid) {
            mMsg.setId(uid);
            return this;
        }

        public Builder setSender(String sender) {
            mMsg.setSender(sender);
            return this;
        }

        public Builder setReceiver(String receiver) {
            mMsg.setReceiver(receiver);
            return this;
        }

        public Builder setReceiveTimestamp(long timestamp) {
            mMsg.put(20, timestamp);
            return this;
        }

        public Builder setSentTimestamp(long timestamp) {
            mMsg.put(21, timestamp);
            return this;
        }

        public Builder setSeenTimestamp(long timestamp) {
            mMsg.put(22, timestamp);
            return this;
        }

        public Builder setType(int type) {
            mMsg.put(23, type);
            return this;
        }

        public Builder setTriedForServer(boolean bool) {
            mMsg.put(24, bool);
            return this;
        }

        public Builder setSpecialParam(int def, String param) {
            mMsg.put(def, param);
            return this;
        }

        public Builder setSpecialParam(int def, boolean param) {
            mMsg.put(def, param);
            return this;
        }

        public Builder setSpecialParam(int def, long param) {
            mMsg.put(def, param);
            return this;
        }
        
        public Builder setSpecialParam(int def, int param) {
            mMsg.put(def, param);
            return this;
        }

        public Builder setSpecialParam(int def, float param) {
            mMsg.put(def, param);
            return this;
        }

        public Builder setSpecialParam(int def, double param) {
            mMsg.put(def, param);
            return this;
        }

        public Message built() {
            if (mMsg.getType() == 0 || mMsg.getSender() == null ||
                    mMsg.getReceiver() == null) {
                throw new IllegalArgumentException("You have to set:" +
                        "sender, receiver and id.");
            }
            return mMsg;
        }
        
        public Builder setRelationNotifier(String s) {
            mMsg.setRelationNotifier(s);
            return this;
        }

        public Builder setWaiting(boolean waiting) {
            mMsg.put(77, waiting);
            return this;
        }

        public Builder putSpecialPacket(JSONObject params) {
            mMsg.put(params);
            return this;
        }

        public Builder putSpecialPacket(com.opcon.notifier.components.SpecialPacket packet) {
          setType(SpecialPacketUtils.packetTypeToMessageType(packet.getId()));
          packet.putTo(mMsg);
          return this;
        }
    }

    public static boolean isSpecialPacket(int type) {
        return  (type >= LOCATION && type <= CONNECTION_STATE);
    }


    public void setSentTimestamp(long timestamp) {
        put(21, timestamp);
    }

    public void setReceiveTimestamp(long timestamp) {
        put(20, timestamp);
    }

    public void setSeenTimestamp(long timestamp) {
        put(22, timestamp);
    }

    public long getSentTimestamp() {
        return getLong(21);
    }

    public long getReceiveTimestamp() {
        return getLong(20);
    }

    public long getSeenTimestamp() {
        return getLong(22);
    }

    public int getType() {
        return getInt(23);
    }

    public boolean isTried() {
        return getBoolean(24);
    }

    public void send(final Context context) {

      setWaiting(false);

      if (!getSender().equals(PresenceManager.uid())) {
          throw new IllegalStateException();
      }

      ComponentSender componentSender = new ComponentSender("msgs/" + getReceiver(), this);

      if (isImageMessage()) {
          componentSender.deleteParams(Message.Picture.FAILED, Message.Picture.DONE, Message.Picture.FILE);
      }
      componentSender.setListener(new ComponentSender.ComponentSentListener() {
        @Override public void onSuccess(Component component) {
            Message msg = (Message) component;
            msg.setTried(true);
            msg.setWaiting(false);
            MessageProvider.Utils.update(context, msg);

          Ack component1 = new Ack(msg.getSid(),
              System.currentTimeMillis(),
              Ack.SENT);
          component1.setSender(PresenceManager.uid());
          component1.setReceiver(PresenceManager.uid());
          ComponentListenerManager.getInstance(context).notifyNewComponent(null, component1);
        }
        @Override
        public void onFail(Component component) {}
      });
      componentSender.sent();
    }


    public boolean isImageMessage() {
        return getType() == LAST_CAPTURED_IMAGE || getType() == PICTURE;
    }

    public void setTried(boolean tried) {
        put(24, tried);
    }

}
