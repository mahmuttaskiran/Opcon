package com.opcon.libs.registration.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.opcon.Build;
import com.opcon.R;
import com.opcon.libs.registration.RegistrationManagement;
import com.opcon.libs.registration.libs.RegistrationEndpoint;
import com.opcon.libs.registration.libs.Request;
import com.opcon.libs.registration.libs.TestTokenRequest;
import com.opcon.libs.registration.libs.TokenRequest;
import com.opcon.libs.registration.utils.BackoffUtils;
import com.opcon.libs.utils.AndroidEnvironmentsUtils;
import com.opcon.libs.utils.JSONObjectUtils;
import com.opcon.ui.activities.MainActivity;
import com.opcon.ui.dialogs.DialogUtils;
import com.opcon.ui.drawables.RandomIconsDrawable;
import com.opcon.utils.MobileNumberUtils;
import com.opcon.utils.PreferenceUtils;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;


public class RequestTokenActivity extends AppCompatActivity {

    public static final String SAVED_PHONE = "savedPhone";
    public static final String SAVED_LOCALE = "savedLocale";
    public static final String SAVED_DIAL_CODE = "savedDialCode";

    public static final String FOR_TEST = "ft";

    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.title)
    TextView mTitle;
    @BindView(R.id.country)
    TextView mCountry;
    @BindView(R.id.dial_code)
    EditText mDialCode;
    @BindView(R.id.phone)
    EditText mPhone;
    @BindView(R.id.details)
    CardView mDetails;
    @BindView(R.id.ok)
    AppCompatButton mOk;
    @BindView(R.id.activity_request_token)
    ScrollView activityRequestToken;

    private PhoneNumberUtil mPhoneNumberUtils = PhoneNumberUtil.getInstance();
    private FirebaseAuth mAuth;


    private String mEndpointAddress;
    private int    mEndpointPort;


    private String mLocale;

    private ProgressDialog mDialog;

    private boolean mForTest = Build.isTesting();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_token);
        ButterKnife.bind(this);
        setDefaultCountry();
        mAuth = FirebaseAuth.getInstance();


        setSupportActionBar(mToolbar);

        if (TextUtils.isEmpty(mEndpointAddress)) {
            mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        RegistrationEndpoint.Builder.takeEndpoint(new RegistrationEndpoint.Builder.RegistrationEndpointGetter() {
                            @Override public void onGet(RegistrationEndpoint endpoint) {
                                mEndpointAddress = endpoint.getAddress();
                                mEndpointPort = endpoint.getPort();
                            }
                            @Override public void onError() {}
                        });

                    }
                }
            });
        }

        RandomIconsDrawable.Builder mDrawableBuilder =
                new RandomIconsDrawable.Builder();
        mDrawableBuilder.setResources(getBaseContext()
                .getResources())
                .setBackgroundColor(getBaseContext()
                        .getResources()
                        .getColor(R.color.white))
                .setIconColor(getBaseContext()
                        .getResources()
                        .getColor(R.color.lightGrey))
                .setMinScale(0)
                .setMaxScale(1)
                .setMax(15);



        mForTest = getIntent().getBooleanExtra(FOR_TEST, false) || Build.isTesting();

    }

    private void setDefaultCountry() {
        Locale aDefault = Locale.getDefault();
        String cName = aDefault.getDisplayName();
        int dial_code = mPhoneNumberUtils.getCountryCodeForRegion(aDefault.getCountry());
        setCountry("+" + dial_code, cName);
    }

    @OnClick({R.id.ok, R.id.country, R.id.tester})
    public void onClick(View v) {
        if (v.getId() == R.id.ok || v.getId() == R.id.tester) {

            if (alertIsPhoneEmpty() || alertIsNoInternetConnection() ||
                    alertIsNonValidNumberFor()) {
                return;
            }

            AlertDialog.Builder builder = sureDialog();

            builder.setPositiveButton(R.string.sent, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDialog = ProgressDialog.show(RequestTokenActivity.this, null, getString(R.string.please_wait_for_request_token), true, false);
                    if (mAuth.getCurrentUser() == null) {
                        mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    checkEndpointAddresses();
                                } else {
                                    mDialog.dismiss();
                                    alertUnExceptedErr();
                                }
                            }
                        });
                    } else {
                        checkEndpointAddresses();
                    }
                }
            });

            builder.show();


        } else if (v.getId() == R.id.country) {
            Intent intent = new Intent(this, CountrySelectActivity.class);
            startActivityForResult(intent, 0);
        }
    }

    private void checkEndpointAddresses() {

        if (!TextUtils.isEmpty(mEndpointAddress)) {
            request();
        } else {
            RegistrationEndpoint.Builder.takeEndpoint(new RegistrationEndpoint.Builder.RegistrationEndpointGetter() {
                @Override
                public void onGet(RegistrationEndpoint endpoint) {
                    mEndpointAddress = endpoint.getAddress();
                    mEndpointPort = endpoint.getPort();
                    request();
                }

                @Override
                public void onError() {
                    if(mDialog != null && mDialog.isShowing()) {
                        mDialog.dismiss();
                    }
                }
            });
        }
    }

    private void request() {


        new Thread(new Runnable() {
            @Override
            public void run() {

                PreferenceUtils.putString(getBaseContext(), SAVED_PHONE, mPhone.getText().toString());
                PreferenceUtils.putString(getBaseContext(), SAVED_DIAL_CODE, mDialCode.getText().toString());
                PreferenceUtils.putString(getBaseContext(), SAVED_LOCALE, mLocale);

                RegistrationManagement.getInstance().setLocale(getBaseContext(), mLocale);
                final Request request;

                if (!mForTest) {
                    request = new TokenRequest(getPhone(), mDialCode.getText().toString(), mLocale, "sms");
                } else {
                    request = new TestTokenRequest(getPhone(), mDialCode.getText().toString(), mLocale);
                }

                request.executeSync(mEndpointAddress, mEndpointPort);
                BackoffUtils.save(getApplicationContext(), request.getBackoff());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!request.isCompleted()) {
                            mDialog.dismiss();
                            DialogUtils.alertOnlyOk(RequestTokenActivity.this, null, getString(R.string.unexcepted_err));
                        } else {
                            try {
                                processResult(request);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });

            }
        }).start();



    }

    private String getPhone() {
        try {
            Phonenumber.PhoneNumber pn = PhoneNumberUtil.getInstance().parse(mDialCode.getText().toString() + mPhone.getText().toString(), mLocale.toUpperCase());
            return PhoneNumberUtil.getInstance().format(pn, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String remainToString(long timestamp) {
        long second = (timestamp / 1000) % 60;
        long minute = (timestamp / (1000 * 60)) % 60;
        long hour = (timestamp / (1000 * 60 * 60)) % 24;

        String time;

        if (minute == 0) {
            time = String.format("%02d", second);
        } else if (hour == 0) {
            time = String.format("%02d:%02d", minute, second);
        } else {
            time = String.format("%02d:%02d:%02d", hour, minute, second);
        }

        return time;
    }

    private void processResult(Request body) throws Exception {


        if (mForTest) {

            TestTokenRequest  test = (TestTokenRequest) body;

            final String email = test.getEmail();
            final String password = test.getPassword();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (mDialog != null && mDialog.isShowing()) {
                                mDialog.dismiss();
                            }

                            if (task.isSuccessful()) {
                                RegistrationManagement.getInstance().setEmail(getBaseContext(), email);
                                RegistrationManagement.getInstance().setPassword(getBaseContext(), password);
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(getApplicationContext(), "an error occurred!", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

        } else if (body.isSuccess()) {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            goToTokenVerify();
        } else {
            if (mDialog != null && mDialog.isShowing()) {
                mDialog.dismiss();
            }
            if (!JSONObjectUtils.isEmpty(body.getBackoff())) {

                if (body.getBackoff().has("request")) {
                    // there is ip restrict!
                    long timestamp = BackoffUtils.getRemainBackoffTime(getApplicationContext(), BackoffUtils.IP_BACKOFF);
                    String time = remainToString(timestamp);

                    DialogUtils.alertOnlyOk(this, getString(R.string.we_are_so_sorry),
                        String.format(getString(R.string.black_ip_address), time));
                } else {

                    long timestamp = BackoffUtils.getRemainBackoffTime(getApplicationContext(), BackoffUtils.SMS_BACKOFF);
                    String time = remainToString(timestamp);

                    DialogUtils.alertOnlyOk(this, getString(R.string.we_are_so_sorry),
                        String.format(getString(R.string.black_sms_method), time));

                }

            } else {
                alertUnExceptedErr();
            }

        }
    }

    private void goToTokenVerify() {

        BackoffUtils.BO callBackoff = BackoffUtils.getBackoffTime(this, BackoffUtils.CALL_BACKOFF);
        if (callBackoff != null && callBackoff.backoff < 1) {
            BackoffUtils.setBackoff(this, BackoffUtils.CALL_BACKOFF, TimeUnit.MINUTES.toMillis(1));
        }

        BackoffUtils.BO smsBackoff = BackoffUtils.getBackoffTime(this, BackoffUtils.SMS_BACKOFF);
        if (smsBackoff != null && smsBackoff.backoff < 1) {
            BackoffUtils.setBackoff(this, BackoffUtils.CALL_BACKOFF, TimeUnit.MINUTES.toMillis(1));
        }

        Intent intent = new Intent(this, VerifyTokenActivity.class);
        intent.putExtra("endpoint",  mEndpointAddress);
        intent.putExtra("port", mEndpointPort);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 0) {
            setCountry(data.getExtras().getString(CountrySelectActivity.DIAL_CODE),
                    data.getExtras().getString(CountrySelectActivity.NAME));
        }
    }

    public void setCountry(String dial_code, String country_name) {
        this.mCountry.setText(country_name);

        if (!dial_code.startsWith("+")) {
            dial_code = "+" + dial_code;
        }
        this.mDialCode.setText(dial_code);

        int mDialCodeVar = Integer.parseInt(dial_code.replaceAll("[^0-9]", ""));

        String regionCode = mPhoneNumberUtils.getRegionCodeForCountryCode(mDialCodeVar);
        this.mLocale = regionCode.toUpperCase();
        Phonenumber.PhoneNumber pn = mPhoneNumberUtils.getExampleNumberForType(regionCode.toUpperCase(), PhoneNumberUtil.PhoneNumberType.MOBILE);
        String format = mPhoneNumberUtils.format(pn, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        Timber.e(format);
        mPhone.setHint(format.replaceAll(" ", "   ").replaceAll("[0-9]", " - "));
    }

    private AlertDialog.Builder sureDialog() {
        AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
        TextView textview = new TextView(this);
        String phone = MobileNumberUtils.toInternational(mDialCode.getText().toString() +  mPhone.getText().toString(), mLocale, true);
        textview.setText(Html.fromHtml(String.format(getString(R.string.are_you_sure_to_request_token_for_this_number), phone)));
        textview.setTextSize(16);
        textview.setPadding(75, 75, 75, 75);
        mDialog.setView(textview);
        mDialog.setCancelable(true)
                .setNegativeButton(R.string.edit_number, null);
        return mDialog;
    }

    private boolean alertIsPhoneEmpty() {
        if (mPhone.getText().toString().isEmpty()) {
            DialogUtils.alertOnlyOk(this, null, getString(R.string.enter_phone_number));
            return true;
        }
        return false;
    }

    private boolean alertIsNoInternetConnection() {
        if (AndroidEnvironmentsUtils.hasActiveInternetConnection(getApplicationContext())) {
            return false;
        }
        DialogUtils.alertOnlyOk(this, null, getString(R.string.not_internet_connection));
        return true;
    }

    private boolean alertIsNonValidNumberFor() {
        if (!MobileNumberUtils.checkValid(mDialCode.getText().toString() + mPhone.getText().toString(), mLocale)) {
            DialogUtils.alertOnlyOk(this, getString(R.string.oppssss), getString(R.string.invalid_format_for_country));
            return true;
        }
        return false;
    }

    private void alertUnExceptedErr() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.alertOnlyOk(RequestTokenActivity.this, null, getString(R.string.unexcepted_err));
            }
        });
    }

    @Override
    public void onBackPressed() {
        // super.onBackPressed();
        // ignore
    }
}
