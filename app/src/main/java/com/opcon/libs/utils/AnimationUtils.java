package com.opcon.libs.utils;

import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Created by Mahmut Taşkiran on 09/03/2017.
 */

public class AnimationUtils {

  public static void scaleDownScaleUp(final View v, final float down, final float up, final int downTime, final int upTime) {
    ViewCompat.animate(v)
        .scaleX(down).scaleY(down)
        .setDuration(downTime)
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            ViewCompat.animate(v)
                .scaleX(up).scaleY(up)
                .setDuration(upTime)
                .start();
          }
        }).start();
  }

  public static void scaleDownScaleUp(final View v, final float down, final float up, final int downTime, final int upTime, final Runnable downEndAction, @Nullable final Runnable upEndAction) {
    if (upEndAction != null) {
      ViewCompat.animate(v)
          .scaleX(down).scaleY(down)
          .setDuration(downTime)
          .withEndAction(new Runnable() {
            @Override
            public void run() {
              ViewCompat.animate(v)
                  .scaleX(up).scaleY(up)
                  .setDuration(upTime)
                  .withEndAction(new Runnable() {
                    @Override
                    public void run() {
                      upEndAction.run();
                    }
                  })
                  .start();
              downEndAction.run();
            }
          }).start();
    } else {
      ViewCompat.animate(v)
          .scaleX(down).scaleY(down)
          .setDuration(downTime)
          .withEndAction(new Runnable() {
            @Override
            public void run() {
              ViewCompat.animate(v)
                  .scaleX(up).scaleY(up)
                  .setDuration(upTime)
                  .start();
              downEndAction.run();
            }
          }).start();
    }

  }

}
