package com.opcon.ui.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.opcon.ui.fragments.occs.ConditionLocation;
import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.components.Ack;
import com.opcon.components.Message;
import com.opcon.database.MessageProvider;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.PicassoCompressor;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.permission.PermissionRequest;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.libs.permission.NotifierPermissionDetective;
import com.opcon.libs.settings.BlackListActivity;
import com.opcon.libs.settings.SettingsUtils;
import com.opcon.libs.utils.AndroidEnvironmentsUtils;
import com.opcon.libs.utils.ImageStorageUtils;
import com.opcon.notifier.SpecialPacketBuilderFactory;
import com.opcon.notifier.components.SpecialPacket;
import com.opcon.services.ImageMessageUploader;
import com.opcon.libs.MessageDispatcher;
import com.opcon.ui.activities.ChatActivity;
import com.opcon.ui.adapters.MessageAdapter;
import com.opcon.ui.adapters.MessageSelectionManagement;
import com.opcon.ui.views.CloudRelativeView;
import com.opcon.ui.views.NewChatInput;
import com.schibstedspain.leku.LocationPickerActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

/**
 *
 * Created by Mahmut Taşkiran on 07/11/2016.
 */

public class ChatFragment extends Fragment implements MessageDispatcher.MessageEventListener, NewChatInput.ChatInputListener {

    private static final int CAMERA_REQUEST = 5522;
    private static final int GALLERY_REQUEST = 5512;
    private static final int LOCATION_REQUEST = 5518;
    private static final int REQUEST_READ_EXTERNAL = 5687;
    private static String CAMERA_REQUEST_FOR_DESTINATION = null;

    private MessageSelectionManagement.MessageSelectionManagementListener mSelectionManagerListener;
    private RecyclerView mRecyclerView;
    private MessageAdapter mMessageAdapter;

    public  String mDestination;
    private ImageMessageUploader mImageMessageUploader;
    private NewChatInput mChatInputView;
    private String mTempCameraFile;
    private PermissionManagement mPermissionManagement;
    private boolean mDestinationInBlackList = false;
    private List<Integer> mMessageIds;

    private TextView mIndicator;

    private CloudRelativeView mNoMessageIndicator;

    public MessageAdapter getMessageAdapter() {
        return this.mMessageAdapter;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MessageDispatcher.getInstance().addMessageEventListener(this);

        mImageMessageUploader = ImageMessageUploader.getInstance(getContext());
        mImageMessageUploader.addListener(mUploadListener);
        mPermissionManagement = PermissionManagement.with(ChatFragment.this);
        if (savedInstanceState != null) {
            this.mDestination = savedInstanceState.getString("mDestination");
        }
        ComponentListenerManager mComponentManagerListener = ComponentListenerManager.getInstance(getContext());
        mComponentManagerListener.addComponentListener(Ack.class, mAckListener);
    }

    public void setSelectionManagerListener(MessageSelectionManagement.MessageSelectionManagementListener                                     mSelectionManagerListener) {
        this.mSelectionManagerListener = mSelectionManagerListener;
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = LayoutInflater.from(getContext()).inflate(R.layout.fragment_chat, container, false);
        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.chatf_recyclerview);
        mChatInputView = (NewChatInput) rootView.findViewById(R.id.chat_input);
        mIndicator = (TextView) rootView.findViewById(R.id.chatFragmentIndicator);
        mNoMessageIndicator = (CloudRelativeView) rootView.findViewById(R.id.no_message);
        mChatInputView.setChatInputListener(this);
        mChatInputView.setActivityReference(getActivity());

        mChatInputView.enterToSend(SettingsUtils.Dialog.enterToSend(getContext()));

