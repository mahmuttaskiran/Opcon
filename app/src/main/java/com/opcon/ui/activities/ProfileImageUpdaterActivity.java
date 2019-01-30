package com.opcon.ui.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.UploadTask;
import com.ipaulpro.afilechooser.utils.FileUtils;
import com.opcon.ui.utils.AvatarLoader;
import com.opcon.R;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.PicassoCompressor;

import com.opcon.libs.utils.AndroidEnvironmentsUtils;
import com.opcon.libs.utils.ImageStorageUtils;
import com.opcon.ui.dialogs.DialogUtils;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;


public class ProfileImageUpdaterActivity extends AppCompatActivity {

    public static final String FROM_CAMERA = "image";

    public static final int REQUEST_CAMERA = 1;
    public static final int REQUEST_GALLERY = 2;

    private String mTempCameraFile;
    private CropImageView mCropImageView;
    private FirebaseStorage mFirebaseStorage;
    private ProgressDialog mProgress;


    private boolean success;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop_image);



        mFirebaseStorage = FirebaseStorage.getInstance();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.profile_image_updater_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
            getSupportActionBar().setElevation(15);
        }

        this.mCropImageView = (CropImageView) findViewById(R.id.crop_image_view);
        this.mCropImageView.setFixedAspectRatio(true);

        if (getIntent().getExtras().getBoolean(FROM_CAMERA)) {
            try {
                goCamera();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            goGallery();
        }

    }

    void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgress != null) {
                    mProgress.dismiss();
                }
            }
        });

    }

    void goGallery() {
        Intent contentIntent = FileUtils.createGetContentIntent();
        contentIntent.setType(FileUtils.MIME_TYPE_IMAGE);
        Intent intent = Intent.createChooser(contentIntent, "Select an image");
        startActivityForResult(intent, REQUEST_GALLERY);
    }

    void goCamera() throws IOException {
        File tempFile = createImageFile();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getApplicationContext(),
                "com.opcon.fileprovider", tempFile));
        startActivityForResult(intent, REQUEST_CAMERA);
    }

    @SuppressLint("SimpleDateFormat")
    private File createImageFile() throws IOException {
        // Create an image file mName
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);



        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mTempCameraFile = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(final int requestCode, int resultCode, final Intent data) {
        if (resultCode != RESULT_OK)
            finish();
        if (REQUEST_GALLERY == requestCode) {
            if (data == null || data.getData() == null) {
                finish();
                return;
            }
        }
        Uri uri;
        if (requestCode == REQUEST_CAMERA) {
            uri = Uri.fromFile(new File(mTempCameraFile));
        } else {
            uri = data.getData();
        }
        mCropImageView.setImageUriAsync(uri);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener =
        new OnSuccessListener<UploadTask.TaskSnapshot>() {
        @Override
        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            String avatar = "false";
            if (taskSnapshot.getDownloadUrl() != null) {
                avatar = taskSnapshot.getDownloadUrl().toString();
            }

            PresenceManager.setAvatarDownloadUrl(getApplicationContext(), avatar);

            FirebaseDatabase.getInstance()
                    .getReference("users/" + PresenceManager.uid())
                    .child("avatar")
                    .setValue(avatar)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                success = true;
                                resultOk();
                            } else {
                                resultFail();
                            }
                        }
                    });
        }
    };


    private void upload() {
        StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("uid", PresenceManager.uid()).build();
        mFirebaseStorage.getReference()
                .child("pictures/" + String.valueOf(new Random().nextInt(10000000)) + ".jpeg")
                .putFile(Uri.fromFile(getProfilePictureFile(getApplicationContext())), metadata)
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        resultFail();
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_okay) {

            if (AndroidEnvironmentsUtils.hasActiveInternetConnection(getBaseContext()) && PresenceManager.getInstance(getApplicationContext()).isConnected() && PresenceManager.getInstance(getApplicationContext()).isJoined()) {

                PresenceManager.setAvatarFileName(getBaseContext(), ImageStorageUtils.getRandomFileNameForTimestamp(System.currentTimeMillis()));

                mProgress = ProgressDialog.show(this, null, getString(R.string.wait_for_update_profile), true, false);
                PicassoCompressor mCompressor = new PicassoCompressor(this);
                mCompressor.compress(mCropImageView.getCroppedImage())
                    .size(AvatarLoader.PROFILE_WIDTH, AvatarLoader.PROFILE_HEIGHT)
                    .to(getProfilePictureFile(getBaseContext()))
                    .quality(100)
                    .centerCrop()
                    .lowMemory(PicassoCompressor.isLowMemoryRequires(getBaseContext()))
                    .listen(new PicassoCompressor.CompressorListener() {
                        @Override
                        public void onCompressed(@Nullable File file, @Nullable Bitmap bitmap) {
                            upload();
                        }
                    }).so();

            } else {
                DialogUtils.alertOnlyOk(this, null, getString(R.string.not_internet_connection));
            }
        } else if (R.id.crop_image_menu_rotate_right == item.getItemId()) {
            mCropImageView.rotateImage(90);
        } else if (android.R.id.home == item.getItemId()) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private void resultOk() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                setResult(RESULT_OK, new Intent());
                finish();
            }
        });
        MainActivity.mAvatarUpdated = true;
    }

    private void resultFail() {

        getProfilePictureFile(getApplicationContext()).delete();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                dismissProgressDialog();
                AlertDialog.Builder mDialog = new AlertDialog.Builder(ProfileImageUpdaterActivity.this);
                mDialog.setTitle(R.string.we_are_so_sorry)
                    .setMessage(R.string.cannot_update_avatar)
                    .setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!TextUtils.isEmpty(mTempCameraFile)) {
            File file = new File(mTempCameraFile);
            if (file.exists()) {
                file.delete();
            }
        }

        String mFileName = PresenceManager.getAvatarFileName(getBaseContext());

        if (!success && !TextUtils.isEmpty(mFileName)) {
            getProfilePictureFile(getApplicationContext()).delete();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_updater_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public static File getProfilePictureDir(Context context) {
        ContextWrapper contextWrapper = new ContextWrapper(context);
        return contextWrapper.getDir("images", MODE_PRIVATE);
    }

    public static File getProfilePictureFile(Context context) {
      String avatarFileName = PresenceManager.getAvatarFileName(context);
      return new File(getProfilePictureDir(context), avatarFileName);
    }

}
