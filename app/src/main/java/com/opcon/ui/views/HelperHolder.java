package com.opcon.ui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.opcon.R;
import com.opcon.utils.PreferenceUtils;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by aslitaskiran on 16/05/2017.
 */
public class HelperHolder extends RecyclerView.ViewHolder {
  @BindView(R.id.negativeAction)
  AppCompatButton mNegativeAction;
  @BindView(R.id.positiveAction)
  AppCompatButton mPositiveAction;
  @BindView(R.id.root)
  LinearLayout mRoot;
  @BindView(R.id.text)
  TextView mText;
  @BindView(R.id.titleOfContent)
  TextView mTitle;
  @BindView(R.id.divider)
  View mDivider;
  @BindView(R.id.cloudView)
  CloudRelativeView mCloudView;
  @BindView(R.id.cardView)
  CardView mCard;

  @BindView(R.id.topIcon)
  ImageView mTopIcon;

  public HelperHolder(View view) {
    super(view);
    ButterKnife.bind(this, view);
    mNegativeAction.setVisibility(View.GONE);
    mPositiveAction.setVisibility(View.GONE);
  }

  public void setPositiveAction(String str, View.OnClickListener onClickListener) {
    mPositiveAction.setText(str);
    mPositiveAction.setVisibility(View.VISIBLE);
    mPositiveAction.setOnClickListener(onClickListener);
  }

  public void setNegativeAction(String str, View.OnClickListener onClickListener) {
    mNegativeAction.setText(str);
    mNegativeAction.setVisibility(View.VISIBLE);
    mNegativeAction.setOnClickListener(onClickListener);
  }

  public void setTitle(String str) {
    mTitle.setText(str);
  }

  public void setText(String str) {
    mText.setText(str);
  }

  public void setText(Spanned text) {
    mText.setText(text);
  }

  public void gone() {
    mRoot.setVisibility(View.GONE);
  }

  public void show() {
    mRoot.setVisibility(View.VISIBLE);
  }

  public Builder newBuilder() {
    return new Builder(this);
  }

  public static class Builder {
    HelperHolder holder;

    private Builder(HelperHolder holder) {
      this.holder = holder;
    }

    public Builder setPositiveButton(@StringRes int resourceId, View.OnClickListener listener) {
      holder.setPositiveAction(holder.mRoot.getContext().getString(resourceId), listener);
      return this;
    }

    public Builder setNegativeButton(@StringRes int resourceId, View.OnClickListener listener) {
      holder.setNegativeAction(holder.mRoot.getContext().getString(resourceId), listener);
      return this;
    }

    public Builder setMessage(@StringRes int resourceId) {
      holder.setText(holder.mRoot.getContext().getString(resourceId));
      return this;
    }

    public Builder setMessageAsHtml(@StringRes int resourceId) {
      holder.setText(Html.fromHtml(holder.mRoot.getContext().getString(resourceId)));
      return this;
    }

    public Builder setTitle(@StringRes int resourceId) {
      holder.setTitle(holder.mRoot.getContext().getString(resourceId));
      return this;
    }

    public Builder setTitle(String st) {
      holder.setTitle(st);
      return this;
    }

    public Builder setMessage(String st){
      setMessage(st, false);
      return this;
    }

    public Builder setTopIcon(Drawable drawable) {
      holder.mTopIcon.setImageDrawable(drawable);
      return this;
    }

    public Builder setMessage(String string, boolean asHtml) {
      if (asHtml) {
        this.holder.setText(Html.fromHtml(string));
      } else {
        this.holder.setText(string);
      }
      return this;
    }

    public Builder setCardBackground(@ColorRes int colorRes) {
      this.holder.mCard.setCardBackgroundColor(gc(colorRes));
      return this;
    }

    public Builder setElevation(int value){
      this.holder.mCard.setCardElevation(value);
      return this;
    }

    public Builder setCornerRadius(int radius) {
      this.holder.mCard.setRadius(radius);
      return this;
    }

    public Builder setTopBackground(@ColorRes int colorRes) {
      this.holder.mCloudView.mRandomIcons.mBackgroundColor = gc(colorRes);
      return this;
    }

    public Builder setTopIconColor(@ColorRes int colorRes) {
      this.holder.mCloudView.mRandomIcons.mIconColor = gc(colorRes);
      return this;
    }

    public Builder setDivider(Drawable drawable) {
      this.holder.mDivider.setBackgroundDrawable(drawable);
      return this;
    }

    private int gc(@ColorRes int ci) {
      return ContextCompat.getColor(this.holder.mRoot.getContext(), ci);
    }

  }

  public static boolean isGotIt(Context context, String matter) {
    return PreferenceUtils.getBoolean(context, "understand_resource_" + matter, false);
  }

  public static void gotIt(Context c, String matter) {
    PreferenceUtils.putBoolean(c, "understand_resource_" + matter, true);
  }

  public static void gotIt(Context c, @StringRes int resourceId) {
    int hash = c.getString(resourceId).hashCode();
    gotIt(c, String.valueOf(hash));
  }

  public static boolean isGotIt(Context c, @StringRes int resourceId) {
    int hash = c.getString(resourceId).hashCode();
    return isGotIt(c, String.valueOf(hash));
  }

  public static void forget(Context c, String matter) {
    PreferenceUtils.putBoolean(c, "understand_resource_" + matter, false);
  }

  public static void forget(Context c, @StringRes int resourceId ){
    forget(c, String.valueOf(c.getString(resourceId).hashCode()));
  }

}
