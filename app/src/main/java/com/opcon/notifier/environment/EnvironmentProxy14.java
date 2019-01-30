package com.opcon.notifier.environment;

import android.content.Context;
import android.content.Intent;

import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.environment.triggers.NewPictureTriggerService;
import com.opcon.notifier.environment.triggers.OutgoingSmsServiceProxy14;

import java.util.List;

/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 */

public class EnvironmentProxy14 extends DefaultEnvironmentImpl {

    public EnvironmentProxy14(Context context) {
        super(context);
    }

    @Override public void builtComponents(State state, List<Notifier> processableNotifiers) {
        super.builtComponents(state, processableNotifiers);
        if (state.isOutgoingSmsConditionExists()) {
            getContext().startService(new Intent(getContext().getApplicationContext(),
                    OutgoingSmsServiceProxy14.class));
        } else {
            getContext().stopService(new Intent(getContext().getApplicationContext(),
                    OutgoingSmsServiceProxy14.class));
        }

        if (state.isCameraConditionExists()) {
            getContext().getApplicationContext().startService(new Intent(getContext().getApplicationContext(), NewPictureTriggerService.class));
        } else {
            getContext().getApplicationContext().stopService(new Intent(getContext().getApplicationContext(),
                NewPictureTriggerService.class));
        }
    }

}
