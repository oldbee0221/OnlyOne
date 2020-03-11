package mms5.onepagebook.com.onlyonesms.api.body;

import java.util.HashMap;

public class SyncContactBody extends HashMap<String, String> {

  public SyncContactBody(String phoneNumber, String contacts, String memid) {
    put("mem_id", memid);
    put("id", phoneNumber);
    put("address", contacts);
  }
}