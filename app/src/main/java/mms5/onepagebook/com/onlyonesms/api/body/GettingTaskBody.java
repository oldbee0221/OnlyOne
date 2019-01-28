package mms5.onepagebook.com.onlyonesms.api.body;

import android.text.TextUtils;

import java.util.HashMap;

public class GettingTaskBody extends HashMap<String, String> {

  public GettingTaskBody(String userId, String idxFromFcm) {
    put("id", userId);
    if (!TextUtils.isEmpty(idxFromFcm) && TextUtils.isDigitsOnly(idxFromFcm)) {
      put("idx", idxFromFcm);
    }
  }
}