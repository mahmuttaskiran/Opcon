package com.opcon.notifier.operations;

import android.content.Context;
import android.support.annotation.Nullable;

import com.opcon.notifier.OperationProcessManager;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.SpecialPacket;

/**
 * Created by Mahmut Taşkiran on 21/03/2017.
 */

public interface OperationProcessor {
  void process(Context context, Notifier notifier, @Nullable SpecialPacket sp, @Nullable OperationProcessManager.OperationProcessListener listener);
}
