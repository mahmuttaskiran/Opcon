package com.opcon.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.text.Html;
import android.widget.TextView;

import com.opcon.Build;
import com.opcon.R;
import com.opcon.libs.settings.SettingsActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class NewAboutActivity extends AppCompatActivity {

  @BindView(R.id.version)
  TextView version;
  @BindView(R.id.versionName)
  TextView versionName;
  @BindView(R.id.seeCopyrightInformation)
  AppCompatButton seeCopyrightInformation;

  @BindView(R.id.by_mmt_taskiran)
  TextView mByMyy;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_new_about);
    ButterKnife.bind(this);
    version.setText(getString(R.string.version) + " " + Build.VERSION);
    versionName.setText(Build.VERSION_NAME);

    mByMyy.setText(Html.fromHtml(getString(R.string.developed_from_izmir_adiyaman)));

  }

  @OnClick(R.id.seeCopyrightInformation)
  public void onViewClicked() {
    Intent intent = new Intent(this, SettingsActivity.class);
    intent.putExtra(PreferenceActivity.EXTRA_SHOW_FRAGMENT, SettingsActivity.CopyrightInformation.class.getName());
    startActivity(intent);
  }

  @OnClick(R.id.by_mmt_taskiran)
  public void onClickedMmtTaskiran() {
    Intent browse = new Intent( Intent.ACTION_VIEW , Uri.parse("https://www.facebook.com/mahmutaskiran/"));
    startActivity(browse);
  }

}
