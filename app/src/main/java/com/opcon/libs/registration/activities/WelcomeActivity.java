package com.opcon.libs.registration.activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.animation.Animation;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.facebook.accountkit.ui.SkinManager;
import com.facebook.accountkit.ui.UIManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.opcon.R;
import com.opcon.firebaseclient.Analytics;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.permission.PermissionManagement;
import com.opcon.libs.permission.PermissionRequest;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.libs.registration.RegistrationManagement;
import com.opcon.libs.registration.libs.RegistrationEndpoint;
import com.opcon.libs.registration.libs.VerifyRequest;
import com.opcon.libs.settings.PrivacyAndPolicyActivity;
import com.opcon.libs.utils.AnimationUtils;
import com.opcon.ui.activities.MainActivity;
import com.opcon.ui.dialogs.DialogUtils;
import com.opcon.ui.drawables.RandomIconsDrawable;
import com.opcon.ui.views.HelperHolder;
import com.opcon.utils.PreferenceUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

/*
* *************** Recognize To Opcon *************
*
* To recognize to Opcon, user have to verify own phone number.
* After verification, Opcon server (not firebase) will send
* verified FirebaseAuth values (email, password).
*
* Address and port of Opcon server defined in FirebaseDatabase.
* User can access this information at the constants/recognize_server.
* (As login anon)
*
* /constants/recognize_server/address
* /constants/recognize_server/port
*
* This server is http server and there are two request:
* (@see TokenRequest, VerifyRequest)
*
* *************** Token Request *************
*
* @Type: http, get.
*
* http://server_url:port/request
* @Param phoneNumber kullanıcının telefon numarası
* @Param dialCode    kullanıcının ülke kodu.
*                    Örneğin; Türkiye (90), US (1)...
* @Param locale      kullanıcının ülke kodu
*                    Örneğin; Türkiye (tr), US (us)...
* @Param method      Doğrulama metodu. Varsayılan olarak sms'dir.
*                    Alabileceği değerler: Sms, Call
*
*
* Kullanıcının telefon numarasını doğrulamak için doğrulama
* kodu istediği hizmettir. Bu hizmet, başka bir hizmet tarafından
* override edilebilir. bkz: AccountKit.
*
* @Return Eğer işlem başarılı bir şekilde gerçekleştirilirse
* "success" değerini döndürür. (saf katar)
*
* Eğer işlem başarısızsa "bad_request" ya da "failed" döndürür.
* (saf katar)
*
* Eğer işlem zorlanmışsa, backoff değeri döndürür. (Json formatta)
* backoff değeri şu şekilde döner:
*
* {
*   "method" (sms ya da call): 1000 (beklenecek sürenin millisaniye cinsi)
* }
*
* *************** Verify Token *************
*
* @Type: http, get
*
* http://server_url:port/verify
*
* Kullanıcının kendisine gönderilen doğrulama kodunu denetlediği hizmet.
* Ya da Account Kit gibi bir hizmet kullanılıyorsa, ilgili token'u doğrulayacağı
* hizmet.
*
* @Param phoneNumber kullanıcının telefon numarası
* @Param dialCode    kullanıcının ülke kodu.
*                    Örneğin; Türkiye (90), US (1)...
* @Param locale      kullanıcının ülke kodu
*                    Örneğin; Türkiye (tr), US (us)...
* @Param token       Doğrulama kodu ya da AccountKit gibi bir hizmet
                     kullanılıyorsa, temsili token.
* @Param method      Eğer Account Kit gibi bir hizmet kullanıldıysa
                     kullanılan hizmet belirtilir. Örneğin, "account_kit"

 @Return

                     Eğer işlem başarılı ise kullanıcının FirebaseAuth tanımı
                     nı gönderir: email, password.

                     {
                        "token": "an_sample_account@opcon.com",
                        "password": "password"
                     }

                     !!! Dönen FirebaseAuth tanımının UID değeri
                     kullanıcının doğrulanmış telefon numarasının .replaceAll(" ", "")
                     formatına eşittir !!!


                    Eğer işlem başarısız ise, "invalid_token", "bad_request",
                    "failed" değerlerini saf katar döndürür.

                    Eğer tanımsız bir hata sözkonusu ise json formatta hatayı
                    döndürür.

                    {
                        "error": "UnexpectedError"
                    }

                    Eğer zorlama varsa backoff değerini döndürür:

                    {
                        "verification", 1000 (millisaniye cinsinden bekleme türü.)
                    }


 */

