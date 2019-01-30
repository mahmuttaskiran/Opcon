package com.opcon.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.database.NotifierProvider;
import com.opcon.libs.permission.NotifierPermissionDetective;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.permission.PermissionRequest;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.notifier.NotifierEventDispatcher;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.environment.EnvironmentManager;
import com.opcon.ui.dialogs.NotifierChoiceDialog;
import com.opcon.ui.dialogs.NotifierPermissionRationaleAlertDialog;
import com.opcon.ui.views.HelperHolder;
import com.opcon.ui.views.NotifierView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;

public class ProfileUpdatersActivity extends AppCompatActivity {

  private static final String FOCUS = "focus";
  private static final int EDIT_REQUEST = 1112;

  private boolean EXTERNAL_PERMISSION_REQUEST;
  private int EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID;

  HelperHolder mUnderstandHolder;


  @BindView(R.id.recyclerView)
  RecyclerView mRecyclerView;

  @BindView(R.id.roott)
  RelativeLayout mRoot;

  @BindView(R.id.there_is_no_profile_updater_indicator)
  TextView mNoProfileUpdaterIndicator;

  private List<Notifier> mProfileUpdaters;

  private NotifierAdapter mAdapter;
  private PermissionManagement mPermissionManagement;
  private LinearLayoutManager mLLManager;

