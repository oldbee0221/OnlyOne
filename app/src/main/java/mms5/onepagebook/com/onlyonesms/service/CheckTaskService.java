package mms5.onepagebook.com.onlyonesms.service;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import me.everything.providers.android.telephony.Mms;
import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.CheckTaskBody;
import mms5.onepagebook.com.onlyonesms.api.body.GettingStatisticsBody;
import mms5.onepagebook.com.onlyonesms.api.response.DefaultResult;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.model.Statistics;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class CheckTaskService extends JobIntentService {
    private static final long MINUTE = 60 * 1000L;
    private static final long MAYBE_SENDING_DURATION = 15 * 60 * 1000L;

    private static final int JOB_ID = 67;
    private static final long DELAY = 60L * 1000L;

    private WorkingThread mWorker;

    public static void enqueue(Context context) {
        enqueueWork(context, CheckTaskService.class, JOB_ID, new Intent());
    }

    @Override
    public boolean onStopCurrentWork() {
        if (mWorker != null) {
            try {
                mWorker.stopWorking();
                mWorker = null;
            } catch (Exception ignored) {
            }
        }
        return true;
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        if (mWorker != null) {
            try {
                mWorker.stopWorking();
                mWorker = null;
            } catch (Exception ignored) {
            }
        }

        mWorker = new WorkingThread();
        mWorker.start();
    }


    private void registerCheckTaskService() {
        CheckTaskService.enqueue(getApplicationContext());
    }

    class WorkingThread extends Thread {
        private boolean mIsRunning;

        WorkingThread() {
            mIsRunning = true;
        }

        void stopWorking() {
            mIsRunning = false;
        }

        @Override
        public void run() {
            if (mIsRunning) {
                final String phoneNumber = Utils.getPhoneNumber(getApplicationContext());
                try {
                    Statistics statistics = RetrofitManager.retrofit(getApplicationContext())
                            .create(Client.class)
                            .getStatistics(new GettingStatisticsBody(phoneNumber))
                            .execute()
                            .body();

                    if (statistics != null) {
                        checkTask(phoneNumber, statistics.month);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    removeSentMessage(getApplicationContext());
                } catch (Exception ignored) {
                }

                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mIsRunning) {
                    registerCheckTaskService();
                }
            }
        }


        private void checkTask(String phoneNumber, int count) {
            try {
                DefaultResult response = RetrofitManager.retrofit(getApplicationContext())
                        .create(Client.class)
                        .checkTasks(new CheckTaskBody(phoneNumber, String.valueOf(count)))
                        .execute()
                        .body();

                if (response != null) {
                    handleTask(response.result);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void handleTask(String result) {
            RealmManager.writeLog("[CheckTaskService] Result of GetTask: " + result);
            if (DefaultResult.RESULT_1.equals(result)) {
//                TaskHandlerService.startWork(getApplicationContext(), "");
                TaskHandlerService.getInstance(getApplicationContext()).onStartCommand("");
            } else if (DefaultResult.RESULT_2.equals(result)) {
                PreferenceManager.getInstance(getApplicationContext()).setChangedNumber(true);
            } else if (DefaultResult.RESULT_3.equals(result)) {
                PreferenceManager.getInstance(getApplicationContext()).setChangedNumber(false);
            }
        }

        private void removeSentMessage(Context context) {
            long elapsedTime = Calendar.getInstance()
                    .getTimeInMillis() - PreferenceManager.getInstance(getApplicationContext()).getLastRemovedTime();
            if (elapsedTime >= MINUTE) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Telephony.Sms.getDefaultSmsPackage(context)
                        .equals(context.getPackageName())) {
                    ArrayList<String> ids = findIdsByUri(context, Mms.uriSent);
                    for (int i = 0; i < ids.size(); i++) {
                        Uri uriForDeleting = Uri.withAppendedPath(Mms.uriSent, ids.get(i));
                        context.getContentResolver().delete(uriForDeleting, null, null);
                    }
                }
                PreferenceManager.getInstance(getApplicationContext())
                        .setLastRemovedTime(Calendar.getInstance().getTimeInMillis());
            }
        }

        private ArrayList<String> findIdsByUri(Context context, Uri uri) {
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
            return ids;
        }
    }
}
