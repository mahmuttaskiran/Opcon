package com.opcon.libs.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opcon.Build;
import com.opcon.R;
import com.opcon.database.ContactBase;
import com.opcon.database.FeatureBase;
import com.opcon.database.MessageProvider;
import com.opcon.database.PostBase;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.registration.activities.RequestTokenActivity;
import com.opcon.libs.utils.AndroidEnvironmentsUtils;
import com.opcon.ui.dialogs.DialogUtils;
import com.opcon.libs.registration.RegistrationManagement;
import com.opcon.ui.management.DialogStoreManagement;
import com.opcon.ui.views.HelperHolder;
import com.opcon.ui.views.VerticalUserView;
import com.opcon.utils.PreferenceUtils;

public class DeleteMyAccountActivity extends AppCompatActivity implements View.OnClickListener {

  ProgressDialog mProgress;

  RelativeLayout mHelperRoot;
  HelperHolder mHelperHolder;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_delete_my_account);
    mHelperRoot = (RelativeLayout) findViewById(R.id.helperRoot);


    mHelperHolder = new HelperHolder(mHelperRoot);
    mHelperHolder.newBuilder()
        .setTopIcon(gd(R.drawable.ic_info_white_48dp))
        .setTopIconColor(R.color.colorPrimaryDark)
        .setTopBackground(R.color.colorPrimary)
        .setTitle(R.string.delete_account_title)
        .setMessage(R.string.delete_account_indicator)
        .setNegativeButton(R.string.delete_account_button, new View.OnClickListener() {
          @Override
          public void onClick(View v) {

            if (!AndroidEnvironmentsUtils.hasActiveInternetConnection(getApplicationContext()))
            {
              DialogUtils.alertOnlyOk(DeleteMyAccountActivity.this, null, getString(R.string.not_internet_connection));
            } else {
              deleteAccount();
            }

          }
        }).setPositiveButton(R.string.no, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });


    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(null);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
      getSupportActionBar().setTitle(R.string.delete_my_account_activity_title);
    }
  }

  Drawable gd(int id){
    return getResources().getDrawable(id);
  }

  @Override public void onClick(View v) {

  }

  void deleteAccount() {
    AlertDialog.Builder m = new AlertDialog.Builder(this);
    m.setTitle(R.string.are_you_sure)
        .setMessage(R.string.we_will_miss_you)
        .setCancelable(true)
        .setNegativeButton(R.string.i_just_kidding, null)
        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            removeSelf();
          }
        }).show();
  }

  private void removeSelf() {
    mProgress = ProgressDialog.show(this, null, getString(R.string.please_wait), true, false);
    String my = PresenceManager.uid();

    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
    reference.child("black/" + my).setValue(null);
    reference.child("msgs/" + my).setValue(null);
    reference.child("notifiers/" + my).setValue(null);
    reference.child("users/" + PresenceManager.uid()).setValue(null);
    reference.child("posts/" + PresenceManager.uid()).setValue(null);
    reference.child("social/contacts/" + PresenceManager.uid()).setValue(null);

    PreferenceUtils.putString(this, RequestTokenActivity.SAVED_DIAL_CODE, null);
    PreferenceUtils.putString(this, RequestTokenActivity.SAVED_LOCALE, null);
    PreferenceUtils.putString(this, RequestTokenActivity.SAVED_PHONE, null);


    PresenceManager.setAvatarDownloadUrl(this, null);
    PresenceManager.setAvatarFileName(this, null);

    PresenceManager.removeAvatar(getApplicationContext());

    MessageProvider.Utils.removeSelf(this);
    NotifierProvider.Utils.removeSelf(this);
    PreferenceUtils.removeSelf(this);
    PostBase.Utils.removeSelf(this);
    DialogStoreManagement.getInstance(getApplicationContext()).clear();
    BlackListActivity.BlackLocaleBase.removeSelf(this);
    ContactBase.Utils.removeSelf(this);

    VerticalUserView.setVisibilitySettings(this, true);

    FeatureBase.getInstance(this).removeSelf();

    RegistrationManagement.getInstance().setPassword(this, null);
    RegistrationManagement.getInstance().setEmail(this, null);

    if (FirebaseAuth.getInstance().getCurrentUser() != null) {
      FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
      if (currentUser != null) {
        currentUser.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            mProgress.dismiss();
            Toast.makeText(DeleteMyAccountActivity.this, R.string.account_deleted, Toast.LENGTH_SHORT).show();
            finish();
          }
        });
      }
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }
}
