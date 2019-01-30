package com.opcon.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.animation.Animation;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.components.Helper;
import com.opcon.components.NotifierLog;
import com.opcon.database.ContactBase;
import com.opcon.database.NotifierLogBase;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.Analytics;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.firebaseclient.NotifierEventSentUtils;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.permission.PermissionRequest;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.libs.permission.NotifierPermissionDetective;
import com.opcon.libs.utils.AnimationUtils;
import com.opcon.notifier.NotifierEventDispatcher;
import com.opcon.notifier.NotifierLogEventDispatcher;
import com.opcon.notifier.components.Condition;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.environment.EnvironmentManager;
import com.opcon.notifier.environment.triggers.BatteryEventReceiver;
import com.opcon.ui.activities.ChatActivity;
import com.opcon.ui.activities.ContactsActivity;
import com.opcon.ui.activities.NotifierBuilderActivity;
import com.opcon.ui.activities.NotifierLogActivity;
import com.opcon.ui.adapters.NotifierAdapter;
import com.opcon.ui.dialogs.NotifierChoiceDialog;
import com.opcon.ui.dialogs.NotifierPermissionRationaleAlertDialog;
import com.opcon.ui.management.DialogStoreManagement;
import com.opcon.ui.views.HelperHolder;

import java.util.ArrayList;

import timber.log.Timber;


/**
 *
 * Created by Mahmut Taşkiran on 05/12/2016.
 */

