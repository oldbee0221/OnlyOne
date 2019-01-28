package mms5.onepagebook.com.onlyonesms.api.body;

import java.util.HashMap;

public class SyncContactBody extends HashMap<String, String> {

  public SyncContactBody(String phoneNumber, String contacts) {
    put("id", phoneNumber);
    put("address", contacts);
  }
}