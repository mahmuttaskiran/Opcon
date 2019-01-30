package com.opcon.ui.views;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Post;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.settings.BlackListActivity;
import com.opcon.libs.utils.AnimationUtils;
import com.opcon.libs.utils.TimeUtils;
import com.opcon.notifier.components.Notifier;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.ui.activities.FullScreenImageViewerActivity;
import com.opcon.ui.activities.NotifierBuilderActivity;
import com.opcon.ui.fragments.occs.ConditionLocation;
import com.vanniktech.emoji.EmojiTextView;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import agency.tango.android.avatarview.views.AvatarView;
import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;


/**
 * Created by Mahmut Taşkiran on 18/03/2017.
 *
 */

public class PostView extends LinearLayout {

  private final PostViewEventListener.DefaultEventListener DEFAULT_EVENT_LISTENER = new PostViewEventListener.DefaultEventListener();

  public interface PostViewEventListener {
    void onRemoveRequest(Post post);
    void onComplaintRequest(Post post);
    void onPrivacyChanged(Post post, int newPrivacy);
    class DefaultEventListener implements PostViewEventListener {
      @Override public void onRemoveRequest(Post post) {
        String sid = post.getSid();
        if (!TextUtils.isEmpty(sid) && post.isMine()) {
          FirebaseDatabase.getInstance().getReference("posts/" + post.getOwner() + "/" + sid).removeValue();
        }
      }
      @Override public void onComplaintRequest(Post post) {}
      @Override public void onPrivacyChanged(Post post, int newPrivacy) {
        if (post.isMine() && post.getSid() != null) {
          post.setPrivacy(newPrivacy);
          FirebaseDatabase.getInstance().getReference("posts/"  + post.getOwner() + "/" + post.getSid()).updateChildren(post.toMap());
        }
      }
    }
  }

  @BindView(R.id.avatar)
  AvatarView mAvatar;
  @BindView(R.id.title)
  TextView mName;

  @BindView(R.id.text)
  EmojiTextView mText;

  @BindView(R.id.charge_text)
  TextView mChargePercent;
  @BindView(R.id.charge)
  RelativeLayout mRootCharge;
  @BindView(R.id.location_image)
  ImageView mLocationImage;
  @BindView(R.id.location_text)
  TextView mLocationText;
  @BindView(R.id.location)
  RelativeLayout mRootLocation;
  @BindView(R.id.lastImageView)
  ImageView mLastImage;
  @BindView(R.id.lastCapturedImage)
  RelativeLayout mRootLastImage;
  @BindView(R.id.privacy)
  ImageView mPrivacy;
  @BindView(R.id.timestamp)
  TextView mTimestamp;
  @BindView(R.id.options)
  ImageView mOptions;
  @BindView(R.id.chatInput)
  NewChatInput mChatInput;
  @BindView(R.id.uploading)
  RelativeLayout mUploading;
  @BindView(R.id.titleDivider)
  View mTitleDivider;

  @BindView(R.id.seeRelationalNotifier)
  CardView mSeeRelationNotifier;

  Post mPost;
  PostViewEventListener mListener;

  PopupMenu mOptionsMenu;

  @OnClick({R.id.privacy, R.id.options, R.id.location_image, R.id.lastImageView, R.id.seeRelationalNotifier})
  public void onClick(View v ) {
    if (v.getId() == mOptions.getId() ){
      mOptionsMenu.show();
    } else if (v.getId() == R.id.location_image) {
      if (mPost.getLatitude() != 0) {
        ConditionLocation.showLocation(getContext(), mPost.getLatitude(), mPost.getLongitude());
      }
    } else if (v.getId() == R.id.lastImageView) {
      if (getValidImagePath() != null) {
        FullScreenImageViewerActivity.show(getContext(), getValidImagePath());
      }
    } else if (v.getId() == R.id.seeRelationalNotifier) {
      AnimationUtils.scaleDownScaleUp(v, 0.7f, 1f, 100, 75);
      Notifier rl = mPost.getRelationNotifier();
      if (rl != null) {
        NotifierBuilderActivity.seenWithNotifierView(getContext(), rl);
      }
    }
  }

