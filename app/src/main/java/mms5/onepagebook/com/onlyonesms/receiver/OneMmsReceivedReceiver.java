package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.klinker.android.send_message.MmsReceivedReceiver;

import mms5.onepagebook.com.onlyonesms.api.ApiCallback;
import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.SendingChangedNumberBody;
import mms5.onepagebook.com.onlyonesms.api.response.DefaultResult;
import mms5.onepagebook.com.onlyonesms.manager.GsonManager;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.model.UserInfo;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class OneMmsReceivedReceiver extends MmsReceivedReceiver {
  private static final String FRAG_01 = "01";
  private static final String FRAG_NUMBER = "입력하신 수신번호";

  @Override
  public void onMessageReceived(Context context, Uri messageUri) {
    try {
      Cursor curPdu = context.getContentResolver().query(Uri.parse("content://mms"), null, null, null, null);
      if (curPdu != null) {
        if (curPdu.moveToNext()) {
          String id = curPdu.getString(curPdu.getColumnIndex("_id"));
          Uri uriAddr = Uri.parse("content://mms/" + id + "/addr");
          Cursor curAddr = context.getContentResolver().query(uriAddr, null, null, null, null);
          if (curAddr != null) {
            if (curAddr.moveToNext()) {
              String address = curAddr.getString(curAddr.getColumnIndex("address"));
              Cursor curPart = context.getContentResolver()
                .query(Uri.parse("content://mms/part"), null, null, null, null);
              if (curPart != null) {
                if (curPart.moveToLast()) {
                  for (int i = 0; i < curPart.getColumnCount(); i++) {
                    String message = curPart.getString(i);
                    if (message.length() > 100 && (message.contains(FRAG_01) || message.contains(FRAG_NUMBER))) {
                      uploadSms(context, address, message);
                      context.getContentResolver().delete(messageUri, null, null);
                    }
                  }
                }
                curPart.close();
              }
            }
            curAddr.close();
          }
        }
        curPdu.close();
      }
    } catch (Exception ignored) {
    }
  }

  @Override
  public void onError(Context context, String s) {
  }

  private void uploadSms(Context context, String address, String message) {
    if (!TextUtils.isEmpty(PreferenceManager.getInstance(context).getUseJson())) {
      UserInfo userInfo = GsonManager.getGson()
        .fromJson(PreferenceManager.getInstance(context).getUseJson(), UserInfo.class);
      if (!TextUtils.isEmpty(userInfo.id)) {
        String number = Utils.getPhoneNumber(context);
        RetrofitManager.retrofit(context).create(Client.class)
          .sendChangedNumber(new SendingChangedNumberBody(userInfo.id, number, address, message))
          .enqueue(new ApiCallback<DefaultResult>() {
            @Override
            public void onSuccess(DefaultResult response) {
            }

            @Override
            public void onFail(int error, String msg) {
            }
          });
      }
    }
  }
}
