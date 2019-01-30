package com.opcon.ui.fragments.occs;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.location.places.AddPlaceRequest;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.firebase.crash.FirebaseCrash;
import com.opcon.R;
import com.opcon.libs.permission.PermissionUtils;
import com.opcon.libs.utils.AnimationUtils;
import com.opcon.ui.utils.Restrict;
import com.opcon.ui.views.DateRestrictView;
import com.opcon.ui.views.TimeRangeRestrictView;
import com.schibstedspain.leku.LocationPickerActivity;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import timber.log.Timber;

/**
 * Created by Mahmut Taşkiran on 18/10/2016.
 */

public class ConditionLocation extends OPCFragment {

    public static final int NEAR = 0;
    public static final int LATITUDE = 1;
    public static final int LONGITUDE = 2;
    public static final int PARAM_STRING_ADDRESS = 3;

    private double longitude = 0;
    private double latitude = 0;

    private String strAddress;

    @BindView(R.id.location_add_mm) TextView add_mm;
    @BindView(R.id.location_subtract_mm) TextView mSubtrack;
    @BindView(R.id.location_address) TextView mAddress;
    @BindView(R.id.location_date_restrict) DateRestrictView dateRestrictView;
    @BindView(R.id.location_select) CardView mSelect;
    @BindView(R.id.location_time_restrict) TimeRangeRestrictView timeRangeRestrictView;
    @BindView(R.id.location_selected_location_static_image) ImageView mStaticImage;
    @BindView(R.id.location_et_mm) EditText et_mm;
    @BindView(R.id.near_string) TextView mNear;
    @BindView(R.id.selectLocationIndicator) TextView mSelectLocationIndicator;

    Restrict timeRestrictUIManagement;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View mainView = inflater.inflate(R.layout.condition_location, container, false);
        ButterKnife.bind(this, mainView);

        this.timeRestrictUIManagement = new Restrict(timeRangeRestrictView, dateRestrictView);

      Animation animation = android.view.animation.AnimationUtils.loadAnimation(getContext(), R.anim.heart);

