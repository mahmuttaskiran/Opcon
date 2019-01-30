package com.opcon.ui.drawables;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;


import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.opcon.R;

/**
 *
 * Created by Mahmut Taşkiran on 28/12/2016.
 */

public class RandomIconsDrawable extends Drawable {
    private static class Randoms {
        private static Random random = new Random();
        public static int rangeInt(int min, int max) {
            return random.nextInt((max - min) + 1) + min;
        }
        public static float nextFloat(float min, float max) {
            return min + random.nextFloat() * (max - min);
        }
    }

    public static class Builder {
        private RandomIconsDrawable mDrawable = new RandomIconsDrawable();
        private Resources mResources;

        public Builder setResources(Resources value) {
            mResources = value;
            return this;
        }
        public Builder setBackgroundColor(@ColorInt int value) {
            mDrawable.mBackgroundColor = value;
            return this;
        }
        public Builder setIconColors(@ColorInt int[]colors){
            mDrawable.mIconColors = colors;
            return this;
        }
        public Builder setMax( int value){
            mDrawable.mMaxIamge = value;
            return this;
        }
        public Builder setIconColor( int value){
            mDrawable.mIconColor = value;
            return this;
        }
        public Builder setMaxRotation( int value){
            mDrawable.mMaxRotation = value;
            return this;
        }
        public Builder setMaxScale(float value){
            mDrawable.mMaxScale = value;
            return this;
        }
        public Builder setMinScale(float value){
            mDrawable.mMinScale = value;
            return this;
        }
        public Builder setColors(Resources mResources, @ColorRes int ... colorResources) {
            int[] mColors = new int[colorResources.length];
            for (int i = 0; i < mColors.length; i++) {
                mColors[i] = mResources.getColor(colorResources[i]);
            }
            mDrawable.mIconColors = mColors;
            return this;
        }
        public RandomIconsDrawable built() {
            mDrawable.loadResourcesRandomly(mResources);
            return mDrawable;
        }
    }

    public int mMaxIamge = 1;
    public int mBackgroundColor = Color.GRAY;
    public int mIconColor = Color.LTGRAY;
    public float mMinScale = 1;
    public float mMaxScale = 5;
    private int[] mIconColors = null;
    public int mMaxRotation = 90;

    private List<Bitmap> bitmaps = null;

    private RandomIconsDrawable() {

    }

    private void loadResourcesRandomly(Resources mResources) {
        if (bitmaps != null) {
            for (Bitmap bitmap : bitmaps) {
                if (bitmap != null)
                    bitmap.recycle();
            }
            bitmaps.clear();
            bitmaps = null;
        }
        bitmaps = new ArrayList<>();
        for (int i = 0; i <= mMaxIamge; i++) {
            BitmapFactory.Options mOptions = new BitmapFactory.Options();
            Bitmap bitmap = BitmapFactory.decodeResource(mResources, randomIcon(), mOptions);
            bitmaps.add(bitmap);
        }
    }

    private int randomIcon() {

        Field[] fields = R.drawable.class.getFields();

        if (fields == null) {
            throw new IllegalStateException("We cannot setRemoteAvatar any drawable on your resources.");
        }

        int icon = 0;
        while (icon == 0) {
            int random = Randoms.rangeInt(0, fields.length -1);
            if (fields[random].getName().startsWith("emoji")) {
                try {
                    icon = fields[random].getInt(null);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return icon;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {


        if (bitmaps == null || bitmaps.isEmpty()) {
            throw new IllegalArgumentException("You have to call setResources() and" +
                    " demonstrate a max image.");
        }


        canvas.drawColor(mBackgroundColor);

        Matrix matrix;

        for (int i = 0; i < bitmaps.size(); i++) {

            Bitmap mBitmap = bitmaps.get(i);
            if (mBitmap == null) {
                continue;
            }

            matrix = new Matrix();
            matrix.postRotate(Randoms.rangeInt(0, mMaxRotation));

            matrix.postTranslate(Randoms.rangeInt(0, canvas.getWidth()),
                    Randoms.rangeInt(0, canvas.getHeight()));

            float mScale = Randoms.nextFloat(mMinScale, mMaxScale);
            matrix.postScale(mScale, mScale);

            Paint mPaint = new Paint();


            int color = mIconColor;

            if (mIconColors != null) {
                color = mIconColors[Randoms.rangeInt(0, mIconColors.length -1)];
            }

            mPaint.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));

            canvas.drawBitmap(bitmaps.get(i), matrix, mPaint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        throw new UnsupportedOperationException();
    }

    public void recycleBitmaps() {
        if (bitmaps != null) {
            for (Bitmap bitmap : bitmaps) {
                if (bitmap != null)
                    bitmap.recycle();
            }
        }
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
