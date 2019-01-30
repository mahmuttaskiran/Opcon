package com.opcon.libs;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.util.Base64;

import com.bumptech.glide.Glide;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Random;

import static android.content.Context.ACTIVITY_SERVICE;


/**
 *
 * Created by Mahmut Taşkiran on 02/02/2017.
 */

public class PicassoCompressor  {

    private Context mContext;
    private int mHeight, mWidth;
    private boolean mCenterCrop;

    private Bitmap mBitmap;
    private Uri mUri;

    private File mFile;
    private File mDestFile;
    private CompressorListener mListener;

    private File mTempF;

    private boolean lowMemory;

    private int mQuality;

    public interface CompressorListener {void onCompressed(File file, Bitmap bitmap);}

    public PicassoCompressor(Context context) {
        mContext = context.getApplicationContext();
    }

    public PicassoCompressor compress(File file) {
        mFile = file;
        return this;
    }

    public PicassoCompressor compress(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }

    public PicassoCompressor compress(Uri uri) {
        mUri = uri;
        return this;
    }

    public PicassoCompressor quality(int q) {
        mQuality = q;
        return this;
    }

    public PicassoCompressor size(int width, int height) {
        mHeight = height;
        mWidth = width;
        return this;
    }

    public PicassoCompressor centerCrop() {
        mCenterCrop = true;
        return this;
    }

    public PicassoCompressor listen(CompressorListener listener) {
        mListener = listener;
        return this;
    }

    public PicassoCompressor to(File file) {
        mDestFile = file;
        return this;
    }

    public void so() {

        checkArguments();

        final Uri cs = getCompressStuff();

        new Thread(new Runnable() {
            @Override public void run() {
                try {
                    Bitmap bitmap = Glide.with(mContext)
                        .load(cs)
                        .asBitmap()
                        .fitCenter()
                        .skipMemoryCache(true)
                        .into(getWidth(), getHeight())
                        .get();
                    result(bitmap);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }}).start();

    }

    private int getWidth() {
        return mWidth;
    }

    private int getHeight() {
        return mHeight;
    }

    public PicassoCompressor lowMemory(boolean requires) {
        lowMemory = requires;
        return this;
    }

    public static boolean isLowMemoryRequires(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo.lowMemory || Build.VERSION.SDK_INT <= 20;
    }

    private void result(Bitmap bitmap) {
        if (mDestFile != null) {
            compressBitmap(bitmap, mDestFile);
        }
        mListener.onCompressed(mDestFile, bitmap);
        if (mTempF != null && mTempF.exists()) {
            mTempF.delete();
        }
    }

    private Uri getCompressStuff() {
        if (mUri != null) {
            return mUri;
        } else if (mFile != null) {
            return Uri.fromFile(mFile);
        } else {
            compressBitmap(mBitmap, mTempF = getTempFile());
            return Uri.fromFile(mTempF);
        }
    }

    private void compressBitmap(Bitmap bit, File to) {
        try {
            FileOutputStream output = new FileOutputStream(to);
            bit.compress(Bitmap.CompressFormat.JPEG, mQuality, output);
            output.flush();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String encode(Bitmap b, int width, int height, int q) {
        b = Bitmap.createScaledBitmap(b, width, height, false);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        b.compress(Bitmap.CompressFormat.JPEG, q, output);
        byte[] bytes = output.toByteArray();
        bytes = Base64.encode(bytes, Base64.DEFAULT);
        String result = null;
        try {
            result = new String(bytes, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static Bitmap decode(String str) {
        try {
            byte[] bytes = str.getBytes("utf-8");
            bytes = Base64.decode(bytes, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void decode(String str, File destinationFile) {
        Bitmap decode = decode(str);
        if (decode != null) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(destinationFile);
                decode.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getTempFile() {
        try {
            return File.createTempFile(String.valueOf(new Random().nextInt()), ".jpeg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void checkArguments() {
        if (mBitmap == null && mUri == null && mFile == null) {
            throw new IllegalArgumentException("What i will compress? User one of compress method.");
        }
        if (mListener == null) {
            throw new IllegalArgumentException("Do you know what you do?");
        }
    }

}
