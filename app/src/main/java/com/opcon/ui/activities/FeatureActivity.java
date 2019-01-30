package com.opcon.ui.activities;

import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.opcon.R;
import com.opcon.components.Dialog;
import com.opcon.components.Feature;
import com.opcon.database.FeatureBase;
import com.opcon.firebaseclient.listeners.GlobalFeatureListener;
import com.opcon.libs.settings.FeedbackActivity;
import com.opcon.ui.management.DialogStoreManagement;

import java.util.Collections;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;

public class FeatureActivity extends AppCompatActivity {

  List<Feature> mFeatures;
  @BindView(R.id.recyclerView)
  RecyclerView mRecyclerView;
  @BindView(R.id.there_is_no_any_feature)
  TextView mThereIsNoFeature;

  FeatureAdapter mAdapter;

  public static Intent getIntent(Context context) {
    return new Intent(context, FeatureActivity.class);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_feature);
    ButterKnife.bind(this);
    mFeatures = FeatureBase.getInstance(this).getFeatures();

    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancel(GlobalFeatureListener.NOTIFICATION_ID);

    Dialog dialog = DialogStoreManagement.getInstance(this).get(FeatureBase.OPCON_TEAM);

    if (dialog != null) {
      dialog.nonSeenMessageLength = 0;
      DialogStoreManagement.getInstance(this).updateRequest(FeatureBase.OPCON_TEAM);
    }

    if (getSupportActionBar() != null) {
      getSupportActionBar().setTitle(null);
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_backspace_white_24dp);
      getSupportActionBar(). setTitle(R.string.opcon_team);
    }

    if (mFeatures.isEmpty()) {
      mThereIsNoFeature.setVisibility(View.VISIBLE);
    } else {
      mThereIsNoFeature.setVisibility(View.GONE);
    }

    Collections.sort(mFeatures, Feature.COMPARATOR);

    mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
    mRecyclerView.setAdapter(getAdapter());
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {

    MenuItem add = menu.add(0, 0, 1, R.string.feedback_activity_title);
    add.setIcon(R.drawable.ic_feed_18);
    add.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
    } else if (item.getItemId() == 0) {
      startActivity(new Intent(this, FeedbackActivity.class));
    }
    return super.onOptionsItemSelected(item);
  }

  FeatureAdapter getAdapter() {
    return (mAdapter = new FeatureAdapter(this));
  }

  public static class FeatureAdapter extends RecyclerView.Adapter {
    FeatureActivity mAc;

    public FeatureAdapter(FeatureActivity ac) {
      super();
      this.mAc = ac;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      return new Holder(this, LayoutInflater.from(parent.getContext()).inflate(R.layout.row_feature, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      if (holder instanceof Holder) {
        Feature f = mAc.mFeatures.get(position);
        ((Holder) holder).show(f);
        FeatureBase.getInstance(mAc.getApplicationContext()).seen(f.getFeatureUID());
      }
    }

    @Override
    public int getItemCount() {
      return mAc.mFeatures.size();
    }

    public static class Holder extends RecyclerView.ViewHolder implements View.OnClickListener {
      @BindView(R.id.imageView)
      ImageView mImage;
      @BindView(R.id.title)
      TextView mTitle;
      @BindView(R.id.subtitle)
      TextView mContent;
      @BindView(R.id.extraAction)
      TextView mExtraAction;
      @BindView(R.id.closeAction)
      AppCompatButton mCloseAction;

      FeatureAdapter mAd;

      public Holder(FeatureAdapter ad, View view) {
        super(view);
        mAd = ad;
        ButterKnife.bind(this, view);
        mExtraAction.setOnClickListener(this);
        mCloseAction.setOnClickListener(this);
      }

      private void show(Feature f) {
        visibilityOf(mImage, f.hasImage());
        visibilityOf(mExtraAction, f.hasExtraAction());

        if (f.hasImage()) {
          Glide.with(mImage.getContext()).load(Uri.parse(f.getImage())).placeholder(R.drawable.no_item_background)
              .centerCrop().diskCacheStrategy(DiskCacheStrategy.NONE).into(mImage);

        }

        if (f.hasExtraAction()) {
          mExtraAction.setText(f.getExtraActionTitle());
        }

        mTitle.setText(f.getContentTitle());
        mContent.setText(f.getContent());
      }

      void visibilityOf(View v, boolean b) {
        if (b) {
          v.setVisibility(View.VISIBLE);
        } else {
          v.setVisibility(View.GONE);
        }
      }

      @Override public void onClick(View v) {
        if (getAdapterPosition() != RecyclerView.NO_POSITION) {
          Feature f = mAd.mAc.mFeatures.get(getAdapterPosition());
          if (v.getId() == mExtraAction.getId()) {
            if (f.getExtraAction() != null) {
              Uri uri = Uri.parse(f.getExtraAction());
              try {
                Intent myIntent = new Intent(Intent.ACTION_VIEW, uri);
                mAd.mAc.startActivity(myIntent);
              } catch (ActivityNotFoundException e) {
                Toast.makeText(mImage.getContext(), "You can't. Please install an browser to open this link.", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
              }
            }
          } else if (v.getId() == mCloseAction.getId()) {
            FeatureBase.getInstance(mImage.getContext()).delete(f.getFeatureUID());
            int index = mAd.mAc.mFeatures.indexOf(f);
            mAd.mAc.mFeatures.remove(f);
            if (index != -1) {
              mAd.notifyItemRemoved(index);
            }

            visibilityOf(mAd.mAc.mThereIsNoFeature, mAd.mAc.mFeatures.isEmpty());
          }
        }
      }
    }
  }

  @Override
  public void onBackPressed() {
    finish();
  }
}
