package mms5.onepagebook.com.onlyonesms.api.body;

import java.util.HashMap;

public class SendingChangedNumberBody extends HashMap<String, String> {

  public SendingChangedNumberBody(String id, String userNumber, String receiverNumber, String smsBody) {
    put("id", id);
    put("send_num", userNumber);
    put("num", receiverNumber);
    put("sms", smsBody);
  }
}