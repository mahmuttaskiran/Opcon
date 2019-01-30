package com.opcon.ui.fragments;

import android.Manifest;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.futuremind.recyclerviewfastscroll.FastScroller;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opcon.R;
import com.opcon.components.Contact;
import com.opcon.database.ContactBase;
import com.opcon.firebaseclient.ContactSync;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.permission.PermissionRequest;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.libs.utils.AndroidEnvironmentsUtils;
import com.opcon.libs.utils.AnimationUtils;
import com.opcon.ui.adapters.NewContactAdapter;
import com.opcon.utils.PreferenceUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 12/10/2016.
 *
 */

public class ContactFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        NewContactAdapter.ContactAdapterClickHandler{
    
    private boolean refreshing = false;
    private List<Integer> contactIds;

    @BindView(R.id.fastscroll) FastScroller mFastScroller;
    @BindView(R.id.fragmentofcontactlist_letter) TextView letter;
    @BindView(R.id.fragmentofcontactlist_recyclerview) RecyclerView recyclerView;
    @BindView(R.id.fragmentofcontactlist_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.fragmentofcontactlist_not_found) TextView notfound;
    @BindView(R.id.fragmentofcontactlist_viewstup_permission) ViewStub viewStub;
    
    private NewContactAdapter newContactAdapter;
    private LinearLayoutManager mRecyclerLayoutManager;
    private PermissionManagement permissionManagement;
    private View permissionDeniedInflate;
    private NewContactAdapter.ContactAdapterClickHandler contactAdapterClikHandler;

    private ContactCache mContactCache = ContactCache.getInstance();

    DatabaseReference mQuery;

    public boolean mOnlyUser;