public class WelcomeActivity extends AppCompatActivity {

    private boolean accountKit = true;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.activity_welcome)
    RelativeLayout activityWelcome;

    RandomIconsDrawable mRandomIconsDrawables;
    @BindView(R.id.user_bla_bla)
    TextView mBlaBla;
    @BindView(R.id.yes)
    AppCompatButton mYep;

    VerifyRequest mRequest;
    ProgressDialog mDialog;
    RegistrationEndpoint endpoint;

    @BindView(R.id.helperHolderRoot)
    RelativeLayout mHelperRoot;

    HelperHolder mHelperHolder;

    PermissionManagement mPermissionManagement;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);

        mPermissionManagement = PermissionManagement.with(this);

        mHelperHolder = new HelperHolder(mHelperRoot);

        FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    RegistrationEndpoint.Builder.takeEndpoint(new RegistrationEndpoint.Builder.RegistrationEndpointGetter()
                    {
                        @Override public void onGet(RegistrationEndpoint e) {
                            endpoint = e;
                        }
                        @Override public void onError() {}
                    });
                }
            }
        });

        RandomIconsDrawable.Builder mDrawableBuilder =
                new RandomIconsDrawable.Builder();
        mDrawableBuilder.setResources(getBaseContext()
                .getResources())
                .setBackgroundColor(getBaseContext()
                        .getResources()
                        .getColor(R.color.colorPrimary))
                .setIconColor(getBaseContext()
                        .getResources()
                        .getColor(R.color.colorPrimaryDark))
                .setMinScale(0)
                .setMaxScale(1)
                .setMax(50);

        mRandomIconsDrawables = mDrawableBuilder.built();
        activityWelcome.setBackgroundDrawable(mRandomIconsDrawables);

        mBlaBla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrivacyAndPolicyActivity.go(WelcomeActivity.this);
            }
        });


        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder
             = new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.CODE);
        UIManager uiManager;

        // Skin is CLASSIC, CONTEMPORARY, or TRANSLUCENT

        uiManager = new SkinManager(
            SkinManager.Skin.TRANSLUCENT,
         getResources().getColor(R.color.colorPrimary));
        configurationBuilder.setUIManager(uiManager);

        mBlaBla.setText(Html.fromHtml(getString(R.string.bla_bla)));
    }

    void updatePlayServicesHelper() {


        mHelperHolder.newBuilder()
            .setPositiveButton(R.string.ok, new View.OnClickListener() {
                @Override public void onClick(View v) {
                    finish();
                }
            }).setTitle(R.string.update_play_services)
            .setMessage(R.string.update_play_services_to_use_Opcon)
            .setDivider(getResources().getDrawable(R.drawable.linear_secondary))
            .setCardBackground(R.color.white)
            .setTopIcon(getResources().getDrawable(R.drawable.ic_info_white_48dp));

    }

    void welcomeHelper() {
      HelperHolder.Builder builder = mHelperHolder.newBuilder()
          .setTitle(R.string.forget_what_you_now)
          .setMessageAsHtml(R.string.what_welcome_activity)
          .setDivider(getResources().getDrawable(R.drawable.linear_secondary))
          .setCardBackground(R.color.white)
          .setTopIcon(getResources().getDrawable(R.drawable.ic_comments_smiley));

      // fix OP-00011

      if (Build.VERSION.SDK_INT < 23) {
        // noting
        builder.setMessageAsHtml(R.string.what_welcome_activity_before_sdk_23);
      } else {
        builder.setPositiveButton(R.string.provide_permissions, new View.OnClickListener() {
          @Override public void onClick(View v) {
            if (PermissionUtils.check(getApplicationContext(),
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.RECEIVE_SMS
            )) {
              Toast.makeText(WelcomeActivity.this, R.string.permissions_is_already_provided, Toast.LENGTH_SHORT).show();
            } else {
              requestPermissions();
            }
          }
        });
      }
    }

    void requestPermissions() {
        mPermissionManagement.builtRequest(0, new PermissionRequest(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_PHONE_STATE
        )).observer(new PermissionManagement.PermissionEventListener() {
            @Override
            public void onAllPermissionsGranted(int requestCode, PermissionRequest permissionRequest) {
                Toast.makeText(WelcomeActivity.this, R.string.permissions_provided, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAnyPermissionsDenied(int requestCode, PermissionRequest permissionRequest) {

            }
        }).request();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPermissionManagement.dispatchEvent(requestCode, grantResults);
    }

    @Override protected void onResume() {
        super.onResume();
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(getApplicationContext()) == ConnectionResult.SUCCESS)
        {
            welcomeHelper();
            mBlaBla.setVisibility(View.VISIBLE);
            mYep.setVisibility(View.VISIBLE);
            mYep.setEnabled(true);
            mYep.setFocusable(true);
            mYep.setClickable(true);
        } else {
            updatePlayServicesHelper();
            mYep.setEnabled(false);
            mYep.setFocusable(false);
            mYep.setClickable(false);
        }
    }

    @OnClick(R.id.yes)
    public void onClick(View view) {
        if (view.getId() == R.id.yes) {
            if (accountKit && !com.opcon.Build.isTesting()) {
              // is i will test, you have to return with my service. OpconSmsVerification with Twilvo.
                startAccountKitSmsVerification();
            } else {
                startOpconSmsVerification();
            }

        }
    }

    public void startOpconSmsVerification() {
        Analytics.instance(this).log("start_opcon_sms_verification");
        Intent intent = new Intent(this, RequestTokenActivity.class);
        startActivity(intent);
    }

    public void startAccountKitSmsVerification() {
      Analytics.instance(this).log("start_account_kit_sms_verification");
      final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
            new AccountKitConfiguration.AccountKitConfigurationBuilder(
                LoginType.PHONE,
                AccountKitActivity.ResponseType.CODE); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(
            AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION,
            configurationBuilder.build());
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("result:%s", "request Code null");
        if (requestCode == 1) { // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            if (loginResult.getError() != null) {
                Toast.makeText(getBaseContext(), loginResult.getError().toString() + "", Toast.LENGTH_SHORT).show();
                startOpconSmsVerification();
            } else if (loginResult.wasCancelled()) {
               // ...
            } else {

                mRequest = new VerifyRequest("fake_phone", "1", getLocale(), loginResult.getAuthorizationCode());
                mRequest.asAccountKit();
                mDialog = ProgressDialog.show(this, null, getString(R.string.please_wait), true);
                if (endpoint == null) {
                    completeEndpoint();
                } else {
                    request();
                }

            }
        }
    }

    String getLocale() {
        Locale aDefault = Locale.getDefault();
        if (aDefault != null ){
            String country = aDefault.getCountry();
            if (country != null) {
                return country.toUpperCase();
            }
        }
        return "TR";
    }


    void completeEndpoint() {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        takeEndpoint();
                    }
                }
            });
        } else {
            takeEndpoint();
        }
    }

    void request() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mRequest.executeSync(endpoint.getAddress(), endpoint.getPort());
                if (mRequest.isSuccess()) {
                    success();
                } else {
                    doYouWantUseOpconVerificationSystem("RecognizeErr");
                    dismissDialog();
                }
            }
        }).start();
    }

    public String getDialCode() {
        return String.valueOf(PhoneNumberUtil.getInstance().getCountryCodeForRegion(getLocale()));
    }

    public void success() {
        final String email = mRequest.getEmail();
        final String password = mRequest.getPassword();

        FirebaseAuth.getInstance().signOut();

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {


                    updateUserInf(getApplicationContext());

                    PreferenceUtils.putString(getBaseContext(),
                        RequestTokenActivity.SAVED_PHONE, FirebaseAuth.getInstance().getCurrentUser().getUid());
                    PreferenceUtils.putString(getBaseContext(),
                        RequestTokenActivity.SAVED_DIAL_CODE, getDialCode());
                    PreferenceUtils.putString(getBaseContext(),
                        RequestTokenActivity.SAVED_LOCALE, getLocale());


                    RegistrationManagement.getInstance().setEmail(getBaseContext(), email);
                    RegistrationManagement.getInstance().setPassword(getBaseContext(), password);

                    HelloWorld();


                } else {
                    dismissDialog();
                    showUnexpectedErr(R.string.unexcepted_err);
                }

            }
        });
    }

    void HelloWorld() {
      runOnUiThread(new Runnable() {
        @Override
        public void run() {
          if (RegistrationManagement.getInstance().getAfterRegistrationActivity() != null) {
            startActivity(new Intent(getApplicationContext(),
                RegistrationManagement.getInstance().getAfterRegistrationActivity()));
            finish();
          } else {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
          }
        }
      });
    }

    public static void updateUserInf(Context context) {
        long now = System.currentTimeMillis();
        FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid())
            .child("recognizeTimestamp").setValue(now);
        FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid())
            .child("recognizeDate").setValue(SimpleDateFormat.getDateTimeInstance().format(new Date(now)));
        FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid())
            .child("os").setValue(System.getProperty("os.version"));
        FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid())
            .child("androidSdk").setValue(Build.VERSION.SDK_INT);
        FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid())
            .child("device").setValue(Build.DEVICE);
        FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid())
            .child("model").setValue(Build.MODEL);
        FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid())
            .child("product").setValue(Build.PRODUCT);
        FirebaseDatabase.getInstance().getReference("users/" + PresenceManager.uid()).child("phoneNumber").setValue(PresenceManager.uid());
        if (FirebaseAuth.getInstance().getCurrentUser() != null ){
            PreferenceUtils.putString(context, "user", FirebaseAuth.getInstance()
                .getCurrentUser().getUid());
            forceChangePassword(context);
        }
        RegistrationEndpoint.instance = null;


      Analytics.instance(context).log("recognized_user_count");
      Analytics.instance(context).log("android_sdk_" + Build.VERSION.SDK_INT);


    }

    public static String getRandomString(int length) {
        StringBuilder builder = new StringBuilder();
        String chars = "1234567890*-!'^%&/()=?qwertyuıopasdfghjklzxcvbnm";
        Random rand = new Random();
        for (int i = 0; i <= length -1; i++) {
            Character c = chars.charAt(rand.nextInt(chars.length() -1));
            if (Character.isLetter(c)) {
                if (rand.nextInt(10) > 5) {
                    c = Character.toUpperCase(c);
                }
            }
            builder.append(c);
        }
        return builder.toString();
    }

    public static synchronized void forceChangePassword (final Context context) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            final String newPassword = getRandomString(20);
            RegistrationManagement.getInstance().setVolatilePassword(context, newPassword);
            user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        RegistrationManagement.getInstance().setPassword(context, newPassword);
                    }
                }
            });
        }
    }

    public void doYouWantUseOpconVerificationSystem(final String withError) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(WelcomeActivity.this);
                builder.setTitle(R.string.something_goes_wrong_with_facebook_account_kit)
                    .setMessage(getString(R.string.something_goes_wrong_with_facebook_account_kit_message) + "\n\n (Error: " + withError + ")")
                    .setPositiveButton(R.string.yep_try_opcon_verification_service, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startOpconSmsVerification();
                        }
                    }).setNegativeButton(R.string.no, null)
                    .show();
            }
        });
    }

    public void takeEndpoint() {
        RegistrationEndpoint.Builder.takeEndpoint(new RegistrationEndpoint.Builder.RegistrationEndpointGetter() {
            @Override
            public void onGet(RegistrationEndpoint e) {
                endpoint = e;
                request();
            }

            @Override
            public void onError() {
                dismissDialog();
                showUnexpectedErr(R.string.unexcepted_err);
            }
        });
    }

    public void dismissDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    if (mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                }
            }
        });
    }

    public void showUnexpectedErr(final @StringRes int stringId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.alertOnlyOk(WelcomeActivity.this, null, getString(stringId));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRandomIconsDrawables !=null) {
            mRandomIconsDrawables.recycleBitmaps();
        }
        if (mDialog != null && mDialog.isShowing()) {
          mDialog.dismiss();
        }
    }



}
