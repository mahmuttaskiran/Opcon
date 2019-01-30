package com.opcon.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.opcon.R;
import com.opcon.components.Post;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.post.PostPoster;
import com.opcon.ui.adapters.PostAdapter;
import com.opcon.ui.views.CloudRelativeView;
import com.opcon.ui.views.PostView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by Mahmut Taşkiran on 20/03/2017.
 */

public class PostFragment extends Fragment implements ChildEventListener, PostPoster.PostEventListener,
    PostView.PostViewEventListener{

  @BindView(R.id.there_is_no_post)
  CloudRelativeView mThereIsNoPost;
  @BindView(R.id.recyclerView)
  RecyclerView mRecyclerView;
  @BindView(R.id.there_is_no_post_message)
  TextView mMessage;

  private PostPoster mPostPoster;

  private PostAdapter mAdapter;
  private String mDestination;
  private Query mRef;
  private List<Post> mPosts;

  public static PostFragment newInstanceFor(String destination) {
    PostFragment pf = new PostFragment();
    pf.mDestination = destination;
    return pf;
  }

  @Override public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mPostPoster = PostPoster.getInstance(getContext());
    if (savedInstanceState != null) {
      if (TextUtils.isEmpty(mDestination)) {
        mDestination = savedInstanceState.getString("destination");
      }
    }
    mPosts = new ArrayList<>();
    mRef = FirebaseDatabase.getInstance().getReference("posts/" + mDestination);
    mRef.keepSynced(true);
    mRef.addChildEventListener(this);
    mPostPoster.addEventListener(this);
  }

  @Override public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("destination", mDestination);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    mPostPoster.removeEventListener(this);
    mRef.removeEventListener(this);
  }

  @Nullable @Override public View
  onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_post, container, false);
    ButterKnife.bind(this, v);
    mAdapter = new PostAdapter(mPosts, this);
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    mRecyclerView.setAdapter(mAdapter);
    return v;
  }

  void decideVisibilityOfMessage() {
    if (PresenceManager.uid().equals(mDestination)) {
      mMessage.setText(getString(R.string.there_is_no_any_post_add));
    } else {
      mMessage.setText(getString(R.string.there_is_no_any_post));
    }
    if (mPosts.isEmpty()) {
      mThereIsNoPost.setVisibility(View.VISIBLE);
    } else {
      mThereIsNoPost.setVisibility(View.GONE);
    }
  }

  @Override public void onResume() {
    super.onResume();
    decideVisibilityOfMessage();
  }


  @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {
    if (dataSnapshot.getValue() != null && dataSnapshot.getValue() instanceof Map) {
      Post p = toPost((Map<String, Object>) dataSnapshot.getValue());
      p.setSid(dataSnapshot.getKey());
      addPost(p);
    }
  }

  @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {}
  @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
  @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
  @Override public void onCancelled(DatabaseError databaseError) {}


  private Comparator<Post> mPostComparator = new Comparator<Post>() {
    @Override
    public int compare(Post o1, Post o2) {
      long diff = o2.getTimestamp() - o1.getTimestamp();
      return (int) diff ;
    }
  };

  private void addPost(final Post post) {
    if (!mPosts.contains(post)) {
      mPosts.add(post);
      Collections.sort(mPosts, mPostComparator);
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          decideVisibilityOfMessage();
          mAdapter.notifyItemChanged(mPosts.size());
        }
      });
    }

  }

  Post toPost(Map<String, Object> snapshot) {
    Post built1 = Post.newBuilder().built();
    built1.put(snapshot);
    return built1;
  }

  @Override
  public void onPosted(Post post) {
    if (getActivity() != null ){
      getActivity().runOnUiThread(new Runnable() {
        @Override public void run() {
          if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
          }
        }
      });
    }
  }

  @Override
  public void onFail(Post post) {
    // ignore
  }

  @Override
  public void onRemoveRequest(Post post) {
    decideVisibilityOfMessage();
  }

  @Override
  public void onComplaintRequest(Post post) {
    // ignore
  }

  @Override
  public void onPrivacyChanged(Post post, int newPrivacy) {
    // ignore
  }
}
