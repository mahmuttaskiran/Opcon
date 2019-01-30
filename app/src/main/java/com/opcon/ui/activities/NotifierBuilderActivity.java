package com.opcon.ui.activities;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.opcon.firebaseclient.Analytics;
import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.database.ContactBase;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.ComponentSender;
import com.opcon.firebaseclient.NotifierEventSentUtils;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.settings.BlackListActivity;
import com.opcon.libs.utils.AnimationUtils;
import com.opcon.notifier.NotifierEventDispatcher;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Conditions;
import com.opcon.notifier.components.constants.Operations;
import com.opcon.notifier.environment.EnvironmentManager;
import com.opcon.ui.dialogs.NotifierSaveDialog;
import com.opcon.ui.fragments.occs.OPCFragment;
import com.opcon.ui.fragments.occs.OperationNotification;
import com.opcon.ui.fragments.occs.OperationPost;
import com.opcon.ui.management.DialogStoreManagement;
import com.opcon.ui.utils.NotifierConstantUtils;
import com.opcon.ui.views.CircleRelativeLayout;
import com.opcon.ui.views.HelperHolder;
import com.opcon.ui.views.NotifierView;
import com.opcon.ui.views.VerticalOccView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.List;

import agency.tango.android.avatarview.views.AvatarView;
import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

public class NotifierBuilderActivity extends AppCompatActivity {

  public static final String DESTINATION = "CURRENT_DESTINATION";
  public static final String NOTIFIER_ID = "lid";
  public static final String BOOL_ONLY_SEEN = "onlySeen";
  public static final String NOTIFIER_PARAMS = "notifierParams";
  public static final String PROFILE_UPDATER_BUILDER = "selfMode";

  public static final String ALREADY_EXISTS = "ae";
  public static final String SHOW_NOTIFIER_VIEW = "showNotifierView";

  private static final int EDIT_MODE = 0;
  private static final int SEEN_MODE = 1;
  private static final int BUILT_MODE = 2;
  private static final int SELF_MODE = 3;

  @BindView(R.id.toolbar)
  Toolbar mToolbar;
  @BindView(R.id.backButton)
  ImageView mBackButton;
  @BindView(R.id.avatar)
  AvatarView mAvatar;
  @BindView(R.id.title)
  TextView mName;
  @BindView(R.id.conditionSelectView)
  VerticalOccView mConditionSelectView;
  @BindView(R.id.operationSelectView)
  VerticalOccView mOperationSelectView;
  @BindView(R.id.activity_new_main)
  CoordinatorLayout activityNewMain;
  @BindView(R.id.conditionSelectIndicator)
  LinearLayout mConditionIndicator;
  @BindView(R.id.conditionDesc)
  TextView mConditionDesc;
  @BindView(R.id.operationSelectIndicator)
  LinearLayout mOperationIndicator;
  @BindView(R.id.operationDesc)
  TextView mOperationDesc;
  @BindView(R.id.subtitle)
  TextView mSubtitle;

  @BindView(R.id.conditionTitleIndicator)
  TextView mConditionTitleIndicator;
  @BindView(R.id.operationTitleIndicator)
  TextView mOperationTitleIndicator;

  @BindView(R.id.operationCardView)
  CardView mOperationCardView;

  @BindView(R.id.notifierView)
  NotifierView mNotifierView;


  @BindView(R.id.meText)
  TextView mMeText;
  @BindView(R.id.meCircle)
  CircleRelativeLayout mMeCircle;
  @BindView(R.id.meAvatar)
  AvatarView mMeAvatar;
  @BindView(R.id.youText)
  TextView mYouText;
  @BindView(R.id.youCircle)
  CircleRelativeLayout mYouCircle;
  @BindView(R.id.youAvatar)
  AvatarView mYouAvatar;
  @BindView(R.id.meRoot)
  RelativeLayout mMeRoot;
  @BindView(R.id.youRoot)
  RelativeLayout mYouRoot;

  private OPCFragment mConditionFragment;
  private OPCFragment mOperationFragment;

  private String mDestination;
  private int mNotifierId;
  private int mMode;

  private Notifier mNotifier;
  private NotifierSaveDialog mNotifierSaveDialog;

