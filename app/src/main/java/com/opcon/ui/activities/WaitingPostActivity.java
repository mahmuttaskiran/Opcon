package com.opcon.ui.activities;

import android.Manifest;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.opcon.R;
import com.opcon.components.Post;
import com.opcon.database.PostBase;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.permission.PermissionRequest;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.libs.post.PostPoster;
import com.opcon.ui.views.PostView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;
import timber.log.Timber;


public class WaitingPostActivity extends AppCompatActivity {

  public static final int NOTIFICATION_ID  =63;

  public static class WaitingPostEventDispatcher {
    public volatile static WaitingPostEventDispatcher singleton;
    private interface EventListener {
      void onNewPost(Post p);
      void onDeleteRequest(Post p);
      void onShareRequest(Post p);
    }
    private EventListener mEventListener;
    public static WaitingPostEventDispatcher getInstance() {
      if (singleton == null) {
        synchronized (WaitingPostEventDispatcher.class ){
          if (singleton == null ){
            singleton = new WaitingPostEventDispatcher();
          }
        }
      }
      return singleton;
    }
    private void setEventListener(EventListener e) {
      mEventListener = e;
    }
    public void dispatchNewPost(Post p) {

      if(mEventListener != null) {
        mEventListener.onNewPost(p);
      }
    }
    private void onDelete(Post p ){
      if(mEventListener != null) {
        mEventListener.onDeleteRequest(p);
      }
    }
    private void onShare(Post p ){
      if(mEventListener != null) {
        mEventListener.onShareRequest(p);
      }
    }
  }

  private PermissionManagement mPermissionManagement;