  public PostView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    LayoutInflater.from(getContext()).inflate(R.layout.post_view, this);
  }

  public void post(Post post) {
    this.mPost = post;
    mName.setText(post.getOwnerName(getContext()));


    if (post.isRcs() && post.getRelationNotifier() != null) {
      mSeeRelationNotifier.setVisibility(VISIBLE);
    } else {
      mSeeRelationNotifier.setVisibility(GONE);
    }

    AvatarLoader.load(mAvatar, post.getOwnerAvatar(getContext()), post.getOwnerName(getContext()));

    setText(post.getText());
    mTimestamp.setText(TimeUtils.shortDateAndTime(post.getTimestamp(), TimeUnit.HOURS.toMillis(2)));
    initOptions();
    setPrivacy(post.getPrivacy());


    if (post.isHaveBody()) {
      if (post.getPacketType() == Packets._LAST_IMAGE) {
        gone(mRootCharge, mRootLocation);
        Uri ip = getValidImagePath();

        if (ip != null) {
          Glide.with(getContext())
              .load(ip)
              .placeholder(R.drawable.no_item_background)
              .centerCrop()
              .sizeMultiplier(0.5f)
              .diskCacheStrategy(DiskCacheStrategy.SOURCE)
              .into(mLastImage);

          visible(mRootLastImage, mLastImage);
        } else {
          gone(mRootLastImage);
        }

      } else if (post.getPacketType() == Packets._LOCATION) {
        gone(mRootLastImage, mRootCharge);
        visible(mRootLocation);
        double latitude = post.getLatitude();
        double longitude = post.getLongitude();
        String address = post.getAddress();

        if (TextUtils.isEmpty(address)) {
          gone(mLocationText);
        } else {
          visible(mLocationText);
          mLocationText.setText(address);
        }

        String url = ConditionLocation.getURLFor(latitude, longitude, 600, 300, 15, true);
        Glide.with(getContext()).load(url).into(mLocationImage);
      } else if (mPost.getPacketType() == Packets._BATTERY_LEVEL) {
        gone(mRootLocation, mRootLastImage);
        visible(mRootCharge);
        int percent = mPost.getBatteryDegree();
        mChargePercent.setText(String.format("%%%d ", percent));
      }
      gone(mTitleDivider);
    } else {
      visible(mTitleDivider);
      gone(mRootCharge, mRootLastImage, mRootLocation);
    }
  }

  public void editableMode(Activity activity) {
    mChatInput.setVisibility(VISIBLE);
    mChatInput.setActivityReference(activity);
    mChatInput.setText(null);
    String text = getContext().getString(R.string.do_write_something);
    mChatInput.setHint(TextUtils.isEmpty(mPost.getText()) ? text: mPost.getText());
    mChatInput.setVisibilityOfSendButton(false);
  }

  public boolean isEdited() {
    return mChatInput.getText() != null;
  }

  public String getEditedText() {
    return  mChatInput.getText();
  }

  Uri getValidImagePath() {

    String downloadLink, localePath;
    downloadLink = mPost.getImageDownloadUrl();
    localePath = mPost.getImageLocalePath();

    if (TextUtils.isEmpty(downloadLink)) {
      if (!TextUtils.isEmpty(localePath)) {
        File file = new File(mPost.getImageLocalePath());
        if (file.exists()) {
          return Uri.fromFile(file);
        }
        return Uri.parse(localePath);
      }
      return null;
    } else {
      return Uri.parse(downloadLink);
    }
  }

  void gone(View ...v) {
    for (View view : v) {
      view.setVisibility(GONE);
    }
  }

  void visible(View ...v) {
    for (View view : v) {
      view.setVisibility(VISIBLE);
    }
  }

  void setPrivacy(int p) {
    // TODO!!!
    mPrivacy.setVisibility(GONE);
  }

  void initOptions() {
    mOptionsMenu = new PopupMenu(getContext(), mOptions);
    if (mPost.isMine()) {
      mOptionsMenu.getMenu().add(0,0,0, R.string.remove_from_profile);
    } else {
      mOptionsMenu.getMenu().add(0,1,0, R.string.complaint_it);
    }
    mOptionsMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == 0) {
          DEFAULT_EVENT_LISTENER.onRemoveRequest(mPost);
          if (mListener != null) mListener.onRemoveRequest(mPost);
        } else {

          AlertDialog.Builder mAlert = new AlertDialog.Builder(getContext());
          mAlert.setTitle(R.string.do_you_want_to_block_that_user)
              .setMessage(R.string.your_complaint_will_evaluate)
              .setPositiveButton(R.string.yes_block, new DialogInterface.OnClickListener() {
                @Override public void onClick(DialogInterface dialog, int which) {
                  getContext().startActivity(new Intent(getContext(), BlackListActivity.class));
                }
              })
              .setNegativeButton(R.string.no, null)
              .show();
          complaint();
          DEFAULT_EVENT_LISTENER.onComplaintRequest(mPost);
          if (mListener != null) {
            mListener.onComplaintRequest(mPost);
          }
        }
        return false;
      }
    });
  }

  public void hideOptions() {
    mOptions.setVisibility(GONE);
  }

  public void setPostEventListener(PostViewEventListener listener) {
    mListener = listener;
  }

  void setText(String text) {
    if (TextUtils.isEmpty(text))  {
      mText.setVisibility(GONE);
    } else {
      mText.setVisibility(VISIBLE);
      mText.setText(text);
    }
  }

  public void setUploading(boolean bool) {
    if (bool) {
      visible(mUploading);
    } else {
      gone(mUploading);
    }
  }

  private void complaint() {
    DatabaseReference complaintsPush = FirebaseDatabase.getInstance()
        .getReference("complaints").push();
    Map<String, Object> map = new HashMap<>();
    map.put("complainant", PresenceManager.uid());
    map.put("accused", mPost.getOwner());
    map.put("relationPost", mPost.toMap());
    map.put("timestamp", System.currentTimeMillis());
    map.put("includeImage", mPost.getImageDownloadUrl() != null);
    complaintsPush.updateChildren(map);
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    try {
      ButterKnife.setDebug(true);
      ButterKnife.bind(this, this);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
