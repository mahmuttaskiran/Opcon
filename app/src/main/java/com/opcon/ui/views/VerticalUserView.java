package com.opcon.ui.views;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Contact;
import com.opcon.utils.PreferenceUtils;

import java.util.List;

import agency.tango.android.avatarview.views.AvatarView;

/**
 * Created by Mahmut Taşkiran on 03/03/2017.
 */

public class VerticalUserView extends RelativeLayout {

  public static void setVisibilitySettings(Context context, boolean bool) {
    PreferenceUtils.putBoolean(context, "visibilityOfUserInviteView", bool);
  }

  public static boolean isChoiceIsVisible(Context context) {
    return PreferenceUtils.getBoolean(context, "visibilityOfUserInviteView", true);
  }

  RecyclerView mRecyclerView;
  VerticalUserViewAdapter mAdapter;
  TitleView mSeeAll;

  public interface VerticalUserViewListener {
    void onContactClick(Contact contact);
    void onRequestAllContacts();
  }

  public VerticalUserView(Context context, AttributeSet attrs) {
    super(context, attrs);
    bindViews();
  }

  private void bindViews() {
    View v = LayoutInflater.from(getContext()).inflate(R.layout.vertical_user_view, this, true);
    mRecyclerView = (RecyclerView) v.findViewById(R.id.recyclerView);
    mSeeAll = (TitleView) v.findViewById(R.id.seeAll);
    mSeeAll.setRightIconClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        setVisibilitySettings(getContext(), false);
        ViewCompat.animate(VerticalUserView.this).alpha(0f).setDuration(300).withEndAction(new Runnable() {
          @Override
          public void run() {
            VerticalUserView.this.setVisibility(GONE);
          }
        });
      }
    });
    mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
  }

  public boolean isNotDefined() {
    return mAdapter == null || mAdapter.mContacts == null;
  }

  public void setListener(VerticalUserViewListener mListener) {
    this.mAdapter.mListener = mListener;
  }

  public void setUsers(List<Contact> contacts) {
    mAdapter = new VerticalUserViewAdapter(contacts);
    mRecyclerView.setAdapter(mAdapter);
  }

  public static class VerticalUserViewAdapter extends RecyclerView.Adapter<VerticalUserViewAdapter.UserHolder> {
    List<Contact> mContacts;
    public VerticalUserViewListener mListener;
    public VerticalUserViewAdapter(List<Contact> contacts) {
      this.mContacts = contacts;
    }
    @Override
    public void onBindViewHolder(UserHolder holder, int position) {
      Contact c = mContacts.get(position);
      if (!TextUtils.isEmpty(c.name)) {
        String[] split = c.name.split(" ");
        holder.name.setText(split[0]);

      }

      AvatarLoader.load(holder.avatar, mContacts.get(position).profileUri, mContacts.get(position).name);
    }
    @Override
    public int getItemCount() {
      return mContacts.size();
    }
    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_vertical_user_view, parent, false);
      return new UserHolder(v, this);
    }
    public static class UserHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
      VerticalUserViewAdapter ref;
      public TextView name;
      public AvatarView avatar;
      public UserHolder(View itemView, VerticalUserViewAdapter ref) {
        super(itemView);
        this.ref = ref;
        this.name = (TextView) itemView.findViewById(R.id.title);
        this.avatar = (AvatarView) itemView.findViewById(R.id.avatar);
        itemView.setOnClickListener(this);
      }

      @Override
      public void onClick(View v) {
        int p = getAdapterPosition();
        if (p == -1) return;
        Contact c = ref.mContacts.get(p);
        ref.mListener.onContactClick(c);
      }
    }
  }
}
