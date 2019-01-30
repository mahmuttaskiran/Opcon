package com.opcon.notifier.packets;

import android.content.Context;
import android.test.InstrumentationTestCase;

import com.opcon.notifier.SpecialPacketBuilderFactory;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.notifier.components.constants.Packets;


/**
 * Created by Mahmut Taşkiran on 23/03/2017.
 */
public class IncomingCallContentProviderTest extends InstrumentationTestCase {

  Context context;

  public void setUp() {
    context = getInstrumentation().getTargetContext();
  }

  public void test1() {

    SpecialPacket packet = SpecialPacketBuilderFactory.instance().getPacket(context, Packets._OUT_MSG);

    assertEquals(packet==null, false);
  }

}