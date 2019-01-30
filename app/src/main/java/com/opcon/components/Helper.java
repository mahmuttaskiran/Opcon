package com.opcon.components;

import android.content.Context;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Created by aslitaskiran on 18/05/2017.
 */

public class Helper {

  private Helper() {}

  public String title, text, idenify;
  public boolean asHtml;

  public int topColor;
  public int topIconColor;

  public int dividerDrawableResourceId;


  public static Builder newBuilder(Context context, String idenify) {
    return new Builder(context, idenify);
  }

  public static Builder newBuilder(Context context, @StringRes int resourceId) {
    return new Builder(context, String.valueOf(context.getString(resourceId).hashCode())).setTitle(resourceId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Helper)) return false;

    Helper helper = (Helper) o;

    return idenify != null ? idenify.equals(helper.idenify) : helper.idenify == null;

  }

  @Override
  public int hashCode() {
    return idenify != null ? idenify.hashCode() : 0;
  }

  @Override
  public String toString() {
    return super.toString();
  }

  public static class Builder {
    Helper mHelper = new Helper();
    Context mContext;
    private Builder(Context context, String idenify) {
      mContext = context;
      mHelper.idenify = idenify;
    }
    public Builder setTitle(@StringRes int resourceId ){
      mHelper.title = mContext.getString(resourceId);
      return this;
    }
    public Builder setMessage(@StringRes int resourceId ){
      mHelper.text = mContext.getString(resourceId);
      return this;
    }

    public Builder setMessage(String message) {
      mHelper.text = message;
      return this;
    }

    public Builder setMessageAsHtml(@StringRes int resourceId ){
      setMessage(resourceId);
      mHelper.asHtml = true;
      return this;
    }
    public Builder setMessageAsHtml(String msg){
      setMessage(msg);
      mHelper.asHtml = true;
      return this;
    }
    public Builder setTopColor(@ColorRes int resourceId) {
      mHelper.topColor = resourceId;
      return this;
    }
    public Builder setTopIconColor(@ColorRes int resourceId) {
      mHelper.topIconColor = resourceId;
      return this;
    }
    public Builder setDividerResourceId(@DrawableRes int resourceId) {
      mHelper.dividerDrawableResourceId = resourceId;
      return this;
    }

    public Helper built() {
      return mHelper;
    }

  }

}
