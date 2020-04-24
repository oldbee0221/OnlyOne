package mms5.onepagebook.com.onlyonesms.api.body;

import java.util.HashMap;

public class CheckTaskBody extends HashMap<String, String> {

  public CheckTaskBody(String phoneNumber, String smsCount, String userId) {
    put("id", phoneNumber);
    put("num_mms", smsCount);
    put("mem_id", userId);
  }
}