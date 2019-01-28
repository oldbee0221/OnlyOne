package mms5.onepagebook.com.onlyonesms.api.body;

import android.content.Context;
import android.os.Build;
import android.provider.Telephony;

import java.util.HashMap;

import mms5.onepagebook.com.onlyonesms.util.Utils;

public class SendingStatusBody extends HashMap<String, String> {

  public SendingStatusBody(Context context, String idx, String phoneNumber, boolean isSent) {
    put("idx", idx);
    put("send_num", Utils.getPhoneNumber(context));
    put("recv_num", phoneNumber);
    if (isSent) {
      put("status", "0");
    }
    else {
      put("status", Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
        !Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName()) ? "-1" : "1");
    }
  }
}