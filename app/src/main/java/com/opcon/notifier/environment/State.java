package com.opcon.notifier.environment;

import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.utils.NotifierUtils;

import java.util.List;

/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 */

public class State {

    private boolean locationalConditionExists;
    private boolean batteryConditionExists;
    private boolean outgoingSmsConditionExists;
    private boolean incomingSmsConditionExists;
    private boolean outgoingCallExists;
    private boolean incomingCallExists;
    private boolean timeConditionExists;
    private boolean cameraConditionExists;

    private State() {
        // ignore
    }

    public static State capture(List<Notifier> notifiers) {

        State mState = new State();

        mState.batteryConditionExists = NotifierUtils.isAnyConditionExists(
            notifiers,
                Conditions._BATTERY,
                Conditions.__BATTERY
        );

        mState.locationalConditionExists = NotifierUtils.isAnyConditionExists(
            notifiers,
                Conditions._LOCATION,
                Conditions.__LOCATION
        );

        mState.outgoingSmsConditionExists = NotifierUtils.isAnyConditionExists(
            notifiers,
                Conditions._OUT_MSG,
                Conditions.__OUT_MSG
        );

        mState.incomingSmsConditionExists = NotifierUtils.isAnyConditionExists(
            notifiers,
                Conditions._IN_MSG,
                Conditions.__IN_MSG
        );

        mState.outgoingCallExists = NotifierUtils.isAnyConditionExists(
            notifiers,
                Conditions._OUT_CALL,
                Conditions.__OUT_CALL
        );

        mState.incomingCallExists = NotifierUtils.isAnyConditionExists(
            notifiers,
                Conditions._IN_CALL,
                Conditions.__IN_CALL
        );

        mState.timeConditionExists = NotifierUtils.isAnyConditionExists(
            notifiers,
                Conditions._TIMELY,
                Conditions.__TIMELY
        );

        mState.cameraConditionExists = NotifierUtils.isAnyConditionExists(
            notifiers,
                Conditions._NEW_PICTURE,
                Conditions.__NEW_PICTURE
        );


        return mState;
    }

    @Override
    public String toString() {
        return "State{" +
            "locationalConditionExists=" + locationalConditionExists +
            ", batteryConditionExists=" + batteryConditionExists +
            ", outgoingSmsConditionExists=" + outgoingSmsConditionExists +
            ", incomingSmsConditionExists=" + incomingSmsConditionExists +
            ", outgoingCallExists=" + outgoingCallExists +
            ", incomingCallExists=" + incomingCallExists +
            ", timeConditionExists=" + timeConditionExists +
            ", cameraConditionExists=" + cameraConditionExists +
            '}';
    }

    public boolean isLocationalConditionExists() {
        return locationalConditionExists;
    }

    public boolean isBatteryConditionExists() {
        return batteryConditionExists;
    }

    public boolean isOutgoingSmsConditionExists() {
        return outgoingSmsConditionExists;
    }

    public boolean isIncomingSmsConditionExists() {
        return incomingSmsConditionExists;
    }

    public boolean isOutgoingCallExists() {
        return outgoingCallExists;
    }

    public boolean isIncomingCallExists() {
        return incomingCallExists;
    }

    public boolean isTimeConditionExists() {
        return timeConditionExists;
    }

    public boolean isCameraConditionExists() {
        return cameraConditionExists;
    }

}
