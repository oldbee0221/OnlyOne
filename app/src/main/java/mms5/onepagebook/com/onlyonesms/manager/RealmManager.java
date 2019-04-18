package mms5.onepagebook.com.onlyonesms.manager;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import mms5.onepagebook.com.onlyonesms.MainActivity;
import mms5.onepagebook.com.onlyonesms.model.LogData;
import mms5.onepagebook.com.onlyonesms.model.ReceiverInfo;
import mms5.onepagebook.com.onlyonesms.model.Reservation;
import mms5.onepagebook.com.onlyonesms.model.Task;

public class RealmManager {
    public static void init(Context context) {
        Realm.init(context);
        RealmConfiguration config = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(config);
    }

    public static void writeLog(final String log) {
        if (MainActivity.HAS_TO_SHOW_LOGS) {
            Log.e("Pumpkin", log);
            try {
                Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
                    @Override
                    public void execute(Realm realm) {
                        LogData logData = realm.createObject(LogData.class);
                        logData.setLog(log);
                        logData.setTime(Calendar.getInstance().getTimeInMillis());
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static ArrayList<String> loadLogs(Realm realm) {
        ArrayList<String> logs = new ArrayList<>();
        RealmResults<LogData> result = realm.where(LogData.class).findAll();
        for (int i = 0; i < result.size(); i++) {
            String log = result.get(i).getFormattedTime() + "    " + result.get(i).getLog() + "\n\n";
            logs.add(log);
        }
        return logs;
    }

    public static void deleteLogs(Realm realm) {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.delete(LogData.class);
            }
        });
    }

    public static void addReservations(Realm realm, Task task) {
        String reqId = task.reqid;
        int closeTime = task.closeTime();

        ArrayList<ReceiverInfo> receiverInfoList = task.getReceiverInfoList();
        for (int i = 0; i < receiverInfoList.size(); i++) {
            ReceiverInfo receiverInfo = receiverInfoList.get(i);

            String title = task.formattedTitle(receiverInfo.rep);
            String body = task.formattedBody(receiverInfo.rep, receiverInfo.bnc);
            long delay = task.calculateDelayInMilli(i + 1);

            Reservation reservation = new Reservation();
            reservation.setIdx(task.getIdx());
            reservation.setReqid(reqId);
            reservation.setDelay(delay);
            reservation.setExpiredTime(closeTime);
            reservation.setPhoneNumber(receiverInfo.num);
            reservation.setTitle(title);
            reservation.setBody(body);
            reservation.setSent(false);

            realm.beginTransaction();
            realm.copyToRealm(reservation);
            realm.commitTransaction();
        }
    }

    public static RealmResults<Reservation> loadReservations(Realm realm) {
        return realm.where(Reservation.class).findAll();
    }

    public static void updateReservationState(Realm realm, Reservation reservation, boolean sent) {
        realm.beginTransaction();
        reservation.setSent(sent);
        realm.commitTransaction();
    }

    public static RealmResults<Reservation> loadNotSentReservations(Realm realm) {
        return realm.where(Reservation.class).equalTo("isSent", false).findAll();
    }

    public static RealmResults<Reservation> loadNotSentReservations(Realm realm, String reqId) {
        return realm.where(Reservation.class)
                .equalTo("reqid", reqId)
                .equalTo("isSent", false)
                .findAll();
    }

    public static void deleteReservations(Realm realm) {
        realm.beginTransaction();
        realm.where(Reservation.class).findAll().deleteAllFromRealm();
        realm.commitTransaction();
    }
}
