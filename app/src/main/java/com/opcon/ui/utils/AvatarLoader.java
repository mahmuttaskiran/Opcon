package com.opcon.ui.utils;

import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.opcon.R;

import java.util.Random;

import agency.tango.android.avatarview.AvatarPlaceholder;
import agency.tango.android.avatarview.ImageLoaderBase;
import agency.tango.android.avatarview.views.AvatarView;

/**
 * Created by Mahmut Taşkiran on 30/03/2017.
 */

public class AvatarLoader extends ImageLoaderBase {

  public static final int THUMBNAIL = 100;
  public static final int PROFILE_WIDTH = 700;
  public static final int PROFILE_HEIGHT = 700;
  public static final int MESSAGE_PICTURE_WIDTH = 1280;
  public static final int MESSAGE_PICTURE_HEIGHT = 720;

  private volatile static AvatarLoader instance;
  private static final int SIZE = 100;

  private static AvatarLoader getInstance() {
    if (instance == null) {
      synchronized (AvatarLoader.class) {
        if (instance == null)
          instance = new AvatarLoader();
      }
    }
    return instance;
  }

  @Override public void loadImage(@NonNull AvatarView avatarView, @NonNull AvatarPlaceholder avatarPlaceholder, String avatarUrl){
    Glide.with(avatarView.getContext())
        .load(avatarUrl)
        .placeholder(avatarPlaceholder)
        .override(SIZE, SIZE)
        .diskCacheStrategy(DiskCacheStrategy.SOURCE)
        .into(avatarView);

    int color = ContextCompat.getColor(avatarView.getContext(), new Random().nextInt(10) < 5 ? R.color.red_ff1744: R.color.colorPrimaryDark);
    avatarPlaceholder.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SCREEN));
  }

  public static void load(@NonNull AvatarView avatarView, String avatarUrl, String name) {
    getInstance().loadImage(avatarView, avatarUrl, name);
  }
}