        initComponents();
        initPermissionAlert();
        BlackListActivity.BlackLocaleBase blk = new BlackListActivity.BlackLocaleBase(getContext());
        mDestinationInBlackList = blk.isAlreadyBlack(mDestination);
        blk.close();
        if (mDestinationInBlackList) {
            mChatInputView.disable(getString(R.string.you_has_blocked));
        }
        return rootView;
    }

    private ComponentListenerManager.ComponentListener mAckListener =
        new ComponentListenerManager.ComponentListener() {
            @Override public boolean onNewComponent(Object component) {
                Ack ack = (Ack) component;
                String messageSid = ack.getMessageSid();
                Message msg = MessageProvider.Utils.getSingleMessage(getContext(), messageSid);
                if (msg != null && msg.getReceiver().equals(mDestination))
                    mMessageAdapter.updateMessage(msg.getId());
                return true;
            }
        };

    private void initComponents() {
        mMessageIds = MessageProvider.Utils.getMessages(getContext(), mDestination);

        this.mMessageAdapter = new MessageAdapter(getContext(), mMessageIds);
        this.mMessageAdapter.getSelectionManagement().setSelectionManagerListener(this.mSelectionManagerListener);
        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        linearLayoutManager.setReverseLayout(false);
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(mMessageAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        decideVisibilityOfAlerts();
    }

    void decideVisibilityOfAlerts() {

      // fix OP-00014
      // this function is called from any-thread that it is not main
      // then we will crash! And first message will don't show.

      // so, why it is called from any-thread? see@ImageMessageUploader

      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (mMessageIds == null || mMessageIds.isEmpty()) {
            mNoMessageIndicator.setVisibility(View.VISIBLE);
            mIndicator.setText(R.string.swipe_to_right_to_show_notifiers_of_this_dialog);
            ((ChatActivity) getActivity()).mToolbarDivider.setVisibility(View.GONE);
          } else {
            if (!PermissionUtils.checkReadWriteExternalStorage(getContext())) {
              mNoMessageIndicator.setVisibility(View.VISIBLE);
              mIndicator.setText(R.string.permission_for_show_images);
            } else {
              mNoMessageIndicator.setVisibility(View.GONE);
              ((ChatActivity) getActivity()).mToolbarDivider.setVisibility(View.VISIBLE);
            }
          }
        }
      });

    }

    @Override
    public void onCamera() {
        PermissionRequest r;
        if (Build.VERSION.SDK_INT >= 16) {
            r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }  else {
            r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (PermissionUtils.check(getContext(), r)) {
            goCamera();
        } else {
            if (PermissionUtils.isAnyPermissionPersistentlyDenied(getContext(), r)) {
                needPermission(R.string.permission_camera_pick_desc);
            } else {
                mPermissionManagement.builtRequest(CAMERA_REQUEST, r).observer(mPermissionEventListener).request();
            }
        }
    }

    @Override
    public void onGallery() {
        PermissionRequest r;
        if (Build.VERSION.SDK_INT >= 16) {
            r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }  else {
            r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (PermissionUtils.check(getContext(), r)) {
            goGallery();
        } else {
            if (PermissionUtils.isAnyPermissionPersistentlyDenied(getContext(), r)) {
                needPermission(R.string.permission_gallery_pick_request);
            } else {
                mPermissionManagement.builtRequest(GALLERY_REQUEST, r).observer(mPermissionEventListener).request();
            }
        }
    }

    @Override public void onPacket(final int packetType) {
        String[] prs = NotifierPermissionDetective.SpecialPacketPermissionDetective.detect(packetType);
        if (prs == null || prs.length == 0) {
            askPacket(packetType);
        } else {
            final PermissionRequest r = new PermissionRequest(prs);
            prs = PermissionUtils.checkAndGetDeniedPermissions(getContext(), r);
            if (prs == null) {
                askPacket(packetType);
                return;
            }
            final boolean pd = PermissionUtils.isAnyPermissionPersistentlyDenied(getContext(), r);
            DialogInterface.OnClickListener positiveListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (!pd) {
                        mPermissionManagement.observer(mPermissionEventListener).builtRequest(packetType, r).request();
                    } else {
                        PermissionManagement.showAppSettingsPageFor(getContext());
                    }
                }
            };
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
            mBuilder.setCancelable(true)
                .setTitle(R.string.need_permission)
                .setMessage(NotifierPermissionDetective.SpecialPacketPermissionDetective.getPermissionDetailsForRuntimePermissions(getContext(), packetType))
                .setPositiveButton(pd ? R.string.permission_to_settings : R.string.ok, positiveListener);
            mBuilder.show();
        }
    }

    @Override public void onText(String text) {
        sent(MessagePrepareUtils.prepareTextMessage(mDestination, text));
    }

    @Override public void onLocation() {
        Timber.d("onLocation():");
        PermissionRequest pr = new PermissionRequest(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION);
        if (PermissionUtils.check(getContext(), pr)) {
            // start location picker
            placePicker();
        } else {
            if (PermissionUtils.isAnyPermissionPersistentlyDenied(getContext(), pr)) {
                needPermission(R.string.permission_specialpacket_location);
            } else {
                mPermissionManagement.builtRequest(LOCATION_REQUEST, pr).observer(mPermissionEventListener).request();
            }
        }
    }

    private MessagePrepareUtils.MessagePreparingListener mMessagePreparingListener = new MessagePrepareUtils.MessagePreparingListener() {
        @Override
        public void onReady(Message msg) {
            sent(msg);
            CAMERA_REQUEST_FOR_DESTINATION = null;
        }
    };

    public static boolean isThereAreCameraRequestFor(String destination) {
        return (CAMERA_REQUEST_FOR_DESTINATION != null && CAMERA_REQUEST_FOR_DESTINATION.equals(destination));
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == GALLERY_REQUEST) {
            String file = FileUtils.getPath(getContext(), data.getData());
            MessagePrepareUtils.prepareUploadImageMessage(getContext(), mDestination, file, mMessagePreparingListener);
        } else if (requestCode == CAMERA_REQUEST) {
            String file = mTempCameraFile;
            MessagePrepareUtils.prepareUploadImageMessage(getContext(), mDestination, file, mMessagePreparingListener);
        } else if (requestCode == LOCATION_REQUEST) {


          double lt = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0);
          double lg = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0);
          String a = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS);

          Timber.d("lt %s, ln %s", String.valueOf(lt), String.valueOf(lg));
          sent(MessagePrepareUtils.prepareLocationMessage(mDestination, lt, lg, a));
        }
    }

    void sent(Message msg) {
      Timber.d("message will send: %s", msg.toString());
        msg.setId(MessageProvider.Utils.newMessage(getContext(), msg));
        if (msg.isImageMessage() && !msg.getBoolean(Message.Picture.DONE) && AndroidEnvironmentsUtils.hasActiveInternetConnection(getContext())) {
            ImageMessageUploader.getInstance(getContext()).start(msg);
        } else {
            msg.send(getContext().getApplicationContext());
        }
        MessageDispatcher.getInstance().notifyNewMessage(msg, false);
    }

    private void placePicker() {
      ConditionLocation.pick_leku(ChatFragment.this, LOCATION_REQUEST, 0,0);
    }

    private PermissionManagement.PermissionEventListener mPermissionEventListener = new PermissionManagement.PermissionEventListener() {
        @Override public void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest) {
            if (requestCode == LOCATION_REQUEST) {
                placePicker();
            } else if (requestCode == GALLERY_REQUEST) {
                goGallery();
            } else if (requestCode == CAMERA_REQUEST) {
                goCamera();
            } else if (requestCode == REQUEST_READ_EXTERNAL) {
                decideVisibilityOfAlerts();
                getMessageAdapter().notifyDataSetChanged();
            } else {
                askPacket(requestCode);
            }
        }
        @Override public void onAnyPermissionsDenied(int requestCode, PermissionRequest permissionRequest) {}
    };

    private void askPacket(int packetType) {
        SpecialPacket sp = SpecialPacketBuilderFactory.instance().getPacket(getContext(), packetType);
        if (sp == null || sp.isEmpty()) {
            Toast.makeText(getContext(), R.string.packet_cannot_created, Toast.LENGTH_SHORT).show();
            return;
        }
        final Message msg = MessagePrepareUtils.prepareSpecialMessage(mDestination, sp);
        com.opcon.ui.views.SpecialPacketView spView = new com.opcon.ui.views.SpecialPacketView(getContext());
        spView.setMessage(msg);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.are_you_sure_for_sending_this_packet)
            .setPositiveButton(R.string.sent, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    sent(msg);
                }
            }).setNegativeButton(R.string.not_now, null).setView(spView).show();
    }

    void needPermission(int s) {
        needPermission(getString(s));
    }

    void needPermission(String s) {
        AlertDialog.Builder mBuilder = new AlertDialog.Builder(getActivity());
        mBuilder.setTitle(R.string.need_permission)
            .setMessage(s)
            .setCancelable(true)
            .setPositiveButton(R.string.permission_to_settings, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PermissionManagement.showAppSettingsPageFor(getContext());
                }
            });
        mBuilder.show();
    }

    private void initPermissionAlert() {
        decideVisibilityOfAlerts();

        mIndicator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!PermissionUtils.checkReadWriteExternalStorage(getContext())) {
                    PermissionRequest r;
                    if (Build.VERSION.SDK_INT >= 16) {
                        r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
                    }  else {
                        r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    if (PermissionUtils.isAnyPermissionPersistentlyDenied(getContext(), r)) {
                        PermissionManagement.showAppSettingsPageFor(getContext());
                    } else {
                        mPermissionManagement.builtRequest(REQUEST_READ_EXTERNAL, r.permissions).observer(mPermissionEventListener).request();
                    }
                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPermissionManagement.dispatchEvent(requestCode, grantResults);
    }

    public void newMessage(Message built) {
        mMessageAdapter.addMessages(Collections.singletonList(built));
        mRecyclerView.smoothScrollToPosition(mMessageAdapter.getItemCount());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MessageDispatcher.getInstance().removeMessageEventListener(this);

        ComponentListenerManager mComponentManagerListener = ComponentListenerManager.getInstance(getContext());
        mComponentManagerListener.removeComponentListener(Ack.class, mAckListener);

        mImageMessageUploader.removeListener(mUploadListener);
        mMessageAdapter.shutdown();
        mRecyclerView.setAdapter(null);
        mMessageAdapter = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("mDestination", mDestination);
    }

    private ImageMessageUploader.UploadListener mUploadListener = new ImageMessageUploader.UploadListener() {
        @Override
        public void uploadSuccess(Message id, Uri dUri) {
            if (mMessageAdapter != null){
                mMessageAdapter.updateMessage(id.getId());
            }
        }

        @Override
        public void uploadFail(Message id) {
            if (mMessageAdapter != null){
                mMessageAdapter.updateMessage(id.getId());
            }
        }

        @Override
        public void uploadStarted(Message msg) {
            if (mMessageAdapter != null){
                mMessageAdapter.updateMessage(msg.getId());
            }
        }
    };

    @Override
    public void onNewMessage(Message message, boolean received) {
        decideVisibilityOfAlerts();

        if (received && (message.getType() == Message.LAST_CAPTURED_IMAGE || message.getType() == Message.PICTURE)) {
            String thumbinal = message.getString(Message.Picture.THUMBNAIL);
            File file = prepareThumbinalFile();
            message.put(Message.Picture.THUMBNAIL, file.getAbsolutePath());
            MessageProvider.Utils.update(getContext(), message);
            PicassoCompressor.decode(thumbinal, file);
        }

        if (message.getRelationship().equals(mDestination)) {
            newMessage(message);
        }
    }

    @Override
    public void detectedDoesNotExists(int id) {
        if (mMessageAdapter != null && mMessageAdapter.getMessageIds() != null) {
            List<Integer> messageIds = mMessageAdapter.getMessageIds();
            int index = -1;
            for (int i= 0; i < messageIds.size(); i++) {
                Integer msgId = messageIds.get(i);
                if (msgId != null && msgId == id) {
                    index = i;
                }
            }
            if (index != -1) {
                messageIds.remove(index);
                mMessageAdapter.notifyItemRemoved(index);
            }
        }
    }

    private File prepareThumbinalFile() {
        String file = Environment.getExternalStorageDirectory() +
            ImageStorageUtils.RECEIVED_PICTURES_DIRECTORIES;
        File ffile = new File(file);
        if (!ffile.exists()) {
            ffile.mkdirs();
        }

        return new File(ffile, ImageStorageUtils.getRandomFileNameForTimestamp(System.currentTimeMillis()));
    }

    public void onDestinationEnabled() {
        mChatInputView.enable();
        mDestinationInBlackList = false;
    }

    public void onDestinationDisabled() {
        mChatInputView.disable(getString(R.string.you_has_blocked));
        mDestinationInBlackList = true;
    }

    void goCamera() {

        CAMERA_REQUEST_FOR_DESTINATION = mDestination;

        File tempFile = null;
        try {
            tempFile = createImageFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(),
            "com.opcon.fileprovider", tempFile));
        startActivityForResult(intent, CAMERA_REQUEST);
    }




    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        // Create an image file mName
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",         /* suffix */
            storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mTempCameraFile = image.getAbsolutePath();
        return image;
    }

    private void goGallery() {
        Intent contentIntent = FileUtils.createGetContentIntent();
        contentIntent.setType(FileUtils.MIME_TYPE_IMAGE);
        Intent intent = Intent.createChooser(contentIntent, "Select an image");
        startActivityForResult(intent, GALLERY_REQUEST);
    }

    public void inHiddenState() {
        mChatInputView.hideKeyboard();
        mChatInputView.hideRecyclerView(false);
    }

    private static class MessagePrepareUtils {

        private interface MessagePreparingListener {
            void onReady(Message msg);
        }

        private static Message prepareTextMessage(String receiver, String text) {
            Message.Builder b = new Message.Builder();
            b.setReceiver(receiver)
                .setSender(PresenceManager.uid())
                .setSentTimestamp(System.currentTimeMillis())
                .setType(Message.TEXT)
                .setSpecialParam(Message.Text.BODY, text);
            return b.built();
        }

        private static Message prepareLocationMessage(String receiver, double latitude, double longitude, String address) {
            Message.Builder b = new Message.Builder();
            b.setReceiver(receiver)
                .setSender(PresenceManager.uid())
                .setSentTimestamp(System.currentTimeMillis())
                .setType(Message.LOCATION)
                .setSpecialParam(Message.Location.ADDRESS, address)
                .setSpecialParam(Message.Location.LATITUDE, latitude)
                .setSpecialParam(Message.Location.LONGITUDE, longitude);
            return b.built();
        }

        private static Message prepareSpecialMessage(String receiver, SpecialPacket sp) {
            Message.Builder b = new Message.Builder();
            b.setReceiver(receiver)
                .setSender(PresenceManager.uid())
                .setSentTimestamp(System.currentTimeMillis())
                .setTriedForServer(false)
                .putSpecialPacket(sp);
            return b.built();
        }

        private static void prepareUploadImageMessage(Context context, String path, PicassoCompressor.CompressorListener listener) {
            // @path is uncompressed file.
            PicassoCompressor compressor = new PicassoCompressor(context);
            String targetFileName = ImageStorageUtils.getRandomFileNameForTimestamp(System.currentTimeMillis());
            File mDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "Opcon" + File.separator + "Sent Images");
            if (!mDirectory.exists()) {
                mDirectory.mkdirs();
            }
            File mTargetFile = new File(mDirectory, targetFileName);
            compressor.compress(new File(path))
                .size(AvatarLoader.MESSAGE_PICTURE_WIDTH, AvatarLoader.MESSAGE_PICTURE_HEIGHT)
                .quality(100)
                .lowMemory(PicassoCompressor.isLowMemoryRequires(context))
                .listen(listener)
                .to(mTargetFile)
                .so();
        }

        private static void prepareUploadImageMessage(final Context c, final String receiver, final String path, final MessagePreparingListener listener) {
            new Thread(new Runnable() {
                @Override public void run() {
                    prepareUploadImageMessage(c, path, new PicassoCompressor.CompressorListener() {
                        @Override
                        public void onCompressed(File file, Bitmap bitmap) {
                            Message.Builder b = new Message.Builder();
                            b.setReceiver(receiver)
                                .setSender(PresenceManager.uid())
                                .setSentTimestamp(System.currentTimeMillis())
                                .setType(Message.PICTURE)
                                .setSpecialParam(Message.Picture.FILE, file.getAbsolutePath())
                                .setSpecialParam(Message.Picture.DONE, false)
                                .setSpecialParam(Message.Picture.THUMBNAIL, PicassoCompressor.encode(bitmap, AvatarLoader.THUMBNAIL, AvatarLoader.THUMBNAIL, 90));
                            listener.onReady(b.built());
                        }
                    });
                }
            }).start();
        }

    }
}
