package com.opcon.libs.settings;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ScrollView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.opcon.R;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.registration.RegistrationManagement;
import com.opcon.ui.dialogs.DialogUtils;
import com.opcon.utils.PreferenceUtils;

import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.BindView;

public class FeedbackActivity extends AppCompatActivity {


  public static final String SAVED_NAME = "savedNameForFeedback";
  public static final String SAVED_EMAIL = "savedEmailForFeedback";

  @BindView(R.id.title)
  EditText mName;
  @BindView(R.id.email)
  EditText mEmail;

  @BindView(R.id.message)
  EditText mMessage;
  @BindView(R.id.activity_feedback)
  ScrollView activityFeedback;

  ProgressDialog mProgress;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feedback);
    ButterKnife.bind(this);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(null);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
      getSupportActionBar().setTitle(R.string.feedback_activity_title);
    }

    String name = PreferenceUtils.getString(this, SAVED_NAME, null);

    if (TextUtils.isEmpty(name)) {
      name = RegistrationManagement.getInstance().getName(this, null);
    }

    mName.setText(name);
    mEmail.setText(PreferenceUtils.getString(this, SAVED_EMAIL, null));

  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.feedback, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.send) {
      // send to db.

      if (!checkInputs()) {
        DialogUtils.alertOnlyOk(this, getString(R.string.oppssss), getString(R.string.feedback_please_enter_all_entries));
        return false;
      }

      if (!isValidEmailAddress(mEmail.getText().toString())) {
        DialogUtils.alertOnlyOk(this, null, getString(R.string.feedback_please_enter_valid_email));
        return false;
      }

      PreferenceUtils.putString(this, SAVED_NAME, mName.getText().toString());
      PreferenceUtils.putString(this, SAVED_EMAIL, mEmail.getText().toString());

      sendFeedback();

    } else if (item.getItemId() == android.R.id.home) {
      super.onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  private void sendFeedback() {

    mProgress = ProgressDialog.show(this, null, getString(R.string.please_wait));

    String name, email, message;

    name = mName.getText().toString();
    email = mEmail.getText().toString();

    message = mMessage.getText().toString();

    Map<String, Object> feedback = new HashMap<>();
    feedback.put("name", name);
    feedback.put("email", email);
    feedback.put("message", message);

    DatabaseReference fRef = FirebaseDatabase.getInstance().getReference("feedbacks/" + PresenceManager.uid());

    fRef.push().updateChildren(feedback).addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(@NonNull Task<Void> task) {
        mProgress.dismiss();
        if (task.isSuccessful()) {
          thanks();
        } else {
          Snackbar.make(activityFeedback, getString(R.string.err_for_feedback), Snackbar.LENGTH_LONG);
        }
      }
    });
  }

  private void thanks() {
    AlertDialog.Builder mDialog = new AlertDialog.Builder(this);
    mDialog.setMessage(R.string.thanks_for_feedback)
        .setTitle(null)
        .setNegativeButton(R.string.ok, null)
        .setCancelable(true)
        .setOnDismissListener(new DialogInterface.OnDismissListener() {
          @Override
          public void onDismiss(DialogInterface dialog) {
            mMessage.setText(null);
            finish();
          }
        });
    mDialog.show();
  }


  @Override
  public void onBackPressed() {
    super.onBackPressed();
    finish();
  }

  private boolean checkInputs() {
    return nie(mName) && nie(mEmail) && nie(mMessage);
  }

  private boolean ie(String e) {
    return TextUtils.isEmpty(e);
  }

  private boolean ie(EditText et) {
    return ie(et.getText().toString());
  }

  private boolean nie(EditText et) {
    return !ie(et.getText().toString());
  }

  public boolean isValidEmailAddress(String email) {
    String ePattern = "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$";
    java.util.regex.Pattern p = java.util.regex.Pattern.compile(ePattern);
    java.util.regex.Matcher m = p.matcher(email);
    return m.matches();
  }

}
