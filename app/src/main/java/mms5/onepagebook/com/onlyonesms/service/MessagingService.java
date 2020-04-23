package mms5.onepagebook.com.onlyonesms.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import mms5.onepagebook.com.onlyonesms.LogInActivity;
import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.api.ApiCallback;
import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.SignInBody;
import mms5.onepagebook.com.onlyonesms.api.response.DefaultResult;
import mms5.onepagebook.com.onlyonesms.manager.GsonManager;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;
import mms5.onepagebook.com.onlyonesms.manager.PushManager;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.model.UserInfo;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class MessagingService extends FirebaseMessagingService {
    long m_pushTime = 0;
    int m_nBack = 0;
    @Override
    public void onNewToken(String newToken) {
        super.onNewToken(newToken);
        RealmManager.writeLog("Receive new token: " + newToken);
        sendRegistrationToServer(newToken);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String body = "";
        String idx = "";
        String sendType = "";
        String send = "";
        for (String key : remoteMessage.getData().keySet()) {
            String value = remoteMessage.getData().get(key);
            RealmManager.writeLog("data key: " + key + ", value: " + value);

            if ("body".equals(key)) {
                body = remoteMessage.getData().get(key);
            } else if ("idx".equals(key)) {
                idx = remoteMessage.getData().get(key);
            } else if ("send_type".equals(key)) {
                sendType = remoteMessage.getData().get(key);
            } else if ("Send".equals(key)) {
                send = remoteMessage.getData().get(key);
            }
        }

        if (TextUtils.isEmpty(idx) || !TextUtils.isDigitsOnly(idx)) {
            try {
                JSONObject obj = new JSONObject(body);
                idx = obj.optString("idx");
                sendType = obj.optString("send_type");
                send = obj.optString("Send");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        handleMessage(send, sendType, idx);
    }

    private void sendRegistrationToServer(String token) {
        Log.i(getString(R.string.app_name), "sendRegistrationToServer Token: " + token);

        PreferenceManager prefManager = PreferenceManager.getInstance(getApplicationContext());
        String userJson = prefManager.getUseJson();
        try {
            UserInfo userInfo = GsonManager.getGson().fromJson(userJson, UserInfo.class);
            if (!TextUtils.isEmpty(userInfo.id) && !TextUtils.isEmpty(userInfo.pw)) {
                signIn(userInfo.id, userInfo.pw, token);
            }
        } catch (Exception ignored) {
        }
    }

    private void handleMessage(String send, String sendType, String idx) {
        RealmManager.writeLog("FCM MESSAGE BODY: Send: " + send + ", send_type: " + sendType + ", idx: " + idx);
        Log.i(getString(R.string.app_name), "send: " + send);
        Log.i(getString(R.string.app_name), "sendType: " + sendType);
        Log.i(getString(R.string.app_name), "idx: " + idx);

//        TaskHandlerService.getInstance(getApplicationContext()).onStartCommand(idx);
        UndeadService.startWork(getApplicationContext(), idx);
//        long cur = System.currentTimeMillis();
//        if (m_pushTime == 0) {
//            TaskHandlerService.getInstance(getApplicationContext()).onStartCommand(idx);
//            m_pushTime = cur;
//            m_nBack = 0;
//        } else {
//            if ((cur - m_pushTime) > 5000) {
//                TaskHandlerService.getInstance(getApplicationContext()).onStartCommand(idx);
//                m_nBack = 0;
//                m_pushTime = cur;
//            } else {
//                m_nBack ++;
//                try {
//                    Thread.sleep(5000 * m_nBack);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                Log.e("Push Time:" , cur + " :: " + m_pushTime);
//                TaskHandlerService.getInstance(getApplicationContext()).onStartCommand(idx);
//            }
//        }

        // 2020-01-02 푸시가 여러건 쌓이도록 수정. by blas.
        showNoti();
    }

    private void showNoti() {
        try {
            Thread.sleep(15000);
        } catch (InterruptedException e) {
        }
        String msg = getString(R.string.msg_processing_sending_mms);

        int notificationId = (int)(Math.random() * 10000) + 1;
        String channelId = PushManager.CHANNEL_SERVICE_ID + notificationId;
        String channelName = PushManager.CHANNEL_SERVICE_CHANNEL +  + notificationId;
        int importance = NotificationManager.IMPORTANCE_DEFAULT;

        Intent intent = new Intent(getApplicationContext(), LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent =  PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.createNotificationChannel(mChannel);
            long when = System.currentTimeMillis();

//            Notification notification = new NotificationCompat.Builder(getApplicationContext(), channelId )
//                    .setSmallIcon(R.mipmap.ic_launcher)
//                    .setContentIntent(pendingIntent)
//                    .setPriority(NotificationCompat.PRIORITY_MIN)
//                    .setAutoCancel(false)
//                    .setContentTitle(getString(R.string.app_name))
//                    .setContentText(msg)
//                    .build();
//
//            startForeground(notificationId, notification);

            Notification.Builder mBuilder = new Notification.Builder(this, channelId)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setWhen(when)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent);

            notificationManager.notify(notificationId, mBuilder.build());
        } else {
            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setAutoCancel(false)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(msg)
                    .build();

            startForeground(notificationId, notification);
        }
    }
    private void signIn(final String id, final String pw, final String token) {
        final PreferenceManager mPrefManager = PreferenceManager.getInstance(getApplicationContext());
        final String phoneNumber = Utils.getPhoneNumber(this);
        final String version = Utils.getAppVersion(this);
        final String telecom = Utils.getTelecom(this);
        final String model = Utils.getDeviceModel();
        RetrofitManager.retrofit(getApplicationContext()).create(Client.class)
                .signIn(new SignInBody(id, pw, phoneNumber, telecom, model, version, token))
                .enqueue(new ApiCallback<DefaultResult>() {
                    @Override
                    public void onSuccess(DefaultResult response) {
                        String result = response.result;
                        RealmManager.writeLog("Sign in result: " + result);
                        RealmManager.writeLog("Firebase token: " + token);
                        mPrefManager.clear("MessagingService clear in sign in1");

                        if (DefaultResult.RESULT_0.equals(result)) {
                            Toast.makeText(getApplicationContext(), R.string.msg_success_to_log_in, Toast.LENGTH_SHORT).show();
                            mPrefManager.setUserId(id);
                            mPrefManager.setUserJson(GsonManager.getGson().toJson(new UserInfo(id, pw)));
                            mPrefManager.setToken(token);
                        } else {
                            showHaveToLoginNotification(token, "MessagingService clear in sign in2");
                        }
                    }

                    @Override
                    public void onFail(int error, String msg) {
                        showHaveToLoginNotification(token, "MessagingService clear in sign in3");
                    }
                });
    }

    private void showHaveToLoginNotification(String token, String from) {
        RealmManager.writeLog("showHaveToLoginNotification, current fcm token: " + token);
        PreferenceManager.getInstance(getApplicationContext()).clear(from);
        PreferenceManager.getInstance(getApplicationContext()).setToken(token);

        PushManager.sendNotification(getApplicationContext(), "자동 로그인 실패", "자동 로그인이 실패했습니다. 다시 로그인 부탁드립니다.");
    }
}