  WaitingPostEventDispatcher.EventListener mEventListener = new WaitingPostEventDispatcher.EventListener() {
    @Override public void onNewPost(final Post p) {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (getBaseContext() != null && mRecyclerView != null) {
            mAdapter.mPosts.add(p);
            mAdapter.notifyItemInserted(mAdapter.mPosts.size());
          }
          decideVisibilityOfNoElementRoot();
        }
      });
    }

    @Override public void onDeleteRequest(Post p) {
      decideVisibilityOfNoElementRoot();
    }
    @Override public void onShareRequest(final Post post) {
      if (!TextUtils.isEmpty(post.getImageLocalePath()) && PermissionUtils.check(getBaseContext(), getStoragePermisison())) {
        mPermissionManagement.builtRequest(post.getId(), getStoragePermisison())
            .observer(new PermissionManagement.PermissionEventListener() {
              @Override
              public void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest) {
                share(post);
              }
              @Override public void onAnyPermissionsDenied(int requestCode, PermissionRequest permissionRequest) {}
            })
        .request();
      } else {
        share(post);
      }
    }

    private void share(Post post) {
      post.setAccepted(true);
      PostBase.Utils.update(getBaseContext(), post);
      PostPoster.getInstance(getApplicationContext()).postIt(post);
      int p = mAdapter.mPosts.indexOf(post);
      if (p != RecyclerView.NO_POSITION) {
        mAdapter.mPosts.remove(p);
        mAdapter.notifyItemRemoved(p);
      }
      decideVisibilityOfNoElementRoot();
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuItem add = menu.add(0, 0, 0, R.string.delete_all);
    add.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
    return super.onCreateOptionsMenu(menu);
  }


  void decideVisibilityOfNoElementRoot() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        if (mAdapter == null || mAdapter.mPosts == null || mAdapter.mPosts.isEmpty()) {
          View viewById = findViewById(R.id.there_is_no_element_ll);
          if (viewById != null)
            viewById.setVisibility(View.VISIBLE);
        } else {
          View viewById = findViewById(R.id.there_is_no_element_ll);
          if (viewById != null) {
            viewById.setVisibility(View.GONE);
          }
        }

      }
    });
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    mPermissionManagement.dispatchEvent(requestCode, grantResults);
  }

  PermissionRequest getStoragePermisison() {
    PermissionRequest pr;
    if (Build.VERSION.SDK_INT >= 16) {
      pr = new PermissionRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    } else {
      pr = new PermissionRequest(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    return pr;
  }

  @BindView(R.id.recyclerView)
  RecyclerView mRecyclerView;

  WaitingPostAdapter mAdapter;

  private LinearLayoutManager mLm;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_waiting_post);
    ButterKnife.bind(this);

    PostBase.Utils.deletePostThatRelationWithRemovedImages(this);

    mPermissionManagement = PermissionManagement.with(this);

    WaitingPostEventDispatcher.getInstance().setEventListener(mEventListener);

    NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    nm.cancel(NOTIFICATION_ID);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
      getSupportActionBar().setTitle(R.string.waiting_posts_activity_title);
    }

    mLm = new LinearLayoutManager(getApplicationContext());
    mRecyclerView.setLayoutManager(mLm);
    mAdapter = getAdapter();
    mRecyclerView.setAdapter(mAdapter);

    if (mAdapter.mPosts != null) {
      mLm.scrollToPosition(mAdapter.mPosts.size());
    }

    decideVisibilityOfNoElementRoot();
  }

  @Override
  protected void onDestroy() {
    WaitingPostEventDispatcher.getInstance().setEventListener(null);
    super.onDestroy();
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    } else if (item.getItemId() == 0) {


      AlertDialog.Builder b = new AlertDialog.Builder(WaitingPostActivity.this);
      b.setTitle(R.string.are_you_sure)
          .setMessage(R.string.are_you_sure_about_that_remove_all_waiting_posts)
          .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override public void onClick(DialogInterface dialog, int which) {
              PostBase.Utils.deleteAll(getApplicationContext());
              if (mAdapter != null) {
                mAdapter.mPosts.clear();
                mAdapter.notifyDataSetChanged();
              }
              decideVisibilityOfNoElementRoot();
            }
          })
          .setNegativeButton(R.string.no, null);

      b.show();


    }
    return super.onOptionsItemSelected(item);
  }

  private WaitingPostAdapter getAdapter() {
    List<Post> post = PostBase.Utils.getAll(getApplicationContext());
    mAdapter = new WaitingPostAdapter(post, this);
    return mAdapter;
  }

  public static class WaitingPostAdapter extends RecyclerView.Adapter {
    List<Post> mPosts;
    WaitingPostActivity mActivity;
    private WaitingPostAdapter(List<Post> mPosts, WaitingPostActivity mActivity) {
      this.mPosts = mPosts;
      this.mActivity = mActivity;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new WaitingPostHolder(LayoutInflater.from(parent.getContext())
          .inflate(R.layout.row_waiting_post, parent, false), this);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      ((WaitingPostHolder) holder).post(mPosts.get(position));
      ((WaitingPostHolder) holder).mPostView.editableMode(mActivity);
    }

    @Override
    public int getItemCount() {
      return mPosts.size();
    }

    public static class WaitingPostHolder extends RecyclerView.ViewHolder implements PostView.PostViewEventListener, View.OnClickListener{
      @BindView(R.id.postView)
      PostView mPostView;
      @BindView(R.id.share)
      ImageButton mShare;
      @BindView(R.id.delete)
      ImageButton mDelete;

      WaitingPostAdapter mAdapter;

      private WaitingPostHolder(View view, WaitingPostAdapter adapter) {
        super(view);
        this.mAdapter = adapter;
        ButterKnife.bind(this, view);
        mPostView.setPostEventListener(this);
        mDelete.setOnClickListener(this);
        mShare.setOnClickListener(this);
      }

      public void post(Post post) {
        mPostView.post(post);
        mPostView.hideOptions();
      }

      @Override public void onRemoveRequest(Post post) {
        remove(post);
      }

      @Override public void onComplaintRequest(Post post) {}

      @Override public void onPrivacyChanged(Post post, int newPrivacy) {
        PostBase.Utils.updatePrivacy(mDelete.getContext(), post.getId(), newPrivacy);
        post.setPrivacy(newPrivacy);
        int p = getAdapterPosition();
        if (p != -1)
          mAdapter.notifyItemChanged(p);
      }

      @Override public void onClick(View v) {
        if (getAdapterPosition() == RecyclerView.NO_POSITION)
          return;

        if (v.getId() == mDelete.getId()) {
          remove(mAdapter.mPosts.get(getAdapterPosition()));
        } else if (v.getId() == mShare.getId()) {
          Post post = mAdapter.mPosts.get(getAdapterPosition());


          if (post != null) {

            if (mPostView.isEdited()) {
              String editedText = mPostView.getEditedText();
              if (!TextUtils.isEmpty(editedText)) {
                post.setText(editedText);
              }
            }
            share(post);
          }


        }
      }

      private void share(Post post) {
        WaitingPostEventDispatcher.getInstance().onShare(post);
      }

      public void remove(Post post) {
        PostBase.Utils.delete(mDelete.getContext(), post.getId());
        int p = getAdapterPosition();
        if (p != RecyclerView.NO_POSITION) {
          mAdapter.mPosts.remove(p);
          mAdapter.notifyItemRemoved(p);
        }
        WaitingPostEventDispatcher.getInstance().onDelete(post);
      }
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }
}