public class NotifierFragment extends Fragment implements
    NotifierAdapter.NotifierViewEventHandler, View.OnClickListener {

    private static final int NOTIFIER_CREATION_REQUEST = 1000;
    private static final int REQUEST_NOTIFIER_EDIT = 1;
    private static final int NOTIFIER_CREATION_REQUEST_WITH_SELECTION_CONTACT = 1001;

    private boolean EXTERNAL_PERMISSION_REQUEST = false;
    private int EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID = -1;


    private RecyclerView verticalRecycler;

    private ViewStub viewStup;
    private ArrayList<Object> notifierIds;
    private NotifierAdapter notifierAdapter;

    private LinearLayoutManager mLayoutManager;

    private TextView mToUnderstandOpcon;
    private CardView mBigButtonToAddingNotifier;

    private String mDestination;
    private PermissionManagement permissionManagement;
    private View inflatedNoItemView;

    private ComponentListenerManager mComponentListenerManager;
    
    private NotifierCache mNotifierCache;


    private ComponentListenerManager.ComponentListener mNotifierLogListener =
        new ComponentListenerManager.ComponentListener() {
            @Override
            public boolean onNewComponent(Object component) {
                NotifierLog log = (NotifierLog) component;
                if (log.getSender() != null && log.getSender().equals(mDestination)) {
                    com.opcon.notifier.components.Notifier notifier = NotifierProvider.Utils.get(getContext(), log.getNotifierSid());
                    if (notifier != null) {
                        int id = notifier.getId();
                        updateNotifier(id);
                    }
                }
                return true;
            }
        };

    private NotifierChoiceDialog.OnChoiceListener onChoiceListener = new NotifierChoiceDialog.OnChoiceListener() {
        @Override
        public void onStop(int LID) {
            // update base.
            // dispatchEvent event.
            NotifierProvider.Utils.updateSingleInt(getContext(), LID, NotifierProvider.STATE, com.opcon.notifier.components.Notifier.STOPPED);
            NotifierEventSentUtils.send(getContext(), LID, NotifierLog.STOPPED);
            NotifierEventDispatcher.getInstance().dispatchStateChanged(LID, com.opcon.notifier.components.Notifier.STOPPED);
        }

        @Override
        public void onStart(final int LID) {
            // forThat permission.
            // is true:
            // {loop_1}
            // update cache.
            // update base.
            // forThat.
            // is false:
            // permissionRequest
            // return is true. goto loop1.
            // is false nothing.
            tryToStart(LID);
        }



        @Override
        public void onDelete(int LID) {
            // update cache element.
            // update base.
            // forThat.
            com.opcon.notifier.components.Notifier notifier = NotifierProvider.Utils.get(getContext(), LID);
            if (notifier != null) {
                NotifierEventSentUtils.sendDeleted(notifier);
                if (notifier.getCondition().getId() == Conditions._TIMELY) {
                    EnvironmentManager.init().removeTimelyNotifierEnvironment(getContext(), LID);
                } else if (notifier.getCondition().getId() == Conditions._LOCATION) {
                    EnvironmentManager.init().removeLocationalNotifierEnvironment(getContext(), LID);
                }
                NotifierProvider.Utils.delete(getContext(), LID);
                NotifierEventDispatcher.getInstance().dispatchDelete(LID);
                DialogStoreManagement.getInstance(getContext()).removeNotifier(LID);
            }
        }

        @Override public void onShowLogs(int LID) {
            NotifierLogActivity.showLogs(getContext(), LID, mDestination);
        }

        @Override
        public void onShare(int LID) {
            updateNotifier(LID);
        }

        @Override
        public void onEdit(int LID) {
            NotifierBuilderActivity.editForResult(NotifierFragment.this, LID, REQUEST_NOTIFIER_EDIT);
        }

        @Override
        public void onSeeDetails(int LID) {
            NotifierBuilderActivity.seen(getActivity(), LID);
        }
    };

    private void tryToStart(final int LID) {

        if (checkNotifierPermission(getContext(), LID)) {

            // database update
            // adapter update
            // dispatchEvent global

            NotifierEventSentUtils.send(getContext(), LID, NotifierLog.RERUN);

            NotifierProvider.Utils.updateSingleInt(getContext(), LID, NotifierProvider.STATE, com.opcon.notifier.components.Notifier.RUNNING);
            NotifierEventDispatcher.getInstance().dispatchStateChanged(LID, com.opcon.notifier.components.Notifier.RUNNING);

            startup(LID);


        } else {

            final String[] permissions = getNotifierPermissions(getContext(), LID);

            NotifierPermissionRationaleAlertDialog notifierPermissionRationaleAlertDialog =
                new NotifierPermissionRationaleAlertDialog(getContext(), LID, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {

                            permissionManagement.builtRequest(LID, permissions)
                                .observer(new PermissionManagement.PermissionEventListener() {
                                    @Override public void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest) {
                                        // update database
                                        // update adapter
                                        // dispatchEvent global event listeners
                                        NotifierProvider.Utils.updateSingleInt(getContext(), requestCode, NotifierProvider.STATE, com.opcon.notifier.components.Notifier.RUNNING);

                                        NotifierEventSentUtils.send(getContext(), LID, NotifierLog.RERUN);
                                        NotifierEventDispatcher.getInstance().dispatchStateChanged(requestCode, com.opcon.notifier.components.Notifier.RUNNING);
                                        startup(LID);
                                    }
                                    @Override public void onAnyPermissionsDenied(final int requestCode, PermissionRequest permissionRequest) {
                                        if (permissionRequest.getPersistentlyDeniedPermissions() != null ) {
                                            Snackbar make = Snackbar.make(verticalRecycler, R.string.go_to_settings_for_notifier_permission, 3000);
                                            make.setAction(R.string.permission_to_settings, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    EXTERNAL_PERMISSION_REQUEST = true;
                                                    EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID = requestCode;

                                                    PermissionManagement.showAppSettingsPageFor(getActivity().getApplicationContext());
                                                }
                                            });
                                            make.show();
                                        }
                                    }
                                }).request();
                        }
                    }
                });
            notifierPermissionRationaleAlertDialog.show();
        }
    }

    void startup(int id) {
        Notifier notifier = NotifierCache.getInstance().getNotificator(getContext(), id);
        Analytics.instance(getContext()).accepted(notifier);
        if (notifier != null) {
            if (Conditions.isBattery(notifier.getCondition().getId())) {
                BatteryEventReceiver.check(getContext().getApplicationContext());
            }
        }
    }

    private NotifierEventDispatcher.NotifierEventListener notifierEventListener =
            new NotifierEventDispatcher.NotifierEventListener() {
        @Override public void onStateChanged(int lid, int state) {

            if (state != com.opcon.notifier.components.Notifier.RUNNING) {
                com.opcon.notifier.components.Notifier notifier = NotifierProvider.Utils.get(getContext(), lid);
                if (Conditions.isTimely(notifier.getCondition().getId())) {
                    EnvironmentManager.init().removeTimelyNotifierEnvironment(getContext(), lid);
                } else if (Conditions.isLocational(notifier.getCondition().getId())) {
                    EnvironmentManager.init().removeLocationalNotifierEnvironment(getContext(), lid);
                }
            }

            mNotifierCache.remove(lid);
            notifierAdapter.notifyItemChanged(findAdapterPosition(lid));
            EnvironmentManager.init().builtEnvironment(getContext());
        }

        @Override public void onDeleted(int lid) {
            notifierAdapter.notifyItemRemoved(findAdapterPosition(lid));
            removeFromIds(lid);
            mNotifierCache.remove(lid);
            decideVisibilityOfNoItem();
            EnvironmentManager.init().builtEnvironment(getContext());
        }

        @Override public void onAdded(int lid) {
            com.opcon.notifier.components.Notifier notifierComponent = NotifierProvider.Utils.get(getContext(), lid);

            NotifierCache.getInstance().put(lid, notifierComponent);
            if (isValidForFilter(notifierComponent) && !notifierIds.contains(Integer.valueOf(lid))) {
                notifierIds.add(lid);
                notifierAdapter.notifyItemInserted(notifierIds.size() -1);
                decideVisibilityOfNoItem();
            }
            EnvironmentManager.init().builtEnvironment(getContext());

            if (notifierComponent!=null) {
                try {
                    ChatActivity activity = (ChatActivity) getActivity();
                    if (activity != null) {
                        activity.focusNotifier(lid, notifierComponent.isOwnerAmI());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            initHelpers();

        }

        @Override public void onEdited(int lid) {
            com.opcon.notifier.components.Notifier notifierComponent = NotifierProvider.Utils.get(getContext(), lid);
            NotifierCache.getInstance().remove(lid);
            NotifierCache.getInstance().put(lid, notifierComponent);
            notifierAdapter.notifyItemChanged(findAdapterPosition(lid));
            EnvironmentManager.init().builtEnvironment(getContext());
        }


            };

    private int findAdapterPosition(int lid) {
        for (int i = 0; i < notifierIds.size(); i++) {
            if (notifierIds.get(i) instanceof Integer && ((Integer) notifierIds.get(i)) == lid) {
                return i;
            }
        }
        return 0;
    }

    private NotifierLogEventDispatcher.NotifierLogEventListener notifierLogEventListener =
        new NotifierLogEventDispatcher.NotifierLogEventListener() {
        @Override public void onNewNotifierLog(NotifierLog notifierLog, boolean receivedLog) {
            NotifierLogBase.Utils.newLog(getContext(), notifierLog);
        }

        @Override public void onDeletedLogs(int id) {
            mNotifierCache.getNotificator(getContext(), id).setNonSeenNotificationLength(0);
            notifierAdapter.notifyDataSetChanged();
        }
    };

    private boolean isValidForFilter(com.opcon.notifier.components.Notifier notifier) {
        return mDestination == null ||
                (notifier.getRelationship().equals(mDestination));
    }

    @Override public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mDestination = savedInstanceState.getString("target");

        }
        mComponentListenerManager = ComponentListenerManager.getInstance(getContext().getApplicationContext());
        mComponentListenerManager.addComponentListener(NotifierLog.class, mNotifierLogListener);
    }

    public static boolean checkNotifierPermission(Context context, int id) {
        return PermissionUtils.check(context, new PermissionRequest(getNotifierPermissions(context, id)));
    }

    public static String[] getNotifierPermissions(Context context, int notifierId) {
      String[] detect = NotifierPermissionDetective.detect(NotifierProvider.Utils.get(context, notifierId));
      // fix OP-00017
      return onlyDeniedPermissions(context, detect);
    }

    static String[] onlyDeniedPermissions(Context context, String[] permissions) {
      ArrayList<String> denied_permissions = new ArrayList<>();
      if (permissions != null) {
        for (String permission : permissions) {
          if (!PermissionUtils.check(context, permission)) {
            denied_permissions.add(permission);
          }
        }
      }
      return denied_permissions.toArray(new String[0]);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("target", mDestination);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        NotifierEventDispatcher.getInstance().addEventListener(notifierEventListener);
        NotifierLogEventDispatcher.getInstance().addEventListener(notifierLogEventListener);
        mNotifierCache = NotifierCache.getInstance();
        return inflater.inflate(R.layout.notifier_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponents(view);

        if (notifierIds == null || notifierIds.isEmpty()) {
            notifierIds = NotifierProvider.Utils.getNotifiers(getContext(), PresenceManager.uid(), mDestination);
        }


        if (notifierIds != null && !notifierIds.isEmpty()) {
            initHelpers();
        }

        this.notifierAdapter = new NotifierAdapter(notifierIds, null, this);



        setupComponents();
        if (permissionManagement == null) {
            permissionManagement = PermissionManagement.with(this);
        }
        decideVisibilityOfNoItem();
    }

    void forgetAll() {
        forget(R.string.helper_no_time_restrict);
        forget(R.string.helper_notifier_why_him);
        forget(R.string.helper_notifier_why_me);
        forget(R.string.helper_packet_details_and_default_behavior);
        forget(R.string.helper_there_is_both_restrict);
        forget(R.string.helper_what_about_swiping);
        forget(R.string.helper_there_is_time_range_restrict);
        forget(R.string.helper_there_is_discomfort);
        forget(R.string.helper_there_is_date_restrict);
        forget(R.string.what_is_profile_updater);
        forget(R.string.what_is_notifier);
    }

    void initHelpers() {

        Helper swipeHelper = helper_swipeBetweenChatAndNotifiers();
        if (dontKnows(R.string.helper_what_about_swiping) && !notifierIds.contains(swipeHelper)) {
            addHelperAt(swipeHelper, 0);
        }


        int to = notifierIds.size();

        for (int i = 0; i < to; i++) {
            if (notifierIds.get(i) instanceof Integer) {
                Helper helper = getRelationHelper(NotifierCache.getInstance().getNotificator(getContext(), (Integer) notifierIds.get(i)));
                if (helper != null && dontKnows(helper.idenify) && !notifierIds.contains(helper)) {
                    addHelperAt(helper, i+1);
                    to++;
                }
            }
        }

        if (notifierAdapter != null) {
            notifierAdapter.notifyDataSetChanged();
        }


    }

    void forget(int id) {
        HelperHolder.forget(getContext(), id);
    }

    @Nullable Helper getRelationHelper(Notifier notifier) {
        Condition condition = notifier.getCondition();
        if (notifier.isTargetAmI()) {
            return helper_packetsDetailsAndDefaultBehavior();
        } else if (notifier.bothProgressable()) {
            return helper_packetsWhyAllIconsIsMe();
        } else if (!notifier.anyProgressable()) {
            return helper_packetsWhyAllIconsIsHe();
        } else if (condition.getTimeRangeRestrictParams() == null && condition.getDateRestrictParams() == null) {
            return helper_thereIsNoTimeRestrict();
        } else if (condition.getTimeRangeRestrictParams() != null &&  condition.getDateRestrictParams() != null) {
            return helper_thereIsBothRestrict(condition);
        } else if (condition.getTimeRangeRestrictParams() != null) {
            return helper_thereIsTimeRangeRestict(condition);
        } else if (condition.getDateRestrictParams() != null)  {
            return helper_thereIsDateRestrict();
        }
        return null;
    }

    void addHelperAt(Helper helper, int at) {
        notifierIds.add(at, helper);
    }

    public boolean dontKnows(@StringRes int res) {
        return !HelperHolder.isGotIt(getContext(), res);
    }

    public boolean dontKnows(String res) {
        return !HelperHolder.isGotIt(getContext(), res);
    }

    Helper helper_swipeBetweenChatAndNotifiers() {
        return Helper.newBuilder(getContext(), R.string.helper_what_about_swiping)
            .setMessage(R.string.helper_what_about_swiping_explantation)
            .setTopIconColor(R.color.colorSecondaryDark)
            .setTopColor(R.color.colorSecondary)
            .setDividerResourceId(R.drawable.linear_secondary)
            .built();
    }

    Helper helper_packetsDetailsAndDefaultBehavior() {
        return Helper.newBuilder(getContext(), R.string.helper_packet_details_and_default_behavior)
            .setMessageAsHtml(R.string.helper_packet_details_and_default_behavior_answer)
            .setTopIconColor(R.color.colorPrimary)
            .setTopColor(R.color.colorPrimaryDark)
            .setDividerResourceId(R.drawable.linear)
            .built();
    }

    Helper helper_packetsWhyAllIconsIsMe() {
        return Helper.newBuilder(getContext(), R.string.helper_notifier_why_me)
            .setMessage(R.string.helper_notifier_why_me_answer)
            .setTopColor(R.color.colorSecondary)
            .setTopIconColor(R.color.colorSecondaryDark)
            .setDividerResourceId(R.drawable.linear_secondary)
            .built();
    }

    Helper helper_thereIsNoTimeRestrict() {
        return Helper.newBuilder(getContext(), R.string.helper_no_time_restrict)
            .setMessage(R.string.helper_no_time_restrict_answer)
            .setTopColor(R.color.colorPrimary)
            .setTopIconColor(R.color.colorPrimaryDark)
            .setDividerResourceId(R.drawable.linear)
            .built();
    }

    Helper helper_packetsWhyAllIconsIsHe() {
        return Helper.newBuilder(getContext(), R.string.helper_notifier_why_him)
            .setMessage(R.string.helper_notifier_why_him_answer)
            .setTopColor(R.color.colorSecondary)
            .setTopIconColor(R.color.colorSecondaryDark)
            .setDividerResourceId(R.drawable.linear_secondary)
            .built();
    }

    Helper helper_thereIsTimeRangeRestict(Condition condition) {
        String from = condition.getFromAsString(), to = condition.getToAsString();
        return Helper.newBuilder(getContext(), R.string.helper_there_is_time_range_restrict)
            .setMessage(String.format(getString(R.string.helper_there_is_time_range_restrict_answer), from, to))
            .setTopColor(R.color.colorPrimary)
            .setTopIconColor(R.color.colorPrimaryDark)
            .setDividerResourceId(R.drawable.linear)
            .built();
    }

    Helper helper_thereIsDateRestrict() {
        return Helper.newBuilder(getContext(), R.string.helper_there_is_date_restrict)
            .setMessage(R.string.helper_there_is_date_restrict_answer)
            .setTopColor(R.color.colorSecondary)
            .setTopIconColor(R.color.colorSecondaryDark)
            .setDividerResourceId(R.drawable.linear_secondary)
            .built();
    }

    Helper helper_thereIsBothRestrict(Condition condition) {
        String date, from, to;
        date = condition.getDateAsString();
        from = condition.getFromAsString();
        to = condition.getToAsString();
        return Helper.newBuilder(getContext(), R.string.helper_there_is_both_restrict)
            .setMessage(String.format(getString(R.string.helper_there_is_both_restrict_answer), date, from, to))
            .setTopColor(R.color.colorSecondary)
            .setTopIconColor(R.color.colorSecondaryDark)
            .setDividerResourceId(R.drawable.linear_secondary)
            .built();
    }

    private void decideVisibilityOfNoItem() {
        if (notifierIds == null || notifierIds.isEmpty()) {
            if (viewStup != null) {
                inflatedNoItemView = viewStup.inflate();
                mBigButtonToAddingNotifier = (CardView) inflatedNoItemView.findViewById(R.id.add_notifier_big);
                mToUnderstandOpcon = (TextView) inflatedNoItemView.findViewById(R.id.to_understand_Opcon);
                mBigButtonToAddingNotifier.setOnClickListener(this);

                Animation firstAnimation = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.heart);
                mBigButtonToAddingNotifier.startAnimation(firstAnimation);

                mToUnderstandOpcon.setOnClickListener(this);
            }
            else
                if (inflatedNoItemView != null)
                    inflatedNoItemView.setVisibility(View.VISIBLE);
            viewStup = null;
        } else {
            if (inflatedNoItemView != null) {
                inflatedNoItemView.setVisibility(View.GONE);
            }
        }
    }

    private void setupComponents() {
        mLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        verticalRecycler.setLayoutManager(mLayoutManager);
        verticalRecycler.setHasFixedSize(true);
        verticalRecycler.setAdapter(notifierAdapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        this.permissionManagement.dispatchEvent(requestCode, grantResults);
    }

    public void focusNotifier(final int notifierId, boolean activateRequest) {
        if (notifierIds != null) {
            int indexOfNotifier = notifierIds.indexOf(notifierId);
            if (indexOfNotifier != -1) {
                final View childAt = mLayoutManager.getChildAt(indexOfNotifier);
                if (childAt != null) {

                    verticalRecycler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (childAt != null) {

                                ViewCompat.animate(childAt)
                                    .scaleX(1.2f)
                                    .scaleY(1.2f)
                                    .alpha(0)
                                    .setDuration(250)
                                    .withEndAction(new Runnable() {
                                        @Override
                                        public void run() {
                                            ViewCompat.animate(childAt)
                                                .scaleY(1f)
                                                .scaleX(1f)
                                                .alpha(1)
                                                .setDuration(250)
                                                .start();
                                        }
                                    }).start();
                            }

                        }
                    }, 200);

                }
                mLayoutManager.scrollToPosition(indexOfNotifier);
                notifierAdapter.notifyItemChanged(indexOfNotifier);
            }
        }

        if (activateRequest && verticalRecycler != null) {
            verticalRecycler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (verticalRecycler != null)
                        tryToStart(notifierId);
                }
            }, 100);
        }

    }


    private void initComponents(View view) {
        this.verticalRecycler = (RecyclerView) view.findViewById(R.id.notifierFragment);
        this.viewStup = (ViewStub) view.findViewById(R.id.viewStup);
    }


    public void setTargetFilter(String targetFilter) {
        this.mDestination = targetFilter;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (EXTERNAL_PERMISSION_REQUEST && EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID != -1) {
            if (checkNotifierPermission(getContext(), EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID)) {
                NotifierProvider.Utils.updateSingleInt(getContext(), EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID, NotifierProvider.STATE, com.opcon.notifier.components.Notifier.RUNNING);
                mNotifierCache.get(EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID).setState(com.opcon.notifier.components.Notifier.RUNNING);

                notifierAdapter.notifyDataSetChanged();
                Snackbar.make(verticalRecycler, R.string.snack_notifier_activated, 2000).show();
            }
            EXTERNAL_PERMISSION_REQUEST = false;
            EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID = -1;
        }
    }

    public void attemptToCreateNotifier() {
        NotifierBuilderActivity.buildForResult(this, mDestination, NOTIFIER_CREATION_REQUEST);
    }

    @Override
    public void onNotificationClick(int id) {
        NotifierLogActivity.showLogs(getActivity(), id, mDestination);
    }

    @Override
    public void onAvatarClick(int id) {

    }

    @Override
    public void onShowOptions(int adapterPosition, int id) {
        NotifierChoiceDialog notifierChoiceDialog = new NotifierChoiceDialog(getContext(), id, onChoiceListener);
        notifierChoiceDialog.show();
    }

    @Override
    public void onDestroy() {
        NotifierEventDispatcher.getInstance().removeEventListener(notifierEventListener);
        NotifierLogEventDispatcher.getInstance().removeListener(notifierLogEventListener);
        mComponentListenerManager.removeComponentListener(NotifierLog.class, mNotifierLogListener);
        if (verticalRecycler != null)
            verticalRecycler.setAdapter(null);
        permissionManagement = null;
        notifierAdapter = null;
        super.onDestroy();
    }

    void removeFromIds(int id) {
        synchronized (this) {
            if (notifierIds != null){
                for (int i = 0; i < notifierIds.size(); i++) {
                    if (notifierIds.get(i) instanceof Integer) {
                        if (((Integer) notifierIds.get(i)) == id) {
                            notifierIds.remove(i);
                            break;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_NOTIFIER_EDIT && resultCode == Activity.RESULT_OK) {
            int notifierId = data.getIntExtra(NotifierBuilderActivity.NOTIFIER_ID, -1);
            if (notifierId != -1) {
                NotifierEventSentUtils.send(getContext(), notifierId, NotifierLog.EDITED);
                NotifierEventDispatcher.getInstance().dispatchUpdate(notifierId);
            }
        } else if (requestCode == NOTIFIER_CREATION_REQUEST && resultCode == Activity.RESULT_OK) {
            int notifierId = data.getIntExtra(NotifierBuilderActivity.NOTIFIER_ID, -1);
            if (notifierId != -1) {
                NotifierEventDispatcher.getInstance().dispatchAdded(notifierId);
            }
        } else if (requestCode == NOTIFIER_CREATION_REQUEST_WITH_SELECTION_CONTACT && resultCode == Activity.RESULT_OK) {
            String internationalNumber = data.getExtras().getString(ContactsActivity.SELECTED_CONTACT_NUMBER);
            if (internationalNumber != null) {
                NotifierBuilderActivity.buildForResult(this, internationalNumber, NOTIFIER_CREATION_REQUEST);
            }
        }
    }


    public void updateNotifier(int id) {
        if (notifierIds != null && !notifierIds.isEmpty()) {
            int index = notifierIds.indexOf(id);
            NotifierCache.getInstance().remove(id);
            notifierAdapter.notifyItemChanged(index);
        }
    }

    public static class NotifierCache extends LruCache<Integer, com.opcon.notifier.components.Notifier> {
        private static int MAX_SIZE_OF_CACHE = (500 * 1024);
        private volatile static NotifierCache singleton;

        public static NotifierCache getInstance() {
            if (singleton == null) {
                synchronized (ContactBase.class) {
                    if (singleton == null) {
                        singleton = new NotifierCache(MAX_SIZE_OF_CACHE);
                    }
                }
            }
            return singleton;
        }

        private NotifierCache(int maxSize) {
            super(maxSize);
        }

        @Override
        protected int sizeOf(Integer key, com.opcon.notifier.components.Notifier value) {
            return super.sizeOf(key, value);
        }

        public com.opcon.notifier.components.Notifier getNotificator(Context context, int id) {
            com.opcon.notifier.components.Notifier fromCache = get(id);
            if (fromCache == null) {
                fromCache = NotifierProvider.Utils.get(context, id);
                if (fromCache != null) {
                    put(id, fromCache);
                }
            }
            return fromCache;
        }

    }

    @Override
    public void onClick(View v) {
        if (v == mBigButtonToAddingNotifier) {
            AnimationUtils.scaleDownScaleUp(mBigButtonToAddingNotifier, 0.5f, 1f, 150, 100);
            mBigButtonToAddingNotifier.postDelayed(new Runnable() {
                @Override
                public void run() {
                    attemptToCreateNotifier();
                }
            }, 200);
        } else if (v == mToUnderstandOpcon) {
            Snackbar.make(verticalRecycler, "Not implemented yet! :)", Snackbar.LENGTH_LONG).show();
        }
    }
}
