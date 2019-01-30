package com.opcon.libs.settings;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.opcon.Build;
import com.opcon.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class AboutActivity extends AppCompatActivity {
  @BindView(R.id.versionNumber)
  TextView versionNumber;
  @BindView(R.id.versionName)
  TextView versionName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    ButterKnife.bind(this);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(null);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
      getSupportActionBar().setTitle(R.string.about_activity_title);
    }
    versionName.setText(Build.VERSION_NAME);
    versionNumber.setText(Build.VERSION);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      onBackPressed();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onBackPressed() {
    finish();
  }

  @OnClick({R.id.versionNumber, R.id.versionName})
  public void onViewClicked(View view) {
    switch (view.getId()) {
      case R.id.versionNumber:
        break;
      case R.id.versionName:
        break;
    }
  }
}
