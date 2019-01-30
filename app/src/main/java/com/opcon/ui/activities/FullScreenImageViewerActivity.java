package com.opcon.ui.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.github.chrisbanes.photoview.PhotoView;
import com.opcon.R;
import com.opcon.libs.utils.ImageStorageUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import butterknife.ButterKnife;
import butterknife.BindView;

public class FullScreenImageViewerActivity extends AppCompatActivity {

  Uri mUri;
  @BindView(R.id.imageview)
  PhotoView mImageView;
  @BindView(R.id.progressbar)
  ProgressBar mProgressBar;
  @BindView(R.id.activity_full_screen_image_viewer)
  RelativeLayout activityFullScreenImageViewer;
  @BindView(R.id.textview)
  TextView mTextView;
  @BindView(R.id.toolbar)
  Toolbar mToolbar;

  private Bitmap mBitmap;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_full_screen_image_viewer);
    ButterKnife.bind(this);
    setSupportActionBar(mToolbar);
    mToolbar.setTitleTextColor(Color.WHITE);
    mProgressBar.setIndeterminate(true);
    mUri = getIntent().getData();

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
    }

    loadImage();
  }


  private void loadImage() {
    Glide.with(getApplicationContext())
        .load(mUri)
        .asBitmap()
        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
        .listener(new RequestListener<Uri, Bitmap>() {
          @Override
          public boolean onException(Exception e, Uri model, Target<Bitmap> target, boolean isFirstResource) {
            mProgressBar.setIndeterminate(false);
            mProgressBar.setVisibility(View.GONE);
            mTextView.setVisibility(View.VISIBLE);
            return false;
          }

          @Override
          public boolean onResourceReady(Bitmap resource, Uri model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {

            mProgressBar.setIndeterminate(false);
            mProgressBar.setVisibility(View.GONE);
            mBitmap = resource;
            return false;
          }
        })
        .into(mImageView);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.full_screen_image_view, menu);
    return super.onCreateOptionsMenu(menu);
  }

  public static void show(Context context, Uri uri) {
    if (uri != null) {
      Intent intent = new Intent(context, FullScreenImageViewerActivity.class);
      intent.setData(uri);
      context.startActivity(intent);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.share) {

      if (mBitmap == null) {

        Toast.makeText(this, R.string.image_not_found, Toast.LENGTH_SHORT).show();

        return false;
      }

      if (ActivityCompat.checkSelfPermission(getApplicationContext(),
          Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
          PackageManager.PERMISSION_GRANTED) {
        share();
      } else {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            0);
      }

    } else if (item.getItemId() == R.id.save) {

      if (mBitmap == null) {
        Toast.makeText(this, R.string.image_not_found, Toast.LENGTH_SHORT).show();
        return false;
      }

      if (ActivityCompat.checkSelfPermission(getApplicationContext(),
          Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
          PackageManager.PERMISSION_GRANTED) {
        saveToGallery();
      } else {
        ActivityCompat.requestPermissions(this,
            new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
            1);
      }

    } else {

      onBackPressed();

    }
    return super.onOptionsItemSelected(item);
  }

  void share() {
    try {
      File file = File.createTempFile(String.valueOf(new Random().nextInt()), ".jpeg",
          Environment.getExternalStorageDirectory());
      FileOutputStream out = new FileOutputStream(file);
      mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
      out.close();
      out.flush();

      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("image/jpeg");
      intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
      startActivity(Intent.createChooser(intent, "Share"));

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void saveToGallery() {


    try {
      String dir = Environment.getExternalStorageDirectory() +
          File.separator +
          "Opcon" +
          File.separator +
          "Pictures";
      File file = new File(dir);
      if (!file.exists())
        file.mkdirs();

      String filename = ImageStorageUtils
          .getRandomFileNameForTimestamp(System.currentTimeMillis());

      File destFile = new File(file, filename);
      FileOutputStream out = new FileOutputStream(destFile);
      mBitmap.compress(Bitmap.CompressFormat.JPEG,
          100, out);
      out.flush();
      out.close();

      Toast.makeText(this, getString(R.string.saved) + "\n" + destFile.toString(), Toast.LENGTH_SHORT).show();
    } catch (IOException e) {
      e.printStackTrace();
      Toast.makeText(this, R.string.cannot_saved, Toast.LENGTH_SHORT).show();
    } finally {
      finish();
    }

  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == 0) {
      if (ActivityCompat.checkSelfPermission(getApplicationContext(),
          Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
          PackageManager.PERMISSION_GRANTED) {
        share();
      }
    } else {
      if (ActivityCompat.checkSelfPermission(getApplicationContext(),
          Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
          PackageManager.PERMISSION_GRANTED) {
        saveToGallery();
      }
    }
  }
}
