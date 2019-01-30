package com.opcon.ui.adapters;

import android.app.ActivityManager;
import android.content.Context;
import android.util.LruCache;

import com.opcon.components.Message;
import com.opcon.database.MessageProvider;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 *
 * Created by Mahmut Taşkiran on 12/11/2016.
 *
 */

class MessageCacheMng extends LruCache<Integer, Message> implements Runnable {

    private static final int CACHE_REPOSITORY_IN_ONE_STEP = 20;

    private Context mContext;
    private List<Integer> mMessageIds;

    private Boolean mCaching = Boolean.FALSE;
    private final Object mLock = new Object();
    private int mCacheEndpoint;

    MessageCacheMng(Context c, List<Integer> msgs, int maxSize) {
        super(maxSize);
        this.mContext =c;
        this.mMessageIds = msgs;
        mCacheEndpoint = msgs.size() -1;
    }

    @Override
    protected int sizeOf(Integer key, Message value) {
        return 1;
    }

    Message _get(int key) {

        Message msg = get(key);
        if (msg == null) {
            msg = MessageProvider.Utils.getSingleMessage(mContext, key);
            put(key, msg);
        }
        forceCache(key);
        return msg;
    }

    private void forceCache(int key) {
        if (mCaching.equals(Boolean.FALSE) && mCacheEndpoint > 0) {
            synchronized (mLock) {
                if (mCaching.equals(Boolean.FALSE)) {
                    int p = mMessageIds.indexOf(key);
                    if (p <= mCacheEndpoint -1) {
                        new Thread(this).start();
                    }
                }
            }
        }
    }

    @Override public void run() {
        synchronized (mLock) {
            mCaching = Boolean.TRUE;
            for (int i = mCacheEndpoint; !isLowMem() && i > mCacheEndpoint - CACHE_REPOSITORY_IN_ONE_STEP && i > 0; i--) {
                int key = mMessageIds.get(i);
                if (key < 0)
                    continue;
                Message msg = get(key);
                if (msg == null) {
                    put(key, MessageProvider.Utils.getSingleMessage(mContext, key));
                }
            }
            mCacheEndpoint -= CACHE_REPOSITORY_IN_ONE_STEP;
            mCaching = Boolean.FALSE;
        }
    }

    private ActivityManager.MemoryInfo getAvailableMemory(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    private boolean isLowMem() {
        return getAvailableMemory(mContext).lowMemory;
    }

    public void cancel() {
        // ignore
    }
}
