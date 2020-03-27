package mms5.onepagebook.com.onlyonesms.service;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.SyncContactBody;
import mms5.onepagebook.com.onlyonesms.api.response.DefaultResult;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class SyncContactsService extends JobIntentService {
    private static final int JOB_ID = 87;
    private static final long MIN_ELAPSED_TIME = 8L * 60L * 60L * 1000L;
    private static final long DELAY = 60L * 60L * 1000L;

    private ArrayList<Long> mContactId = new ArrayList<>();
    private WorkingThread mWorker;

    public static void enqueue(Context context) {
        enqueueWork(context, SyncContactsService.class, JOB_ID, new Intent());
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

    private void registerSyncContactsService() {
        SyncContactsService.enqueue(getApplicationContext());
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
                long contactsUpdatedAt = PreferenceManager.getInstance(getApplicationContext()).getContactsUpdatedAt();
                long elapsedTime = Calendar.getInstance().getTimeInMillis() - contactsUpdatedAt;
                if ((contactsUpdatedAt == 0 || MIN_ELAPSED_TIME <= elapsedTime)) {
                    syncContacts();
                }

                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (mIsRunning) {
                    registerSyncContactsService();
                }
            }
        }

        private void syncContacts() {
            String contactsByGroup;
            try {
                contactsByGroup = getPhoneNumsbyGroups();
            } catch (Exception ignored) {
                contactsByGroup = "[\"Can not get phone numbers\"]";
            }

            try {
                String phoneNumber = Utils.getPhoneNumber(getApplicationContext());
                String id = Utils.GetStringSharedPreference(getApplicationContext(), "ID");

                DefaultResult response = RetrofitManager.retrofit(getApplicationContext())
                        .create(Client.class)
                        .syncContacts(new SyncContactBody(phoneNumber, contactsByGroup, id))
                        .execute()
                        .body();

                if (response != null) {
                    if (DefaultResult.RESULT_0.equals(response.result)) {
                        PreferenceManager.getInstance(getApplicationContext())
                                .setContactsUpdatedAt(Calendar.getInstance().getTimeInMillis());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String getPhoneNumsbyGroups() {
            StringBuilder contacts = new StringBuilder("[");
            mContactId.clear();
            Uri uri = ContactsContract.Groups.CONTENT_URI;
            final String[] GROUP_PROJECTION = new String[]{ContactsContract.Groups._ID, ContactsContract.Groups.TITLE};
            Cursor c = getContentResolver().query(uri, GROUP_PROJECTION, null, null, ContactsContract.Groups.TITLE);
            if (c != null) {
                while (c.moveToNext()) {

                    String id = c.getString(c.getColumnIndex(ContactsContract.Groups._ID));
                    String gTitle = (c.getString(c.getColumnIndex(ContactsContract.Groups.TITLE)));

                    if (gTitle.contains("Group:")) {
                        gTitle = gTitle.substring(gTitle.indexOf("Group:") + 6).trim();
                    } else if (gTitle.contains("Favorite_")) {
                        gTitle = "Favorites";
                    } else if (gTitle.contains("Starred in Android") || gTitle.contains("My Contacts")) {
                        //continue;
                        gTitle = "MyContacts";
                    }
                    contacts.append(getContactsByGroupId(gTitle, id));
                }

                c.close();
                contacts.append("{\"NO_GROUP\":" + getNoGroupContacts() + "}");

                contacts.append("]");
            }
            return contacts.toString();
        }


        private String getContactsByGroupId(String groupName, String groupId) {
            StringBuilder contacts = new StringBuilder();
            String[] cProjection = {
                    ContactsContract.Contacts.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID
            };
            Cursor gc = getContentResolver().query(
                    ContactsContract.Data.CONTENT_URI,
                    cProjection,
                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID + "= ?" + " AND " +
                            ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE + "='" +
                            ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'",
                    new String[]{groupId}, null);
            if (gc != null && gc.moveToFirst()) {
                contacts.append("{\"" + groupName + "\":[");
                do {
                    String name = gc.getString(gc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    long contactId = gc.getLong(gc.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                    mContactId.add(contactId);
                    Cursor cc = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId,
                            null,
                            null);
                    if (cc != null) {
                        if (cc.moveToFirst()) {
                            String phoneNumber = "";
                            do {
                                phoneNumber = cc.getString(cc.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                //MLog.i(groupName, name,  phoneNumber);
                                contacts.append("{\"" + name + "\":\"" + phoneNumber + "\"},");
                            } while (cc.moveToNext());

                        }
                        cc.close();
                    }
                } while (gc.moveToNext());
                gc.close();
                contacts.setLength(contacts.length() - 1);
                contacts.append("]},");
            }

            return contacts.toString();
        }

        private String getNoGroupContacts() {
            Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            String[] projection = new String[]{
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
            };

            String[] selectionArgs = new String[]{};
            String sortOrder = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " COLLATE LOCALIZED ASC";
            Cursor c = getContentResolver().query(uri, projection, null, selectionArgs, sortOrder);
            StringBuilder contacts = new StringBuilder("[");
            if (c != null) {
                if (c.moveToFirst()) {
                    do {
                        long contactID = c.getLong(0);
                        String number = c.getString(1).replace("-", "").replace("+82", "0").replace(" ", "");
                        String name = c.getString(2);
                        if (mContactId.contains(contactID)) {
                            continue;
                        }
                        contacts.append("{\"" + name + "\":\"" + number + "\"},");

                    } while (c.moveToNext());
                    contacts.setLength(contacts.length() - 1);
                }
                c.close();
            }
            contacts.append("]");
            return contacts.toString();
        }
    }
}