  private NotifierEventDispatcher.NotifierEventListener mNotifierEventListener = new NotifierEventDispatcher.NotifierEventListener() {
    @Override
    public void onStateChanged(int LID, int state) {
    }

    @Override
    public void onDeleted(int LID) {
      if (mProfileUpdaters != null) {
        Notifier r = null;
        for (Notifier mProfileUpdater : mProfileUpdaters) {
          if (mProfileUpdater.getId() == LID) {
            r = mProfileUpdater;
            break;
          }
        }
        if (r != null) {
          int index = mProfileUpdaters.indexOf(r);
          mProfileUpdaters.remove(r);
          if (mAdapter != null && index != -1) {
            mAdapter.notifyItemRemoved(index);
          }
        }
      }
    }

    @Override
    public void onAdded(int LID) {
    }

    @Override
    public void onEdited(int LID) {
    }
  };


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_profile_updaters);
    ButterKnife.bind(this);
    setTitle(R.string.profile_updater_activity_title);

    NotifierEventDispatcher.getInstance().addEventListener(mNotifierEventListener);

    mPermissionManagement = PermissionManagement.with(this);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
    }

    mUnderstandHolder = new HelperHolder(findViewById(android.R.id.content));
    mUnderstandHolder.newBuilder()
        .setNegativeButton(R.string.understand, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            HelperHolder.gotIt(getApplicationContext(), R.string.what_is_profile_updater);
            mUnderstandHolder.gone();
            visibilityOfNonUpdaters();
          }
        }).setPositiveButton(R.string.add_an_profile_updater, new View.OnClickListener() {
        @Override public void onClick(View v) {
          addNewProfileUpdaterTrigger();
        }
    }).setTitle(R.string.what_is_profile_updater)
        .setMessage(R.string.what_is_profile_updater_answer);

    mProfileUpdaters = NotifierProvider.Utils.getProfileUpdaters(getBaseContext());
    mLLManager = new LinearLayoutManager(getBaseContext());
    mRecyclerView.setLayoutManager(mLLManager);
    mAdapter = new NotifierAdapter(this);
    mRecyclerView.setAdapter(mAdapter);

    visibilityOfNonUpdaters();

    if (getIntent() != null && getIntent().getExtras() != null) {
      int id = getIntent().getExtras().getInt(FOCUS, -1);
      if (id != -1) {
        focusNotifier(id, 450);
      }

    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      super.onBackPressed();
    } else if (item.getItemId() == 1112) {
      addNewProfileUpdaterTrigger();
    }
    return super.onOptionsItemSelected(item);
  }

  private void addNewProfileUpdaterTrigger() {
    NotifierBuilderActivity.profileUpdaterBuilder(this, 1);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == 1 && resultCode == RESULT_OK) {
      int notifierId = data.getIntExtra(NotifierBuilderActivity.NOTIFIER_ID, -1);
      if (notifierId != -1) {
        if (!NotifierBuilderActivity.isAlreadyExists(data)) {
          addNotifier(notifierId);
        }
        focusNotifier(notifierId, 200);
      }
    } else if (requestCode == EDIT_REQUEST && resultCode == RESULT_OK) {
      int notifierId = data.getIntExtra(NotifierBuilderActivity.NOTIFIER_ID, -1);
      if (notifierId != -1) {
        updateInList(notifierId);
        updateInAdapter(notifierId);
        focusNotifier(notifierId, 200);
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem add = menu.add(0, 1112, 0, R.string.add_an_profile_updater);
    add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    add.setIcon(R.drawable.ic_heart_no_outline);
    return super.onCreateOptionsMenu(menu);
  }

  private void visibilityOfNonUpdaters() {

    if (mProfileUpdaters.isEmpty()) {

      if (!HelperHolder.isGotIt(this, R.string.what_is_profile_updater)) {

        mUnderstandHolder.show();
        mNoProfileUpdaterIndicator.setVisibility(View.GONE);
      } else {

        mNoProfileUpdaterIndicator.setVisibility(View.VISIBLE);
        mUnderstandHolder.gone();
      }

    } else {

      mUnderstandHolder.gone();
      mNoProfileUpdaterIndicator.setVisibility(View.GONE);

    }
  }

  public static void go(Context ac) {
    ac.startActivity(new Intent(ac, ProfileUpdatersActivity.class));
  }

  public static void go(Context context, int id) {
    Intent in = new Intent(context, ProfileUpdatersActivity.class);
    in.putExtra(FOCUS, id);
    context.startActivity(in);
  }

  Notifier findNotifier(int id) {
    for (int i = 0; i < mProfileUpdaters.size(); i++) {
      if (id == mProfileUpdaters.get(i).getId()) {
        return mProfileUpdaters.get(i);
      }
    }
    return null;
  }

  void updateInAdapter(int id) {
    if (mAdapter != null && mProfileUpdaters != null) {
      int index = -1;
      for (int i = 0; i < mProfileUpdaters.size(); i++) {
        if (id == mProfileUpdaters.get(i).getId()) {
          index = i;
          break;
        }
      }
      if (index != -1) {
        mAdapter.notifyItemChanged(index);
      }
    }
  }

  void updateInList(int id) {
    if (mProfileUpdaters != null) {
      int index = -1;
      for (int i = 0; i < mProfileUpdaters.size(); i++) {
        if (id == mProfileUpdaters.get(i).getId()) {
          index = i;
          break;
        }
      }
      if (index != -1) {
        mProfileUpdaters.remove(index);
        Notifier notifier = NotifierProvider.Utils.get(getApplicationContext(), id);
        if (notifier != null) {
          mProfileUpdaters.add(index, notifier);
        }
      }
    }
  }

  void deleteOnAdapter(int id) {
    if (mAdapter != null && mProfileUpdaters != null) {
      int index = -1;
      for (int i = 0; i < mProfileUpdaters.size(); i++) {
        if (id == mProfileUpdaters.get(i).getId()) {
          index = i;
          break;
        }
      }
      if (index != -1) {
        mProfileUpdaters.remove(index);
        mAdapter.notifyItemRemoved(index);
      }
    }
    visibilityOfNonUpdaters();
  }

  void addNotifier(int id) {
    Notifier r = NotifierProvider.Utils.get(getApplicationContext(), id);
    if (r != null) {
      mProfileUpdaters.add(r);
      if (mAdapter != null) {
        mAdapter.notifyItemInserted(mProfileUpdaters.size());
      }
    }
    visibilityOfNonUpdaters();
  }

  int findNotifierPosition(int id) {
    if (mProfileUpdaters != null) {
      for (int i = 0; i < mProfileUpdaters.size(); i++) {
        if (id == mProfileUpdaters.get(i).getId())
          return i;
      }
    }
    return -1;
  }

  void focusNotifier(final int id, long delay) {
    mNoProfileUpdaterIndicator.postDelayed(new Runnable() {
      @Override
      public void run() {
        if (mLLManager != null) {
          int index = findNotifierPosition(id);
          if (index != -1) {
            mLLManager.scrollToPosition(index);
          }
          tryToStart(id);
        }
      }
    }, delay);
  }

  private NotifierChoiceDialog.OnChoiceListener onChoiceListener = new NotifierChoiceDialog.OnChoiceListener() {
    @Override
    public void onStop(int LID) {
      // update base.
      // dispatchEvent event.
      NotifierProvider.Utils.updateSingleInt(getApplicationContext(), LID, NotifierProvider.STATE, Notifier.STOPPED);

      Notifier notifier = findNotifier(LID);

      if (notifier != null) {
        notifier.setState(Notifier.STOPPED);
        updateInAdapter(LID);
      }

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

      Notifier notifier = NotifierProvider.Utils.get(getApplicationContext(), LID);

      if (Conditions.isTimely(notifier.getCondition().getId())) {
        EnvironmentManager.init().removeTimelyNotifierEnvironment(getApplicationContext(), LID);
      } else if (Conditions.isLocational(notifier.getCondition().getId())) {
        EnvironmentManager.init().removeLocationalNotifierEnvironment(getApplicationContext(), LID);
      }

      NotifierProvider.Utils.delete(getApplicationContext(), LID);
      deleteOnAdapter(LID);
    }

    @Override
    public void onShowLogs(int LID) {
    }

    @Override
    public void onShare(int LID) {
      updateInAdapter(LID);
    }

    @Override
    public void onEdit(int LID) {
      NotifierBuilderActivity.editForResult(ProfileUpdatersActivity.this, LID, EDIT_REQUEST);
    }

    @Override
    public void onSeeDetails(int LID) {
      NotifierBuilderActivity.seen(ProfileUpdatersActivity.this, LID);
    }
  };

  private void tryToStart(final int LID) {

    final PermissionRequest permissions = new PermissionRequest(NotifierPermissionDetective.detect(NotifierProvider.Utils.get(getApplicationContext(), LID)));
    boolean check = PermissionUtils.check(getApplicationContext(), permissions);

    if (check) {

      // database update
      // adapter update
      // dispatchEvent global


      NotifierProvider.Utils.updateSingleInt(getApplicationContext(), LID, NotifierProvider.STATE, Notifier.RUNNING);
      EnvironmentManager.init().builtEnvironment(getApplicationContext());
      Notifier notifier = findNotifier(LID);
      if (notifier != null) {
        notifier.setState(Notifier.RUNNING);
        updateInAdapter(LID);
      }

    } else {


      NotifierPermissionRationaleAlertDialog notifierPermissionRationaleAlertDialog =
          new NotifierPermissionRationaleAlertDialog(ProfileUpdatersActivity.this, LID, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              if (which == DialogInterface.BUTTON_POSITIVE) {

                mPermissionManagement.builtRequest(LID, permissions.permissions)
                    .observer(new PermissionManagement.PermissionEventListener() {
                      @Override
                      public void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest) {
                        // update database
                        // update adapter
                        // dispatchEvent global event listeners
                        NotifierProvider.Utils.updateSingleInt(getApplicationContext(), requestCode, NotifierProvider.STATE, Notifier.RUNNING);
                        EnvironmentManager.init().builtEnvironment(getApplicationContext());
                        Notifier notifier = findNotifier(LID);
                        if (notifier != null) {
                          notifier.setState(Notifier.RUNNING);
                          updateInAdapter(LID);
                        }
                      }

                      @Override
                      public void onAnyPermissionsDenied(final int requestCode, PermissionRequest permissionRequest) {

                        if (permissionRequest.getPersistentlyDeniedPermissions() != null) {
                          Snackbar make = Snackbar.make(mRoot, R.string.go_to_settings_for_notifier_permission, 3000);
                          make.setAction(R.string.permission_to_settings, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                              EXTERNAL_PERMISSION_REQUEST = true;
                              EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID = requestCode;

                              PermissionManagement.showAppSettingsPageFor(getApplicationContext());
                            }
                          });

                          make.show();

                        }

                        EnvironmentManager.init().builtEnvironment(getApplicationContext());
                      }
                    }).request();


              }
            }
          });
      notifierPermissionRationaleAlertDialog.show();

    }
  }

  public static boolean checkPermissions(Context context, int id) {
    return PermissionUtils.check(context, new PermissionRequest(getPermissions(context, id)));
  }

  public static String[] getPermissions(Context context, int id) {

    return NotifierPermissionDetective.detect(NotifierProvider.Utils.get(context, id));
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    this.mPermissionManagement.dispatchEvent(requestCode, grantResults);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (EXTERNAL_PERMISSION_REQUEST && EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID != -1) {
      if (checkPermissions(getApplicationContext(), EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID)) {
        NotifierProvider.Utils.updateSingleInt(getApplicationContext(), EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID, NotifierProvider.STATE, Notifier.RUNNING);
        Notifier notifier = findNotifier(EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID);
        if (notifier != null) {
          notifier.setState(Notifier.RUNNING);
          updateInAdapter(EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID);
        }
        mAdapter.notifyDataSetChanged();
        Snackbar.make(mRoot, R.string.snack_notifier_activated, 2000).show();
      }
      EXTERNAL_PERMISSION_REQUEST = false;
      EXTERNAL_PERMISSION_REQUEST_NOTIFIER_LID = -1;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (mAdapter != null && mAdapter.mDialog != null) {
      if (mAdapter.mDialog.isShowing()) {
        mAdapter.mDialog.dismiss();
      }
    }
    NotifierEventDispatcher.getInstance().removeEventListener(mNotifierEventListener);
  }

  static class NotifierAdapter extends RecyclerView.Adapter {
    ProfileUpdatersActivity mAcRef;
    NotifierChoiceDialog mDialog;

    NotifierAdapter(ProfileUpdatersActivity acRef) {
      this.mAcRef = acRef;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new NotifierHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.row_notifierview, parent, false), this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      NotifierHolder rHolder = (NotifierHolder) holder;
      rHolder.setNotifier(mAcRef.mProfileUpdaters.get(position));
      rHolder.mNotifierView.hideNotification();
    }

    @Override
    public int getItemCount() {
      return mAcRef.mProfileUpdaters.size();
    }

    static class NotifierHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
      NotifierView mNotifierView;
      NotifierAdapter mAdapter;

      NotifierHolder(View itemView, NotifierAdapter adapterRef) {
        super(itemView);
        mAdapter = adapterRef;
        mNotifierView = ((NotifierView) itemView.findViewById(R.id.notifierView));
        mNotifierView.mOptions.setOnClickListener(this);

      }

      public void setNotifier(Notifier r) {
        mNotifierView.with(r);
      }

      @Override
      public void onClick(View v) {
        if (getAdapterPosition() != -1) {
          Notifier notifier = mAdapter.mAcRef.mProfileUpdaters.get(getAdapterPosition());
          if (notifier != null) {
            mAdapter.mDialog = new NotifierChoiceDialog(mAdapter.mAcRef, notifier.getId(), mAdapter.mAcRef.onChoiceListener);
            mAdapter.mDialog.show();
          }
        }
      }
    }

  }

  @Override
  public void onBackPressed() {
    // super.onBackPressed();
    finish();
  }

}
