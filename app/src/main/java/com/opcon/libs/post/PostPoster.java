package com.opcon.libs.post;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.opcon.ui.utils.AvatarLoader;
import com.opcon.components.Post;
import com.opcon.database.PostBase;
import com.opcon.database.NotifierProvider;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.PicassoCompressor;
import com.opcon.notifier.components.constants.Packets;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 *
 * Created by Mahmut Taşkiran on 19/03/2017.
 */

public class PostPoster {

  public interface PostEventListener {
    void onPosted(Post post);
    void onFail(Post post);
  }

  private volatile static PostPoster singleton;

  private List<PostEventListener> listeners;

  public static PostPoster getInstance(Context context){
    if (context == null && singleton == null)
      throw new IllegalAccessError("Please init with an context before any usage.");

    if (singleton == null) {
      synchronized (PostPoster.class) {
        if (singleton == null) {
          singleton = new PostPoster(context.getApplicationContext());
        }
      }
    }
    return singleton;
  }

  private Context mContext;
  private StorageReference mRef;

  private PostPoster(Context context) {
    Timber.d("PostPoster initialized!");
    mContext = context;
    mRef = FirebaseStorage.getInstance().getReference("posts/" + PresenceManager.uid());
    listeners = new ArrayList<>();

    initImagePostListener();
    detectUnawareUpdatedImages();
    postIts(PostBase.Utils.getAcceptedLocationalPosts(mContext));
  }

  public void addEventListener (PostEventListener listener) {
    if (!listeners.contains(listener))
      listeners.add(listener);
  }

  public void removeEventListener(PostEventListener listener) {
    listeners.remove(listener);
  }

  void failed(Post post){
    for (PostEventListener listener : listeners) {
      if (listener != null) {
        listener.onFail(post);
      }
    }
  }

  void posted(Post post) {
    for (PostEventListener listener : listeners) {
      if (listener != null) {
        listener.onPosted(post);
      }
    }
  }

  public boolean isPosting(Post post){
    return isUploading(post.getUploadedFilename());
  }

  void postIts(List<Post> posts) {
    for (Post post : posts) {
      postIt(post);
    }
  }

  public static String completeAddressOf(Context context, double latitude, double longitude) {
    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
    try {
      List<Address> fromLocation = geocoder.getFromLocation(latitude, longitude, 1);
      if (fromLocation != null && !fromLocation.isEmpty()) {
        String address = fromLocation.get(0).getLocality();
        Timber.d("subadminarea: %s", fromLocation.get(0).getSubAdminArea());
        Timber.d("locality: %s", fromLocation.get(0).getLocality());
        if (address == null) {
          Timber.d("locality is null");
          address = fromLocation.get(0).getSubAdminArea();
        }
        return address;
      }
    } catch (IOException e) {
      e.printStackTrace();
      Timber.d("locational post cannot posted");
    }
    return null;
  }

  public void postIt(Post post) {
    if (post.isLocationPost() && TextUtils.isEmpty(post.getAddress())) {
      Timber.d("location does not have address");
      Timber.d("will complete");

      String address = completeAddressOf(mContext, post.getLatitude(), post.getLongitude());

      Timber.d("address os this post: %s", address);

      if (address != null) {
        Timber.d("post posted");
        post.setAddress(address);
        postRemote(post);
        PostBase.Utils.delete(mContext, post.getId());
      }

    } else if (isLastImagePost(post)) {
      if (!TextUtils.isEmpty(post.getImageLocalePath())) {
        uploadImage(post);
      } else {
        Timber.d("file cannot find.");
      }
    } else {
      postRemote(post);
      PostBase.Utils.delete(mContext, post.getId());
    }
  }

