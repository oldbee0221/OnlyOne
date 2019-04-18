package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.Telephony;
import android.text.TextUtils;

import com.klinker.android.send_message.MmsSentReceiver;

import java.util.ArrayList;
import java.util.Calendar;

import me.everything.providers.android.telephony.Mms;

public class OneMmsSentReceiver extends MmsSentReceiver {
    private static final long MAYBE_SENDING_DURATION = 5 * 60 * 1000L;

    @Override
    public void onMessageStatusUpdated(Context context, Intent intent, int i) {
        String contentUri = intent.getStringExtra("content_uri");
        if (TextUtils.isEmpty(contentUri)) {
            removeSentMessageWithUri(context, Mms.uriSent);
            removeSentMessageWithUri(context, Mms.uriOutbox);
        } else {
            removeSentMessage(context, Uri.parse(contentUri));
        }
    }

    private void removeSentMessage(Context context, Uri sentMmsUri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&
                Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {
            context.getContentResolver().delete(sentMmsUri, null, null);
        }
    }

    private void removeSentMessageWithUri(Context context, Uri uri) {
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