  private HelperHolder mHelper;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_notifier_builder);
    ButterKnife.bind(this);
    setSupportActionBar(mToolbar);
    Bundle extras = getIntent().getExtras();

    mHelper = new HelperHolder(findViewById(android.R.id.content));
    mHelper = new HelperHolder(findViewById(android.R.id.content));
    if (extras != null) {
      mDestination = extras.getString(DESTINATION);
      mNotifierId = extras.getInt(NOTIFIER_ID);
      if (mDestination != null) {
        mMode = BUILT_MODE;
      } else if (extras.getBoolean(BOOL_ONLY_SEEN)) {
        mMode = SEEN_MODE;
      } else if (mNotifierId != 0) {
        mMode = EDIT_MODE;
      } else {
        if (extras.getBoolean(PROFILE_UPDATER_BUILDER, false)) {
          mMode = SELF_MODE;
        }
      }
    }

    mConditionSelectView.setListener(mConditionSelectListener);
    mOperationSelectView.setListener(mOperationSelectListener);
    if (isEditMode() || isSeenMode()) {

      mConditionSelectView.setVisibility(View.GONE);
      mOperationSelectView.setVisibility(View.GONE);
      mConditionIndicator.setVisibility(View.GONE);
      mOperationIndicator.setVisibility(View.GONE);

      mOperationDesc.setVisibility(View.VISIBLE);
      mConditionDesc.setVisibility(View.VISIBLE);

      if (getIntent().getExtras() != null) {
        String notifierParams = getIntent().getExtras().getString(NOTIFIER_PARAMS, null);
        if (!TextUtils.isEmpty(notifierParams)) {
          try {
            JSONObject joParams = new JSONObject(notifierParams);
            mNotifier = new Notifier(joParams);
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
      }

      if (mNotifier == null) {
        mNotifier = NotifierProvider.Utils.get(this, mNotifierId);
      }

      if (mNotifier != null) {
        mConditionDesc.setText(Html.fromHtml(mNotifier.getConditionDescription(this)));
        mOperationDesc.setText(Html.fromHtml(mNotifier.getOperationDescription(this)));
        mDestination = mNotifier.getReceiver();
        selectCondition(mNotifier.getCondition().getId(), mNotifier.getCondition().toPureJson());
        selectOperation(mNotifier.getOperation().getId(), mNotifier.getOperation().toPureJson());
        mConditionSelectView.setComponent(VerticalOccView.Component.condition, mNotifier.getCondition().getId());
        mOperationSelectView.setComponent(VerticalOccView.Component.operation, mNotifier.getOperation()
            .getId());
      }

      if (isSeenMode() || isEditMode()) {

        if (extras != null && extras.getBoolean(SHOW_NOTIFIER_VIEW)) {
          mNotifierView.setVisibility(View.VISIBLE);

          try {
            mNotifierView.with(new Notifier(new JSONObject(extras.getString(NOTIFIER_PARAMS))));
          } catch (JSONException e) {
            e.printStackTrace();
          }
        }
        mNotifierView.hideRightSide();

        CardView mSb = (CardView) findViewById(R.id.saveButton);
        ImageView mSbi = (ImageView) findViewById(R.id.saveButtonIcon);
        mSbi.setImageResource(R.drawable.ic_close_blue_24dp);
        mSb.setCardBackgroundColor(Color.RED);
        mSbi.setColorFilter(Color.WHITE);
      }

    } else if (isSelfMode()) {

      mConditionDesc.setVisibility(View.GONE);
      mConditionTitleIndicator.setText(R.string.what_if_what_happens_on_your_phone_for_profile_updater);
      mOperationTitleIndicator.setText(R.string.what_do_you_want_to_share_on_your_profile);
      mOperationSelectView.setVisibility(View.GONE);
      mOperationDesc.setVisibility(View.GONE);
      mOperationCardView.setVisibility(View.GONE);
      mAvatar.setVisibility(View.GONE);
      mName.setText(R.string.add_an_profile_updater);
      mSubtitle.setVisibility(View.GONE);
      mOperationSelectView.setVisibility(View.GONE);
      selectOperation(Operations.__POST, null);
      mConditionSelectView.noFilter();
      mConditionSelectView.setComponents(NotifierConstantUtils.getConditions(getBaseContext(), NotifierConstantUtils.ON_THE_OWNER));
    }

    if (isBuiltMode() ) {
      initFilterView();
    } else {
      findViewById(R.id.onWhoRoot).setVisibility(View.GONE);
      mMeRoot.setVisibility(View.GONE);
      mYouRoot.setVisibility(View.GONE);
    }
    if (mDestination != null) {
      setToolbar(mDestination);
    }

    initHelper();

    if (mNotifierId != -1 && isSeenMode()) {
      NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
      nm.cancel(mNotifierId);
    }

  }

  void initHelper() {


    if (isSelfMode())  {
      if (HelperHolder.isGotIt(this, R.string.what_is_profile_updater)) {
        mHelper.gone();
      } else {
        mHelper.newBuilder()
            .setNegativeButton(R.string.understand_shut_up, new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                mHelper.gone();
                HelperHolder.gotIt(getApplicationContext(), R.string.what_is_profile_updater);
              }
            }).setTitle(R.string.what_is_profile_updater)
            .setMessage(R.string.what_is_profile_updater_answer)
            .setCardBackground(R.color.white)
        .setTopBackground(R.color.colorSecondary)
            .setDivider(getResources().getDrawable(R.drawable.linear_secondary))
        .setTopIconColor(R.color.colorSecondaryDark);
        mHelper.show();
      }

    } else if (isBuiltMode()) {
      if (HelperHolder.isGotIt(this, R.string.what_is_notifier)) {
        mHelper.gone();
      } else {
        mHelper.newBuilder()
            .setNegativeButton(R.string.understand_shut_up, new View.OnClickListener() {
              @Override
              public void onClick(View v) {
                mHelper.gone();
                HelperHolder.gotIt(getApplicationContext(), R.string.what_is_notifier);
              }
            }).setTitle(R.string.what_is_notifier)
            .setMessageAsHtml(R.string.what_is_notifier_answer)
            .setCardBackground(R.color.white)
            .setDivider(getResources().getDrawable(R.drawable.linear_secondary))
            .setTopBackground(R.color.colorSecondary)
            .setTopIconColor(R.color.colorSecondaryDark);
        mHelper.show();
      }
    } else {
      mHelper.gone();
    }

  }

  private void initFilterView() {


    final String meAvatar, youAvatar;
    meAvatar = PresenceManager.getAvatar(this);
    youAvatar = ContactBase.Utils.getValidAvatar(this, mDestination);

    Glide.with(this).load(meAvatar).asBitmap().listener(new RequestListener<String, Bitmap>() {
      @Override
      public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
        mMeCircle.setVisibility(View.VISIBLE);
        mMeAvatar.setVisibility(View.GONE);
        return false;
      }

      @Override
      public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
        mMeAvatar.setVisibility(View.VISIBLE);
        mMeCircle.setVisibility(View.GONE);
        mMeAvatar.setImageBitmap(resource);
        return false;
      }
    }).into(150,150);



    Glide.with(this).load(youAvatar).asBitmap().listener(new RequestListener<String, Bitmap>() {
      @Override
      public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
        mYouAvatar.setVisibility(View.GONE);
        mYouCircle.setVisibility(View.VISIBLE);
        return false;
      }

      @Override
      public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
        mYouAvatar.setVisibility(View.VISIBLE);
        mYouCircle.setVisibility(View.GONE);
        Timber.d("ready!");
        mYouAvatar.setImageBitmap(resource);
        return false;
      }
    }).into(150, 150);


    mYouRoot.setScaleX(0.8f);
    mYouRoot.setScaleY(0.8f);


    mMeText.setTextColor(Color.DKGRAY);
    mYouText.setTextColor(Color.WHITE);

    mMeCircle.setColor(getResources().getColor(R.color.white));
    mYouCircle.setColor(getResources().getColor(R.color.colorPrimary));

    mYouRoot.setOnClickListener(mFilterRootsListener);
    mMeRoot.setOnClickListener(mFilterRootsListener);


    List<NotifierConstantUtils.Component> operations = NotifierConstantUtils.getOperations(this, NotifierConstantUtils.ON_THE_TARGET);
    boolean b = operations
        .addAll(
            NotifierConstantUtils.getOperations(this, NotifierConstantUtils.ON_THE_OWNER)
        );
    mOperationSelectView.setComponents(
        operations
    );

    mConditionSelectView.setComponents(
        NotifierConstantUtils.getConditions(getBaseContext(), NotifierConstantUtils.ON_THE_OWNER)
    );

  }

  private View.OnClickListener mFilterRootsListener = new View.OnClickListener() {
    @Override public void onClick(View v) {
      selectCondition(-1, null);

      ViewCompat.animate(mConditionSelectView).alpha(0f).setDuration(275).withEndAction(new Runnable() {
        @Override
        public void run() {
          ViewCompat.animate(mConditionSelectView).alpha(1f).setDuration(375).start();
        }
      }).start();

      if (v == mYouRoot) {


        mConditionSelectView.setComponent(null,-1);

        mConditionSelectView.setComponents(
            NotifierConstantUtils.getConditions(getBaseContext(), NotifierConstantUtils.ON_THE_TARGET)
        );

        mYouText.setTextColor(Color.DKGRAY);
        mMeText.setTextColor(Color.WHITE);

        mYouCircle.setColor(getResources().getColor(R.color.white));
        mMeCircle.setColor(getResources().getColor(R.color.colorPrimary));

        AnimationUtils.scaleDownScaleUp(mYouRoot, 1.4f, 1f, 100, 100);
        mMeRoot.setScaleX(0.8f);
        mMeRoot.setScaleY(0.8f);


      } else {

        mConditionSelectView.setComponent(null,-1);

        mConditionSelectView.setComponents(
            NotifierConstantUtils.getConditions(getBaseContext(), NotifierConstantUtils.ON_THE_OWNER)
        );

        mMeText.setTextColor(Color.DKGRAY);
        mYouText.setTextColor(Color.WHITE);

        mMeCircle.setColor(getResources().getColor(R.color.white));
        mYouCircle.setColor(getResources().getColor(R.color.colorPrimary));

        AnimationUtils.scaleDownScaleUp(mMeRoot, 1.4f, 1f, 100, 100);
        mYouRoot.setScaleX(0.8f);
        mYouRoot.setScaleY(0.8f);


      }
    }
  };

  boolean isSelfMode() {
    return mMode == SELF_MODE;
  }

  boolean isBuiltMode() {
    return mMode == BUILT_MODE;
  }

  boolean isSeenMode() {
    return mMode == SEEN_MODE;
  }

  boolean isEditMode() {
    return mMode == EDIT_MODE;
  }

  private VerticalOccView.ComponentClickListener mConditionSelectListener = new VerticalOccView.ComponentClickListener() {
    @Override
    public void onComponentClick(NotifierConstantUtils.Component c) {
      selectCondition(c.uid, null);
    }

    @Override
    public void onTargetChanged() {
      selectCondition(-1, null); // reset.
    }
  };

  private VerticalOccView.ComponentClickListener mOperationSelectListener = new VerticalOccView.ComponentClickListener() {
    @Override
    public void onComponentClick(NotifierConstantUtils.Component c) {
      selectOperation(c.uid, null);
    }

    @Override
    public void onTargetChanged() {
      selectOperation(-1, null); // reset.
    }
  };

  void selectCondition(int uid, @Nullable JSONObject mParams) {

    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction fr = fm.beginTransaction();

    if (uid == -1) {
      if (mConditionFragment != null) {
        fr.remove(mConditionFragment);
        fr.commit();
        mConditionFragment = null;
      }
      findViewById(R.id.conditionContainer).setVisibility(View.GONE);
      findViewById(R.id.conditionContainerCard).setVisibility(View.GONE);
    } else {
      OPCFragment f = OPCFragment.getConditionFragment(uid, mParams);
      if (f == null) {
        return;
      }

      if (mConditionFragment != null) {
        fr.remove(mConditionFragment);
      }

      mConditionFragment = f;

      fr.setTransition(1000);
      fr.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      fr.replace(R.id.conditionContainer, f);
      fr.commit();

      fillPackets();
      inoutCall();

      findViewById(R.id.conditionContainerCard).setVisibility(View.VISIBLE);
      findViewById(R.id.conditionContainer).setVisibility(View.VISIBLE);
      animate(R.id.conditionContainer);
    }
  }

  void selectOperation(int uid, @Nullable JSONObject mParams) {
    FragmentManager fm = getSupportFragmentManager();
    FragmentTransaction ft = fm.beginTransaction();

    if (uid == -1) {
      if (mOperationFragment != null) {
        ft.remove(mOperationFragment);
        ft.commit();
        mOperationFragment = null;
      }
      findViewById(R.id.operationContainerCard).setVisibility(View.GONE);
      findViewById(R.id.operationContainer).setVisibility(View.GONE);
    } else {
      OPCFragment f = OPCFragment.getOperationFragment(uid, mParams);
      if (f != null && (isSeenMode() || isEditMode())) {
        if (mNotifier != null && mNotifier.isTargetAmI())
          f.forTarget();
      }

      if (f == null) {
        return;
      }

      if (mOperationFragment != null) {
        ft.remove(mOperationFragment);
      }

      mOperationFragment = f;

      ft.setTransition(1000);
      ft.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
      ft.replace(R.id.operationContainer, f);
      ft.commit();

      fillPackets();
      inoutCall();

      if (mOperationFragment instanceof OperationPost) {
        ((OperationPost) mOperationFragment).forProfileUpdaters(isSelfMode());
      }

      findViewById(R.id.operationContainerCard).setVisibility(View.VISIBLE);
      findViewById(R.id.operationContainer).setVisibility(View.VISIBLE);
      animate(R.id.operationContainer);
    }
  }

  boolean atTheSameSide(int ct, int ot) {

    return (Conditions.isOnTheOwner(ct) && Operations.isOnTheOwner(ot)) ||
        (Conditions.isOnTheTarget(ct) && Operations.isOnTheTarget(ot));

  }

  void inoutCall() {
    if (mConditionFragment != null && mOperationFragment != null) {
      if (mOperationFragment instanceof OperationNotification) {
        if (Conditions.isCall(mConditionFragment.getType())) {
          if (atTheSameSide(mConditionFragment.getType(), mOperationFragment.getType())) {
            ((OperationNotification) mOperationFragment).noneNotification();
          } else {
            ((OperationNotification) mOperationFragment).showNotification();
          }
        }
      }
    }
  }

  void fillPackets() {
    if (isBuiltMode() && mConditionFragment != null && mOperationFragment != null && !(mOperationFragment instanceof OperationPost)){
      mOperationFragment.setPacketFilter(NotifierConstantUtils.getRelationPackets(mConditionFragment.getType()));
    }
  }

  private void animate(@IdRes int id) {
    View v = findViewById(id);
    v.setAlpha(0);
    ViewCompat.animate(v)
        .alpha(1)
        .setDuration(1000)
        .start();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // getMenuInflater().inflate(R.menu.notifier_builder, menu);
    if (isBuiltMode() || isSelfMode() || isEditMode()) {
      MenuItem add = menu.add(0, 0, 0, R.string.ok);
      add.setIcon(R.drawable.ic_check_18_white);
      add.setVisible(true);
      add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
      findViewById(R.id.saveButton).setVisibility(View.GONE);
    } else {
      if (mNotifier != null) {
        if (mNotifier.amIRelation()) {
          MenuItem miDelete = menu.add(1, 1, 1, R.string.delete);
          miDelete.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

          String miStateTitle = getString(R.string.notification_off);

          if (!mNotifier.isNotificationOn(getApplicationContext())) {
            miStateTitle = getString(R.string.notification_on);
          }

          MenuItem miState = menu.add(1, 2, 2, miStateTitle);
          miState.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        }
      }
    }
    return super.onCreateOptionsMenu(menu);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == 0) {
      trySave();
    } else if (item.getItemId() == 1) {

      if (!mNotifier.isProfileUpdater())
        NotifierEventSentUtils.sendDeleted(mNotifier);

      if (Conditions.isTimely(mNotifier.getCondition().getId())) {
        EnvironmentManager.init().removeTimelyNotifierEnvironment(getApplicationContext(), mNotifier.getId());
      } else if (Conditions.isLocational(mNotifier.getCondition().getId())) {
        EnvironmentManager.init().removeLocationalNotifierEnvironment(getApplicationContext(), mNotifier.getId());
      }

      NotifierProvider.Utils.delete(getApplicationContext(), mNotifier.getId());
      NotifierEventDispatcher.getInstance().dispatchDelete(mNotifier.getId());

      finish();
    } else if (item.getItemId() == 2) {

      if (mNotifier != null) {
        Notifier.setNotificationState(getApplicationContext(), mNotifier.getId(), !mNotifier.isNotificationOn(getApplicationContext()));
        String miStateTitle = getString(R.string.notification_off);
        if (!mNotifier.isNotificationOn(getApplicationContext())) {
          miStateTitle = getString(R.string.notification_on);
        }
        item.setTitle(miStateTitle);
      }

    }
    return super.onOptionsItemSelected(item);
  }

  public static Bitmap loadBitmapFromView(View v) {
    Bitmap b = Bitmap.createBitmap(1500, 500, Bitmap.Config.ARGB_8888);
    Canvas c = new Canvas(b);
    v.layout(0, 0, 1500, 500);
    v.draw(c);
    return b;
  }

  private void trySave() {

    if (isSeenMode()) {
      finish();
      return;
    }

    if (mDestination != null) {
      BlackListActivity.BlackLocaleBase blk = new BlackListActivity.BlackLocaleBase(getBaseContext());
      if (blk.isAlreadyBlack(mDestination)) {
        alert(getString(R.string.this_is_an_black_man_so_you_cannot_add_notifier));
        blk.close();
        return;
      }
      blk.close();
    }

    if (checkArguments()) {

      if (isBuiltMode() || isSelfMode()) {
        Notifier.Builder builder = new Notifier.Builder();
        builder.setOperation(mOperationFragment.getParams())
            .setCondition(mConditionFragment.getParams())
            .setTarget(isSelfMode() ? "PROFILE_UPDATER" : mDestination)
            .setOwner(PresenceManager.uid())
            .setRelationshipState(Notifier.NOT_DETERMINED)
            .setState(Notifier.NOT_DETERMINED)
            .setTimestamp(System.currentTimeMillis());
        mNotifier = builder.built();


        int isExists = isBuiltMode()
            ?
            NotifierProvider.Utils.isNotifierExistsForTarget(
                getBaseContext(), mDestination, mNotifier.getCondition().toString(), mNotifier.getOperation().toString(), mNotifier.getOperation().getPacketType()
            )
            :
            NotifierProvider.Utils.isExistsProfileUpdater(
                getBaseContext(), mNotifier.getCondition().toString(), mNotifier.getOperation().toString(),
                mNotifier.getOperation().getPacketType()
            );

        if (isExists > 0) {
          Toast.makeText(this, R.string.notifier_already_exists, Toast.LENGTH_SHORT).show();
          Intent intent = new Intent();
          intent.putExtra(NOTIFIER_ID, isExists);
          intent.putExtra(ALREADY_EXISTS, true);
          setResult(RESULT_OK, intent);
          finish();
          return;
        }


      }


      if (isBuiltMode()) {

        mNotifierSaveDialog = new NotifierSaveDialog(NotifierBuilderActivity.this, isBuiltMode(), mNotifierSaveDialogListener, mNotifier);
        mNotifierSaveDialog.show();
      } else if (isEditMode()) {

        if (mNotifier.getCondition().getId() != mConditionFragment.getType()) {
          Snackbar.make(activityNewMain,
              R.string.you_cannot_change_condition_of_existing_notifier,
              Snackbar.LENGTH_LONG).show();
          return;
        }

        if (mNotifier.getOperation().getId() != mOperationFragment.getType()) {
          Snackbar.make(activityNewMain,
              R.string.you_cannot_change_condition_of_existing_notifier,
              Snackbar.LENGTH_LONG).show();
          return;
        }

        AlertDialog.Builder mDialogBuilder = new AlertDialog.Builder(this);
        mDialogBuilder.setTitle(R.string.are_you_sure)
            .setMessage(mNotifier.isProfileUpdater() ? R.string.are_you_sure_to_edit_profile_updater :R.string.are_you_sure_for_editing_this_notifier)
            .setCancelable(true)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
              @Override
              public void onClick(DialogInterface dialog, int which) {

                Notifier.Builder mBuilder = new Notifier.Builder();
                mBuilder.setSID(mNotifier.getSid())
                    .setCondition(mConditionFragment.getParams())
                    .setOperation(mOperationFragment.getParams())
                    .setTarget(mNotifier.getReceiver())
                    .setOwner(mNotifier.getSender())
                    .setProfileUpdater(mNotifier.isProfileUpdater())
                    .setTimestamp(mNotifier.getTimestamp())
                    .setState(mNotifier.getState());



                Notifier newNotifier = mBuilder.built();
                newNotifier.setId(mNotifier.getId());

                NotifierProvider.Utils.updateNotifier(getBaseContext(), newNotifier);


                Intent intent = new Intent();
                intent.putExtra(NOTIFIER_ID, mNotifierId);
                setResult(RESULT_OK, intent);
                Analytics.instance(getApplicationContext()).log(newNotifier);
                finish();
              }
            });
        mDialogBuilder.show();
      } else if (isSelfMode()) {


        mNotifierSaveDialog = new NotifierSaveDialog(NotifierBuilderActivity.this, true, new NotifierSaveDialog.OnCompleteListener() {
          @Override
          public void onDescriptionEntered(boolean global, String desc) {
            mNotifier.setDescription(desc);
            mNotifier.itIsProfileUpdater();
            mNotifier.setSid(generateRandomSid());
            int id = NotifierProvider.Utils.newNotifier(getApplicationContext(), mNotifier);
            Intent intent = new Intent();
            intent.putExtra(NOTIFIER_ID, id);
            setResult(RESULT_OK, intent);
            Analytics.instance(getApplicationContext()).log(mNotifier);
            finish();
          }
        }, mNotifier);
        mNotifierSaveDialog.setCheckButtonText(getString(R.string.save_profile_updater));
        mNotifierSaveDialog.hideNote();
        mNotifierSaveDialog.show();
      } else {
        finish();
      }
    }
  }


  @OnClick({R.id.saveButton, R.id.backButton, R.id.avatar})
  public void onClick(final View v) {
    if (v.getId() == R.id.saveButton) {
      AnimationUtils.scaleDownScaleUp(v, 0.7f, 1f, 100, 75);
      trySave();
    } else if (v.getId() == R.id.backButton || v.getId() == R.id.avatar) {
      super.onBackPressed();
    }
  }

  private String generateRandomSid() {
    return "SID_" + System.currentTimeMillis();
  }


  private NotifierSaveDialog.OnCompleteListener mNotifierSaveDialogListener = new NotifierSaveDialog.OnCompleteListener() {
    @Override
    public void onDescriptionEntered(boolean global, String desc) {
      mNotifier.setDescription(desc);
      ComponentSender componentSender = new ComponentSender("notifiers/" + mNotifier.getReceiver(), mNotifier);
      DialogStoreManagement.getInstance(getApplicationContext()).onNewComponent(mNotifier);
      String sid = componentSender.sent();
      mNotifier.setSid(sid);
      int id = NotifierProvider.Utils.newNotifier(getApplicationContext(), mNotifier);
      Intent intent = new Intent();
      intent.putExtra(NOTIFIER_ID, id);
      intent.putExtra(DESTINATION, mDestination);
      setResult(RESULT_OK, intent);
      finish();
    }
  };

  private boolean checkArguments() {
    if (mConditionFragment == null) {
      alert(getString(R.string.please_select_a_condition));
      return false;
    } else if (mOperationFragment == null) {
      alert(getString(R.string.please_select_a_operation));
      return false;
    }
    boolean result = mConditionFragment.checkForms() && mOperationFragment.checkForms();
    if (!result) {
      alert(mConditionFragment.getAlert());
      alert(mOperationFragment.getAlert());
    }
    return result;
  }

  void alert(String str) {
    if (!TextUtils.isEmpty(str)) {
      final WeakReference<View> v = new WeakReference<>(findViewById(R.id.saveButton));
      ViewCompat.animate(v.get())
          .scaleX(0)
          .scaleY(0)
          .setDuration(100)
          .withEndAction(new Runnable() {
            @Override
            public void run() {
              if (v.get() != null)
                v.get().postDelayed(new Runnable() {
                  @Override
                  public void run() {
                    if (v.get() != null)
                      ViewCompat.animate(v.get())
                          .scaleX(1)
                          .scaleY(1)
                          .setDuration(200)
                          .start();
                  }
                }, 2100);
            }
          })
          .start();
      Snackbar.make(activityNewMain, str, 2400).setAction(R.string.ok, new View.OnClickListener() {
        @Override
        public void onClick(View vv) {
          if (v.get() != null)
            v.get().setScaleY(1);
          v.get().setScaleX(1);
        }
      }).show();
    }
  }

  public static void buildForResult(Activity ac, String destination, int requetsCode) {
    Intent mIntent = new Intent(ac, NotifierBuilderActivity.class);
    mIntent.putExtra(DESTINATION, destination);
    ac.startActivityForResult(mIntent, requetsCode);
  }

  public static void buildForResult(Fragment ac, String destination, int requetsCode) {
    Intent mIntent = new Intent(ac.getContext(), NotifierBuilderActivity.class);
    mIntent.putExtra(DESTINATION, destination);
    ac.startActivityForResult(mIntent, requetsCode);
  }

  public static void seen(Context context, int id) {
    Intent mIntent = new Intent(context, NotifierBuilderActivity.class);
    mIntent.putExtra(BOOL_ONLY_SEEN, true);
    mIntent.putExtra(NOTIFIER_ID, id);
    context.startActivity(mIntent);
  }

  public static void seen(Context context, JSONObject params) {
    Intent intent = new Intent(context, NotifierBuilderActivity.class);
    intent.putExtra(BOOL_ONLY_SEEN, true);
    intent.putExtra(NOTIFIER_PARAMS, params.toString());
    context.startActivity(intent);
  }

  public static void seenWithNotifierView(Context context, Notifier r) {
    Intent intent = new Intent(context, NotifierBuilderActivity.class);
    intent.putExtra(BOOL_ONLY_SEEN, true);
    intent.putExtra(NOTIFIER_PARAMS, r.toString());
    intent.putExtra(SHOW_NOTIFIER_VIEW, true);
    context.startActivity(intent);
  }

  public static void editForResult(Fragment ac, int id, int requestCode) {
    Intent mIntent = new Intent(ac.getContext(), NotifierBuilderActivity.class);
    mIntent.putExtra(NOTIFIER_ID, id);
    ac.startActivityForResult(mIntent, requestCode);
  }

  public static void editForResult(Activity ac, int id, int requestCode) {
    Intent mIntent = new Intent(ac, NotifierBuilderActivity.class);
    mIntent.putExtra(NOTIFIER_ID, id);
    ac.startActivityForResult(mIntent, requestCode);
  }

  public static void profileUpdaterBuilder(Activity ac, int requestCode) {
    Intent intent = new Intent(ac, NotifierBuilderActivity.class);
    intent.putExtra(PROFILE_UPDATER_BUILDER, true);
    ac.startActivityForResult(intent, requestCode);
  }

  @Override
  protected void onPause() {
    super.onPause();
    if (mNotifierSaveDialog != null) {
      if (mNotifierSaveDialog.isShowing()) {
        mNotifierSaveDialog.dismiss();
      }
    }
  }

  public static boolean isAlreadyExists(@Nullable Intent intent) {
    return intent != null && intent.getExtras() != null && intent.getBooleanExtra(ALREADY_EXISTS, false);
  }

  public void setToolbar(String mDestination) {

    if (isSeenMode() || isEditMode()) {
      mSubtitle.setVisibility(View.GONE);
      if (mNotifier != null && mNotifier.isProfileUpdater()) {
        mName.setText(isSeenMode() ? R.string.details : R.string.edit_profile_updater);
      } else {
        mName.setText(isSeenMode() ? R.string.details_of_notifier: R.string.edit_notifier);
      }
      mName.setTextSize(18);
      mAvatar.setVisibility(View.GONE);
    } else {
      mAvatar.setVisibility(View.VISIBLE);
      AvatarLoader.load(mAvatar, ContactBase.Utils.getValidAvatar(this, mDestination), ContactBase.Utils.getName(this
          , mDestination));
      mName.setText(ContactBase.Utils.getName(this, mDestination));
    }
  }

}
