package com.opcon.notifier;

import android.content.Context;

import com.opcon.notifier.components.SpecialPacket;
import com.opcon.notifier.packets.SpecialPacketProvider;

import java.util.List;

/**
 * Created by Mahmut Taşkiran on 13/01/2017.
 */

public class SpecialPacketBuilderFactory {

    static volatile SpecialPacketBuilderFactory instance = new SpecialPacketBuilderFactory();

    public synchronized static SpecialPacketBuilderFactory instance() {
        if (instance == null) {
          synchronized (SpecialPacketBuilderFactory.class) {
            if (instance == null) {
              instance = new SpecialPacketBuilderFactory();
            }
          }
        }
        return instance;
    }

    public SpecialPacket getPacket(Context context, int specialPacketType) {
        List<SpecialPacketProvider> providers = SpecialPacketProviderBuilderFactory
                .instance()
                .getProviders(specialPacketType);
        SpecialPacket packet = null;
        if (providers != null) {
            for (SpecialPacketProvider provider : providers) {
                packet = provider.getPacket(context);
                if (!com.opcon.libs.utils.SpecialPacketUtils.isEmpty(packet)) {
                    return packet;
                }
            }
        }
        return packet;
    }

    public SpecialPacket getPacket(Context context, Class<? extends SpecialPacketProvider> provider) {
        SpecialPacket sp = null;
        try {
            SpecialPacketProvider specialPacketProvider = provider.newInstance();
            sp = specialPacketProvider.getPacket(context);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return sp;
    }

}
