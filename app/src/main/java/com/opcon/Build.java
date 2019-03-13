package com.opcon;

/**
 * Created by Mahmut Taşkiran on 19/04/2017.
 */

public class Build {

  public static final boolean LIMITED_VERSION = false;

  public static final String VERSION = "0.1.910";
  public static final String VERSION_NAME = "Hello, World!";

  public static final int RELEASE = 1;
  public static final int DEBUG = 2;
  public static final int SLOGAN = 3;

  public static final int TESTING = 4;

  public static int BUILT_TYPE = TESTING;

  public static void setBuiltType(int type) {
    BUILT_TYPE = type;
  }
  public static boolean isRelease() {
    return BUILT_TYPE == RELEASE;
  }
  public static boolean isDebug() {
    return BUILT_TYPE == DEBUG;
  }
  public static boolean isSlogan() {
    return BUILT_TYPE == SLOGAN;
  }
  public static boolean isTesting() {
    return BUILT_TYPE == TESTING;
  }
  public static boolean isLimitedVersion() {return LIMITED_VERSION;}
}
