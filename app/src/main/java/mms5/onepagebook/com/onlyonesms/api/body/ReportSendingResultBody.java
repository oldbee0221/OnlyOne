package mms5.onepagebook.com.onlyonesms.api.body;

import java.util.HashMap;

public class ReportSendingResultBody extends HashMap<String, String> {

  public ReportSendingResultBody(String reqId, String failedPhoneNumbers) {
    put("reqid", reqId);
    put("error", failedPhoneNumbers);
  }
}