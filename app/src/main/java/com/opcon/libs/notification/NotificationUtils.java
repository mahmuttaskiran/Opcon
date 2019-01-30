package com.opcon.libs.notification;

import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.opcon.R;
import com.opcon.ui.drawables.CropCircleTransformation;

/**
 * Created by Mahmut Taşkiran on 21/02/2017.
 */

public class NotificationUtils {
  public interface Event {
    void onEvent(NotificationCompat.Builder b);
  }
  public static void addAvatar(final Context c, final NotificationCompat.Builder b, final String a, final Event e) {


    Glide.with(c)
        .load(a)
        .asBitmap()
        .transform(new CropCircleTransformation(c))
        .placeholder(R.drawable.no_avatar)
        .listener(new RequestListener<String, Bitmap>() {
          @Override
          public boolean onException(Exception exception, String model, Target<Bitmap> target, boolean isFirstResource) {
            b.setLargeIcon(BitmapFactory.decodeResource(c.getResources(), R.drawable.no_avatar));
            e.onEvent(b);
            return false;
          }

          @Override
          public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
            b.setLargeIcon(resource);
            e.onEvent(b);
            return false;
          }
        }).into(500, 500);



  }
  public static NotificationCompat.Builder defaultBuilder(Context context, String title, String content, PendingIntent pi) {
    NotificationCompat.Builder nb = new NotificationCompat.Builder(context);
    nb.setContentTitle(title)
        .setContentText(content)
        .setContentIntent(pi)
        .setSmallIcon(R.drawable.opcon_small_icon);
    if (Build.VERSION.SDK_INT >= 21) {
      nb.setCategory(NotificationCompat.CATEGORY_MESSAGE);
    }
    return nb;
  }
}
