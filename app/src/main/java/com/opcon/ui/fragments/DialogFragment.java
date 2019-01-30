package com.opcon.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.opcon.R;
import com.opcon.components.Ack;
import com.opcon.components.Dialog;
import com.opcon.components.Feature;
import com.opcon.components.Message;
import com.opcon.database.FeatureBase;
import com.opcon.database.MessageProvider;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.NotifierEventSentUtils;
import com.opcon.libs.MessageDispatcher;
import com.opcon.notifier.components.Notifier;
import com.opcon.ui.activities.ChatActivity;
import com.opcon.ui.activities.ContactsActivity;
import com.opcon.ui.activities.FeatureActivity;
import com.opcon.ui.activities.ProfileActivity;
import com.opcon.ui.adapters.DialogAdapter;
import com.opcon.ui.management.DialogStoreManagement;

import java.util.List;

import timber.log.Timber;


/**
 *
 * Created by Mahmut Taşkiran on 28/11/2016.
 *
 */

public class DialogFragment extends Fragment implements
        MessageDispatcher.MessageEventListener {

    private static final int REQUEST_CONTACT = 1;

    private MessageDispatcher mMessageDispatcher = MessageDispatcher.getInstance();
    private View mRootView;
    private RecyclerView mRecyclerView;
    private DialogAdapter mDialogAdapter;
    private DialogStoreManagement mDialogStoreManagement;
    private RelativeLayout mNoDialog;

    private String mLastDestination;

    private DialogStoreManagement.DialogEventListener dialogEventHandler = new DialogStoreManagement.DialogEventListener() {
        @Override public void onDialogRemoved(final int index) {
            if (getActivity() != null && getContext() != null && mDialogAdapter != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialogAdapter.notifyItemRemoved(index);
                        visibilityOfNoDialogMessage();
                    }
                });
            }
        }
        @Override public void onDialogAdded(final int index, Dialog dialog) {
            if (getActivity() != null && getContext() != null && mDialogAdapter != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialogAdapter.notifyItemInserted(index);
                        visibilityOfNoDialogMessage();
                    }
                });
            }
        }
        @Override public void onDialogUpdated(final int index, Dialog dialog) {
            if (getActivity() != null && getContext() != null && mDialogAdapter != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialogAdapter.notifyItemChanged(index);
                    }
                });
            }
        }

        @Override public void onMoveFirst(final int index) {
            if (getActivity() != null && getContext() != null && mDialogAdapter != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mDialogAdapter.notifyItemMoved(index, 0);
                    }
                });
            }
        }
    };

    private ComponentListenerManager.ComponentListener mAckListener = new ComponentListenerManager.ComponentListener() {
        @Override
        public boolean onNewComponent(Object component) {
            DialogStoreManagement.getInstance(getContext()).onAck((Ack) component);
            return true;
        }
    };

    private ComponentListenerManager.ComponentListener mFeatureListener = new ComponentListenerManager.ComponentListener() {
        @Override public boolean onNewComponent(Object component) {

            getActivity().runOnUiThread(new Runnable() {
                @Override public void run() {
                    Dialog dialog = FeatureBase.getInstance(getContext())
                        .prepareDialog(getContext());
                    if (dialog != null) {
                        Timber.d("forced dialog: %s", dialog.destination);
                        mDialogStoreManagement.forceDialog(dialog);
                    }
                }
            });


            return false;
        }
    };



    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // listen global events for change mDialogStoreManagement content
        // at runtime.

        mDialogStoreManagement = DialogStoreManagement.getInstance(getContext());
        mMessageDispatcher.addMessageEventListener(this);
        mDialogStoreManagement.setDialogEventListener(dialogEventHandler);
        ComponentListenerManager.getInstance(getContext().getApplicationContext())
            .addComponentListener(Ack.class, mAckListener);
        ComponentListenerManager.getInstance(getContext().getApplicationContext()).addComponentListener(Feature.class, mFeatureListener);
        // for firstly, read message base and notifier base for detect
        // dialog objects. and add all to DialogStoreManagement.
    }




    @Nullable @Override public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment, container, false);
    }

    @Override public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.mRootView = view;
        initComponents();
        visibilityOfNoDialogMessage();
    }

    private DialogAdapter.DialogEventListener mDialogEventListener = new DialogAdapter.DialogEventListener() {
        @Override public void onDialogClick(Dialog dialog) {
            if (dialog.getIntent() != null ){
                startActivity(dialog.getIntent());
            } else {
                if (dialog.isNotifierBiggerThanMessage() && !(dialog.lastMessage != null && dialog.lastMessage.isWaiting())) {
                    ChatActivity.focusNotifier(getActivity(), dialog.destination, dialog.lastNotifier.getId());
                    mLastDestination = dialog.destination;
                } else {
                    ChatActivity.go(getContext(), dialog.destination);
                    mLastDestination = dialog.destination;
                }
            }
        }

        @Override
        public void onDialogAvatarClick(Dialog dialog) {
            if (dialog.isAssistant()) {
                startActivity(FeatureActivity.getIntent(getContext()));
            } else {
                ProfileActivity.profile(getContext(), dialog.destination);
                mLastDestination = dialog.destination;
            }
        }
        @Override public void onDialogLongClick(final Dialog d) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            mBuilder.setTitle(null)
                .setMessage(null)
                .setItems(getChoicesFor(d), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (d.isAssistant()){
                            FeatureBase.getInstance(getContext()).deleteAll();
                            mDialogStoreManagement.removeAssistant();
                        } else {
                            ProgressDialog show = ProgressDialog.show(getActivity(), null, getString(R.string.please_wait), true, false);
                            if (choiceIsDialog(d, which)) {
                                // delete messages.
                                d.lastMessage = null;
                                MessageProvider.Utils.deleteAllMessages(getContext(), d.destination);
                            } else {
                                d.lastNotifier = null;
                                List<Notifier> notifiers = NotifierProvider.Utils.getRelationalNotifiers(getContext(), d.destination);
                                for (Notifier notifier : notifiers) {
                                    NotifierProvider.Utils.delete(getContext(), notifier.getId());
                                    NotifierEventSentUtils.sendDeleted(notifier);
                                }

                            }
                            if (isRemovable(d)) {
                                mDialogStoreManagement.remove(d);
                            }
                            show.dismiss();
                        }

                        if (mDialogAdapter != null) {
                            mDialogAdapter.notifyDataSetChanged();
                        }

                    }
                }).show();
        }
    };

    CharSequence[] getChoicesFor(Dialog dialog) {
        if (dialog.isAssistant()) {
            return new CharSequence[] {getString(R.string.delete)};
        } else if (dialog.lastMessage != null && dialog.lastNotifier != null) {
            return new CharSequence[] {getString(R.string.remove_dialog), getString(R.string.remove_notifiers)};
        } else if (dialog.lastMessage == null && dialog.lastNotifier == null){
            return new CharSequence[] {getString(R.string.delete)};
        } else if (dialog.lastMessage != null) {
            return new CharSequence[] {getString(R.string.remove_dialog)};
        } else {
            return new CharSequence[] {getString(R.string.remove_notifiers)};
        }
    }

    boolean choiceIsDialog(Dialog dialog, int which) {
        if (dialog.lastMessage != null && dialog.lastNotifier != null) {
            return which == 0;
        } else return dialog.lastMessage != null;
    }

    boolean isRemovable(Dialog d) {
        String destination = d.destination;

        List<Notifier> rls = NotifierProvider.Utils.getRelationalNotifiers(getContext(), destination);
        List<Integer> messages = MessageProvider.Utils.getMessages(getContext(), destination);
        return ((rls == null || rls.isEmpty()) && (messages == null || messages.isEmpty()));
    }

    void initComponents() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.dialog_recyclerview);
        mNoDialog = (RelativeLayout) mRootView.findViewById(R.id.no_dialog);
        mNoDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptToNewDialog();
            }
        });
        mDialogAdapter = new DialogAdapter(mDialogEventListener);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mDialogAdapter);
    }

    @Override
    public void onDestroy() {
        // remove global event listeners.
        super.onDestroy();
        mMessageDispatcher.removeMessageEventListener(this);
        mDialogStoreManagement.setDialogEventListener(null);

        ComponentListenerManager.getInstance(getContext().getApplicationContext()).removeComponentListener(Ack.class, mAckListener);
        ComponentListenerManager.getInstance(getContext().getApplicationContext()).removeComponentListener(Feature.class, mFeatureListener);
        if (mRecyclerView != null) {
          mRecyclerView.setAdapter(null);
        }
        mDialogAdapter = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CONTACT && resultCode == Activity.RESULT_OK) {
            String number = data.getExtras().getString(ContactsActivity.SELECTED_CONTACT_NUMBER);
            ChatActivity.go(getContext(), number);
            mLastDestination = number;
        }
    }

    public void attemptToNewDialog() {
        if (getActivity() == null)
            return;
        Intent intent = new Intent(getActivity().getApplicationContext(), ContactsActivity.class);
        intent.putExtra(ContactsActivity.ONLY_USER, true);
        startActivityForResult(intent, REQUEST_CONTACT);
    }

    @Override
    public void onNewMessage(final Message message, boolean received) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDialogStoreManagement.onNewComponent(message);
            }
        });
    }

    @Override
    public void detectedDoesNotExists(int id) {
        mDialogStoreManagement.removeMessage(id);
    }

    @Override
    public void onResume() {
        super.onResume();
        visibilityOfNoDialogMessage();

        if (mLastDestination != null) {
            mDialogStoreManagement.forceForUpdate(mLastDestination);
            mLastDestination = null;
        }

    }

    private void visibilityOfNoDialogMessage() {
        if (mNoDialog!=null)
            if (mDialogStoreManagement.isEmpty())
                mNoDialog.setVisibility(View.VISIBLE);
             else
                mNoDialog.setVisibility(View.GONE);
    }

}
