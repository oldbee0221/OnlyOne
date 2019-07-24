package mms5.onepagebook.com.onlyonesms.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;

import me.leolin.shortcutbadger.ShortcutBadger;
import mms5.onepagebook.com.onlyonesms.CBMListActvitity;
import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.SteppingStoneActivity;
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

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class OnePageMmsReceiver extends BroadcastReceiver implements Constants {
    private static long mRecvMillis = 0L;
    private Context _context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.Log("OnePageMmsReceiver");

        _context = context;

        Runnable runn = new Runnable() {
            @Override
            public void run() {
                parseMMS();
            }
        };
        Handler handler = new Handler();
        handler.postDelayed(runn, 6000);
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

    private boolean checkMsg(String m) {
        if (m.contains(FRAG_01)) {
            if (m.contains(FRAG_S4)) return true;
            if (m.contains(FRAG_S8)) return true;
            if (m.contains(FRAG_S6)) return true;
            if (m.contains(FRAG_S7)) return true;
            if (m.contains(FRAG_S2)) return true;
            if (m.contains(FRAG_S1)) return true;
            if (m.contains(FRAG_S3)) return true;
            if (m.contains(FRAG_S5)) return true;
        }

        return false;
    }

    private void spitNotification(Context context, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "default");
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(message);
        builder.setDefaults(NotificationCompat.DEFAULT_SOUND);
        builder.setAutoCancel(true);

        Intent intent = new Intent(context, SteppingStoneActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        builder.setContentIntent(contentIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(new NotificationChannel("default", "기본채널", NotificationManager.IMPORTANCE_DEFAULT));
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());

        int badgeCount = Utils.GetIntSharedPreference(context, PREF_BADGE_CNT) + 1;
        ShortcutBadger.applyCount(context, badgeCount);
        Utils.PutSharedPreference(context, PREF_BADGE_CNT, badgeCount);
    }

    private void parseMMS() {
        ContentResolver contentResolver = _context.getContentResolver();
        final String[] projection = new String[] { "_id" };
        Uri uri = Uri.parse("content://mms");
        Cursor cursor = contentResolver.query(uri, projection, null, null, "_id desc limit 1");

        if (cursor.getCount() == 0) {
            cursor.close();
            return;
        }

        cursor.moveToFirst();
        String id = cursor.getString(cursor.getColumnIndex("_id"));
        cursor.close();

        String number = parseNumber(id);
        String msg = parseMessage(id);
        Utils.Log("MMSReceiver.java | parseMMS |" + number + "|" + msg);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Telephony.Sms.getDefaultSmsPackage(_context).equals(_context.getPackageName())) {
            //spitNotification(_context, msg);
        }

        if(mRecvMillis + 2000 < System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                    && Telephony.Sms.getDefaultSmsPackage(_context).equals(_context.getPackageName())) {

                boolean check1 = Utils.GetBooleanSharedPreference(_context, PREF_CHECK1);
                if (check1) {
                    Intent it = new Intent(_context, CBMListActvitity.class);
                    it.putExtra(EXTRA_SND_NUM, number);
                    it.addFlags(FLAG_ACTIVITY_NEW_TASK);
                    _context.startActivity(it);
                }
            }
        }

        mRecvMillis = System.currentTimeMillis();
    }

    private String parseNumber(String $id) {
        String result = null;

        Uri uri = Uri.parse(MessageFormat.format("content://mms/{0}/addr", $id));
        String[] projection = new String[] { "address" };
        String selection = "msg_id = ? and type = 137";// type=137은 발신자
        String[] selectionArgs = new String[] { $id };

        Cursor cursor = _context.getContentResolver().query(uri, projection, selection, selectionArgs, "_id asc limit 1");

        if (cursor.getCount() == 0) {
            cursor.close();
            return result;
        }

        cursor.moveToFirst();
        result = cursor.getString(cursor.getColumnIndex("address"));
        cursor.close();

        return result;
    }

    private String parseMessage(String $id) {
        String result = null;

        // 조회에 조건을 넣게되면 가장 마지막 한두개의 mms를 가져오지 않는다.
        Cursor cursor = _context.getContentResolver().query(Uri.parse("content://mms/part"), new String[] { "mid", "_id", "ct", "_data", "text" }, null, null, null);

        Utils.Log("MMSReceiver.java | parseMessage |mms 메시지 갯수 : " + cursor.getCount() + "|");
        if (cursor.getCount() == 0) {
            cursor.close();
            return result;
        }

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            String mid = cursor.getString(cursor.getColumnIndex("mid"));
            if ($id.equals(mid)) {
                String partId = cursor.getString(cursor.getColumnIndex("_id"));
                String type = cursor.getString(cursor.getColumnIndex("ct"));
                if ("text/plain".equals(type)) {
                    String data = cursor.getString(cursor.getColumnIndex("_data"));

                    if (TextUtils.isEmpty(data))
                        result = cursor.getString(cursor.getColumnIndex("text"));
                    else
                        result = parseMessageWithPartId(partId);
                }
            }
            cursor.moveToNext();
        }
        cursor.close();

        return result;
    }


    private String parseMessageWithPartId(String $id) {
        Uri partURI = Uri.parse("content://mms/part/" + $id);
        InputStream is = null;
        StringBuilder sb = new StringBuilder();
        try {
            is = _context.getContentResolver().openInputStream(partURI);
            if (is != null) {
                InputStreamReader isr = new InputStreamReader(is, "UTF-8");
                BufferedReader reader = new BufferedReader(isr);
                String temp = reader.readLine();
                while (!TextUtils.isEmpty(temp)) {
                    sb.append(temp);
                    temp = reader.readLine();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                }
            }
        }

        return sb.toString();
    }
}
