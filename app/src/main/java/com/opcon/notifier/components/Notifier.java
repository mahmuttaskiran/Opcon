package com.opcon.notifier.components;


import android.content.Context;

import com.opcon.components.Component;
import com.opcon.database.ComponentSettings;
import com.opcon.database.ContactBase;
import com.opcon.database.NotifierLogBase;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.constants.Operations;
import com.opcon.ui.utils.ConditionCommentInterpreter;
import com.opcon.ui.utils.OperationCommentInterpreter;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * I love notifiers...
 * Created by Mahmut Taşkiran on 31/10/2016.
 *
 */

public class Notifier extends Component {

    public static final int RUNNING = 3, STOPPED = 1, DELETED = 2, NOT_DETERMINED = 0;

    public Notifier() {

    }

    public Notifier(JSONObject json) {
        put(json);
    }

    public Notifier(Component component) {
        put(component.toJson());
    }

   

    public void setRelationshipState(int state) {
        put(1, state);
    }

    public int getRelationshipState() {
        return getInt(1);
    }

    public String getDescription() {
        return getString(2);
    }

    public boolean isProfileUpdater() {
        return getBoolean(114);
    }

    public void itIsProfileUpdater() {
        put(114, true);
    }

    public void setDescription(String description) {
        put(2, description);
    }

    public Condition getCondition() {
        return new Condition(getComponent(3).toJson());
    }



    public void setCondition(Condition condition) {
        put(3, condition);
    }

    public Operation getOperation() {
        return new Operation(getComponent(4).toJson());
    }

    public void setOperation(Operation operation) {
        put(4, operation);
    }

    public int getState() {
        return getInt(5);
    }
    public void setState(int state) {
        put(5, state);
    }

    public void setTimestamp(long timestamp) {put(6, timestamp);}
    public long getTimestamp() {
        return getLong(6);
    }

    public boolean isOwnerAmI() {
        return getSender().equals(PresenceManager.uid());
    }
    public boolean isTargetAmI() {
        return getReceiver().equals(PresenceManager.uid());
    }

    public String getConditionDescription(Context context)
    {
        String desc = getString(77);
        if (desc == null) {
            desc = ConditionCommentInterpreter.interpret(context, getCondition().getId(),
                getCondition().toPureJson(), isOwnerAmI(), amIRelation());
            put(77, desc);
        }
        return desc;
    }

    public boolean amIRelation() {
        return getSender().equals(PresenceManager.uid()) || getReceiver().equals(PresenceManager.uid());
    }

    public String getOperationDescription(Context context) {
        String desc = getString(78);
        if (desc == null) {
            desc = OperationCommentInterpreter.interpret(context, getOperation().getId(),
                getOperation().toPureJson(), isOwnerAmI(), amIRelation());
            put(78, desc);
        }
        return desc;
    }

    public String getRelationship() {
        if (isOwnerAmI()) {
            return getReceiver();
        } else {
            return getSender();
        }
    }

    public boolean isConditionProgressable() {
       return isTargetAmI()
           ?
                Conditions.isOnTheTarget(getCondition().getId())
           :
                    Conditions.isOnTheOwner(getCondition().getId());
    }

    public boolean anyProgressable() {
        return isConditionProgressable() || isOperationProgressable();
    }

    public boolean bothProgressable() {
        return isConditionProgressable() && isOperationProgressable();
    }

    public boolean isOperationProgressable() {
        return isTargetAmI()
            ?
                Operations.isOnTheTarget(getOperation().getId())
            :
                Operations.isOnTheOwner(getOperation().getId());
    }

    public String getOwnerAvatar(Context context) {
        String a = getString(81);
        if (a == null) {
            a = ContactBase.Utils.getValidAvatar(context, getSender());
            put(81, a);
        }
        return a;
    }

    public String getTargetAvatar(Context context) {
        String a = getString(82);
        if (a == null) {
            a = ContactBase.Utils.getValidAvatar(context, getReceiver());
            put(82, a);
        }
        return a;
    }

    public String getConditionProcessor() {
        boolean on_the_target = Conditions.isOnTheTarget(getCondition().getId());
        if (on_the_target) {
            return getReceiver();
        } else {
            return getSender();
        }
    }

    public String getOperationProcessor() {
        boolean on_the_target = Operations.isOnTheTarget(getOperation().getId());
        if (on_the_target) {
            return getReceiver();
        } else {
            return getSender();
        }
    }

    public int getConditionProcessorState() {
        if (getConditionProcessor().equals(getRelationship())) {
            return getRelationshipState();
        } else {
            return getState();
        }
    }

    public int getOperationProcessorState() {
        if (getOperationProcessor().equals(getRelationship())) {
            return getRelationshipState();
        } else {
            return getState();
        }
    }

