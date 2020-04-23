package mms5.onepagebook.com.onlyonesms.api.body;

import android.text.TextUtils;

import java.util.HashMap;

public class GettingTaskBody extends HashMap<String, String> {

  public GettingTaskBody(String tel, String idxFromFcm, String userId) {
    put("id", userId);
    put("mem_id", userId);
    if (!TextUtils.isEmpty(idxFromFcm) && TextUtils.isDigitsOnly(idxFromFcm)) {
      put("idx", idxFromFcm);
    }
  }
}