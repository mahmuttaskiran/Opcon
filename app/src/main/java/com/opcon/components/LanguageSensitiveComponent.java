package com.opcon.components;

import org.json.JSONObject;

import java.util.Locale;

/**
 * Created by aslitaskiran on 29/05/2017.
 */

public class LanguageSensitiveComponent extends Component {

  public LanguageSensitiveComponent(JSONObject json) {
    put(json);
  }

  public String getLanguageSensitiveString(String param) {
    if (isStringExists(getLanguage() + "_" + param)) {
      return getString(getLanguage() + "_" + param);
    } else {
      if (isStringExists("en" + "_" + param)) {
        return getString("en" + "_" + param);
      } else if (isStringExists("tr" + "_" + param)) {
        return getString("tr" + "_" + param);
      } else {
        return getString(param);
      }
    }
  }
  private String getLanguage() {
    return Locale.getDefault().getLanguage();
  }
  private boolean isStringExists(String param) {
    return getString(param) != null;
  }
}
