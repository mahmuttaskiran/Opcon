package com.opcon.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.opcon.Build;
import com.opcon.R;
import com.opcon.database.ContactBase;
import com.opcon.firebaseclient.ContactSync;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.permission.PermissionRequest;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.ui.drawables.RandomIconsDrawable;
import com.opcon.ui.fragments.PostFragment;
import com.opcon.utils.MobileNumberUtils;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class ProfileActivity extends AppCompatActivity {

  private static boolean ON_UI = false;

  @BindView(R.id.avatar)
  ImageView mAvatar;
  @BindView(R.id.title)
  TextView mTitle;
  @BindView(R.id.subtitle)
  TextView mSubtitle;
  @BindView(R.id.toolbar)
  Toolbar mToolbar;
  @BindView(R.id.collapsingToolbar)
  CollapsingToolbarLayout mCollapsingToolbarLayout;
  @BindView(R.id.container)
  RelativeLayout mContainer;
  PostFragment mPostFragment;
  @BindView(R.id.fab)
  FloatingActionButton mFab;
  @BindView(R.id.fab_Edit)
  FloatingActionButton mFabEdit;

  PermissionManagement mPermissionManagement;


  @BindView(R.id.parent)
  CoordinatorLayout mParent;

  private String mDestination;
  private RandomIconsDrawable mRandomIcons;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ON_UI = true;
    setContentView(R.layout.activity_profile);
    ButterKnife.bind(this);

    mPermissionManagement = PermissionManagement.with(this);


    setSupportActionBar(mToolbar);
    getSupportActionBar().setTitle(null);
    Drawable ic = getResources().getDrawable(R.drawable.ic_keyboard_backspace_white_24dp);
    ic.setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.MULTIPLY);
    getSupportActionBar().setHomeAsUpIndicator(ic);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    if (savedInstanceState != null) {
      mDestination = savedInstanceState.getString("destination");
      mPostFragment = (PostFragment) getSupportFragmentManager().getFragment(savedInstanceState, "postFragment");
    }

    if (mDestination == null) {
      mDestination = getIntent().getExtras().getString("destination");
    }

    if (mPostFragment == null) {
      mPostFragment = PostFragment.newInstanceFor(mDestination);
    }

    mCollapsingToolbarLayout.setTitleEnabled(false);
    getSupportFragmentManager().beginTransaction().replace(R.id.container, mPostFragment).commit();

    setToolbarStuffs();

    if (isMe()) {
      mFabEdit.setVisibility(View.VISIBLE);
    } else {
      mFabEdit.setVisibility(View.GONE);
    }

  }

  @OnClick({R.id.fab, R.id.fab_Edit})
  public void onClick(View v) {
    if (v.getId() == R.id.fab) {
      if (isMe()) {
        NotifierBuilderActivity.profileUpdaterBuilder(this, 0);
      } else {
        ChatActivity.go(this, mDestination);
        finish();
      }
    } else {
      UserProfileUtils.showProfileOptions(this, new WeakReference<TextView>(mTitle), new WeakReference<ImageView>(mAvatar), mPermissionManagement);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode == RESULT_OK) {
      if (requestCode == 0) {
        int intExtra = data.getIntExtra(NotifierBuilderActivity.NOTIFIER_ID, -1);
        if (intExtra != -1) {
          ProfileUpdatersActivity.go(this, intExtra);
        }
      }
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      super.onBackPressed();
    } else if (item.getItemId() == 0) {
      ProfileUpdatersActivity.go(this);
    } else if (item.getItemId() == 1) {

      PermissionRequest pr = new PermissionRequest(android.Manifest.permission.CALL_PHONE);
      if (PermissionUtils.isAnyPermissionPersistentlyDenied(this, pr)) {
        Snackbar make = Snackbar.make(mParent, R.string.need_permission_to_call, Snackbar.LENGTH_LONG);
        make.setAction(R.string.go_to_application_settings, new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            PermissionManagement.showAppSettingsPageFor(getApplicationContext());
          }
        });
      } else {
        if (PermissionUtils.check(this, pr)) {
          Intent intent = new Intent(Intent.ACTION_CALL);
          intent.setData(Uri.parse("tel:" + mDestination));
          try {
            startActivity(intent);
          } catch (SecurityException e) {
            e.printStackTrace();
          }
        } else {
          mPermissionManagement.builtRequest(0, pr).observer(new PermissionManagement.PermissionEventListener() {
            @Override public void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest) {
              Intent intent = new Intent(Intent.ACTION_CALL);
              intent.setData(Uri.parse("tel:" + mDestination));
              try {
                startActivity(intent);
              } catch (SecurityException e) {
                e.printStackTrace();
              }
            }
            @Override public void onAnyPermissionsDenied(int requestCode, PermissionRequest permissionRequest) {

            }
          }).request();
        }
      }

    } else if (item.getItemId() == 2) {



      Intent smsIntent = new Intent(Intent.ACTION_VIEW);
      smsIntent.setData(Uri.parse("sms:"));
      smsIntent.putExtra("address", mDestination);
      startActivity(smsIntent);



    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    mPermissionManagement.dispatchEvent(requestCode, grantResults);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    getSupportFragmentManager().putFragment(outState, "postFragment", mPostFragment);
    outState.putString("destination", mDestination);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    ON_UI = false;
    if (mRandomIcons != null) {
      mRandomIcons.recycleBitmaps();
    }
  }


  public static void profile(Context context, String destination) {
    if (!ON_UI) {
      Intent intent = new Intent(context, ProfileActivity.class);
      intent.putExtra("destination", destination);
      context.startActivity(intent);
    }
  }

  boolean isMe() {
    return mDestination.equals(PresenceManager.uid());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    if (isMe()) {

      MenuItem miSeeProfileUpdaters = menu.add(0, 0, 0, R.string.see_my_profile_updaters);
      miSeeProfileUpdaters.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);


    } else {

      MenuItem miCall = menu.add(0, 1, 0, R.string.call);
      miCall.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

      MenuItem miSendMessage = menu.add(0, 2, 0, R.string.sendSms);
      miSendMessage.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

    }

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  protected void onResume() {
    super.onResume();
    String avatar = isMe() ? PresenceManager.getAvatar(getBaseContext()) : ContactBase.Utils.getValidAvatar(getBaseContext(), mDestination);
    if (avatar != null) {
      Glide.with(getApplicationContext()).load(avatar).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mAvatar);
    }
  }

  void setToolbarStuffs() {


    if (isMe()) {
      mFab.setImageResource(R.drawable.ic_heart_no_outline);
    } else {
      mFab.setImageResource(R.drawable.ic_send_white_18dp);
    }

    mRandomIcons = new RandomIconsDrawable.Builder()
        .setResources(getResources())
        .setBackgroundColor(gcolor(R.color.colorSecondaryDark))
        .setIconColor(gcolor(R.color.colorSecondary))
        .setMax(10)
        .setMaxScale(1.1f)
        .setMinScale(0.9f)
        .built();

    mAvatar.setBackgroundDrawable(mRandomIcons);
    mTitle.setText(ContactBase.Utils.getName(getApplicationContext(), mDestination));
    mSubtitle.setText(MobileNumberUtils.toInternational(mDestination, null, true));

    String avatar = isMe() ? PresenceManager.getAvatar(getBaseContext()) : ContactBase.Utils.getValidAvatar(getBaseContext(), mDestination);
    Glide.with(getApplicationContext()).load(avatar).diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mAvatar);
    if (!isMe()) {
      ContactSync.getInstance(getApplicationContext()).singleQuery(mDestination, new ContactSync.OnQueryResultListener() {
        @Override public void onFind(String phone, String avatar) {
          if (mAvatar != null && avatar != null) {
            Glide.with(getApplicationContext()).load(avatar).centerCrop().diskCacheStrategy(DiskCacheStrategy.SOURCE).into(mAvatar);
          }
        }
        @Override public void onNonUser(String phone) {}
        @Override public void onQueryEnded() {}
      });
    }

    if (!Build.isSlogan()) {
      mSubtitle.setVisibility(View.VISIBLE);
    } else {
      mSubtitle.setVisibility(View.GONE);
    }
  }

  int gcolor(int c) {
    return getResources().getColor(c);
  }


  @Override
  public void onBackPressed() {
    finish();
  }
}