  private void detectUnawareUpdatedImages() {
    List<Post> ifHasImage = PostBase.Utils.getIfHasImage(mContext);
    if (ifHasImage != null && !ifHasImage.isEmpty()) {
      Timber.d("we will detect unaware images");
      for (final Post post : ifHasImage) {
        String filename = post.getUploadedFilename();
        mRef.child(filename).getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
          @Override public void onComplete(@NonNull Task<Uri> task) {

            if (task.isSuccessful() && task.getResult()!=null) {
              PostBase.Utils.delete(mContext, post.getId());
              post.setDownloadUrl(task.getResult().toString());
              postRemote(post);
              Timber.d("detected unaware upload: %s, %s", post.getImageDownloadUrl(), post.getUploadedFilename());
            } else {
              if (post.isAccepted()) {
                if (!isUploading(post.getUploadedFilename())) {
                  postIt(post);
                  Timber.d("detected accepted post but don't uploaded: %s", post.getUploadedFilename());
                }
              }
            }
          }
        });
      }
    }
  }

  private boolean isLastImagePost(Post post) {
    return post.getPacketType() == Packets._LAST_IMAGE;
  }

  private void uploadImage(final Post post) {

    if (!isUploading(post.getUploadedFilename())) {
      try {
        Timber.d("post will be upload: %s, %s", post.getImageLocalePath(), post.getUploadedFilename());
        File dir = new File(mContext.getCacheDir(), "posts");
        if (!dir.exists()) {
          dir.mkdirs();
        }


        // fix OP-00013

        Uri uri;

        File localeFile = new File(post.getImageLocalePath());
        if (localeFile.exists()) {
          uri = Uri.fromFile(localeFile);
        } else {
          uri = Uri.parse(post.getImageLocalePath());
        }


        File tempImage = File.createTempFile(post.getUploadedFilename().substring(0,
            post.getUploadedFilename().lastIndexOf(".")), ".jpeg", dir);

        PicassoCompressor compressor = new PicassoCompressor(mContext);
        compressor.compress(uri)
            .to(tempImage)
            .quality(80)
            .lowMemory(PicassoCompressor.isLowMemoryRequires(mContext))
            .size(AvatarLoader.MESSAGE_PICTURE_WIDTH, AvatarLoader.MESSAGE_PICTURE_HEIGHT)
            .listen(new PicassoCompressor.CompressorListener() {
              @Override public void onCompressed(File file, Bitmap bitmap) {
                Timber.d("compressed image will be upload: %s", file.getAbsoluteFile());
                Timber.d("compressed image size: %d", (int) file.length());
                StorageMetadata metadata = new StorageMetadata.Builder()
                    .setCustomMetadata("uid", PresenceManager.uid())
                    .setCustomMetadata("lfile", file.getAbsolutePath())
                    .build();

                mRef.child(post.getUploadedFilename()).putFile(Uri.fromFile(file), metadata).addOnCompleteListener(mOnCompleteListener);

              }
            }).so();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else {
      Timber.d("post is already uploading: %s", post.getUploadedFilename());
    }

  }

  private OnCompleteListener<UploadTask.TaskSnapshot> mOnCompleteListener = new OnCompleteListener<UploadTask.TaskSnapshot>() {
    @Override
    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
      Timber.d("uploading result: success: %s", task.isSuccessful());
      if (task.isSuccessful()) {
        if (task.getResult().getMetadata() != null && task.getResult().getDownloadUrl() != null) {

          String lfile = task.getResult().getMetadata().getCustomMetadata("lfile");

          if (!TextUtils.isEmpty(lfile)) {
            if (new File(lfile).delete()) {
              Timber.d("cached post file (as image) deleted.");
            }
          }

          String name = task.getResult().getMetadata().getName();
          PostBase db = PostBase.getInstance(mContext);
          SQLiteDatabase rdb = db.getReadableDatabase();

          Timber.d("uploaded file name as: %s", name);

          Cursor c = rdb.query("posts", null, "uploadedFilename = ?", new String[]{name}, null, null, null);
          if (c != null && c.moveToNext()) {
            Post p = PostBase.Utils.parse(c);
            c.close();
            rdb.close();
            p.setDownloadUrl(task.getResult().getDownloadUrl().toString());
            postRemote(p);
            Timber.d("posted remote: %s", p.getImageDownloadUrl());
          } else {
            Timber.d("uploading success but post cannot find.");
          }
          if (c != null) {
            c.close();
          }
        }
      }
    }
  };

  private void postRemote(Post p) {
    Timber.d("posted: %d", p.getId());
    Timber.d("posted: %s", p.getUploadedFilename());
    p.prepareForPost();
    if (p.isRcs()) p.setRelationNotifier(NotifierProvider.Utils.get(mContext, p.getRelationNotifierSid()));
    DatabaseReference postRef = FirebaseDatabase.getInstance().getReference("posts/" + p.getOwner()).push();
    postRef.updateChildren(p.toMap());

    PostBase.Utils.delete(mContext, p.getId());

    posted(p);
  }

  private void initImagePostListener() {
    List<UploadTask> uploads = mRef.getActiveUploadTasks();
    Timber.d("there are %d active uploading operation.", uploads.size());
    if (!uploads.isEmpty()) {
      Timber.d("listening UN-LISTENING post uploading operations");
      for (UploadTask upload : uploads) {
        upload.addOnCompleteListener(mOnCompleteListener);
      }
    }
  }

  private boolean isUploading(String filename) {
    List<UploadTask> activeUploadTasks = mRef.getActiveUploadTasks();
    if (activeUploadTasks.isEmpty()) {
      return false;
    }
    for (UploadTask activeUploadTask : activeUploadTasks) {
      UploadTask.TaskSnapshot snapshot = activeUploadTask.getSnapshot();
      if (snapshot.getMetadata() == null || TextUtils.isEmpty(snapshot.getMetadata().getName()))
        continue;
      if (snapshot.getMetadata().getName().equals(filename))
        return true;
    }
    return false;
  }

}
