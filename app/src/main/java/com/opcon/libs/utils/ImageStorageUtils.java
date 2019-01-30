package com.opcon.libs.utils;

import android.annotation.SuppressLint;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 *
 * Created by Mahmut Taşkiran on 19/12/2016.
 */

public class ImageStorageUtils {

    public static final String SENT_PICTURES_DIRECTORIES = "/Opcon/Pictures/Sent";
    public static final String RECEIVED_PICTURES_DIRECTORIES = "/Opcon/Pictures/Received";

    @SuppressLint("SimpleDateFormat")
    public static String getRandomFileNameForTimestamp(long timestamp) {
        Random random = new Random();
        int r_identify = random.nextInt(10000) + 1000;

        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(String.format("SSSS'%sOP-'yyyy'-'MM'-'dd'" +
                        "-'hh'-'mm'.jpeg'", r_identify));
        return simpleDateFormat.format(new Date(timestamp));
    }

    @SuppressLint("SimpleDateFormat")
    public static String getRandomFilename(long timestamp) {
        Random random = new Random();
        int r_identify = random.nextInt(10000) + 1000;

        SimpleDateFormat simpleDateFormat =
                new SimpleDateFormat(String.format("SSSS'%sOP-'yyyy'-'MM'-'dd'" +
                        "-'hh'-'mm", r_identify));
        return simpleDateFormat.format(new Date(timestamp));
    }

    public static File getProfilePicturesDir() {
        String profile_pictures = Environment.getExternalStorageDirectory() +
                File.separator +
                "Opcon" +
                File.separator +
                "Profile Pictures";
        File file = new File(profile_pictures);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }
}
