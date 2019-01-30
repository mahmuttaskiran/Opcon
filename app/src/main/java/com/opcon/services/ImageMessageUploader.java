package com.opcon.services;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.opcon.components.Message;
import com.opcon.database.MessageProvider;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.utils.AndroidEnvironmentsUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 *
 * Created by Mahmut Taşkiran on 03/02/2017.
 *
 */

public class ImageMessageUploader {

    public volatile static ImageMessageUploader singleton;
    public static ImageMessageUploader getInstance(@Nullable Context c) {
        if (singleton == null) {
            synchronized (ImageMessageUploader.class) {
                if (singleton == null) {
                    if (c == null) {
                        throw new IllegalStateException("for first initiate context cannot be null.");
                    }
                    singleton = new ImageMessageUploader(c);
                }
            }
        }
        return singleton;
    }

    public interface UploadListener {
        void uploadSuccess(Message id, Uri dUri);
        void uploadFail(Message id);
        void uploadStarted(Message msg);
    }

    private Context mContext;

    private HashMap<Integer, StorageTask<UploadTask.TaskSnapshot>> mUploads;
    private List<UploadListener> mListeners;
    private StorageReference mReference;

    private ImageMessageUploader(Context c) {
        mUploads = new HashMap<>();
        mListeners = new ArrayList<>();
        FirebaseStorage mFirebaseStorage = FirebaseStorage.getInstance();
        mReference = mFirebaseStorage.getReference("messages").child(PresenceManager.uid());
        mContext = c.getApplicationContext();
    }

    public void addListener(UploadListener listener) {
        if (!mListeners.contains(listener))
            mListeners.add(listener);
    }

    public void removeListener(UploadListener listener) {
        mListeners.remove(listener);
    }

    public void start(Message msg) {

        synchronized (ImageMessageUploader.class) {
            if (!isUploading(msg.getId())) {
                start(msg, Uri.fromFile(new File(msg.getString(Message.Picture.FILE))));
            }
        }

    }

    public void start(Message id, Uri u) {
        if (mUploads.containsKey(Integer.valueOf(id.getId()))) return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null)
            throw new IllegalStateException("before start download please sign in.");


        if (!AndroidEnvironmentsUtils.hasActiveInternetConnection(mContext)) {
            return;
        }


        StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("uid", PresenceManager.uid()).build();

        Upload mUpload = new Upload(id);
        StorageTask<UploadTask.TaskSnapshot> ts = mReference.child(gfn())
                .putFile(u, metadata)
                .addOnCompleteListener(mUpload)
                .addOnFailureListener(mUpload);
        mUploads.put(id.getId(), ts);

        if (mListeners != null) {
            for (UploadListener mListener : mListeners) {
                if (mListener != null) {
                    mListener.uploadStarted(id);
                }
            }
        }

    }

    public boolean isInQueue(int id) {
        return mUploads.get(id) != null;
    }

    public boolean isUploading(int id) {
        return mUploads.get(id) != null && mUploads.get(id).isInProgress();
    }

    public void stop(int id) {
        StorageTask<UploadTask.TaskSnapshot> ts = mUploads.get(id);
        if (ts != null) {
            ts.pause();
            ts.cancel();
        }
        mUploads.remove(id);
    }

    private void fail(Message id) {
        Message msg = MessageProvider.Utils.getSingleMessage(mContext, id.getId());
        if (msg != null) {
            msg.put(Message.Picture.DONE, false);
            msg.put(Message.Picture.FAILED, true);
            MessageProvider.Utils.update(mContext, msg);
            for (UploadListener mListener : mListeners) {
                if (mListener != null) {
                    mListener.uploadFail(msg);
                }
            }

            // there is an important bug!
            // mUploads.remove(id) !!! what is that !!!
            // fuck you. look at the type of mUploads!

            mUploads.remove(Integer.valueOf(id.getId()));

        }
    }

    private void success(Message msg, Uri downloadUrl) {
        if (msg != null && downloadUrl != null) {

            msg.put(Message.Picture.DONE, true);
            msg.put(Message.Picture.FAILED, false);
            msg.put(Message.Picture.URL, downloadUrl.toString());

            msg.send(mContext);

            MessageProvider.Utils.update(mContext, msg);

            for (UploadListener mListener : mListeners) {
                if (mListener != null) {
                    mListener.uploadSuccess(msg, downloadUrl);
                }
            }

            mUploads.remove(msg.getId());
        }
    }

    private String gfn() {
        return String.valueOf(new Random().nextInt(1000000 * 123)) + ".jpeg";
    }

    private static class Upload implements OnCompleteListener<UploadTask.TaskSnapshot>,
    OnFailureListener {
        private Message mId;
        private Upload(Message id) {
            this.mId = id;
        }
        @Override public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
            if (task.isSuccessful() && ImageMessageUploader.getInstance(null).isInQueue(mId.getId())) {
                mId.put(Message.Picture.DONE, true);
                ImageMessageUploader.getInstance(null)
                    .success(mId, task.getResult().getDownloadUrl());
            }
        }
        @Override public void onFailure(@NonNull Exception e) {
            ImageMessageUploader.getInstance(null)
                    .fail(mId);
        }
    }

}
