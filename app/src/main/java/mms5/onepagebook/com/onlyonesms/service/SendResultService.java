package mms5.onepagebook.com.onlyonesms.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import mms5.onepagebook.com.onlyonesms.api.ApiCallback;
import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.SendingStatusBodyEnd;
import mms5.onepagebook.com.onlyonesms.api.response.DefaultResult;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2019-11-13.
 */
public class SendResultService extends Service implements Constants {

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();
        String idx = Utils.GetStringSharedPreference(context, PREF_IDX);

        if(!Utils.IsEmpty(idx)) {
            String sn = Utils.GetStringSharedPreference(context, PREF_SN);
            String pn = Utils.GetStringSharedPreference(context, PREF_PN);
            String et = Utils.GetStringSharedPreference(context, PREF_ET);
            String sent = Utils.GetStringSharedPreference(context, PREF_SENT);

            sendStatusEnd(idx, sn, pn, sent, et);

            Utils.PutSharedPreference(context, PREF_IDX, "");
            Utils.PutSharedPreference(context, PREF_PN, "");
            Utils.PutSharedPreference(context, PREF_SN, "");
            Utils.PutSharedPreference(context, PREF_ET, "");
            Utils.PutSharedPreference(context, PREF_SENT, "");
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void sendStatusEnd(String idx,
                               String sendNumber,
                               String phoneNumber,
                               String isSent,
                               String endTime) {
        try {
            RetrofitManager.retrofit(getApplicationContext()).create(Client.class)
                    .sendSendingStatusEnd(new SendingStatusBodyEnd(idx,
                            sendNumber,
                            phoneNumber,
                            isSent,
                            endTime))
                    .enqueue(new ApiCallback<DefaultResult>() {
                        @Override
                        public void onSuccess(DefaultResult response) {

                        }

                        @Override
                        public void onFail(int error, String msg) {

                        }
                    });
        } catch (Exception ignored) {
        }
    }
}
