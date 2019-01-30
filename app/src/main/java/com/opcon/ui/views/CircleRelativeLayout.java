package com.opcon.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.opcon.R;

/**
 *
 * Created by Mahmut Taşkiran on 05/03/2017.
 *
 */

public class CircleRelativeLayout extends RelativeLayout {

  private int mBackgroundColor = Color.RED;
  private int mStrokeColor = Color.WHITE;
  private int mStrokeWidth = 0;

  private Paint mBgPaint, mStrokePaint;
  private Path mBgPath, mStrokePath;

  public CircleRelativeLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    if (!isInEditMode()) {
      int tColor = ContextCompat.getColor(context, R.color.transparent);
      TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.CircleRelativeLayout);
      mBackgroundColor = ta.getColor(R.styleable.CircleRelativeLayout_circleColor, Color.BLACK);
      mStrokeColor = ta.getColor(R.styleable.CircleRelativeLayout_circleStrokeColor, tColor);
      mStrokeWidth = ta.getDimensionPixelSize(R.styleable.CircleRelativeLayout_circleStrokeWidth, tColor);
      setWillNotDraw(false);
      ta.recycle();
    }

  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    drawPaths();
  }

  private void drawPaths() {
    mBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mBgPaint.setStyle(Paint.Style.FILL);
    mBgPaint.setColor(mBackgroundColor);


    mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mStrokePaint.setColor(mStrokeColor);
    mStrokePaint.setStyle(Paint.Style.STROKE);
    mStrokePaint.setStrokeWidth(mStrokeWidth);

    int d = getMeasuredHeight() / 2;
    int ed = mStrokeWidth > 0 ? d - mStrokeWidth/2 : d;

    mBgPath = new Path();
    mBgPath.addCircle(d,d,d, Path.Direction.CCW);
    mStrokePath = new Path();
    mStrokePath.addCircle(d,d,ed, Path.Direction.CCW);
  }

  public void setColor(int color) {
    mBackgroundColor = color;
    drawPaths();
    invalidate();
  }

  public void setStrokeColor(int color) {
    mStrokeColor = color;
    drawPaths();
    invalidate();
  }

  public void setStrokeWidth(int w) {
    mStrokeWidth = w;
    drawPaths();
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {
    canvas.drawPath(mBgPath, mBgPaint);
    if (mStrokeWidth>0) {
      canvas.drawPath(mStrokePath, mStrokePaint);
    }
    super.onDraw(canvas);
  }
}
