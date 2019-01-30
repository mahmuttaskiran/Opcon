package com.opcon.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionMenu;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.opcon.R;
import com.opcon.components.Contact;
import com.opcon.database.ContactBase;
import com.opcon.database.NotifierProvider;
import com.opcon.database.PostBase;
import com.opcon.firebaseclient.ContactSync;
import com.opcon.firebaseclient.FirebaseDatabaseManagement;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.post.PostPoster;
import com.opcon.libs.registration.RegistrationManagement;
import com.opcon.libs.settings.FeedbackActivity;
import com.opcon.libs.settings.SettingsActivity;
import com.opcon.notifier.NotifierEventDispatcher;
import com.opcon.notifier.environment.EnvironmentManager;
import com.opcon.ui.drawables.RandomIconsDrawable;
import com.opcon.ui.fragments.DialogFragment;
import com.opcon.ui.views.CircleRelativeLayout;
import com.opcon.ui.views.VerticalUserView;

import java.lang.ref.WeakReference;
import java.util.List;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.views.AvatarView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements VerticalUserView.VerticalUserViewListener {

  public static boolean mAvatarUpdated = false;
  public static boolean onUI = false;
  private static final int NOTIFIER_REQUEST = 735;
  private static final int PROFILE_UPDATER_REQUEST = 1231;

  @BindView(R.id.toolbar)
  Toolbar mToolbar;
  @BindView(R.id.activity_new_main)
  CoordinatorLayout activityNewMain;

  @BindView(R.id.container)
  RelativeLayout mContainer;
  @BindView(R.id.menu)
  FloatingActionMenu faMenu;
  @BindView(R.id.navigationView)
  NavigationView mNavigationView;
  @BindView(R.id.drawerLayout)
  DrawerLayout mDrawerLayout;

  private NavigationHolder mNavigationHolder;
  private DialogFragment mDialogFragment;
  private PermissionManagement mPermissionManagement;


  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);


    setContentView(R.layout.activity_new_main);
    ButterKnife.bind(this);
    mAvatarUpdated = true; // for initiate.
    onUI = true;
    setSupportActionBar(mToolbar);
    mToolbar.setTitleTextColor(Color.WHITE);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(null);
      getSupportActionBar().setDisplayHomeAsUpEnabled(false);
      getSupportActionBar().setElevation(3);
    }
    mNavigationHolder = new NavigationHolder(mNavigationView.getHeaderView(0));
    mPermissionManagement = PermissionManagement.with(this);
    inflateDialogFragment(savedInstanceState);
    initComponents();


    faMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
      @Override
      public void onMenuToggle(boolean opened) {
        if (opened) {

          faMenu.setClickable(true);


        } else {

          faMenu.setClickable(false);

        }
      }
    });

    mDrawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
      @Override
      public void onDrawerSlide(View drawerView, float slideOffset) {

      }

      @Override
      public void onDrawerOpened(View drawerView) {
        if (faMenu.isOpened()) {
          faMenu.close(false);
        }
      }

      @Override
      public void onDrawerClosed(View drawerView) {

      }

      @Override
      public void onDrawerStateChanged(int newState) {

      }
    });


    faMenu.setClickable(false);
  }

  @OnClick(R.id.menu)
  public void onFabClick() {

    if (faMenu.isOpened()) {
      faMenu.close(true);
    }

  }

  int dp(int dp) {
    Resources r = getResources();
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics());
  }

  @Override protected void onDestroy() {
    onUI = false;
    NotifierProvider.Utils.deleteIsNecessary(getApplicationContext());
    super.onDestroy();
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_ac, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.app_settings) {
      Intent i = new Intent(getBaseContext(), SettingsActivity.class);
      startActivity(i);
    } else if (item.getItemId() == R.id.contacts) {
      ContactsActivity.goProfile(MainActivity.this);
    } else if (item.getItemId() == R.id.toProfile) {
      ProfileUpdatersActivity.go(this);
    } else if (item.getItemId() == R.id.toProfileUpdater) {
      ProfileActivity.profile(this, PresenceManager.uid());
    }
    return super.onOptionsItemSelected(item);
  }

  private void initComponents() {
    PresenceManager mPresenceManager = PresenceManager.getInstance(getApplicationContext());
    RegistrationManagement mRegistrationManagement = RegistrationManagement.getInstance();
    if (mRegistrationManagement.isRegistrationRequires(getBaseContext())) {
      mRegistrationManagement.startRegistration(this, MainActivity.class);
      finish();
    } else {
      mPresenceManager.init();
      mPresenceManager.sing(mRegistrationManagement.getEmail(getApplicationContext()),
          mRegistrationManagement.getPassword(getApplicationContext()));
      PostPoster.getInstance(getApplicationContext());
      FirebaseDatabaseManagement.getInstance(getApplicationContext());
      ContactSync.getInstance(getApplicationContext());
      mPresenceManager.goOnline();
    }
  }

  void decideVisibilityOfWaitingPostIndicator() {
    SQLiteDatabase readableDatabase = PostBase.getInstance(getApplicationContext()).getReadableDatabase();
    long count = DatabaseUtils.queryNumEntries(readableDatabase, "posts", "accepted = 0");
    readableDatabase.close();
    final View viewById = findViewById(R.id.there_is_waiting_post);
    if (viewById != null) {
      if (count < 1) {
        viewById.setVisibility(View.GONE);
      } else {
        viewById.setVisibility(View.VISIBLE);
        Animation firstAnimation = AnimationUtils.loadAnimation(this, R.anim.heart);
        findViewById(R.id.heart).startAnimation(firstAnimation);
        ((TextView) findViewById(R.id.postCount)).setText(String.valueOf(count));
      }
    }
  }

  @Override
  public void onBackPressed() {
    if (!faMenu.isOpened()) {
      super.onBackPressed();
    } else {
      faMenu.close(false);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    faMenu.close(false);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (RegistrationManagement.getInstance().isRegistrationRequires(getBaseContext())) {
      RegistrationManagement.getInstance().startRegistration(this, MainActivity.class);
      finish();
    }
    /*
    if (isContactsReadable()) {
      if (mVerticalUserView.isNotDefined()) {
        List<Contact> users = ContactBase.Utils.getContacts(this, ContactBase.USER + "=0", null, 15);
        if (users.isEmpty() || users.size() < 10 || !VerticalUserView.isChoiceIsVisible(getApplicationContext())) {
          mVerticalUserView.setVisibility(View.GONE);
        } else {
          mVerticalUserView.setUsers(users);
          mVerticalUserView.setListener(this);
        }
      }
    } else {

    }
     */


    if (mAvatarUpdated) {
      setupUserAvatar();
      mAvatarUpdated = false;
    }
    decideVisibilityOfWaitingPostIndicator();
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    mPermissionManagement.dispatchEvent(requestCode, grantResults);
  }

  @OnClick({R.id.menu_sendMsg, R.id.menu_addNotifier, R.id.there_is_waiting_post, R.id.openDrawer, R.id.menu_addProfileUpdater})
  public void onClick(View v) {
    int id = v.getId();
    if (id == R.id.menu_sendMsg) {
      faMenu.close(false);
      if (mDialogFragment != null) {
        mDialogFragment.attemptToNewDialog();
      }
    } else if (id == R.id.menu_addNotifier) {
      faMenu.close(false);
      ContactsActivity.goResultForMainActivity(this, true, NOTIFIER_REQUEST);
    } else if (id == R.id.there_is_waiting_post) {
      Intent i = new Intent(getBaseContext(), WaitingPostActivity.class);
      i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
      startActivity(i);
    } else if (R.id.openDrawer == v.getId()) {
      mDrawerLayout.openDrawer(Gravity.START, true);
    } else if (R.id.menu_addProfileUpdater == v.getId()) {
      NotifierBuilderActivity.profileUpdaterBuilder(this, PROFILE_UPDATER_REQUEST);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == NOTIFIER_REQUEST && resultCode == RESULT_OK) {
      NotifierBuilderActivity.buildForResult(this, data.getStringExtra(ContactsActivity.SELECTED_CONTACT_NUMBER), 1111);
    } else if (requestCode == 1111 && resultCode == RESULT_OK) {
      int notifierId = data.getIntExtra(NotifierBuilderActivity.NOTIFIER_ID, -1);
      ChatActivity.goWithActivateRequest(this, data.getStringExtra(NotifierBuilderActivity.DESTINATION), notifierId);
      if (notifierId != -1) {
        NotifierEventDispatcher.getInstance().dispatchAdded(notifierId);
        EnvironmentManager.init().builtEnvironment(getApplicationContext());
      }
    } else if (requestCode == PROFILE_UPDATER_REQUEST && resultCode == RESULT_OK) {
      int notifierId = data.getIntExtra(NotifierBuilderActivity.NOTIFIER_ID, -1);
      ProfileUpdatersActivity.go(this, notifierId);
    }
    setupUserAvatar();
  }

  private void inflateDialogFragment(Bundle savedInstanceState) {

    if (savedInstanceState != null) {
      mDialogFragment = (DialogFragment) getSupportFragmentManager()
          .getFragment(savedInstanceState, "df");
    }

    if (mDialogFragment == null) {
      mDialogFragment = new DialogFragment();
    }

    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();
    ft.replace(R.id.container, mDialogFragment);
    ft.commit();
  }

  private void setupUserAvatar() {
    if (mAvatarUpdated) {
      mAvatarUpdated =false;

      String fileName = PresenceManager.getAvatarFileName(getBaseContext());

      String userAvatarFile = null;
      if (fileName != null ){
         userAvatarFile = PresenceManager.getAvatar(getBaseContext());
      }

      AvatarPlaceholder placeholderDrawable = new AvatarPlaceholder("+");
      placeholderDrawable.setColorFilter(getResources().getColor(R.color.colorSecondaryDark), PorterDuff.Mode.SCREEN);

      Glide.with(getApplicationContext())
          .load(userAvatarFile)
          .placeholder(placeholderDrawable)
          .override(300, 300)
          .centerCrop()
          .into(mNavigationHolder.mAvatar);

      String name = RegistrationManagement.getInstance().getName(getApplicationContext());
      mNavigationHolder.mName.setText(name);
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mDialogFragment != null) {
      getSupportFragmentManager().putFragment(outState, "df", mDialogFragment);
    }
  }

  private boolean isContactsReadable() {
    return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS) ==
        PackageManager.PERMISSION_GRANTED || ContactBase.Utils.getContactsSize(getApplicationContext()) > 1;
  }

  @Override
  public void onContactClick(Contact contact) {
    invite(getBaseContext());
  }

  @Override
  public void onRequestAllContacts() {
    invite(getBaseContext());
  }

  public void invite(Context context) {
    Intent intent = new AppInviteInvitation.IntentBuilder(context.getString(R.string.invitation_title))
        .setMessage(context.getString(R.string.invitation_message))
        .setDeepLink(Uri.parse("https://play.google.com/store/apps/details?id=com.opcon"))
        .build();
    startActivityForResult(intent, 1);
  }

  class NavigationHolder {
    @BindView(R.id.avatar)
    AvatarView mAvatar;
    @BindView(R.id.changeProfileButton)
    CircleRelativeLayout mChangeProfileButton;
    @BindView(R.id.title)
    TextView mName;
    @BindView(R.id.seeProfile)
    TextView mSeeProfile;
    @BindView(R.id.invite)
    TextView mInvite;
    @BindView(R.id.application_settings)
    TextView mApplicationSettings;
    @BindView(R.id.feedback)
    TextView mFeedback;
    @BindView(R.id.help)
    TextView mHelpAbout;
    @BindView(R.id.facebook)
    LinearLayout mFacebook;
    @BindView(R.id.toProfileUpdaters)
    TextView mSeeProfileUpdaters;

    NavigationHolder(View view) {
      ButterKnife.bind(this, view);
    }

    @OnClick({R.id.avatar, R.id.toProfileUpdaters, R.id.changeProfileButton, R.id.help, R.id.feedback, R.id.seeProfile, R.id.application_settings, R.id.invite, R.id.facebook})
    public void onClick(View v) {
      switch (v.getId()) {
        case R.id.avatar:
        case R.id.changeProfileButton:
          UserProfileUtils.showProfileOptions(MainActivity.this, new WeakReference<>(mName),
              new WeakReference<ImageView>(mAvatar), mPermissionManagement);
          break;
        case R.id.seeProfile:
          ProfileActivity.profile(MainActivity.this, PresenceManager.uid());
          break;
        case R.id.application_settings:
          startActivity(new Intent(getBaseContext(), SettingsActivity.class));
          break;
        case R.id.invite:
          invite(getBaseContext());
          break;
        case R.id.feedback:
          startActivity(new Intent(MainActivity.this, FeedbackActivity.class));
          break;
        case R.id.help:
          startActivity(new Intent(MainActivity.this, NewAboutActivity.class));
          break;
        case R.id.toProfileUpdaters:
          ProfileUpdatersActivity.go(MainActivity.this);
          break;
        case R.id.facebook:
          Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse("https://www.facebook.com/OpconApp/"));
          startActivity(browse);
          break;
      }
      mDrawerLayout.closeDrawer(Gravity.START, false);
    }
  }
}
