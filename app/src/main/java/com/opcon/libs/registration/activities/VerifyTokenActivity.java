package com.opcon.libs.registration.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.opcon.R;
import com.opcon.libs.registration.RegistrationManagement;
import com.opcon.libs.registration.libs.RegistrationEndpoint;
import com.opcon.libs.registration.libs.Request;
import com.opcon.libs.registration.libs.TextViewBackCounter;
import com.opcon.libs.registration.libs.TokenRequest;
import com.opcon.libs.registration.libs.VerifyRequest;
import com.opcon.libs.registration.utils.BackoffUtils;
import com.opcon.libs.utils.AndroidEnvironmentsUtils;
import com.opcon.libs.utils.JSONObjectUtils;
import com.opcon.ui.dialogs.DialogUtils;
import com.opcon.utils.MobileNumberUtils;
import com.opcon.utils.PreferenceUtils;

import java.lang.ref.WeakReference;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

public class VerifyTokenActivity extends AppCompatActivity {

    @BindView(R.id.phone)
    TextView mToolbarTitle;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.info)
    TextView mInfo;
    @BindView(R.id.send)
    CardView mSend;
    @BindView(R.id.country)
    CardView mCountry;
    @BindView(R.id.enter_verify_code_info)
    TextView mBackCounterVerify;
    @BindView(R.id.retry_sms_backcounter)
    TextView mBackCounterSms;
    @BindView(R.id.retry_sms)
    RelativeLayout mRetrySms;
    @BindView(R.id.retry_call_backcounter)
    TextView mRetryCallBackCounter;
    @BindView(R.id.retry_call)
    RelativeLayout mRetryCall;
    @BindView(R.id.retry_newNumber)
    RelativeLayout mRetryNewNumber;
    @BindView(R.id.details)
    CardView details;
    @BindView(R.id.activity_request_token)
    RelativeLayout activityRequestToken;
    @BindView(R.id.token)
    EditText mToken;

    private String mSavedPhone, mSavedLocale, mSavedDialCode;
    private TextViewBackCounter mSmsBackCounter, mCallBackCounter, mVerifyBackCounter;

    private FirebaseAuth mAuth;

    private String mEndpoint;
    private int mEndpointPort;

    private ProgressDialog mDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_token);
        ButterKnife.bind(this);

        if (getIntent() != null && getIntent().getExtras()!= null) {
            mEndpoint = getIntent().getExtras().getString("endpoint", null);
            mEndpointPort = getIntent().getExtras().getInt("port", 0);
        }


        mAuth = FirebaseAuth.getInstance();
        mSavedDialCode = PreferenceUtils.getString(getBaseContext(), RequestTokenActivity.SAVED_DIAL_CODE, "");
        mSavedLocale = PreferenceUtils.getString(getBaseContext(),RequestTokenActivity.SAVED_LOCALE, "");
        mSavedPhone = PreferenceUtils.getString(getBaseContext(), RequestTokenActivity.SAVED_PHONE, "");
        String phone = mSavedDialCode + " " + mSavedPhone;
        phone = MobileNumberUtils.toInternational(phone, null, true);
        mToolbarTitle.setText(String.format(getString(R.string.we_send_token_to_you), phone));
    }

    @Override
    protected void onResume() {
        super.onResume();
        initBackCounts();
    }

    private void initBackCounts() {
        long sms_backoff = BackoffUtils.getRemainBackoffTime(getBaseContext(), BackoffUtils.SMS_BACKOFF);

        if (sms_backoff == 0) {
            if (mSmsBackCounter != null) {
                mSmsBackCounter.stop();
            }
            mBackCounterSms.setVisibility(View.GONE);
            enableRetryView(mRetrySms);
        } else {
            mBackCounterSms.setVisibility(View.VISIBLE);
            disableRetryView(mRetrySms);
            if (mSmsBackCounter == null) {
                mSmsBackCounter = new TextViewBackCounter(sms_backoff, new WeakReference<>(mBackCounterSms), new TextViewBackCounter.BackCounterListener() {
                    @Override
                    public void endOfBackCount() {
                        enableRetryView(mRetrySms);
                        mSmsBackCounter = null;
                    }
                });
                mSmsBackCounter.start();
            }
        }

        long call_backoff = BackoffUtils.getRemainBackoffTime(getBaseContext(), BackoffUtils.CALL_BACKOFF);
        if (call_backoff == 0) {
            if (mCallBackCounter != null) {
                mCallBackCounter.stop();
            }
            enableRetryView(mRetryCall);
            mRetryCallBackCounter.setVisibility(View.GONE);
        } else {
            mRetryCallBackCounter.setVisibility(View.VISIBLE);
            disableRetryView(mRetryCall);
            if (mCallBackCounter == null) {
                mCallBackCounter = new TextViewBackCounter(call_backoff, new WeakReference<>(mRetryCallBackCounter), new TextViewBackCounter.BackCounterListener() {
                    @Override
                    public void endOfBackCount() {
                        enableRetryView(mRetryCall);
                    }
                });
                mCallBackCounter.start();
            }
        }

        long verify_backoff = BackoffUtils.getRemainBackoffTime(getBaseContext(), BackoffUtils.VERIFY_BACKOFF);

        if (verify_backoff == 0) {
            stateOfSentButton(true);
            mBackCounterVerify.setVisibility(View.VISIBLE);
            mBackCounterVerify.setText(R.string.enter_token);
            if (mVerifyBackCounter != null) {
                mVerifyBackCounter.setTime(0);
                mVerifyBackCounter.stop();
            }
        } else {
            stateOfSentButton(false);
            if (mVerifyBackCounter ==null){
                mVerifyBackCounter = new TextViewBackCounter(verify_backoff, new WeakReference<>(mBackCounterVerify), new TextViewBackCounter.BackCounterListener() {
                    @Override
                    public void endOfBackCount() {
                        stateOfSentButton(true);
                        mBackCounterVerify.setText(R.string.enter_token);
                    }
                });
                mVerifyBackCounter.start();
            }
        }
    }

    @OnClick({R.id.send, R.id.retry_sms, R.id.retry_call, R.id.retry_newNumber})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.send:
                request(new VerifyRequest(getPhone(), mSavedDialCode, mSavedLocale, mToken.getText().toString()));
                break;
            case R.id.retry_sms:
                request(new TokenRequest(mSavedPhone, mSavedDialCode, mSavedLocale, "sms"));
                break;
            case R.id.retry_call:
                request(new TokenRequest(mSavedPhone, mSavedDialCode, mSavedLocale, "call"));
                break;
            case R.id.retry_newNumber:
                Intent i = new Intent(this, RequestTokenActivity.class);
                startActivity(i);
                finish();
                break;
        }
    }

    private String getPhone() {
        try {
            Phonenumber.PhoneNumber pn = PhoneNumberUtil.getInstance().parse(mSavedDialCode + mSavedPhone, mSavedLocale.toUpperCase());
            return PhoneNumberUtil.getInstance().format(pn, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL);
        } catch (NumberParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void request(final Request request) {

        if (alertIsNoInternetConnection()) {
            return;
        }

        if (request instanceof VerifyRequest) {
            if (alertIsTokenEmpty()) {
                return;
            }
        }

        mDialog = ProgressDialog.show(VerifyTokenActivity.this, null, getString(R.string.please_wait), true, false);

        completeEndpointRequires(new Runnable() {
            @Override
            public void run() {

                if (TextUtils.isEmpty(mEndpoint)) {
                    dismissDialog();
                    alertUnExceptedErr();

                } else {

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            request.executeSync(mEndpoint, mEndpointPort);

                            BackoffUtils.save(getApplicationContext(), request.getBackoff());
                            reinitBackcounts();

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (request instanceof TokenRequest) {
                                        dismissDialog();
                                        if (request.isSuccess()) {
                                            Toast.makeText(VerifyTokenActivity.this, R.string.token_will_be_send, Toast.LENGTH_SHORT).show();
                                            BackoffUtils.resetBackoff(getApplicationContext(), BackoffUtils.VERIFY_BACKOFF);
                                            reinitBackcounts();
                                        }
                                    } else {
                                        processVerifyRequestResult(request);
                                    }
                                }
                            });



                        }
                    }).start();
                }

            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mCallBackCounter != null) {
            mCallBackCounter.stop();
            mCallBackCounter = null;
        }

        if (mSmsBackCounter != null) {
            mSmsBackCounter.stop();
            mSmsBackCounter = null;
        }

        if (mVerifyBackCounter != null) {
            mVerifyBackCounter.stop();
            mVerifyBackCounter = null;
        }

    }

    private void processVerifyRequestResult(Request result) {

        VerifyRequest vr = (VerifyRequest) result;

        if (result.isSuccess()) {

            final String email = vr.getEmail();
            final String password = vr.getPassword();

            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        WelcomeActivity.updateUserInf(getApplicationContext());
                        RegistrationManagement.getInstance().setEmail(getBaseContext(), email);
                        RegistrationManagement.getInstance().setPassword(getBaseContext(), password);
                        if (RegistrationManagement.getInstance().getAfterRegistrationActivity() != null) {
                            startActivity(new Intent(getApplicationContext(),
                                RegistrationManagement.getInstance().getAfterRegistrationActivity()));
                            finish();
                        }
                    } else {
                        alertUnExceptedErr();
                    }
                    dismissDialog();
                }
            });
        } else {
            dismissDialog();

            if (JSONObjectUtils.isEmpty(vr.getBackoff())) {
                if (vr.getException() != null && vr.getException().equals("invalid_code")) {
                    DialogUtils.alertOnlyOk(this, null, getString(R.string.invalid_verify_code));
                } else {
                    alertUnExceptedErr();
                }
            } else {
                DialogUtils.alertOnlyOk(this, null, getString(R.string.you_have_to_wait_for_verify));
            }
        }

    }

    private void reinitBackcounts() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                initBackCounts();
            }
        });
    }

    private void dismissDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mDialog != null) {
                    mDialog.dismiss();
                }
            }
        });
    }

    private boolean alertIsTokenEmpty() {
        if (mToken.getText().toString().isEmpty()) {
            DialogUtils.alertOnlyOk(this, null, getString(R.string.enter_valid_token));
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

    private void alertUnExceptedErr() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DialogUtils.alertOnlyOk(VerifyTokenActivity.this, null, getString(R.string.unexcepted_err));
            }
        });
    }

    private void completeEndpointRequires(final Runnable callback) {

        if (mEndpoint != null) {
            callback.run();
        } else {
            if (mAuth.getCurrentUser() == null) {
                mAuth.signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            completeEndpoint(callback);
                        } else {
                            callback.run();
                        }
                    }
                });
            }  else {
                completeEndpoint(callback);
            }
        }

    }

    private void completeEndpoint(final Runnable callback) {
        if (TextUtils.isEmpty(mEndpoint)) {
            RegistrationEndpoint.Builder.takeEndpoint(new RegistrationEndpoint.Builder.RegistrationEndpointGetter() {
                @Override public void onGet(RegistrationEndpoint endpoint) {
                    mEndpoint = endpoint.getAddress();
                    mEndpointPort = endpoint.getPort();
                    callback.run();
                }

                @Override public void onError() {
                    callback.run();
                }
            });
        }
    }

    private void disableRetryView(RelativeLayout rl) {
        rl.setClickable(false);
        rl.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.lightGrey));
    }

    private void enableRetryView(RelativeLayout rl) {
        rl.setClickable(true);
        rl.getChildAt(0).setBackgroundColor(getResources().getColor(R.color.white));
    }

    private void stateOfSentButton(boolean state) {
        if (state) {
            mSend.setCardBackgroundColor(getResources().getColor(R.color.colorPrimary));
            mSend.setClickable(true);
        } else {
            mSend.setCardBackgroundColor(getResources().getColor(R.color.lightGrey));
            mSend.setClickable(false);
        }
    }

    @Override
    public void onBackPressed() {}
}
