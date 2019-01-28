package mms5.onepagebook.com.onlyonesms.api.body;

import java.util.HashMap;

public class GettingStatisticsBody extends HashMap<String, String> {

  public GettingStatisticsBody(String phoneNumber) {
    put("id", phoneNumber);
  }
}