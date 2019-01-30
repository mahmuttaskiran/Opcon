# Add project specific ProGuard notifiers here.
# By default, the flags in this file are appended to flags specified
# in /Users/aslitaskiran/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keep class fqcn.of.javascript.interface.for.webview {
#   public *;
#}


-dontwarn okio.**
-dontwarn butterknife.internal.**
-dontwarn com.squareup.picasso.**

-keep public class com.opcon.ui.utils.NotifierConstantUtils{*;}
-keep public class com.opcon.notifier.components.constants.Conditions{*;}
-keep public class com.opcon.notifier.components.constants.Operations{*;}
-keep public class com.opcon.notifier.components.constants.Packets{*;}
-keep class org.ocpsoft** {*;}

-keep class **$$ViewInjector { *; }
-keepnames class * { @butterknife.BindView *;}

# ProGuard Rules

#If you are using ProGuard with certain older versions of the Account Kit SDK for Android, Account Kit may not be able to collect Facebook Analytics for your app. To ensure that your Account Kit conversion analytics remain available, you should include the following ProGuard rules:

-keep class com.facebook.FacebookSdk {
   boolean isInitialized();
}
-keep class com.facebook.appevents.AppEventsLogger {
   com.facebook.appevents.AppEventsLogger newLogger(android.content.Context);
   void logSdkEvent(java.lang.String, java.lang.Double, android.os.Bundle);
}