    private ContactSync.OnQueryResultListener mContactQueryResultListener = new ContactSync.OnQueryResultListener() {
        @Override
        public void onFind(String phone, @Nullable String avatar) {
          Contact contact = ContactCache.getInstance().getContact(phone);
          if (contact != null) {
            contact.hasOpcon = true;
            contact.profileUri = avatar;
          }
        }

        @Override
        public void onNonUser(String phone) {
            // ignored
        }

        @Override
        public void onQueryEnded() {
            refreshing = false;
            contactIds = mContactCache.getRefreshedContactIds(getContext());
            newContactAdapter.setContactIds(contactIds);
            if (isVisible() && mFastScroller!=null && newContactAdapter != null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                        // ContactCache.getInstance().evictAll();
                        newContactAdapter.notifyDataSetChanged();
                        visibilityOfInviteAndLetter();
                        onContactsReady();
                    }
                });
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permissionManagement = PermissionManagement.with(this);
        mQuery = FirebaseDatabase.getInstance().getReference().child("users");
        ContactSync.getInstance(getContext()).addQueryResultListener(mContactQueryResultListener);
    }

    @Override
    public void onDestroy() {
      super.onDestroy();
      ContactSync.getInstance(getContext()).removeOnQueryResultListener(mContactQueryResultListener);
      permissionManagement = null;
      if (recyclerView != null) {
          recyclerView.setAdapter(null);
          recyclerView.destroyDrawingCache();
        }
      newContactAdapter = null;
    }
    
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }

    private RecyclerView.OnScrollListener onScrollListener = new RecyclerView.OnScrollListener() {
        @Override public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
        }
        @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (newContactAdapter == null) return;
            int firstPosition = mRecyclerLayoutManager.findFirstVisibleItemPosition();
            if (contactIds.get(firstPosition) == -1) {
                letter.setVisibility(View.GONE);
            } else {
                letter.setVisibility(View.VISIBLE);
                Contact contact = newContactAdapter.getContact(firstPosition, getContext());
                if (contact != null && contact.name != null) {
                    String firstChar = String.valueOf(contact.name.charAt(0));
                    letter.setText(firstChar);
                }
            }

        }
    };

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        boolean permissionGranted = PermissionUtils
                .check(getContext(), Manifest.permission.READ_CONTACTS);
        setupPermissionState(permissionGranted);
        initAdapter();
        initRecyclerView();
        recyclerView.addOnScrollListener(onScrollListener);
        swipeRefreshLayout.setOnRefreshListener(this);
        swipeRefreshLayout.setColorSchemeColors(gc(R.color.colorPrimary), gc(R.color.red_ff1744), gc(R.color.colorSecondary));
        mFastScroller.setRecyclerView(recyclerView);
        mFastScroller.setHandleColor(Color.WHITE);
        visibilityOfInviteAndLetter();
    }

    public int gc(@ColorRes int c) {
        return getResources().getColor(c);
    }

    private void bindViews(View parent) {
        letter = (TextView) parent.findViewById(R.id.fragmentofcontactlist_letter);
        recyclerView = (RecyclerView) parent.findViewById(R.id.fragmentofcontactlist_recyclerview);
        swipeRefreshLayout = (SwipeRefreshLayout) parent.findViewById(R.id.fragmentofcontactlist_swipe_refresh);
        notfound = (TextView) parent.findViewById(R.id.fragmentofcontactlist_not_found);
        viewStub = (ViewStub) parent.findViewById(R.id.fragmentofcontactlist_viewstup_permission);
        mFastScroller = (FastScroller) parent.findViewById(R.id.fastscroll);
    }

    void onContactsReady() {
        if (ContactBase.Utils.getUserCount(getContext()) > 0){
            // hide invite holder
            if (contactIds.get(0) == -1) {
                contactIds.remove(0);
            }
            if (newContactAdapter != null) {
                newContactAdapter.notifyItemRemoved(0);
            }
        } else {
            // show invite holder
            if (contactIds.isEmpty() || contactIds.get(0) != -1){
                contactIds.add(0, Integer.valueOf(-1));
                letter.setVisibility(View.GONE);
            }

            if (newContactAdapter != null) {
                newContactAdapter.notifyItemInserted(0);
            }
        }
    }

    void setupPermissionState(boolean permissionGranted) {
        if (!permissionGranted) {
            swipeRefreshLayout.setEnabled(false);
            letter.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            if (viewStub != null) {
                permissionDeniedInflate = viewStub.inflate();
                viewStub = null;
            }
            permissionDeniedInflate.setVisibility(View.VISIBLE);
            AppCompatButton goToSettings = (AppCompatButton) permissionDeniedInflate.findViewById(R.id.permission_denied_go_to_settings);

            final boolean persistentlyDenied = PermissionUtils.isAnyPermissionPersistentlyDenied(getContext(),
                    new PermissionRequest(Manifest.permission.READ_CONTACTS));

            if (persistentlyDenied) {
                goToSettings.setText(R.string.permission_to_settings);
            } else {
                goToSettings.setText(R.string.provide_permissions);
            }

            goToSettings.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (persistentlyDenied) {
                        PermissionManagement.showAppSettingsPageFor(getContext());
                    } else {

                        PermissionManagement.PermissionEventListener pel = new PermissionManagement.PermissionEventListener() {
                            @Override
                            public void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest) {
                                updateContactsAsync(true, true);
                            }
                            @Override public void onAnyPermissionsDenied(int requestCode, PermissionRequest permissionRequest) {}
                        };

                        permissionManagement.observer(pel)
                            .builtRequest(123, Manifest.permission.READ_CONTACTS)
                            .request();

                    }
                }
            });
        } else {
            if (permissionDeniedInflate != null) {
                permissionDeniedInflate.setVisibility(View.GONE);
            }
            swipeRefreshLayout.setEnabled(true);
            letter.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    public void showDoNotFind(String forWhat) {
        this.notfound.setVisibility(View.VISIBLE);
        this.notfound.setText(String.format(getString(R.string.not_found_for), forWhat));
        this.swipeRefreshLayout.setVisibility(View.GONE);
        this.letter.setVisibility(View.GONE);
    }

    public void hideDoNotFind() {
        this.notfound.setVisibility(View.GONE);
        this.swipeRefreshLayout.setVisibility(View.VISIBLE);
        this.letter.setVisibility(View.VISIBLE);
    }

    private void initAdapter() {
        this.newContactAdapter = new NewContactAdapter(getContext(), this, getContactIds());
        this.newContactAdapter.setFragment(this);
    }

    private void initRecyclerView() {
        this.mRecyclerLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(this.mRecyclerLayoutManager);
        recyclerView.setAdapter(newContactAdapter);
        recyclerView.setHasFixedSize(true);
        if (contactIds != null && !contactIds.isEmpty()) {
            onContactsReady();
        }

    }

    public void setFilter(String forWhat) {

        // maybe permission denied. and adapter not initialized.

        if (newContactAdapter == null) {
            return;
        }

        if (TextUtils.isEmpty(forWhat)) {
            if (newContactAdapter.getHighlightText() == null) {
                return;
            }
            newContactAdapter.setHighlightText(null);
            newContactAdapter.setContactIds(getContactIds());
            newContactAdapter.notifyDataSetChanged();
            hideDoNotFind();
        } else {
            newContactAdapter.setHighlightText(forWhat);

            List<Integer> ids = ContactBase.Utils.searchIds(getContext(), forWhat);
            if (ids == null || ids.isEmpty()) {
                showDoNotFind(forWhat);
            } else {
                newContactAdapter.setContactIds(ids);
                newContactAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionManagement.dispatchEvent(requestCode, grantResults);
    }

    private List<Integer> getContactIds() {

        if (PermissionUtils.check(getContext(), Manifest.permission.READ_CONTACTS)) {
            contactIds =  mContactCache.getContactIds(getContext());
            return contactIds;
        } else {
            return Collections.emptyList();
        }

    }

    @Override public void onResume() {
        super.onResume();
        boolean check = PermissionUtils.check(getContext(), Manifest.permission.READ_CONTACTS);
        setupPermissionState(check);
        if (check) {
            updateContactsAsync(isFirstRefresh(), isFirstRefresh());
        }
    }

    private boolean isFirstRefresh(){
        boolean refreshContactListFirstInit = PreferenceUtils.getBoolean(getContext(), "refreshContactListFirstInit", true);
        if (refreshContactListFirstInit) {
            PreferenceUtils.putBoolean(getContext(), "refreshContactListFirstInit", false);
        }
        return refreshContactListFirstInit;
    }

    public void updateContactsAsync(final boolean force, final boolean forceForReloadContacts) {

        if (refreshing) return;

        refreshing = true;

        new Thread(new Runnable() {
            @Override public void run() {
                boolean localeUpdated = false;
                if (ContactBase.Utils.isUpdateNecessary(getContext(), forceForReloadContacts) || contactIds == null || contactIds.isEmpty()) {
                  Timber.d("update is necessary!");
                    setRefreshing(true);
                    ContactBase.Utils.update(getContext());
                    localeUpdated = true;
                } else {
                  Timber.d("update is un-necessary!");
                }
                if (force && AndroidEnvironmentsUtils
                    .hasActiveInternetConnection(getContext()) &&
                    PresenceManager.getInstance(getContext()).isJoined())
                {
                    setRefreshing(true);
                    ContactSync.getInstance(getContext()).sync();
                } else
                {
                    if (localeUpdated) {
                        setRefreshing(true);
                        contactIds = mContactCache.getRefreshedContactIds(getContext());
                        newContactAdapter.setContactIds(contactIds);
                    }
                    getActivity().runOnUiThread(new Runnable() {
                        @Override public void run() {
                            newContactAdapter.notifyDataSetChanged();
                            swipeRefreshLayout.setRefreshing(false);
                            refreshing = false;
                            onContactsReady();
                        }
                    });
                }

            }
        }).start();
    }

    private void setRefreshing(final boolean bool) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (swipeRefreshLayout != null) {
                    swipeRefreshLayout.setRefreshing(bool);
                }
            }
        });
    }

    private void visibilityOfInviteAndLetter() {
        if (contactIds == null || contactIds.isEmpty() || !PermissionUtils.check(getContext(), Manifest.permission.READ_CONTACTS)) {
            letter.setVisibility(View.GONE);
        } else {
            letter.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefresh() {

        if (!AndroidEnvironmentsUtils.hasActiveInternetConnection(getContext())) {
            Toast.makeText(getContext(), R.string.there_is_no_internet_connection, Toast.LENGTH_SHORT).show();
        }

        if (refreshing) {
            return;
        }
        updateContactsAsync(true, true);
    }

    @Override
    public void onAvatarClick(int id) {

        if (contactAdapterClikHandler != null) {
            contactAdapterClikHandler.onAvatarClick(id);
        }

    }

    @Override
    public void onRefreshRequest() {
        this.onRefresh();
    }

    @Override public void onContactClick(int id) {
        if (contactAdapterClikHandler != null) {
            if (mOnlyUser && ContactBase.Utils.getContact(getContext(), id).hasOpcon) {
                contactAdapterClikHandler.onContactClick(id);
            } else {
                contactAdapterClikHandler.onContactClick(id);
            }
        }
    }

    @Override
    public void onGavelClick(int id) {
        if (contactAdapterClikHandler != null) {
            contactAdapterClikHandler.onGavelClick(id);
        }
    }

    public void setContactClickHandler(NewContactAdapter.ContactAdapterClickHandler handler) {
        this.contactAdapterClikHandler = handler;
    }

    public static class ContactCache extends LruCache<Integer, Contact> {
        private static int MAX_SIZE_OF_CACHE = (250);
        private volatile static ContactCache singleton;

        private List<Integer> mContactIds;

        public static ContactCache getInstance() {
            if (singleton == null) {
                synchronized (ContactBase.class) {
                    if (singleton == null) {
                        singleton = new ContactCache(MAX_SIZE_OF_CACHE);
                    }
                }
            }
            return singleton;
        }

        private ContactCache(int maxSize) {
            super(maxSize);
        }

        public Contact getContact(Context context, int contactId) {
            Contact fromCache = get(contactId);
            if (fromCache == null) {
                fromCache = ContactBase.Utils.getContact(context, contactId);
                if (fromCache != null) {
                    put(contactId, fromCache);
                }
            }
            return fromCache;
        }

        // TODO test it.
        // in test!
        public Contact getContact(String phone) {
          Set<Map.Entry<Integer, Contact>> entries = snapshot().entrySet();
          for (Map.Entry<Integer, Contact> entry : entries) {
            if (entry.getValue().number.equals(phone)) {
              return entry.getValue();
            }
          }
          return null;
        }

        public List<Integer> getContactIds(Context context) {
          if (mContactIds == null || mContactIds.isEmpty())
            mContactIds = ContactBase.Utils.getAllContactIds(context);
          return mContactIds;
        }

        public List<Integer> getRefreshedContactIds(Context context) {
          mContactIds = ContactBase.Utils.getAllContactIds(context);
          return mContactIds;
        }


    }

}
