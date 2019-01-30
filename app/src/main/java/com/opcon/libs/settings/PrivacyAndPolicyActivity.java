package com.opcon.libs.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.opcon.R;
import com.opcon.components.LanguageSensitiveComponent;
import com.opcon.firebaseclient.PresenceManager;
import com.opcon.libs.utils.AndroidEnvironmentsUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import butterknife.ButterKnife;
import butterknife.BindView;
import timber.log.Timber;

public class PrivacyAndPolicyActivity extends AppCompatActivity {

  @BindView(R.id.webView)
  WebView mVebView;
  @BindView(R.id.progress)
  ProgressBar mProgress;
  @BindView(R.id.textAlert)
  TextView mTextAlert;
  @BindView(R.id.pleaseWaitLl)
  LinearLayout mPleaseWaitLL;

  LanguageSensitiveComponent mPrivacy;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_privacy_and_policy);
    ButterKnife.bind(this);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
      getSupportActionBar(). setTitle(R.string.privacy_activity_title);
    }

    if (AndroidEnvironmentsUtils.hasActiveInternetConnection(this)){
      loadPrivacy();
    } else {
      mPleaseWaitLL.setVisibility(View.VISIBLE);
      mProgress.setVisibility(View.GONE);
      mTextAlert.setText(R.string.there_is_no_internet_conenction_to_loading_privacy);
    }

  }

  private void loadPrivacy() {
    mVebView.setVisibility(View.GONE);
    mProgress.setVisibility(View.VISIBLE);
    mTextAlert.setText(R.string.please_wait);
    mPleaseWaitLL.setVisibility(View.VISIBLE);

    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
      FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(new OnCompleteListener<AuthResult>() {
        @Override public void onComplete(@NonNull Task<AuthResult> task) {
          if (task.isSuccessful()) {
            loadPrivacyFiles();
          } else {
            cannotLoaded();
          }
        }
      });
    } else {
      loadPrivacyFiles();
    }

  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      super.onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  private void loadPrivacy(@Nullable String file) {
    if (TextUtils.isEmpty(file)) {
      cannotLoaded();
    } else {
      mVebView.setVisibility(View.VISIBLE);

      Timber.d("privacyFile: %s", file);

      mVebView.loadUrl(file);
      mVebView.setWebViewClient(new WebViewClient() {

        @Override
        public void onPageFinished(WebView view, String url) {
          super.onPageFinished(view, url);
          mPleaseWaitLL.setVisibility(View.GONE);
        }
      });
    }
  }

  private void loadPrivacyFiles() {
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference("constants/privacy");
    reference.keepSynced(true);
    reference.addListenerForSingleValueEvent(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        if (dataSnapshot.getValue() != null) {
          JSONObject json = new JSONObject(
              dataSnapshot.getValue(new GenericTypeIndicator<Map<String, Object>>() {})
          );
          mPrivacy = new LanguageSensitiveComponent(json);
          loadPrivacy(mPrivacy.getLanguageSensitiveString("privacy"));
        } else {
          cannotLoaded();
        }
      }
      @Override public void onCancelled(DatabaseError databaseError) {}
    });
  }

  private void cannotLoaded() {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        mPleaseWaitLL.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.GONE);
        mTextAlert.setText(R.string.static_image_cannot_load);
        mVebView.setVisibility(View.GONE);

      }
    });
  }

  public static void go(Context context) {
    context.startActivity(new Intent(context, PrivacyAndPolicyActivity.class));
  }


}
