package com.opcon.notifier.components;

import com.opcon.components.Component;
import com.opcon.components.Message;


/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class SpecialPacket extends Component {
    public SpecialPacket(int type) {
        setId(type);
    }
    @Override public boolean isEmpty() {
        return getTotalElement() == 1;
    }

    public void putTo(Message msg) {
        int id = getId();
        delete(LID);
        delete(SID);
        msg.put(toJson());
        setId(id);
    }
}
