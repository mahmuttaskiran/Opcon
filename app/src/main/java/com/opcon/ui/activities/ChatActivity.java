package com.opcon.ui.activities;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Message;
import com.opcon.database.ComponentSettings;
import com.opcon.database.ContactBase;
import com.opcon.database.MessageProvider;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.ConnectionDispatcher;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.PicassoCompressor;
import com.opcon.libs.notification.MessageNotificator;
import com.opcon.libs.registration.RegistrationManagement;
import com.opcon.libs.settings.BlackListActivity;
import com.opcon.libs.utils.ImageStorageUtils;
import com.opcon.libs.utils.TimeUtils;
import com.opcon.notifier.components.Notifier;
import com.opcon.services.ImageMessageUploader;
import com.opcon.ui.adapters.MessageSelectionManagement;
import com.opcon.ui.fragments.ChatFragment;
import com.opcon.ui.fragments.NotifierFragment;
import com.opcon.ui.management.DialogStoreManagement;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import agency.tango.android.avatarview.views.AvatarView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;


public class ChatActivity extends AppCompatActivity {

    public  static final  String DESTINATION = "dest";
    public  static final  String GOTO = "goto";
    public  static String CURRENT_DESTINATION;
    public  static String FOCUS = "focus";

    public View mToolbarDivider;

    private ViewPager mViewPager;
    private TextView mTitle;
    private TextView mSubtitle;
    private AvatarView mAvatar;

    private ChatFragment mChatFragment;
    private NotifierFragment mNotifierFragment;

    private MenuItem miRemove;
    private MenuItem miCopy;
    private MenuItem miSeeRelationNotifier;
    private MenuItem miGavel;

    private String mName;

    private String mDestination;
    private DatabaseReference mStateRef;
    private DatabaseReference mStateStatusRef;

    private DatabaseReference mStateLastActivityRef;
    private long mLastActivity;

    private boolean mState;
    private MessageSelectionManagement mSelectionManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        MessageProvider.Utils.deleteWaitingMessagesThatDoesNotExists(getApplicationContext());
        initDestination(savedInstanceState);
        initFragments(savedInstanceState);
        bindViews();
        initStateOfDestination();

