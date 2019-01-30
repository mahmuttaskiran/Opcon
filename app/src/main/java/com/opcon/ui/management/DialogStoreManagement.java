package com.opcon.ui.management;

import android.content.Context;

import com.opcon.components.Ack;
import com.opcon.components.Component;
import com.opcon.components.Dialog;
import com.opcon.components.Message;
import com.opcon.database.FeatureBase;
import com.opcon.database.MessageProvider;
import com.opcon.database.NotifierProvider;
import com.opcon.notifier.components.Notifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

/**
 *
 * Created by Mahmut Taşkiran on 14/12/2016.
 *
 */

public class DialogStoreManagement {

    public boolean isEmpty() {
        return dialogs.isEmpty();
    }

    public interface DialogEventListener {
        void onDialogRemoved(int index);
        void onDialogAdded(int index, Dialog dialog);
        void onDialogUpdated(int index, Dialog dialog);
        void onMoveFirst(int index);
    }

    private volatile static DialogStoreManagement singleton;
    private Context mContext;
    private final List<Dialog> dialogs;

    private DialogEventListener dialogEventListener;
    private DialogEventListener getDialogEventListener() {
        return dialogEventListener;
    }

    public void setDialogEventListener(DialogEventListener dialogEventListener) {
        this.dialogEventListener = dialogEventListener;
    }

    private DialogStoreManagement(Context context) {
        this.dialogs = new ArrayList<>();
        mContext = context.getApplicationContext();
        init();
    }

    private void init() {
        synchronized (dialogs) {
            List<Dialog> dialogs = MessageProvider.DialogUtils.getFromMessages(mContext);
            NotifierProvider.Utils.getAndPutDialogs(mContext, dialogs);
            Dialog fromFeaturesBase = FeatureBase.getInstance(mContext)
                .prepareDialog(mContext);
            if (fromFeaturesBase != null) {
                dialogs.add(fromFeaturesBase);
            }
            Collections.sort(dialogs);
            this.dialogs.addAll(dialogs);

            if (getDialogEventListener() != null) {
                for (int i = 0; i < dialogs.size(); i++) {
                    getDialogEventListener().onDialogAdded(i, dialogs.get(i));
                }
            }

        }
    }

    public static DialogStoreManagement getInstance(Context context) {
        if (singleton == null) {
            synchronized (DialogStoreManagement.class) {
                if (singleton == null) {
                    singleton = new DialogStoreManagement(context);
                }
            }
        }
        return singleton;
    }

    public int size() {
        synchronized (dialogs)
        {
            return dialogs.size();
        }
    }

    public Dialog get(int index) {
        synchronized (dialogs)
        {
            return dialogs.get(index);
        }
    }

    public Dialog get(String destination) {
        synchronized (dialogs) {
            for (Dialog dialog : dialogs) {
                if (dialog.destination.equals(destination)) {
                    return dialog;
                }
            }
        }
        return null;
    }

    public void updateRequest(String destination) {
        synchronized (dialogs) {
            for (int i = 0; i < dialogs.size(); i++) {
                if (dialogs.get(i).destination.equals(destination)) {
                    if (getDialogEventListener() != null) {
                        getDialogEventListener().onDialogUpdated(i, dialogs.get(i));
                    }
                    break;
                }
            }
        }
    }

    public void forceForUpdate(String destination) {
        synchronized (dialogs) {
            Dialog dialog = find(destination);
            if (dialog != null) {
                dialog.lastMessage = MessageProvider.DialogUtils
                    .getLastMessage(mContext, destination);
                dialog.lastNotifier = NotifierProvider.Utils
                    .getLastNotifier(mContext,destination);
                if (getDialogEventListener() != null) {
                    getDialogEventListener().onDialogUpdated(dialogs.indexOf(dialog), dialog);
                }
            }
        }


    }

