package com.opcon.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.text.Spanned;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.opcon.R;

/**
 * Created by Mahmut Taşkiran on 05/03/2017.
 */

public class TitleView extends RelativeLayout {

  ImageView mLeft;
  TextView mText;
  CircleRelativeLayout mCrl;
  ImageView mRight;

  private RelativeLayout mRoot;
  private Drawable mLeftIcon;
  private int mLeftIconColor;
  private String mContent;
  private int mContentColor;
  private int mRightIconColor;
  private int mRightIconStrokeColor;
  private boolean mHideRightSide;
  private Drawable mRightIcon;
  private int mBgColor;



  public TitleView(Context context, AttributeSet attrs) {
    super(context, attrs);
    bindViews(inflate(getContext(), R.layout.title_view, this));
    getValues(attrs);
    setupViews();
  }

  private void setupViews() {
    mLeft.setImageDrawable(mLeftIcon);
    mLeft.setColorFilter(mLeftIconColor);
    mText.setText(mContent);
    mText.setTextColor(mContentColor);
    mCrl.setStrokeColor(mRightIconStrokeColor);
    mRight.setImageDrawable(mRightIcon);
    mRight.setColorFilter(mRightIconColor);
    if (mHideRightSide) {
      mRight.setVisibility(GONE);
      mCrl.setVisibility(GONE);
    }
    mRoot.setBackgroundColor(mBgColor);
  }

  private void getValues(AttributeSet attrs) {
    TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.TitleView);

    mLeftIcon = ta.getDrawable(R.styleable.TitleView_leftIcon);
    mRightIcon = ta.getDrawable(R.styleable.TitleView_rightIcon);
    mLeftIconColor = ta.getColor(R.styleable.TitleView_leftIconColor, gc(R.color.colorPrimary));
    mContent = ta.getString(R.styleable.TitleView_content);
    mContentColor = ta.getColor(R.styleable.TitleView_contentColor, gc(R.color.colorPrimary));
    mRightIconColor = ta.getColor(R.styleable.TitleView_rightIconColor, gc(R.color.colorPrimary));
    mRightIconStrokeColor = ta.getColor(R.styleable.TitleView_rightIconStrokeColor, gc(R.color.softGrey));
    mHideRightSide = ta.getBoolean(R.styleable.TitleView_hideRightSide, false);
    mBgColor = ta.getColor(R.styleable.TitleView_color, Color.WHITE);
    ta.recycle();
  }

  private int gc(@ColorRes int c) {
    return getContext().getResources().getColor(c);
  }

  private void bindViews(View v) {
    mLeft = (ImageView) v.findViewById(R.id.left);
    mRight = (ImageView) v.findViewById(R.id.right);
    mText = (TextView) v.findViewById(R.id.text);
    mCrl = (CircleRelativeLayout) v.findViewById(R.id.crl);
    mRoot = (RelativeLayout) v.findViewById(R.id.root);
  }

  public void hideRightSide() {
    mRight.setVisibility(GONE);
    mCrl.setVisibility(GONE);
  }

  public void showRightSide() {
    mRight.setVisibility(VISIBLE);
    mCrl.setVisibility(VISIBLE);
  }

  public void hideLeftSide() {
    mLeft.setVisibility(GONE);
  }

  public void showLeftSide() {
    mLeft.setVisibility(VISIBLE);
  }

  public void setLeftIcon(int i) {
    mLeft.setImageResource(i);
  }

  public void setRightIcon(int i) {
    mRight.setImageResource(i);
  }

  public void setContent(String c) {
    mText.setText(c);
  }

  public void setContent(@StringRes int resId) {
    mText.setText(resId);
  }

  public void setContent(Spanned s) {
    mText.setText(s);
  }

  public void setContentColor(int c) {
    mText.setTextColor(c);
  }

  public void setRightIconColor(int c) {
    mRight.setColorFilter(c);
  }

  public void setLeftIconColor(int c) {
    mLeft.setColorFilter(c);
  }

  @Override
  public void setOnClickListener(View.OnClickListener l) {
    this.mRoot.setOnClickListener(l);
  }

  public void setRightIconStrokeColor(int rightIconStrokeColor) {
    this.mCrl.setStrokeColor(rightIconStrokeColor);
  }

  public void setRightIconClickListener(View.OnClickListener l) {
    this.mCrl.setOnClickListener(l);
  }
}
