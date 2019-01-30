package com.opcon.notifier.environment;

import android.content.Context;

import com.opcon.notifier.components.Notifier;

import java.util.List;

/**
 * Created by Mahmut Taşkiran on 09/01/2017.
 */

public abstract class Environment {

    private Context mContext;

    public Environment(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public abstract void builtComponents(State capturedState, List<Notifier> processableNotifiers);

}