    public String getConditionProcessorAvatar(Context context) {
        String a = getString(83);
        if (a == null) {
            a = ContactBase.Utils.getValidAvatar(context, getConditionProcessor());
            put(83, a);
        }
        return a;
    }


    public String getOperationProcessorAvatar(Context c ){
        String a = getString(84);
        if (a == null) {
            a = ContactBase.Utils.getValidAvatar(c, getOperationProcessor());
            put(84, a);
        }
        return a;
    }

    public String getOperationProcessorName(Context c) {
        String name = getString(111);
        if (name == null) {
            name = ContactBase.Utils.getName(c, getOperationProcessor());
            put(111, name);
        }
        return name;
    }

    public String getConditionProcessorName(Context c) {
        String name = getString(112);
        if (name == null) {
            name = ContactBase.Utils.getName(c, getConditionProcessor());
            put(112, name);
        }
        return name;
    }

    public void setNonSeenNotificationLength(int length) {
        put(80, length);
    }

    public int getNonSeenNotificationLength(Context context) {
        int v = getInt(80);
        if (v == -1) {
            v = NotifierLogBase.Utils.getNonSeenLogCount(context, getId());
            put(80, v);
        }
        return v;
    }

    public boolean isNotificationOn(Context context) {
        return ComponentSettings.getBoolean(context, String.valueOf(getId()), ComponentSettings.NOTIFICATION, true);
    }

    public boolean isPacketAutomated(Context context) {
        return ComponentSettings.getBoolean(context, String.valueOf(getId()), ComponentSettings.NOTIFICATION_SOUND, false);
    }

    public static void setNotificationState(Context context, int id, boolean state) {
        ComponentSettings.setBoolean(context, String.valueOf(id), ComponentSettings.NOTIFICATION, state);
    }

    public static void setPacketAutomationState(Context context, int id, boolean state) {
        ComponentSettings.setBoolean(context, String.valueOf(id), ComponentSettings.NOTIFICATION_SOUND, state);
    }

    public  boolean isPacketAutomateable() {
        int oid = getOperation().getId();
        return getOperationProcessor().equals(PresenceManager.uid()) && (oid == Operations.__SENT_MSG || oid == Operations._SENT_MSG);
    }

    public String getRelationshipName(Context context) {
        return ContactBase.Utils.getName(context, getRelationship());
    }

    public String getRelationshipAvatar(Context context) {
        return ContactBase.Utils.getValidAvatar(context, getRelationship());
    }

    @Override public int hashCode() {
        return getId(); // locale id
    }

    @Override public boolean equals(Object o) {
        try {
            Notifier r = (Notifier) o; // with locale ID.
            return r.getId() == getId();
        } catch (ClassCastException e) {
            return false;
        }
    }

    public String getOwnerName(Context context) {
        return ContactBase.Utils.getName(context, getSender());
    }

    public void update(Context mContext) {
        NotifierProvider.Utils.updateNotifier(mContext, this);
    }


  public static class Builder {
        private Notifier mNotifier;
        public Builder () {
            mNotifier = new Notifier();
        }
        public Builder setUID(int uid) {
            mNotifier.setId(uid);
            return this;
        }
        public Builder setOwner(String value) {
            mNotifier.setSender(value);
            return this;
        }

        public Builder setProfileUpdater(boolean vb) {
            if (vb) {
                mNotifier.itIsProfileUpdater();
                mNotifier.setReceiver("PROFILE_UPDATER");
                mNotifier.setSender(PresenceManager.uid());
            }
            return this;
        }

        public Builder setTarget(String value) {
            mNotifier.setReceiver(value);
            return this;
        }
        public Builder setDescription(String value) {
            mNotifier.setDescription(value);
            return this;
        }
        public Builder setOperation(String operationJsonParams) {
            try {
                this.setOperation(new JSONObject(operationJsonParams));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }
        public Builder setCondition(String conditionJsonParams ){
            try {
                this.setCondition(new JSONObject(conditionJsonParams));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return this;
        }
        public Builder setOperation(JSONObject value) {
            Operation operation = new Operation(value);
            mNotifier.setOperation(operation);
            return this;
        }
        public Builder setCondition(JSONObject value) {
            Condition condition = new Condition(value);
            mNotifier.setCondition(condition);
            return this;
        }
        public Builder setTimestamp(long timestamp) {
            mNotifier.setTimestamp(timestamp);
            return this;
        }
        public Builder setState(int state){
            mNotifier.setState(state);
            return this;
        }
        public Builder setRelationshipState(int state) {
            mNotifier.setRelationshipState(state);
            return this;
        }
        public Builder setSID(String sid) {
            mNotifier.put(Component.SID, sid);
            return this;
        }
        public Notifier built() {
            return this.mNotifier;
        }
    }


}