      mSelect.startAnimation(animation);
        return mainView;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        mStaticImage.destroyDrawingCache();
    }

    @Override public void review() {
        Integer near = (Integer) getParam(String.valueOf(NEAR));
        Double latitude = (Double) getParam(String.valueOf(LATITUDE));
        Double longitude = (Double) getParam(String.valueOf(LONGITUDE));
        if (latitude != null) this.latitude = latitude;
        if (longitude != null) this.longitude = longitude;
        String address = (String) getParam(String.valueOf(PARAM_STRING_ADDRESS));
        if (address != null && latitude != null && longitude != null)
            setLocation(longitude, latitude, address);
        et_mm.setText(String.valueOf(near));

    }

    private void setLocation(double longitude, double latitude, String address_) {

        if (longitude != 0) {
          mSelect.clearAnimation();
        }


        this.strAddress = address_;
        this.longitude = longitude;
        this.latitude = latitude;

        if (longitude == 0 || latitude == 0 || address_ == null) {
            mSelect.setVisibility(View.GONE);
            this.mAddress.setVisibility(View.VISIBLE);
            this.mStaticImage.setVisibility(View.VISIBLE);
        } else {
            this.mAddress.setText(address_);
            String urlFor = getURLFor(latitude, longitude, 500, 200, 15, true);
            mStaticImage.setVisibility(View.VISIBLE);

            Glide.with(getContext())
                .load(urlFor)
                .asBitmap()
                .listener(new RequestListener<String, Bitmap>() {
                    @Override public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }
                    @Override public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        mStaticImage.setVisibility(View.VISIBLE);
                        mSelect.setVisibility(View.GONE);
                        mSelectLocationIndicator.setVisibility(View.GONE);
                        if (strAddress != null && !strAddress.isEmpty()) {
                          mAddress.setVisibility(View.VISIBLE);
                          mAddress.setText(strAddress);
                        } else {
                          mAddress.setVisibility(View.GONE);
                        }
                        return false;
                    }
                }).into(mStaticImage);
        }

    }

    @OnClick({R.id.location_selected_location_static_image, R.id.selectLocationIndicator})
    public void onImageClick() {
        pick();
    }

    @OnClick(R.id.location_add_mm)
    public void onAddClick() {

        int mm = getMM();
        if (mm == 0) {
            mm = 50;
        } else {
            mm += 10;
        }

        et_mm.setText(String.valueOf(mm));
        setNear(et_mm.getText().toString());

    }

    @OnClick(R.id.location_subtract_mm)
    public void subtrackClick() {
        int mm = getMM();
        if (mm == 0) {
            mm = 50;
        } else {
            mm -= 10;
        }

        et_mm.setText(String.valueOf(mm));
        setNear(et_mm.getText().toString());
    }

    private int getMM() {
        Editable text = this.et_mm.getText();
        if (text == null) {
            return 0;
        }
        if (text.toString().equals(""))
            return 0;
        try {
            return Integer.parseInt(text.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @OnClick(R.id.location_select)
    public void onSelectClick() {
        AnimationUtils.scaleDownScaleUp(mSelect, 0.5f, 1f, 100, 100);
        mSelect.postDelayed(new Runnable() {
            @Override
            public void run() {
                pick();
            }
        }, 200);
    }

    private void pick() {
        if (getInitType() == InitType.INIT) {
            showLocation(getContext(), latitude, longitude);
        } else {

          pickWithLeku();

        }
    }

    void pickWithPlacePicker() {
      PlacePicker.IntentBuilder ppBuilder = new PlacePicker.IntentBuilder();
      try {
        startActivityForResult(ppBuilder.build(getActivity()), 0);
      } catch (Exception e) {
        e.printStackTrace();
        FirebaseCrash.log(e.toString());
      }
    }

    void pickWithLeku() {

      double lt = 0, ln = 0;

      if (latitude != 0) {
        lt = latitude;
        ln = longitude;
      }

      pick_leku(this, 0, lt, ln);

    }

    public static void pick_leku(Activity ac, int rc, double lt, double ln) {
      ac.startActivityForResult(__pick_leku(ac.getApplicationContext(), rc, lt, ln), 0);
    }


    public static void pick_leku(Fragment ac, int rc, double lt, double ln) {
      ac.startActivityForResult(__pick_leku(ac.getContext().getApplicationContext(), rc, lt, ln), rc);
    }

    private static Intent __pick_leku(Context context, int rc, double lt, double ln) {
      if (lt == 0 || ln == 0) {
        if (PermissionUtils.isLocationalPermissionsGranted(context))
        {
          LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
          Location gps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
          Location network = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
          Location lc = gps;
          if (lc == null) {
            lc = network;
          }
          if (lc != null) {
            ln = lc.getLongitude();
            lt = lc.getLatitude();
          }
        }
      }

      LocationPickerActivity.Builder b =
          new LocationPickerActivity.Builder()
          .withSatelliteViewHidden();

      if (ln != 0 && lt != 0) {
        b.withLocation(lt, ln);
      }

      Intent intent = b
          .build(context.getApplicationContext());
      return intent;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
          activityResultLeku(data);
        }
    }

    void activityResultLeku(Intent data) {
      double latitude = data.getDoubleExtra(LocationPickerActivity.LATITUDE, 0);
      double longitude = data.getDoubleExtra(LocationPickerActivity.LONGITUDE, 0);
      String address = data.getStringExtra(LocationPickerActivity.LOCATION_ADDRESS);

      this.longitude = longitude;
      this.latitude = latitude;

      address = address == null ? "": address;

      this.strAddress = address;

      setLocation(longitude, latitude, address);
    }

    void activityResultPicker(Intent data) {
      Place place = PlacePicker.getPlace(getContext(), data);
      if (place != null) {
        String address = null;

        if (place.getAddress() != null) {
          address = place.getAddress().toString();
        }

        address = address == null ? "" : address;

        setLocation(place.getLatLng().longitude, place.getLatLng().latitude,
            address);

        this.longitude = place.getLatLng().longitude;
        this.latitude = place.getLatLng().latitude;
        this.strAddress = address;
      }
    }

    @Override
    public boolean checkForms() {
        if (this.longitude == 0 || this.latitude == 0) {
            setAlert(getString(R.string.location_select_a_location));
            return false;
        }

        if (getMM() == 0) {
            setAlert(getString(R.string.location_please_enter_near_info));
            return false;
        }

        String params = timeRestrictUIManagement.getParams();
        Restrict.putTimeRestrictToConditionParams(super.mParams, params);

        putParam(String.valueOf(LONGITUDE), longitude);
        putParam(String.valueOf(LATITUDE), latitude);
        putParam(String.valueOf(PARAM_STRING_ADDRESS), strAddress);
        putParam(String.valueOf(NEAR), getMM());

        return true;
    }

    public static String getURLFor(double latitude, double longitude, int width, int height, int zoom, boolean sensor) {
        return "http://maps.google.com/maps/api/staticmap?center=" +
                latitude + "," + longitude + "&zoom=" + zoom + "&size="
                + width + "x" + height + "&sensor=" + String.valueOf(sensor);
    }

    public void setNear(String near) {
        String format = getString(R.string.location_near);
        mNear.setText(String.format(format, near));
    }

    public static void showLocation(Context context, double latitude, double longitude) {
        String uriBegin = "geo:" + latitude + "," + longitude;
        String query = latitude + "," + longitude + "(" + "Location" + ")";
        String encodedQuery = Uri.encode(query);
        String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
        Uri uri = Uri.parse(uriString);
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }
}
