package mms5.onepagebook.com.onlyonesms.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;

import mms5.onepagebook.com.onlyonesms.common.Constants;

public class Utils {
  public static String[] checkPermissions(Context context) {
    ArrayList<String> permissionToRequest = new ArrayList<>();

    if (checkNoPermission(context, Manifest.permission.SEND_SMS)) {
      permissionToRequest.add(Manifest.permission.SEND_SMS);
    }
    if (checkNoPermission(context, Manifest.permission.READ_SMS)) {
      permissionToRequest.add(Manifest.permission.READ_SMS);
    }

    if (checkNoPermission(context, Manifest.permission.READ_PHONE_STATE)) {
      permissionToRequest.add(Manifest.permission.READ_PHONE_STATE);
    }
    if (checkNoPermission(context, Manifest.permission.READ_CONTACTS)) {
      permissionToRequest.add(Manifest.permission.READ_CONTACTS);
    }

    if (checkNoPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
      permissionToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    }
    if (checkNoPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
      permissionToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    if (checkNoPermission(context, Manifest.permission.CAMERA)) {
      permissionToRequest.add(Manifest.permission.CAMERA);
    }

    String[] permissionArray = new String[permissionToRequest.size()];
    for (int i = 0; i < permissionToRequest.size(); i++) {
      permissionArray[i] = permissionToRequest.get(i);
      Log("perm " + i + " " + permissionArray[i]);
    }

    return permissionArray;
  }

  public static boolean checkNoPermission(Context context, String permission) {
    int permissionState = ActivityCompat.checkSelfPermission(context, permission);
    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !(permissionState == PackageManager.PERMISSION_GRANTED);
  }

  public static boolean isNetworkStateFine(Context context) {
    if (context == null) {
      return true;
    }
    ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo mobile = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
    NetworkInfo wifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
    NetworkInfo lte_4g = manager.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
    boolean blte_4g = false;
    if (lte_4g != null) { blte_4g = lte_4g.isConnected(); }
    if (mobile != null) {
      if (mobile.isConnected() || wifi.isConnected() || blte_4g) { return true; }
    }
    else {
      if (wifi.isConnected() || blte_4g) { return true; }
    }
    return false;
  }

  @SuppressLint("HardwareIds")
  public static String getPhoneNumber(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (ActivityCompat.checkSelfPermission(context,
                                           Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
      return "";
    }
    String phoneNumber = telephonyManager == null || TextUtils.isEmpty(telephonyManager.getLine1Number()) ?
      "" :
      telephonyManager.getLine1Number();
    if (phoneNumber.startsWith("+82")) {
      phoneNumber = "0" + phoneNumber.substring(3);
    }

    phoneNumber = phoneNumber.replace("-", "");
    phoneNumber = phoneNumber.replace(" ", "");

    return phoneNumber;
  }

  public static boolean hasUsim(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    int simState = telephonyManager != null ? telephonyManager.getSimState() : TelephonyManager.SIM_STATE_UNKNOWN;
    return simState != TelephonyManager.SIM_STATE_ABSENT && simState != TelephonyManager.SIM_STATE_UNKNOWN;
  }

  public static String getTelecom(Context context) {
    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (ActivityCompat.checkSelfPermission(context,
                                           Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
      return "";
    }

    if (telephonyManager != null) {
      return telephonyManager.getNetworkOperatorName();
    }
    else {
      return "";
    }
  }

  public static String getDeviceModel() {
    return Build.MODEL + "_" + Build.DEVICE;
  }

  public static String getAppVersion(Context context) {
    try {
      return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
      return "";
    }
  }

  public static boolean IsEmpty(String data) {
    if(data == null) return true;
    String d = data.trim();
    if(d.length() == 0) return true;

    return false;
  }

  public static void Log(String msg) {
    if(Constants.LOG_VISIBLE) {
      Log.d("Pumpkin", msg);
    }
  }
}
