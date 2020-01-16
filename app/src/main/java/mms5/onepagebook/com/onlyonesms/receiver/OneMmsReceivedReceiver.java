package mms5.onepagebook.com.onlyonesms.receiver;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.klinker.android.send_message.MmsReceivedReceiver;

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

public class OneMmsReceivedReceiver extends MmsReceivedReceiver implements Constants {

    @Override
    public void onMessageReceived(Context context, Uri messageUri) {
        Utils.Log("onMessageReceived");
        String sndPhoneNum = "";

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
                            sndPhoneNum = address;
                            Cursor curPart = context.getContentResolver().query(Uri.parse("content://mms/part"), null, null, null, null);

                            if (curPart != null) {
                                if (curPart.moveToLast()) {
                                    int index = curPart.getColumnIndex("text");
                                    String message1 = curPart.getString(index);
                                    uploadSms(context, address, message1);
                                    if (checkMsg(message1)) {
                                        context.getContentResolver().delete(messageUri, null, null);
                                    } else {
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                                                && Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {
                                            spitNotification(context, message1);
                                        }
                                    }
//                                    for (int i = 0; i < curPart.getColumnCount(); i++) {
//                                        String message = curPart.getString(i);
//                                        if (message.length() > 100) {
//                                            uploadSms(context, address, message);
//                                            if (checkMsg(message)) {
//                                                context.getContentResolver().delete(messageUri, null, null);
//                                            } else {
//                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
//                                                        && Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {
//                                                    spitNotification(context, message);
//                                                }
//                                            }
//                                        }
//                                    }
                                }
                                curPart.close();
                            }
                        }
                        curAddr.close();
                    }
                }
                curPdu.close();
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                    && Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {

                boolean check1 = Utils.GetBooleanSharedPreference(context, PREF_CHECK1);
                if (check1) {
                    if(Utils.Is010PhoneNumber(sndPhoneNum)) {
                        Intent it = new Intent(context, CBMListActvitity.class);
                        it.putExtra(EXTRA_SND_NUM, sndPhoneNum);
                        it.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(it);
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onError(Context context, String s) {
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
}
