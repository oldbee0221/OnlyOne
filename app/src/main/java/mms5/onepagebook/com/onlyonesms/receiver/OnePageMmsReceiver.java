package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.Telephony;

import com.android.mms.transaction.PushReceiver;

import java.text.MessageFormat;

import mms5.onepagebook.com.onlyonesms.CBMListActvitity;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.util.Utils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by jeonghopark on 2019-11-06.
 */
public class OnePageMmsReceiver extends PushReceiver implements Constants {
    private static long mRecvMillis = 0L;
    private Context _context;

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.Log("OnePageMmsReceiver ======");
        super.onReceive(context, intent);

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
        Utils.Log("MMSReceiver.java | parseMMS |" + number + "|");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && Telephony.Sms.getDefaultSmsPackage(_context).equals(_context.getPackageName())) {
            //spitNotification(_context, msg);
        }

        if(mRecvMillis + 2000 < System.currentTimeMillis()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                    && Telephony.Sms.getDefaultSmsPackage(_context).equals(_context.getPackageName())) {

                boolean check1 = Utils.GetBooleanSharedPreference(_context, PREF_CHECK1);
                if (check1) {
                    if(Utils.Is010PhoneNumber(number)) {
                        Intent it = new Intent(_context, CBMListActvitity.class);
                        it.putExtra(EXTRA_SND_NUM, number);
                        it.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        _context.startActivity(it);
                    }
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
}
