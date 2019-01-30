package com.opcon;

import android.Manifest;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.FirebaseDatabase;
import com.opcon.database.ContactBase;
import com.opcon.database.KeyBackoff;
import com.opcon.firebaseclient.ComponentListenerManager;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.notification.MessageNotificator;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.notifier.environment.Environment;
import com.opcon.notifier.environment.EnvironmentManager;
import com.squareup.leakcanary.LeakCanary;

import java.io.File;

import timber.log.Timber;

/**
 *
 * Created by Mahmut Taşkiran on 04/10/2016.
 *
 */

public class Opcon extends MultiDexApplication {

    public Opcon() {}

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseAnalytics.getInstance(this);

        registerActivityLifecycleCallbacks(PresenceManager.getInstance(getApplicationContext()));

        FirebaseApp.initializeApp(getApplicationContext());

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

        KeyBackoff.getInstance(getApplicationContext());

        Timber.plant(new Timber.DebugTree() {
            @Override protected String createStackElementTag(StackTraceElement element) {
                return super.createStackElementTag(element) + " "
                    + element.getMethodName() + " [" + element.getLineNumber() + "] ";
            }
        });

        ComponentListenerManager.getInstance(getApplicationContext());

        MessageNotificator.getInstance(getApplicationContext());
        // LeakCanary.install(this);
        ContactBase.getInstance(getApplicationContext());
        EnvironmentManager.init().builtEnvironment(getApplicationContext());
        PermissionManagement.init(getApplicationContext());

        // fix OP-00003

        if (!Build.isRelease()) {
          globalExceptionHandlerInit();
        } else {
          // // delete if exists.
          try {
            if (PermissionUtils.checkReadWriteExternalStorage(this)) {
              File file = new File(android.os.Environment.getExternalStorageDirectory(), "OpconExceptions.txt");
              if (file.exists()) {
                file.delete();
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

    }

    private void globalExceptionHandlerInit() {
        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                        PackageManager.PERMISSION_GRANTED) {
            Thread.setDefaultUncaughtExceptionHandler(new LocaleUncaughtExceptionHandler());
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            PresenceManager.getInstance(getApplicationContext())
                .hiddenUi();
        }
    }

}