        if (getIntent().getIntExtra(FOCUS, -1) != -1) {
            mSubtitle.postDelayed(new Runnable() {
                @Override
                public void run() {
                    focusNotifier(getIntent().getExtras().getInt(FOCUS), getIntent().getBooleanExtra("activate", false));
                }
            },450);
        }
    }

    void initStateOfDestination() {

        if (mStateRef != null) {
            mStateRef.removeEventListener(mStateListener);
        }

        mStateRef = FirebaseDatabase.getInstance().getReference()
            .child("presence").child(mDestination);
        mStateRef.keepSynced(true);

        mStateStatusRef = mStateRef.child("status");
        mStateStatusRef.keepSynced(true);

        mStateLastActivityRef = mStateRef.child("lastActivity");
        mStateLastActivityRef.keepSynced(true);

        mStateRef.addChildEventListener(mStateListener);

        requestLastTimes();
    }

    void initDestination(Bundle bundle) {

        mDestination = null;

        if (bundle != null) {
            mDestination = bundle.getString(DESTINATION);
        }

        if (mDestination == null) {
            mDestination = getIntent().getExtras().getString(DESTINATION);
        }

        CURRENT_DESTINATION = mDestination;
        mName = ContactBase.Utils.getName(getApplicationContext(), mDestination);

    }

    void initFragments(Bundle bundle) {

        if (mNotifierFragment != null || mChatFragment != null) {
            mChatFragment = null;
            mNotifierFragment = null;
        }

        if (bundle != null) {
            mChatFragment = (ChatFragment) getSupportFragmentManager().getFragment(bundle, "messages");
            mNotifierFragment = (NotifierFragment) getSupportFragmentManager().getFragment(bundle, "notifiers");
        }

        if (mNotifierFragment == null) {
            mNotifierFragment = new NotifierFragment();
            mNotifierFragment.setHasOptionsMenu(false);
            mNotifierFragment.setRetainInstance(false);
        }

        if (mChatFragment == null) {
            mChatFragment = new ChatFragment();
            mChatFragment.setSelectionManagerListener(mSelectionManagerListener);
            mChatFragment.setRetainInstance(false);
        }

        mNotifierFragment.setTargetFilter(mDestination);
        mChatFragment.mDestination = mDestination;

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        initDestination(null);
        Timber.d("destination: called: %s", mDestination);
        initFragments(null);
        initStateOfDestination();
        bindViews();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, "notifiers", mNotifierFragment);
        getSupportFragmentManager().putFragment(outState, "messages", mChatFragment);
        outState.putString(DESTINATION, mDestination);
    }

    private void showLastState() {
        mSubtitle.setVisibility(View.VISIBLE);
        // fix OP-00010
        if (mState && mLastActivity > (System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(5))) {
            mSubtitle.setText(R.string.online);
        } else {
            if (mLastActivity == 0) {
                mSubtitle.setVisibility(View.GONE);
            } else {
                mSubtitle.setText(TimeUtils.shortDateAndTime(mLastActivity, TimeUnit.HOURS.toMillis(5)));
            }
        }
    }

    private MessageSelectionManagement.MessageSelectionManagementListener mSelectionManagerListener = new MessageSelectionManagement.MessageSelectionManagementListener() {
        @Override
        public void onSelectionState() {
            mSelectionManagement = mChatFragment.getMessageAdapter().getSelectionManagement();
            mChatFragment.getMessageAdapter().notifyDataSetChanged();
            miCopy.setVisible(mSelectionManagement.isOnlyText());
            setSelectionMode(true);
            miToSingle();
        }

        @Override
        public void onDeselectionState() {
            mChatFragment.getMessageAdapter().notifyDataSetChanged();
            setSelectionMode(false);
            miToDefault();
        }

        @Override
        public void onSelected(int key, int forAdapterPosition) {
            mChatFragment.getMessageAdapter().notifyItemChanged(forAdapterPosition);
            mTitle.setText(String.valueOf(mSelectionManagement.getSelectedKeys().size()));
            miCopy.setVisible(mSelectionManagement.isOnlyText());
            if (mSelectionManagement.isSinglePacket()) {
                Message msg = MessageProvider.Utils.getSingleMessage(getApplicationContext(), mSelectionManagement.getSelectedKeys().get(0));
                if (msg != null)
                    miSeeRelationNotifier.setVisible(!TextUtils.isEmpty(msg.getRelationNotifier()));
            } else {
                miSeeRelationNotifier.setVisible(false);
            }
        }

        @Override
        public void onDeselected(int key, int forAdapterPosition) {
            mChatFragment.getMessageAdapter().notifyItemChanged(forAdapterPosition);
            mTitle.setText(String.valueOf(mSelectionManagement.getSelectedKeys().size()));
            miCopy.setVisible(mSelectionManagement.isOnlyText());
            miSeeRelationNotifier.setVisible(mSelectionManagement.isSinglePacket());
            if (mSelectionManagement.isSinglePacket()) {
                Message msg = MessageProvider.Utils.getSingleMessage(getApplicationContext(), mSelectionManagement.getSelectedKeys().get(0));
                miSeeRelationNotifier.setVisible(!TextUtils.isEmpty(msg.getRelationNotifier()));
            } else {
                miSeeRelationNotifier.setVisible(false);
            }
        }
    };


    private ConnectionDispatcher.FirebaseConnectionListener mConnectionListener =
        new ConnectionDispatcher.FirebaseConnectionListener() {
            @Override
            public void onConnectionChanged(boolean connected) {
                if (!connected) {
                    mSubtitle.setVisibility(View.GONE);
                }
            }
        };

    private ChildEventListener mStateListener = new ChildEventListener() {
        @Override public void onChildChanged(DataSnapshot dataSnapshot, String s) {
            Timber.d("state: %s:%s", dataSnapshot.getKey(), dataSnapshot.getValue());
            if (dataSnapshot.getKey().equals("lastActivity")) {
                mLastActivity = dataSnapshot.getValue(Long.class);
                showLastState();
            } else if (dataSnapshot.getKey().equals("status")) {
                mState = Boolean.parseBoolean(dataSnapshot.getValue().toString());
                showLastState();
            }
        }
        @Override public void onChildAdded(DataSnapshot dataSnapshot, String s) {}
        @Override public void onChildRemoved(DataSnapshot dataSnapshot) {}
        @Override public void onChildMoved(DataSnapshot dataSnapshot, String s) {}
        @Override public void onCancelled(DatabaseError databaseError) {}
    };

    private ValueEventListener mFirstStatusListener = new ValueEventListener() {
        @Override public void onDataChange(DataSnapshot dataSnapshot) {
            Object value = dataSnapshot.getValue();
            mState = Boolean.parseBoolean(value != null ? value.toString() : "false");
            showLastState();
            mStateStatusRef.removeEventListener(this);
        }
        @Override public void onCancelled(DatabaseError databaseError) {
            mStateStatusRef.removeEventListener(this);
        }
    };

    private ValueEventListener mFirstLastTimeListener = new ValueEventListener() {
        @Override public void onDataChange(DataSnapshot dataSnapshot) {
            Long val =dataSnapshot.getValue(Long.class);
            if (val == null) {
                mLastActivity = 0;
                return;
            }
            mLastActivity = val;
            showLastState();

        }
        @Override public void onCancelled(DatabaseError databaseError) {}
    };

    private void requestLastTimes() {
        mStateStatusRef.addListenerForSingleValueEvent(mFirstStatusListener);
        mStateLastActivityRef.addListenerForSingleValueEvent(mFirstLastTimeListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CURRENT_DESTINATION = null;
        mStateRef.removeEventListener(mStateListener);
        mStateStatusRef.removeEventListener(mFirstStatusListener);
        mStateLastActivityRef.removeEventListener(mFirstLastTimeListener);
        if (mViewPager != null) {
            mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
        }
    }

    private ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        @Override public void onPageSelected(int position) {
            if (mSelectionManagement != null && mSelectionManagement.inSelectionState()) {
                mSelectionManagement.getSelectedKeys().clear();
                setSelectionMode(false);
                mChatFragment.getMessageAdapter().notifyDataSetChanged();
                miToDefault();
            }
            mChatFragment.inHiddenState();
        }
        @Override public void onPageScrollStateChanged(int state) {}
    };

    @OnClick({R.id.chatactivity_toolbar_back, R.id.chatactivity_toolbar_avatar})
    public void onBackImageClick(View v) {
        onBackPressed();
    }

    private void bindViews() {
        mViewPager = (ViewPager) findViewById(R.id.chat_viewpager);
        mAvatar = (AvatarView) findViewById(R.id.chatactivity_toolbar_avatar);
        mTitle = (TextView) findViewById(R.id.title);
        mSubtitle = (TextView) findViewById(R.id.subtitle);
        mToolbarDivider = findViewById(R.id.toolbarDivider);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        mTitle.setText(mName);

        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        if (mViewPager != null) {
            mViewPager.setAdapter(null);
            mViewPager.removeOnPageChangeListener(mOnPageChangeListener);
        }

        mViewPager.setAdapter(new SliderAdapter(getSupportFragmentManager()));
        mViewPager.addOnPageChangeListener(mOnPageChangeListener);


        String avatar = ContactBase.Utils.getValidAvatar(getApplicationContext(), mDestination);
        AvatarLoader.load(mAvatar, avatar, mName);
        mSubtitle.setVisibility(View.GONE);

        if (getIntent().getStringExtra(GOTO) != null ){

            if (getIntent().getStringExtra(GOTO).equals("notifiers")) {
                mViewPager.setCurrentItem(1, false);
            }

        }

    }

    @Override
    public boolean  onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_activity, menu);

        miGavel = menu.findItem(R.id.menuitem_add_notifier);
        miRemove = menu.findItem(R.id.menuitem_remove);
        miCopy = menu.findItem(R.id.menuitem_copy);
        miSeeRelationNotifier = menu.findItem(R.id.see_relation_notifier);
        miSeeRelationNotifier.setVisible(false);

        MenuItem miBlack = menu.findItem(R.id.state_of_black_list);
        MenuItem miNotification = menu.findItem(R.id.state_of_notification);
        MenuItem miNotificationSound = menu.findItem(R.id.state_of_notification_sound);

        boolean notify = ComponentSettings.isNotificationOn(getBaseContext(), mDestination);
        if (notify) {
            miNotification.setTitle(R.string.notification_off);
        } else {
            miNotification.setTitle(R.string.notification_on);
        }

        boolean notifySound = ComponentSettings.isNotificationSoundOn(getBaseContext(), mDestination);
        if (notifySound) {
            miNotificationSound.setTitle(R.string.notification_sound_off);
        } else {
            miNotificationSound.setTitle(R.string.notification_sound_on);
        }

        BlackListActivity.BlackLocaleBase blb = new BlackListActivity.BlackLocaleBase(getBaseContext());
        boolean black = blb.isAlreadyBlack(mDestination);
        if (black) {
            miBlack.setTitle(R.string.non_black);
        } else {
            miBlack.setTitle(R.string.black);
        }

        return super.onCreateOptionsMenu(menu);
    }

    private void miToSingle() {
        miGavel.setVisible(false);
        miRemove.setVisible(true);
    }

    private void miToDefault() {
        miGavel.setVisible(true);
        miRemove.setVisible(false);
        miCopy.setVisible(false);
        miSeeRelationNotifier.setVisible(false);
    }

    private void setSelectionMode(boolean selectionMode) {
        if (selectionMode) {
            mSubtitle.setVisibility(View.GONE);
            mTitle.setText("1");
            if (mTitle.getTag() == null) {
                mTitle.setTag(mTitle.getTextSize());
            }
            mTitle.setTextSize(21);
            mAvatar.setVisibility(View.GONE);
        } else {
            mTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX,(Float) mTitle.getTag());
            mSubtitle.setVisibility(View.VISIBLE);
            mTitle.setText(mName);
            mAvatar.setVisibility(View.VISIBLE);
        }
    }

    public boolean isValidTime() {
        return mLastActivity != 0;
    }

    private class SliderAdapter extends FragmentStatePagerAdapter{
        private SliderAdapter(FragmentManager fm) {
            super(fm);
        }
        @Override public Fragment getItem(int position) {
            return position == 0 ? mChatFragment : mNotifierFragment;
        }
        @Override public int getCount() {
            return 2;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        CURRENT_DESTINATION = mDestination;
        ConnectionDispatcher.getInstance().addConnectionListener(mConnectionListener);
        MessageNotificator.getInstance(getApplicationContext()).cancel();
    }

    public void focusNotifier(int id, boolean activateRequest) {
        if (mViewPager.getCurrentItem() != 1) {
            mViewPager.setCurrentItem(1, true);
        }
        mNotifierFragment.focusNotifier(id, activateRequest);
    }

    public static void sendWaitingImageMessage(final Context c, final Message msg) {
        PicassoCompressor compressor = new PicassoCompressor(c);
        compressor.compress(new File(msg.getString(Message.Picture.FILE)))
            .size(AvatarLoader.MESSAGE_PICTURE_WIDTH, AvatarLoader.MESSAGE_PICTURE_HEIGHT)
            .quality(100)
            .lowMemory(PicassoCompressor.isLowMemoryRequires(c))
            .to(createSentImageFile())
            .listen(new PicassoCompressor.CompressorListener() {
                @Override public void onCompressed(@Nullable final File file, @Nullable final Bitmap bitmap) {
                    if (file != null) {
                        msg.put(Message.Picture.FILE, file.getAbsolutePath());
                        msg.setWaiting(false);
                        msg.put(Message.Picture.THUMBNAIL, PicassoCompressor.encode(bitmap, AvatarLoader.THUMBNAIL, AvatarLoader.THUMBNAIL, 50));
                        MessageProvider.Utils.update(c, msg);
                        Handler h = new Handler(Looper.getMainLooper());
                        h.post(new Runnable() {
                            @Override public void run() {
                                ImageMessageUploader.getInstance(c).start(msg);
                            }
                        });
                    }
                }
            }).so();
    }


    private static File createSentImageFile() {
        String file = Environment.getExternalStorageDirectory() + ImageStorageUtils.SENT_PICTURES_DIRECTORIES;
        File ffile = new File(file);
        if (!ffile.exists()) {
            ffile.mkdirs();
        }
        return new File(ffile, ImageStorageUtils.getRandomFileNameForTimestamp(System.currentTimeMillis()));
    }

    @SuppressLint("SimpleDateFormat")
    public static String copyTextMessageContent(Context context, Message textMessage) {
        String name, body, time;
        if (textMessage.isSenderAmI()) {
            name = RegistrationManagement.getInstance().getName(context, R.string.you);
        } else {
            name = ContactBase.Utils.getName(context, textMessage.getSender());
        }
        body = textMessage.getString(Message.Text.BODY);
        time = new SimpleDateFormat("dd MM yyyy, hh: mm").format(new java.util.Date(textMessage.getSentTimestamp()));
        return "[" + name + " " + time + "] " + body + "\n";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.state_of_notification) {
            boolean notifyNewState = !ComponentSettings.isNotificationOn(getBaseContext(), mDestination);
            if (notifyNewState) {
                item.setTitle(R.string.notification_off);
            } else {
                item.setTitle(R.string.notification_on);
            }
            ComponentSettings.setNotification(getBaseContext(), mDestination, notifyNewState);
        } else if (item.getItemId() == R.id.state_of_notification_sound) {
            boolean notifyNewState = !ComponentSettings.isNotificationSoundOn(getBaseContext(), mDestination);
            if (notifyNewState) {
                item.setTitle(R.string.notification_sound_off);
            } else {
                item.setTitle(R.string.notification_sound_on);
            }
            ComponentSettings.setNotificationSound(getBaseContext(), mDestination, notifyNewState);
        } else if (item.getItemId() == R.id.state_of_black_list) {
            BlackListActivity.BlackLocaleBase blb = new BlackListActivity.BlackLocaleBase(getBaseContext());
            boolean black = blb.isAlreadyBlack(mDestination);
            if (black) {
                item.setTitle(R.string.black);
                FirebaseDatabase.getInstance().getReference("black/" + PresenceManager.uid())
                    .child(mDestination)
                    .setValue(null);
                blb.remove(mDestination);
                mChatFragment.onDestinationEnabled();
            } else {
                item.setTitle(R.string.non_black);
                DatabaseReference blackRef = FirebaseDatabase.getInstance()
                    .getReference("black/" + PresenceManager.uid());
                Map<String, Object> map = new HashMap<>();
                map.put(mDestination, new BlackListActivity.BlackUser(null, mDestination).toMap());
                blackRef.updateChildren(map);
                blb.add(ContactBase.Utils.getName(getBaseContext(), mDestination),
                        mDestination);
                mChatFragment.onDestinationDisabled();
            }
        } else if (item.getItemId() == miCopy.getItemId()) {

            if (mSelectionManagement != null &&
                    mSelectionManagement.inSelectionState() &&
                    mSelectionManagement.isOnlyText())
            {
                List<Integer> keys = mSelectionManagement.getSelectedKeys();
                StringBuilder strB = new StringBuilder();
                for (int key: keys) {
                    Message textMsg = mChatFragment.getMessageAdapter().getMessageFromCache(key);
                    strB.append(copyTextMessageContent(getApplicationContext(), textMsg));
                }
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);

                ClipData clipData = ClipData.newPlainText("simple text", strB.toString());
                clipboardManager.setPrimaryClip(clipData);

                mSelectionManagement.getSelectedKeys().clear();
                setSelectionMode(false);
                miToDefault();
                mChatFragment.getMessageAdapter().notifyDataSetChanged();
            }

        } else if (item.getItemId() == miRemove.getItemId()) {

            if (mSelectionManagement != null && mSelectionManagement.inSelectionState()) {

                for (int i = 0; i < mSelectionManagement.getSelectedKeys().size(); i++) {
                    int delId = mSelectionManagement.getSelectedKeys().get(i);
                    MessageProvider.Utils.delete(getBaseContext(), delId);
                    DialogStoreManagement.getInstance(getApplicationContext())
                        .removeMessage(delId);
                }

                mSelectionManagement.getSelectedKeys().clear();
                setSelectionMode(false);
                miToDefault();

                mChatFragment.getMessageAdapter().setMessageIds(MessageProvider.Utils.getMessages(getApplicationContext(), mDestination));
                mChatFragment.getMessageAdapter().notifyDataSetChanged();

            }

        } else if (item.getItemId() == R.id.menuitem_add_notifier) {
            mNotifierFragment.attemptToCreateNotifier();
        } else if (item.getItemId() == R.id.see_relation_notifier) {
            int id = mSelectionManagement.getSelectedKeys().get(0);
            Message msg = MessageProvider.Utils.getSingleMessage(getApplicationContext(), id);
            Notifier relationNotifier = NotifierProvider.Utils.get(getApplicationContext(), msg.getRelationNotifier());
            if (relationNotifier == null) {
                Toast.makeText(this, R.string.cannot_seen_message, Toast.LENGTH_SHORT).show();
            } else {
                focusNotifier(relationNotifier.getId(), false);
            }
            mSelectionManagement.getSelectedKeys().clear();
            setSelectionMode(false);
            miToDefault();

            mSelectionManagement.getSelectedKeys().clear();
            mChatFragment.getMessageAdapter().notifyDataSetChanged();

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();
        CURRENT_DESTINATION = null;
        ConnectionDispatcher.getInstance().removeListener(mConnectionListener);
    }

    @Override
    public void onBackPressed() {
        if (mSelectionManagement != null && mSelectionManagement.inSelectionState()) {
            mSelectionManagement.getSelectedKeys().clear();
            setSelectionMode(false);
            mChatFragment.getMessageAdapter().notifyDataSetChanged();
            miToDefault();
        } else {
            finish();
        }
    }

    public static void go(Context c, String des) {
        Intent i = new Intent(c, ChatActivity.class);
        i.putExtra(DESTINATION, des);
        c.startActivity(i);
    }

    public static void focusNotifier(Context context, String des, int id) {
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(DESTINATION, des);
        intent.putExtra(FOCUS, id);
        context.startActivity(intent);
    }

    public static void goWithActivateRequest(Context context, String des, int id){
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(DESTINATION, des);
        intent.putExtra(FOCUS, id);
        intent.putExtra("activate", true);
        context.startActivity(intent);
    }

}
