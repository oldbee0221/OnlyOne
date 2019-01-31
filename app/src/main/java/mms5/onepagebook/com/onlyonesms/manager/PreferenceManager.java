package mms5.onepagebook.com.onlyonesms.manager;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
  private static final String KEY_USER_ID = "com.onlyonesms.PreferenceManager.user_id";
  private static final String KEY_USER_JSON = "com.onlyonesms.PreferenceManager.user_json";
  private static final String KEY_AGREE_WITH_POLICY = "com.onlyonesms.PreferenceManager.agree_with_policy";
  private static final String KEY_APP_VERSION = "com.onlyonesms.PreferenceManager.app_version";
  private static final String KEY_CONTACTS_UPDATED_AT = "com.onlyonesms.PreferenceManager.contacts_updated_at";
  private static final String KEY_CHANGED_NUMBER = "com.onlyonesms.PreferenceManager.changed_number";
  private static final String KEY_TOKEN = "com.onlyonesms.PreferenceManager.token";
  private static final String KEY_IS_TASK_RUNNING = "com.onlyonesms.PreferenceManager.is_task_running";
  private static final String KEY_SHOW_MAKEING_DEFAULT = "com.onlyonesms.PreferenceManager.show_making_default";
  private static final String KEY_LAST_REMOVED_TIME = "com.onlyonesms.PreferenceManager.last_removed_time";
  private static final String KEY_BASE_URL = "com.onlyonesms.PreferenceManager.baseurl";
  private static final String KEY_CALLBACK_MSG_USE = "com.onlyonesms.PreferenceManager.cbmsguse";

  private static SharedPreferences pref;
  private static PreferenceManager instance;

  public static PreferenceManager getInstance(Context context) {
    if (instance == null) {
      synchronized (PreferenceManager.class) {
        if (pref == null) {
          pref = android.preference.PreferenceManager.getDefaultSharedPreferences(context);
        }

        instance = new PreferenceManager();
      }
    }

    return instance;
  }

  public void setUserId(String id) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putString(KEY_USER_ID, id);
    editor.apply();
  }

  public String getUseId() {
    return pref.getString(KEY_USER_ID, "");
  }

  public void setUserJson(String userJson) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putString(KEY_USER_JSON, userJson);
    editor.apply();
  }

  public String getUseJson() {
    return pref.getString(KEY_USER_JSON, "");
  }

  public boolean getAgreeWithPolicy() {
    return pref.getBoolean(KEY_AGREE_WITH_POLICY, false);
  }

  public void setAgreeWithPolicy(boolean agree) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putBoolean(KEY_AGREE_WITH_POLICY, agree);
    editor.apply();
  }

  public String getVersion() {
    return pref.getString(KEY_APP_VERSION, "");
  }

  public void setVersion(String version) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putString(KEY_APP_VERSION, version);
    editor.apply();
  }

  public long getContactsUpdatedAt() {
    return pref.getLong(KEY_CONTACTS_UPDATED_AT, 0);
  }

  public void setContactsUpdatedAt(long timeInMillis) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putLong(KEY_CONTACTS_UPDATED_AT, timeInMillis);
    editor.apply();
  }

  public boolean getChangedNumber() {
    return pref.getBoolean(KEY_CHANGED_NUMBER, false);
  }

  public void setChangedNumber(boolean changed) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putBoolean(KEY_CHANGED_NUMBER, changed);
    editor.apply();
  }

  public void clear(String from) {
    setUserId("");
    setUserJson("");
    setToken("");
    setChangedNumber(false);
    setIsTaskRunning(false);
  }

  public String getToken() {
    return pref.getString(KEY_TOKEN, "");
  }

  public void setToken(String token) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putString(KEY_TOKEN, token);
    editor.apply();
  }

  public boolean getIsTaskRunning() {
    return pref.getBoolean(KEY_IS_TASK_RUNNING, false);
  }

  public void setIsTaskRunning(boolean isRunning) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putBoolean(KEY_IS_TASK_RUNNING, isRunning);
    editor.apply();
  }

  public boolean getIsUseCBMsg() {
    return pref.getBoolean(KEY_CALLBACK_MSG_USE, false);
  }

  public void setIsUseCBMsg(boolean isRunning) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putBoolean(KEY_CALLBACK_MSG_USE, isRunning);
    editor.apply();
  }

  public void setShowMakingDefault(boolean yes) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putBoolean(KEY_SHOW_MAKEING_DEFAULT, yes);
    editor.apply();
  }

  public boolean getShowMaingDefault() {
    return pref.getBoolean(KEY_SHOW_MAKEING_DEFAULT, false);
  }

  public long getLastRemovedTime() {
    return pref.getLong(KEY_LAST_REMOVED_TIME, 0);
  }

  public void setLastRemovedTime(long time) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putLong(KEY_LAST_REMOVED_TIME, time);
    editor.apply();
  }

  public String getBaseUrl() {
    return pref.getString(KEY_BASE_URL, RetrofitManager.BASE_URL);
  }

  public void setBaseUrl(String baseUrl) {
    SharedPreferences.Editor editor = pref.edit();
    editor.putString(KEY_BASE_URL, baseUrl);
    editor.apply();
  }
}