    public void onNewComponent(Component component) {
        synchronized (dialogs) {
            String relationship = component.getRelationship();
            Dialog dialog = find(relationship);
            if (dialog != null) {
                int indexOf = dialogs.indexOf(dialog);
                if (indexOf != -1) {
                    dialogs.remove(indexOf);
                    dialogs.add(0, dialog);
                    if (getDialogEventListener() != null) {
                        getDialogEventListener().onMoveFirst(indexOf);
                    }
                }
            } else {
                dialog = new Dialog(relationship);
                dialog.name = component.getRelationshipName(mContext);
                dialog.avatarPath = component.getRelationshipAvatar(mContext);
                dialogs.add(0, dialog);
                if (getDialogEventListener() != null) {
                    getDialogEventListener().onDialogAdded(0, dialog);
                }
            }

            int indexOf = dialogs.indexOf(dialog);

            if (component instanceof Message) {
                dialog.lastMessage = (Message) component;
                if (dialog.lastMessage.received()) {
                    dialog.nonSeenMessageLength++;
                }
                if (indexOf != -1) {
                    if (getDialogEventListener() != null) {
                        getDialogEventListener().onDialogUpdated(indexOf, dialog);
                    }
                }
            } else if (component instanceof Notifier) {
                Timber.d("onNewNotifierComponent");
                dialog.lastNotifier = (Notifier) component;
                if (indexOf != -1) {
                    if (getDialogEventListener() != null) {
                        getDialogEventListener().onDialogUpdated(indexOf, dialog);
                    }
                }
            }
        }
    }

    public void removeMessage(int id) {
        synchronized (dialogs) {
            int index = 0;
            for (Dialog dialog : dialogs) {
                index ++;
                if (dialog != null && dialog.lastMessage != null) {
                    if (dialog.lastMessage.getId() == id) {
                        dialog.lastMessage = null;
                        if (getDialogEventListener() != null) {
                            getDialogEventListener().onDialogUpdated(index, dialog);
                        }
                    }
                }
            }
        }

    }

    public void removeNotifier(int id) {
        synchronized (dialogs) {
            int index = 0;
            for (Dialog dialog : dialogs) {
                index = index ++;
                if (dialog != null && dialog.lastNotifier != null) {
                    if (dialog.lastNotifier.getId() == id) {
                        dialog.lastNotifier = null;
                        if (getDialogEventListener() != null) {
                            getDialogEventListener().onDialogUpdated(index, dialog);
                        }
                    }
                }
            }
        }
    }

    public void forceDialog(Dialog dialog) {
        Timber.d("forced dialog: %s", dialog.destination);
        synchronized (dialogs) {
            Dialog d = find(dialog.destination);
            if (d != null) {
                int indexOf = dialogs.indexOf(d);
                if (indexOf != -1) {
                    dialogs.remove(indexOf);
                    if (getDialogEventListener() != null) {
                        getDialogEventListener().onDialogRemoved(indexOf);
                    }
                }
            }
            dialogs.add(0, dialog);
            if (getDialogEventListener() != null) {
                getDialogEventListener().onDialogAdded(0, dialog);
            }
        }

    }

    public void onAck(Ack ack) {
        synchronized (dialogs) {
            Dialog dialog = find(ack.getRelationship());
            if (dialog != null) {
                if (dialog.lastMessage != null) {
                    if (ack.getState() == Ack.SEEN) {
                        dialog.nonSeenMessageLength--;
                        Timber.d("newNonSeenMessageLength: %d", dialog.nonSeenMessageLength);
                    }
                    if (ack.getMessageSid().equals(dialog.lastMessage.getSid())) {
                        ack.putToMessage(dialog.lastMessage);
                        int indexOf = dialogs.indexOf(dialog);
                        if (indexOf != -1) {
                            if (getDialogEventListener() != null) {
                                getDialogEventListener().onDialogUpdated(indexOf, dialog);
                            }
                        }
                    }
                }
            }
        }

    }

    public Dialog find(String r) {
        synchronized (dialogs) {
            for (Dialog d : dialogs) {
                if (d.destination.equals(r)) {
                    return d;
                }
            }
            return null;
        }
    }

    public void removeAssistant() {
        synchronized (dialogs) {
            int index = -1;
            for (int i = 0; i < dialogs.size(); i++) {
                Dialog d = dialogs.get(i);
                if (d.isAssistant()) {
                    index = i;
                    break;
                }
            }
            if (index != -1) {
                dialogs.remove(index);
                if (getDialogEventListener() != null) {
                    getDialogEventListener().onDialogRemoved(index);
                }
            }
        }

    }

    public void remove(Dialog d) {
        synchronized (this.dialogs) {
            int index = this.dialogs.indexOf(d);
            this.dialogs.remove(d);
            if (getDialogEventListener() != null) {
                getDialogEventListener().onDialogRemoved(index);
            }
        }
    }

    public void clear() {
        synchronized (dialogs) {
            dialogs.clear();
        }
    }

}