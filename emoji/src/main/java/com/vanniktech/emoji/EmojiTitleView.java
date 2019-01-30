package com.vanniktech.emoji;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


/**
 * Created by Mahmut Taşkiran on 09/03/2017.
 */

public class EmojiTitleView extends RelativeLayout implements View.OnClickListener {

  private static final String TAG = "EmojiTitleView";

  public interface EmojiTitleListener {

    void onPositionSelected(int p);
    void onBackspace();
  }
  private LinearLayout mRoot;

  private EmojiTitleListener mTitleListener;
  private int mSelectedItemColor;
  private int mUnselectedItemColor;
  private int mSelectedItemPosition = -1;
  private View mCurrentSelectedView;
  public EmojiTitleView(Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    mRoot = (LinearLayout) inflate(getContext(), R.layout.emoji_title, this).findViewById(R.id.emoji_title);
    for (int i = 0; i < mRoot.getChildCount(); i++) {
      mRoot.getChildAt(i).setOnClickListener(this);
    }
  }

  @Override
  public void onClick(View v) {
    int p = findPosition(v);
    if (p == -1) {
      throw new IllegalStateException();
    }

    if (p == mRoot.getChildCount()-1 && mTitleListener != null) {
      mTitleListener.onBackspace();
      backspaceAnimation((ImageView)v);
    } else {
      if (mTitleListener != null)
        mTitleListener.onPositionSelected(p);
      select(p);
    }
  }

  private void backspaceAnimation(final ImageView v) {
    v.setColorFilter(mSelectedItemColor);
    ViewCompat.animate(v)
        .scaleX(0.5F)
        .scaleY(0.5F)
        .setDuration(50)
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            ViewCompat.animate(v)
                .scaleX(1F)
                .scaleY(1F)
                .setDuration(50)
                .withEndAction(new Runnable() {
                  @Override
                  public void run() {
                    v.setColorFilter(mUnselectedItemColor);
                  }
                })
                .start();
          }
        }).start();
  }

  public void setSelectedItemColor(int c) {
    mSelectedItemColor = c;
    if (mSelectedItemPosition != -1)
      ((ImageView)mRoot.getChildAt(mSelectedItemPosition)).setColorFilter(mSelectedItemColor);
  }

  public void setUnselectedItemColor(int c) {
    mUnselectedItemColor = c;
    for (int i = 0; i < mRoot.getChildCount(); i++) {
      if (i != mSelectedItemPosition) {
        ((ImageView)mRoot.getChildAt(i)).setColorFilter(mUnselectedItemColor);
      }
    }
  }

  public void select(int p) {
    View mPreviousSelectedView = mCurrentSelectedView;
    mSelectedItemPosition = p;
    mCurrentSelectedView = mRoot.getChildAt(p);

    if (mPreviousSelectedView != null) {
      unselect(mPreviousSelectedView);
    }

    selectAnimation(mCurrentSelectedView);
    ImageView iv = (ImageView) mCurrentSelectedView;
    iv.setColorFilter(mSelectedItemColor);
  }

  private void unselect(@NonNull View v) {
    ImageView iv = (ImageView) v;
    iv.setColorFilter(mUnselectedItemColor);
    unselectAnimation(v);
  }

  public void showAnimation() {
    for (int i = 0; i < mRoot.getChildCount(); i+=2) {
      final View v = mRoot.getChildAt(i);
      if (v!=null) {
        final int ii= i;
        v.setScaleX(0);
        v.setScaleY(0);
        ViewCompat.animate(v)
            .scaleX(0.5F)
            .scaleY(0.5F)
            .setDuration(100)
            .withEndAction(new Runnable() {
              @Override
              public void run() {
                ViewCompat.animate(v).scaleY(1).scaleX(1).setDuration(100).start();
                View vv = mRoot.getChildAt(ii+1);
                if (vv!=null) {
                  vv.setScaleY(0);
                  vv.setScaleX(0);
                  ViewCompat.animate(vv).scaleX(1).scaleY(1).setDuration(200).start();
                }
              }
            }).start();
      }
    }
  }



  private void unselectAnimation(View v) {
    ViewCompat.animate(v)
        .scaleX(1)
        .scaleY(1)
        .setDuration(100)
        .start();
  }

  private void selectAnimation(final View v) {
    ViewCompat.animate(v)
        .scaleX(0.5F)
        .scaleY(0.5F)
        .setDuration(50)
        .withEndAction(new Runnable() {
          @Override
          public void run() {
            ViewCompat.animate(v)
                .scaleX(1F)
                .scaleY(1F)
                .setDuration(50)
                .start();
          }
        }).start();
  }

  public void setEmojiTitleListener(EmojiTitleListener l) {
    mTitleListener = l;
  }

  private int findPosition(View v) {
    for (int i = 0; i < mRoot.getChildCount(); i++) {
      if (v == mRoot.getChildAt(i)) {
        Log.d(TAG, "findPosition: " + i);
        return i;
      }
    }
    return -1;
  }

}
