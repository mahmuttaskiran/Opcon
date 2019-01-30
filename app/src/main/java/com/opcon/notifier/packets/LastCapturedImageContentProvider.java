package com.opcon.notifier.packets;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.opcon.components.Message;
import com.opcon.notifier.components.constants.Packets;
import com.opcon.notifier.components.SpecialPacket;

/**
 * Created by Mahmut Taşkiran on 12/01/2017.
 */

public class LastCapturedImageContentProvider implements SpecialPacketProvider {
    @Override
    public SpecialPacket getPacket(Context context) {
        SpecialPacket sp = new SpecialPacket(Packets._LAST_IMAGE);

        ContentResolver cr = context.getContentResolver();
        Cursor cursor = cr.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new String[]{MediaStore.Images.ImageColumns.DATA},
                null, null, MediaStore.Images.ImageColumns.DATE_ADDED + " desc");

        while (cursor != null && cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
            if (!TextUtils.isEmpty(path)) {
                if (isInCameraFolder(path)) {
                    sp.put(Message.Picture.FILE, path);
                    break;
                }
            }
        }

        if (cursor != null) cursor.close();
        return sp;
    }

    public static boolean isInCameraFolder(String path) {
        return path != null && path.contains("/DCIM/");
    }

    @Override
    public SpecialPacket getForTest(Context context) {
        return getPacket(context);
    }
}
