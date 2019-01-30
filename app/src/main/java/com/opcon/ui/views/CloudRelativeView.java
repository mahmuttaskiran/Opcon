package com.opcon.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.opcon.R;
import com.opcon.ui.drawables.RandomIconsDrawable;

/**
 * Created by Mahmut Taşkiran on 05/03/2017.
 */

public class CloudRelativeView extends RelativeLayout {

  int mMaxIcon;
  float mIconMaxSize;
  float mIconMinSize;
  int mIconColor;
  public int mBackgroundColor;


  int mTopCloudColor;
  int mTopCloudWidth;
  int mTopCloudStrokeColor;
  int mTopCloudStrokeWidth;

  int mBottomCloudColor;
  int mBottomCloudWidth;
  int mBottomCloudStrokeColor;
  int mBottomCloudStrokeWidth;

  RelativeLayout mCloudRoot;

  CloudView mTopCloud;
  CloudView mBottomCloud;

  public RandomIconsDrawable mRandomIcons;

  public CloudRelativeView(Context context, AttributeSet attrs) {
    super(context, attrs);
    initValues(attrs);
    bindViews();
  }

  private void bindViews() {
    View root = LayoutInflater.from(getContext()).inflate(R.layout.cloud_relative_layout, this, true);
    mCloudRoot = (RelativeLayout) root.findViewById(R.id.root);
    mTopCloud = (CloudView) root.findViewById(R.id.topCloud);
    mBottomCloud = (CloudView) root.findViewById(R.id.bottomCloud);
    RandomIconsDrawable.Builder b = new RandomIconsDrawable.Builder();


    b.setBackgroundColor(mBackgroundColor)
        .setResources(getResources())
        .setIconColor(mIconColor)
        .setMax(mMaxIcon)
        .setMaxScale(mIconMaxSize)
        .setMinScale(mIconMinSize);

    mRandomIcons = b.built();

    mCloudRoot.setBackgroundDrawable(mRandomIcons);

    if (hasTopCloud()) {
      mTopCloud.setVisibility(VISIBLE);


      RelativeLayout.LayoutParams newParam = new LayoutParams(mTopCloud.getLayoutParams());
      newParam.height = mTopCloudWidth;

      mTopCloud.setLayoutParams(newParam);

      mTopCloud.setCloudColor(mTopCloudColor);
      mTopCloud.setStrokeColor(mTopCloudStrokeColor);
      mTopCloud.setCloudWidth(mTopCloudWidth);
      mTopCloud.setStrokeWidth(mTopCloudStrokeWidth);
      mTopCloud.invalidate();

    } else {
      mTopCloud.setVisibility(GONE);
    }

    if (hasBottomCloud()) {
      mBottomCloud.setVisibility(VISIBLE);

      RelativeLayout.LayoutParams newParam = new LayoutParams(mBottomCloud.getLayoutParams());

      newParam.height = mBottomCloudWidth;
      newParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
      mBottomCloud.setLayoutParams(newParam);

      mBottomCloud.setCloudColor(mBottomCloudColor);
      mBottomCloud.setStrokeColor(mBottomCloudStrokeColor);
      mBottomCloud.setCloudWidth(mBottomCloudWidth);
      mBottomCloud.setStrokeWidth(mBottomCloudStrokeWidth);
      mBottomCloud.setReversed(true);
      mBottomCloud.invalidate();
    } else {
      mBottomCloud.setVisibility(GONE);
    }
  }

  private boolean hasTopCloud() {
    return mTopCloudWidth > 0;
  }

  private boolean hasBottomCloud() {
    return mBottomCloudWidth > 0;
  }

  private void initValues(AttributeSet attrs) {
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CloudRelativeView);
    mMaxIcon = ta.getInt(R.styleable.CloudRelativeView_maxIcon, 10);
    mIconMaxSize = ta.getFloat(R.styleable.CloudRelativeView_iconMaxSize, 0.6F);
    mIconMinSize = ta.getFloat(R.styleable.CloudRelativeView_iconMinSize, 0.2F);
    mIconColor = ta.getColor(R.styleable.CloudRelativeView_iconColor, gc(R.color.white));
    mBackgroundColor = ta.getColor(R.styleable.CloudRelativeView_bgColor, Color.RED);

    mTopCloudColor = ta.getColor(R.styleable.CloudRelativeView_topCloudColor, Color.WHITE);
    mTopCloudWidth = ta.getDimensionPixelSize(R.styleable.CloudRelativeView_topCloudWidth, 0);
    mTopCloudStrokeColor = ta.getColor(R.styleable.CloudRelativeView_topCloudStrokeColor, Color.WHITE);
    mTopCloudStrokeWidth = ta.getDimensionPixelSize(R.styleable.CloudRelativeView_topCloudStrokeWidth, 0);

    mBottomCloudColor = ta.getColor(R.styleable.CloudRelativeView_bottomCloudColor, Color.BLACK);
    mBottomCloudWidth = ta.getDimensionPixelSize(R.styleable.CloudRelativeView_bottomCloudWidth, 0);
    mBottomCloudStrokeColor = ta.getColor(R.styleable.CloudRelativeView_bottomCloudStrokeColor, Color.WHITE);
    mBottomCloudStrokeWidth = ta.getDimensionPixelSize(R.styleable.CloudRelativeView_bottomCloudStrokeWidth, 0);

    ta.recycle();
  }

  private int gc(@ColorRes int i) {
    return ContextCompat.getColor(getContext(), i);
  }

}
