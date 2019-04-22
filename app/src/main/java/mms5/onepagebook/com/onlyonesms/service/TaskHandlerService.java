package mms5.onepagebook.com.onlyonesms.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;

import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Message;
import com.klinker.android.send_message.Transaction;

import org.apache.commons.text.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import io.realm.Realm;
import io.realm.RealmResults;
import me.everything.providers.android.telephony.Mms;
import mms5.onepagebook.com.onlyonesms.LogInActivity;
import mms5.onepagebook.com.onlyonesms.MainActivity;
import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.api.ApiCallback;
import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.GettingTaskBody;
import mms5.onepagebook.com.onlyonesms.api.body.ReportSendingResultBody;
import mms5.onepagebook.com.onlyonesms.api.body.SendingStatusBody;
import mms5.onepagebook.com.onlyonesms.api.response.DefaultResult;
import mms5.onepagebook.com.onlyonesms.manager.BusManager;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;
import mms5.onepagebook.com.onlyonesms.manager.PushManager;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.model.Reservation;
import mms5.onepagebook.com.onlyonesms.model.Task;
import mms5.onepagebook.com.onlyonesms.util.Settings;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class TaskHandlerService extends Service {
    private static final String EXTRA_IDX = "mms5.onepagebook.com.onlyonesms.service.extra.idx";
    private static final long MAYBE_SENDING_DURATION = 15 * 60 * 1000L;

    private static final int FOREGROUND_NOTIFICATION_ID = 14;
    private static final int START_HOUR = 8;

    private Transaction mSendTransaction;
    private Settings mSettings;
    private Realm mRealm = null;

    public TaskHandlerService() {
    }

    public static void startWork(Context context, String idx) {
        RealmManager.writeLog("Service, startWork");
        /*if (!PreferenceManager.getInstance(context).getIsTaskRunning()) {
          PreferenceManager.getInstance(context).setIsTaskRunning(true);
          Intent intent = new Intent(context, TaskHandlerService.class);
          intent.putExtra(TaskHandlerService.EXTRA_IDX, idx);
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
          }
          else {
            context.startService(intent);
          }
        }*/

        Intent intent = new Intent(context, TaskHandlerService.class);
        intent.putExtra(TaskHandlerService.EXTRA_IDX, idx);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stopWork(Context context) {
        RealmManager.writeLog("Service, stopWork");
        context.stopService(new Intent(context, TaskHandlerService.class));
    }

    private static PendingIntent makeMainIntent(Context context) {
        Intent intent = new Intent(context, LogInActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        makeForeground();
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mRealm != null && !mRealm.isClosed()) {
            mRealm.close();
        }
        PreferenceManager.getInstance(getApplicationContext()).setIsTaskRunning(false);
        stopForeground(true);
    }

    private void makeForeground() {
        String msg = getString(R.string.msg_processing_sending_mms);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification notification = new NotificationCompat.Builder(getApplicationContext(), PushManager.CHANNEL_SERVICE_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(makeMainIntent(this))
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setAutoCancel(false)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(msg)
                    .build();

            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        } else {
            Notification notification = new NotificationCompat.Builder(getApplicationContext())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(makeMainIntent(this))
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setAutoCancel(false)
                    .setContentTitle(getString(R.string.app_name))
                    .setContentText(msg)
                    .build();

            startForeground(FOREGROUND_NOTIFICATION_ID, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String idx = intent.getStringExtra(EXTRA_IDX);
        RealmManager.writeLog("Service, onStartCommand, idx: " + idx);
        prepareSending(idx);
        return START_STICKY;
    }

    private void prepareSending(final String idx) {
        mSettings = Settings.get(getApplicationContext());
        com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
        sendSettings.setMmsc(mSettings.getMmsc());
        sendSettings.setProxy(mSettings.getMmsProxy());
        sendSettings.setPort(mSettings.getMmsPort());
        sendSettings.setUseSystemSending(true);
        mSendTransaction = new Transaction(getApplicationContext(), sendSettings);

        ApnUtils.initDefaultApns(getApplicationContext(), new ApnUtils.OnApnFinishedListener() {
            @Override
            public void onFinished() {
                mSettings = Settings.get(getApplicationContext(), true);
                com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
                sendSettings.setMmsc(mSettings.getMmsc());
                sendSettings.setProxy(mSettings.getMmsProxy());
                sendSettings.setPort(mSettings.getMmsPort());
                sendSettings.setUseSystemSending(true);

                mSendTransaction = new Transaction(getApplicationContext(), sendSettings);

                new WorkingThread(idx).start();
            }
        });
    }

    class WorkingThread extends Thread {
        private String mIdx;

        WorkingThread(String idx) {
            mIdx = idx;
        }

        @Override
        public void run() {
            try {
                Task response = RetrofitManager.retrofit(getApplicationContext())
                        .create(Client.class)
                        .getTasks(new GettingTaskBody(Utils.getPhoneNumber(getApplicationContext()), mIdx))
                        .execute()
                        .body();

                if (response != null) {
                    handleTask(response);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(MAYBE_SENDING_DURATION);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            removeSentMessage(getApplicationContext(), Mms.uriSent);
            removeSentMessage(getApplicationContext(), Mms.uriOutbox);
            removeSentMessage(getApplicationContext(), Mms.uriDraft);

            stopWork(getApplicationContext());
        }

        private void handleTask(Task task) {
            if (TextUtils.isEmpty(task.reqid)) {
                return;
            }

            Realm realm = Realm.getDefaultInstance();
            completeFailTask(realm);
            Bitmap[] images = task.imageBitmaps(getApplicationContext());

            RealmManager.addReservations(realm, task);

            RealmResults<Reservation> reservations = RealmManager.loadReservations(realm);
            for (int i = 0; i < reservations.size(); i++) {
                Reservation reservation = reservations.get(i);
                if (reservation != null) {
                    try {
                        sendMessage(realm, reservation, images);
                    } catch (Exception ignored) {
                        RealmManager.updateReservationState(realm, reservation, false);
                    }

                    sendStatus(reservation);
                    if (reservation.getDelay() > 0) {
                        try {
                            Thread.sleep(reservation.getDelay());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            for (Bitmap image : images) {
                if (!image.isRecycled()) {
                    image.recycle();
                }
            }

            RealmResults<Reservation> not = RealmManager.loadNotSentReservations(realm, task.reqid);
            ArrayList<String> errorList = new ArrayList<>();
            for (int i = 0; i < not.size(); i++) {
                errorList.add(not.get(i).getPhoneNumber());
            }

            completeTask(realm, task.reqid, errorList);

            realm.close();
        }

        private void completeFailTask(Realm realm) {
            HashMap<String, ArrayList<String>> errorListMap = notSentReservations(realm);
            for (String reqId : errorListMap.keySet()) {
                ArrayList<String> errorList = errorListMap.get(reqId);
                if (errorList != null) {
                    completeTask(realm, reqId, errorList);
                }
            }

            RealmManager.deleteReservations(realm);
        }

        private HashMap<String, ArrayList<String>> notSentReservations(Realm realm) {
            HashMap<String, ArrayList<String>> map = new HashMap<>();
            RealmResults<Reservation> not = RealmManager.loadNotSentReservations(realm);
            for (int i = 0; i < not.size(); i++) {
                Reservation reservation = not.get(i);
                if (reservation != null) {
                    ArrayList<String> list = new ArrayList<>();
                    if (map.get(reservation.getReqid()) != null) {
                        list = map.get(reservation.getReqid());
                    } else {
                        map.put(reservation.getReqid(), list);
                    }
                    list.add(reservation.getPhoneNumber());
                }
            }
            return map;
        }

        private void completeTask(Realm realm, final String reqId, ArrayList<String> notSentNumbers) {
            String errorList = notSentNumbers.size() > 0 ? notSentNumbers.toString() : "";
            errorList = errorList.replace("[", "");
            errorList = errorList.replace("]", "");
            try {
                DefaultResult response = RetrofitManager.retrofit(getApplicationContext())
                        .create(Client.class)
                        .reportSendingResult(new ReportSendingResultBody(reqId, errorList))
                        .execute()
                        .body();
                if (response != null) {
                    RealmManager.deleteReservations(realm);

                    BusManager.getInstance().post(MainActivity.Refresh.newInstance());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void sendMessage(Realm realm, Reservation reservation, Bitmap[] images) {
            int currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if (START_HOUR <= currentHour && currentHour < reservation.getExpiredTime()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                        !Telephony.Sms.getDefaultSmsPackage(getApplicationContext()).equals(getPackageName())) {
                    RealmManager.updateReservationState(realm, reservation, false);
                    return;
                }

                Message msg = new Message();
                if (images.length > 0) {
                    msg.setImages(images);
                }
                msg.setSubject(reservation.getTitle());
                msg.setText(StringEscapeUtils.unescapeHtml4(reservation.getBody()).replace("\\", ""));
                msg.setAddress(reservation.getPhoneNumber());
                msg.setSave(false);

                mSendTransaction.sendNewMessage(msg, Transaction.NO_THREAD_ID);
                RealmManager.updateReservationState(realm, reservation, true);
            } else {
                RealmManager.updateReservationState(realm, reservation, false);
            }
        }

        private void sendStatus(Reservation reservation) {
            if (!TextUtils.isEmpty(reservation.getIdx())) {
                try {
                    RetrofitManager.retrofit(getApplicationContext()).create(Client.class)
                            .sendSendingStatus(new SendingStatusBody(getApplicationContext(),
                                    reservation.getIdx(),
                                    reservation.getPhoneNumber(),
                                    reservation.isSent()))
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

        private void removeSentMessage(Context context, Uri uri) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                    Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {
                ArrayList<String> ids = new ArrayList<>();
                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            int idIdx = cursor.getColumnIndex(Telephony.BaseMmsColumns._ID);
                            int id = cursor.getInt(idIdx);
                            int dateIdx = cursor.getColumnIndex(Telephony.BaseMmsColumns.DATE);
                            long date = cursor.getLong(dateIdx) * 1000;
                            long time = Calendar.getInstance().getTimeInMillis() - date;

                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                                int creatorIdx = cursor.getColumnIndex(Telephony.BaseMmsColumns.CREATOR);
                                String creator = cursor.getString(creatorIdx);
                                if (TextUtils.isEmpty(creator)) {
                                    if (MAYBE_SENDING_DURATION > time) {
                                        ids.add(String.valueOf(id));
                                    }
                                } else {
                                    if (context.getPackageName().equals(creator)) {
                                        ids.add(String.valueOf(id));
                                    }
                                }
                            } else {
                                if (MAYBE_SENDING_DURATION > time) {
                                    ids.add(String.valueOf(id));
                                }
                            }
                        } while (cursor.moveToNext());
                    }
                    cursor.close();
                }

                for (int i = 0; i < ids.size(); i++) {
                    Uri uriForDeleting = Uri.withAppendedPath(uri, ids.get(i));
                    context.getContentResolver().delete(uriForDeleting, null, null);
                }
            }
        }
    }
}
