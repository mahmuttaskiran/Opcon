package com.opcon.notifier.components;

import com.opcon.components.Component;
import com.opcon.notifier.components.constants.Operations;
import com.opcon.notifier.operations.PlaySoundLocaleProcessor;
import com.opcon.notifier.operations.OperationProcessor;
import com.opcon.notifier.operations.PlaySoundRemoteProcessor;
import com.opcon.notifier.operations.PostProcessor;
import com.opcon.notifier.operations.SendMessageLocale;
import com.opcon.notifier.operations.SendMessageRemote;
import com.opcon.ui.fragments.occs.OPCFragment;

import org.json.JSONObject;

/**
 * Created by Mahmut Taşkiran on 31/10/2016.
 *
 */

public class Operation extends Component {

    public Operation(JSONObject json) {
        put(json);
    }

    public boolean isPacketExists() {
        return getInt(OPCFragment.PACKET) != 0 && getInt(OPCFragment.PACKET) != -1;
    }

    public int getPacketType() {
        return getInt(OPCFragment.PACKET);
    }

    public OperationProcessor getProcessor() {
        OperationProcessor processor = null;
        switch (getId()) {
            case Operations.__PLAY_SOUND:
                processor = PlaySoundRemoteProcessor.getInstance();
                break;
            case Operations._PLAY_SOUND:
                processor = PlaySoundLocaleProcessor.getInstance();
                break;
            case Operations._SENT_MSG:
                processor = SendMessageLocale.getInstance();
                break;
            case Operations.__SENT_MSG:
                processor = SendMessageRemote.getInstance();
                break;
            case Operations.__POST:
                processor = PostProcessor.getInstance();
                break;
        }
        return processor;
    }

    public String alias(){
        return Operations.getOperationAlias(getId());
    }

}
