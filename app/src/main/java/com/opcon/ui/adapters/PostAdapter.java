package com.opcon.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.opcon.R;
import com.opcon.components.Post;
import com.opcon.libs.post.PostPoster;
import com.opcon.ui.views.PostView;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 *
 * Created by Mahmut Taşkiran on 20/03/2017.
 */

public class PostAdapter extends RecyclerView.Adapter {

  private List<Post> mPosts;
  private PostView.PostViewEventListener mListener;

  public PostAdapter(List<Post> mPosts, PostView.PostViewEventListener listener) {
    this.mPosts = mPosts;
    this.mListener = listener;
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new PostHolder(
        LayoutInflater.from(parent.getContext()).inflate(R.layout.row_post, parent, false)
        , this
    );
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    ((PostHolder) holder).post(mPosts.get(position));
  }

  @Override
  public int getItemCount() {
    return mPosts.size();
  }


  static class PostHolder extends RecyclerView.ViewHolder implements PostView.PostViewEventListener {
    @BindView(R.id.postView)
    PostView mPostView;
    PostAdapter mAdapter;
    PostHolder(View view, PostAdapter ar) {
      super(view);
      this.mAdapter = ar;
      ButterKnife.bind(this, view);
    }
    void post(Post p) {
      mPostView.post(p);
      mPostView.setPostEventListener(this);
      mPostView.setUploading(PostPoster.getInstance(mPostView.getContext()).isPosting(p));
    }

    @Override public void onRemoveRequest(Post post) {
      int indexOfPost = mAdapter.mPosts.indexOf(post);
      mAdapter.mPosts.remove(post);
      if (indexOfPost != -1) {
        mAdapter.notifyItemRemoved(indexOfPost);
      }
      mAdapter.mListener.onRemoveRequest(post);
    }

    @Override public void onComplaintRequest(Post post) {
      mAdapter.mListener.onComplaintRequest(post);
    }

    @Override public void onPrivacyChanged(Post post, int newPrivacy) {
      int indexOfPost = mAdapter.mPosts.indexOf(post);
      if (indexOfPost != -1) {
        mAdapter.notifyItemChanged(indexOfPost);
      }
      mAdapter.mListener.onPrivacyChanged(post, newPrivacy);
    }
  }
}
