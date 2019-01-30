package com.opcon.ui.views;


import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import com.opcon.R;

/**
 *
 * Created by Mahmut Taşkiran on 05/03/2017.
 *
 */

public class CloudView extends View {

  private static final int DEFAULT_RADIUS = 50;

  private int mRadius;
  private int mColor;
  private Path mPath;
  private Paint mPaint;
  private Path mStrokePath;
  private Paint mStrokePaint;
  private int mStrokeWidth;
  private int mStrokeColor;
  private boolean mReversed;

  public CloudView(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.CloudView);
    mRadius = ta.getDimensionPixelSize(R.styleable.CloudView_radius, DEFAULT_RADIUS);
    mColor = ta.getColor(R.styleable.CloudView_cloudColor, Color.BLACK);
    mStrokeColor = ta.getColor(R.styleable.CloudView_strokeColor, Color.BLACK);
    mStrokeWidth = ta.getDimensionPixelSize(R.styleable.CloudView_strokeWidth, 0);
    mReversed = ta.getBoolean(R.styleable.CloudView_atBottom, false);
    ta.recycle();
    setSkeleton();
  }

  private void setSkeleton() {
    mPath = new Path();

    mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mPaint.setColor(mColor);
    mPaint.setStyle(Paint.Style.FILL);

    mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mStrokePaint.setColor(mStrokeColor);
    mStrokePaint.setStrokeWidth(mStrokeWidth);
    mStrokePaint.setStyle(Paint.Style.STROKE);
    mStrokePath = new Path();
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    drawClouds();
  }

  private void drawClouds() {
    mPath.reset();
    mStrokePath.reset();
    int width = getMeasuredWidth();
    int dp = 0;
    int y = mReversed ? getMeasuredHeight(): 0;
    while (dp <= width+50) {
      mPath.addCircle(dp, y, mRadius, Path.Direction.CW);
      if (mStrokeWidth > 0)
        mStrokePath.addCircle(dp, y, mRadius + 1, Path.Direction.CW);
      dp += mRadius *2;
    }

  }

  @Override protected void onDraw(Canvas canvas) {
    canvas.drawPath(mPath, mPaint);
    if (mStrokeWidth > 0)
      canvas.drawPath(mStrokePath, mStrokePaint);
    super.onDraw(canvas);
  }

  public void setCloudColor(int cloudColor) {
    mColor = cloudColor;
    setSkeleton();
  }

  public void setStrokeColor(int color) {
    mStrokeColor = color;
    setSkeleton();
  }

  public void setStrokeWidth(int w) {
    mStrokeWidth = w;
    setSkeleton();
  }

  public void setCloudWidth(int w) {
    mRadius = w;
    setSkeleton();
  }

  public void setReversed(boolean reversed) {
    this.mReversed = reversed;
  }
}
