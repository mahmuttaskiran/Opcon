package com.opcon;

import android.os.Environment;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Mahmut Taşkiran on 11/12/2016.
 */
public class LocaleUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    private static File exceptionLogFile;
    public LocaleUncaughtExceptionHandler() {
        exceptionLogFile = new File(Environment.getExternalStorageDirectory(), "OpconExceptions.txt");
    }
    @Override public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
        String fullException = getFullString(e);
        writeToLog(fullException);
    }
    private static String getFullString(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        return sw.toString();
    }
    public static void writeToLog(String exceptionStr) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[");
        stringBuilder.append(SimpleDateFormat.getDateInstance().format(new Date(System.currentTimeMillis())));
        stringBuilder.append("]\n");
        stringBuilder.append(exceptionStr);
        stringBuilder.append("*********************************************************************\n\n\n");

        try {
            FileWriter fileWriter = new FileWriter(exceptionLogFile, true);
            fileWriter.write(stringBuilder.toString());
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void writeToLogPure(String str) {
        try {
            FileWriter fileWriter = new FileWriter(exceptionLogFile, true);
            fileWriter.write(str + "\n");
            fileWriter.flush();
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
