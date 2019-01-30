package com.opcon.notifier;

import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.packets.BatteryLevelProvider;
import com.opcon.notifier.packets.IncomingCallContentProvider;
import com.opcon.notifier.packets.IncomingCallPrefProvider;
import com.opcon.notifier.packets.IncomingMessageContentProvider;
import com.opcon.notifier.packets.IncomingMessagePrefProvider;
import com.opcon.notifier.packets.LastCapturedImageContentProvider;
import com.opcon.notifier.packets.LastCapturedImagePrefProvider;
import com.opcon.notifier.packets.LocationDefaultProvider;
import com.opcon.notifier.packets.LocationPrefProvider;
import com.opcon.notifier.packets.OutgoingCallDefaultProvider;
import com.opcon.notifier.packets.OutgoingCallPrefProvider;
import com.opcon.notifier.packets.OutgoingMessageDefaultProvider;
import com.opcon.notifier.packets.SpecialPacketProvider;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Mahmut Taşkiran on 13/01/2017.
 */

public class SpecialPacketProviderBuilderFactory {

    static volatile SpecialPacketProviderBuilderFactory instance = new SpecialPacketProviderBuilderFactory();

    public synchronized static SpecialPacketProviderBuilderFactory instance() {
        if (instance == null) {
          synchronized (SpecialPacketProviderBuilderFactory.class) {
            if (instance == null) {
              instance = new SpecialPacketProviderBuilderFactory();
            }
          }
        }
        return instance;
    }

    public List<SpecialPacketProvider> getProviders(int specialPacketType) {
        switch (specialPacketType) {
            case Packets._BATTERY_LEVEL:
                return getBatteryLevelProviders();
            case Packets._IN_CALL:
                return getIncomingCallProviders();
            case Packets._IN_MSG:
                return getIncomingMessageProviders();
            case Packets._LAST_IMAGE:
                return getLastCapturesImageProviders();
            case Packets._LOCATION:
                return getLocationProviders();
            case Packets._OUT_CALL:
                return getOutgoingCallProviders();
            case Packets._OUT_MSG:
                return getOutgoingMessageProviders();
        }
        return null;
    }


    public List<SpecialPacketProvider> getBatteryLevelProviders() {
        SpecialPacketProvider provider1 = new BatteryLevelProvider();
        return Arrays.asList(provider1);
    }

    public List<SpecialPacketProvider> getIncomingCallProviders() {
        SpecialPacketProvider provider1 = new IncomingCallPrefProvider();
        SpecialPacketProvider provider2 = new IncomingCallContentProvider();
        return Arrays.asList(provider1, provider2);
    }

    public List<SpecialPacketProvider> getIncomingMessageProviders() {
        SpecialPacketProvider provider1 = new IncomingMessagePrefProvider();
        SpecialPacketProvider provider2 = new IncomingMessageContentProvider();
        return Arrays.asList(provider1, provider2);
    }

    public List<SpecialPacketProvider> getLastCapturesImageProviders() {
        SpecialPacketProvider provider1 = new LastCapturedImagePrefProvider();
        SpecialPacketProvider provider2 = new LastCapturedImageContentProvider();
        return Arrays.asList(provider1, provider2);
    }

    public List<SpecialPacketProvider> getLocationProviders() {
        SpecialPacketProvider provider1 = new LocationDefaultProvider();
        SpecialPacketProvider provider2 = new LocationPrefProvider();
        return Arrays.asList(provider1, provider2);
    }

    public List<SpecialPacketProvider> getOutgoingMessageProviders() {
        SpecialPacketProvider provider1 = new OutgoingMessageDefaultProvider();
        return Arrays.asList(provider1);
    }

    public List<SpecialPacketProvider> getOutgoingCallProviders() {
        SpecialPacketProvider provider1 = new OutgoingCallPrefProvider();
        SpecialPacketProvider provider2 = new OutgoingCallDefaultProvider();
        return Arrays.asList(provider1, provider2);
    }
}
