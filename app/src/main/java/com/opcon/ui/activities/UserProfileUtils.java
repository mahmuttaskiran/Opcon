package com.opcon.ui.activities;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.opcon.R;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.permission.PermissionRequest;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.libs.registration.RegistrationManagement;


import java.io.File;
import java.lang.ref.WeakReference;

import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 03/03/2017.
 */

public class UserProfileUtils {
  protected static void showProfileOptions(final Activity context, final WeakReference<TextView> tv, final WeakReference<ImageView> iv, final PermissionManagement pm) {
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    String[] arr;
    if (TextUtils.isEmpty(PresenceManager.getAvatar(context))) {
      arr = context.getResources().getStringArray(R.array.arr_options_nav_view_no_picture);
    } else {
      arr = context.getResources().getStringArray(R.array.arr_options_nav_view_has_picture);
    }

    builder.setItems(arr, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        if (which == 1)
          changeProfilePictureWithGallery(context, pm);
        else if (which == 0)
          changeProfilePictureWithCamera(context, pm);
        else if (which == 2)
          changeName(context, tv);
        else if (which == 3)
          showProfilePicture(context);
        else if (which == 4)
          removeProfilePicture(context, iv);
      }
    });
    builder.show();
  }

  private static void removeProfilePicture(final Context context, final WeakReference<ImageView> iv) {


      final ProgressDialog mProgressDialog = ProgressDialog.show(context, null,
          context.getString(R.string.photo_will_remove),
          true,
          true);

      FirebaseDatabase.getInstance()
          .getReference("users/" + PresenceManager.uid())
          .child("avatar")
          .setValue(false)
          .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              mProgressDialog.dismiss();
              if (task.isSuccessful()) {
                MainActivity.mAvatarUpdated = true;
                ProfileImageUpdaterActivity.getProfilePictureFile(context).delete();
                setupUserAvatar(context, iv);
                Toast.makeText(context, R.string.removed_profile_picture, Toast.LENGTH_SHORT).show();
              } else {
                Toast.makeText(context, R.string.cannat_removed_profile_picture, Toast.LENGTH_SHORT).show();
                Timber.d("error occurred!", task.getException());
              }
            }
          });

  }

  private static void setupUserAvatar(Context context, WeakReference<ImageView> iv) {
    ContextWrapper contextWrapper = new ContextWrapper(context);
    File directory = contextWrapper.getDir("images", ContextWrapper.MODE_PRIVATE);
    final File userAvatarFile = ProfileImageUpdaterActivity.getProfilePictureFile(context);
    if (userAvatarFile == null || !userAvatarFile.exists()) {
      if (iv.get() != null) {
        iv.get().setImageBitmap(null);
      }
    } else {
      ImageView imageView = iv.get();
      if (imageView != null) {
        imageView.setImageBitmap(null);
        Glide.with(context)
            .load(userAvatarFile)
            .override(300, 300)
            .diskCacheStrategy(DiskCacheStrategy.SOURCE)
            .error(R.drawable.no_avatar)
            .placeholder(R.drawable.no_avatar)
            .into(imageView);
      }
    }
  }


  private static void showProfilePicture(Context context) {
      String avatar= PresenceManager.getAvatar(context);
      FullScreenImageViewerActivity.show(context, Uri.parse(avatar));
  }

  private static void changeName(final Context context, final WeakReference<TextView> tv) {
      String currentName = RegistrationManagement.getInstance().getName(context);
      AlertDialog.Builder builder = new AlertDialog.Builder(context);
      builder.setTitle(R.string.change_your_name);
      final EditText editText = new EditText(context);
      editText.setHint(TextUtils.isEmpty(currentName) ||
          currentName.equals(context.getResources().getString(R.string.no_name)) ?
          context.getString(R.string.enter_a_name) : currentName);
      editText.setHintTextColor(context.getResources().getColor(R.color.midGrey));
      editText.setTextColor(context.getResources().getColor(R.color.materialGrey));
      editText.setPadding(50, 50, 50, 50);
      editText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
      builder.setView(editText);
      builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          if (!TextUtils.isEmpty(editText.getText().toString())) {
            FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid()).child("name").setValue(editText.getText().toString());
            RegistrationManagement.getInstance().setName(context, editText.getText().toString());
            TextView textView = tv.get();
            if (textView != null) {
              textView.setText(editText.getText().toString().trim());
            }


            Toast.makeText(context, context.getString(R.string.hello) + ", " + editText.getText().toString(), Toast.LENGTH_SHORT).show();

          } else {
            Toast.makeText(context, R.string.please_enter_valid_name, Toast.LENGTH_SHORT).show();
          }
        }

      });
      builder.setNegativeButton(R.string.cancel, null);
      builder.show();
  }

  private static void changeProfilePictureWithGallery(final Context context, PermissionManagement pm) {

    PermissionRequest r;
    if (Build.VERSION.SDK_INT >= 16) {
      r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }  else {
      r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }


    if (PermissionUtils.check(context, r)) {
      goGallery(context);
    } else {

      if (PermissionUtils.isAnyPermissionPersistentlyDenied(context, r)) {
        final DialogInterface.OnClickListener gs = new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            PermissionManagement.showAppSettingsPageFor(context);
          }
        };
        AlertDialog.Builder mBuilder= new AlertDialog.Builder(context);
        mBuilder.setCancelable(true)
            .setTitle(R.string.need_permission)
            .setMessage(R.string.permission_for_change_avatar_with_gallery)
            .setPositiveButton(R.string.permission_to_settings, gs);
        mBuilder.show();

      } else {

        PermissionManagement.PermissionEventListener pel = new PermissionManagement.PermissionEventListener() {
          @Override
          public void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest) {
            goGallery(context);
          }

          @Override
          public void onAnyPermissionsDenied(int requestCode, PermissionRequest permissionRequest) {
            // ignore
          }
        };

        pm.observer(pel)
            .builtRequest(1223, r.permissions)
            .request();
      }

    }

  }

  private static void changeProfilePictureWithCamera(final Context context, PermissionManagement pm) {
    PermissionRequest r;
    if (Build.VERSION.SDK_INT >= 16) {
      r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }  else {
      r =new PermissionRequest(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }

    if (PermissionUtils.check(context, r)) {
      goCamera(context);
    } else {

      if (PermissionUtils.isAnyPermissionPersistentlyDenied(context,
          r)) {

        final DialogInterface.OnClickListener gs = new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialogInterface, int i) {
            PermissionManagement.showAppSettingsPageFor(context);
          }
        };

        AlertDialog.Builder mBuilder = new AlertDialog.Builder(context);
        mBuilder.setCancelable(true)
            .setTitle(R.string.need_permission)
            .setMessage(R.string.permission_for_change_avatar_with_camera)
            .setPositiveButton(R.string.permission_to_settings, gs);
        mBuilder.show();

      } else {

        PermissionManagement.PermissionEventListener pel = new PermissionManagement.PermissionEventListener() {
          @Override
          public void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest) {
            goCamera(context);
          }

          @Override
          public void onAnyPermissionsDenied(int requestCode, PermissionRequest permissionRequest) {
            // ignore
          }
        };

        pm.observer(pel).builtRequest(1222, r.permissions).request();
      }

    }
  }

  private static void goCamera(Context c) {
    Intent intent = new Intent(c, ProfileImageUpdaterActivity.class);
    intent.putExtra(ProfileImageUpdaterActivity.FROM_CAMERA, true);
    c.startActivity(intent);
  }

  private static void goGallery(Context c) {
    Intent intent = new Intent(c, ProfileImageUpdaterActivity.class);
    intent.putExtra(ProfileImageUpdaterActivity.FROM_CAMERA, false);
    c.startActivity(intent);
  }

}
