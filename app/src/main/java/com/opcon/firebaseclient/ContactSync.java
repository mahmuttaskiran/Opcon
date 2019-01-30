package com.opcon.firebaseclient;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.AnyThread;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opcon.components.Contact;
import com.opcon.database.ContactBase;
import com.opcon.ui.fragments.ContactFragment;
import com.opcon.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 05/04/2017.
 *
 */

public class ContactSync {

  private volatile static ContactSync instance;

  private Context mContext;
  private DatabaseReference mQuery;
  private List<OnQueryResultListener> mListeners;
  private Map<String, Object> mUsers;
  private boolean syncing;
  private final Object mLock = new Object();
  private ThreadPoolExecutor mPool;

  public interface OnQueryResultListener {
    void onFind(String phone, @Nullable String avatar);
    void onNonUser(String phone);
    @AnyThread void onQueryEnded();
  }

  public static ContactSync getInstance(Context context) {
    if (instance == null) {
      synchronized (ContactSync.class) {
        if (instance == null) {
          instance = new ContactSync(context);
        }
      }
    }
    return instance;
  }

  private void syncIsRequires() {
    long lt = PreferenceUtils.getLong(mContext, "lastContactsSyncTimestamp", 0);
    if (lt == 0 || System.currentTimeMillis() > lt + TimeUnit.HOURS.toMillis(4)) {
      sync();
    }
  }

  private ContactSync(Context context) {

    mContext = context.getApplicationContext();
    mQuery = FirebaseDatabase.getInstance().getReference().child("users");
    mListeners = new ArrayList<>();
    syncIsRequires();
  }

  public void addQueryResultListener(OnQueryResultListener listener) {
    if (!mListeners.contains(listener))
      mListeners.add(listener);
  }

  public void removeOnQueryResultListener(OnQueryResultListener listener) {
    mListeners.remove(listener);
  }

  private void endSearch() {
    for (OnQueryResultListener mListener : mListeners) {
      if (mListener!=null) mListener.onQueryEnded();
    }
    updateRemoteContacts();
    syncing = false;
    mPool.shutdown();
  }

  private void updateRemoteContacts() {
    if (mUsers!=null && !mUsers.isEmpty()) {
      DatabaseReference remoteRef = FirebaseDatabase.getInstance().getReference("social/contacts/" + PresenceManager.uid());
      remoteRef.setValue(null);
      remoteRef.updateChildren(mUsers);
      mUsers.clear();
      mUsers = null;
    }
  }

  private void find(final String phone, final @Nullable String avatar) {
    if (mUsers!=null) {
      mUsers.put(phone, true);
    }
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        for (OnQueryResultListener mListener : mListeners) {
          if(mListener!=null) {
            mListener.onFind(phone, avatar);
          }
        }
      }
    });
  }

  private void nonUser(final String phone){
    Handler handler = new Handler(Looper.getMainLooper());
    handler.post(new Runnable() {
      @Override
      public void run() {
        for (OnQueryResultListener mListener : mListeners) {
          if (mListener!=null) {
            mListener.onNonUser(phone);
          }
        }
      }
    });
  }

  public void sync() {

    synchronized (mLock) {
      if (syncing || TextUtils.isEmpty(PresenceManager.uid()) || !PresenceManager.getInstance(mContext).isJoined()) {
        return;
      }
    }

    // fix OP-00009

    mPool = new ThreadPoolExecutor(1, 1, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

    mPool.execute(new Runnable() {
      @Override public void run() {
        PreferenceUtils.putLong(mContext, "lastContactsSyncTimestamp", System.currentTimeMillis());
        List<String> cn = ContactBase.Utils.getContactsNumbers(mContext);
        if (cn != null && !cn.isEmpty()) {
          FindUserEventListener.TotalPhones = cn.size();
          for (String phone : cn) {
            mQuery.child(phone).keepSynced(true);
            mQuery.child(phone).child("avatar").keepSynced(true);
            mQuery.child(phone).child("avatar").addValueEventListener(new FindUserEventListener(mContext, phone));
          }
        }
      }
    });

    syncing = true;

    mUsers = new HashMap<>();

  }

  private void inPool(Runnable r) {
    if (mPool == null) {
      throw new NullPointerException();
    }

    mPool.execute(r);
  }

  public void singleQuery(final String phone, final OnQueryResultListener listener) {
    mQuery.child(phone).keepSynced(true);
    mQuery.child(phone).child("avatar").keepSynced(true);
    mQuery.child(phone).child("avatar").addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        Object value = dataSnapshot.getValue();
        if (value != null) {
          if (value.toString().equals("false")) {
            ContactSync.getInstance(mContext).find(phone, null);
            listener.onFind(phone, null);
          } else {
            ContactBase.Utils.setRemoteAvatar(mContext, phone, value.toString());
            listener.onFind(phone, value.toString());
          }
        } else {
          ContactBase.Utils.nonUser(mContext, phone);
          listener.onNonUser(phone);
        }
      }
      @Override public void onCancelled(DatabaseError databaseError) {}
    });
  }

  private static class FindUserEventListener implements ValueEventListener {
    String phone;
    Context context;
    private static int ResultTotal = 0;
    private static int TotalPhones = 0;

    private FindUserEventListener(Context context, String phone) {
      this.phone = phone;
      this.context = context.getApplicationContext();
    }

    @Override
    public void onDataChange(final DataSnapshot dataSnapshot) {
      synchronized (FindUserEventListener.class) {
        ResultTotal++;
      }
      ContactSync.getInstance(context).inPool(new Runnable() {
        @Override
        public void run() {
          if (context != null || !TextUtils.isEmpty(phone)) {
            Object value = dataSnapshot.getValue();
            if (value != null) {
              if (value.toString().equals("false")) {
                ContactSync.getInstance(context).find(phone, null);
                ContactBase.Utils.setRemoteAvatar(context, phone, null);
              } else {
                ContactBase.Utils.setRemoteAvatar(context, phone, value.toString());
                ContactSync.getInstance(context).find(phone, value.toString());
              }
            } else {
              ContactBase.Utils.nonUser(context, phone);
              ContactSync.getInstance(context).nonUser(phone);
            }
            FirebaseDatabase.getInstance().getReference().child("users")
                .child(phone).child("avatar").removeEventListener(FindUserEventListener.this);
          }

          if (ResultTotal == TotalPhones) {
            ContactSync.getInstance(context).endSearch();
            ResultTotal = 0;
            TotalPhones = 0;
          }
        }
      });
    }
    @Override
    public void onCancelled(DatabaseError databaseError) {
      FirebaseDatabase.getInstance().getReference()
          .child("users").child(phone).child("avatar")
          .removeEventListener(this);
    }
  }
}
