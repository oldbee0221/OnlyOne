package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mms5.onepagebook.com.onlyonesms.CBMListActvitity;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.util.Utils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class OnePageMmsReceiver extends BroadcastReceiver implements Constants {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean check1 = Utils.GetBooleanSharedPreference(context, PREF_CHECK1);
        if(check1) {
            Intent it = new Intent(context, CBMListActvitity.class);
            it.addFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(it);
        }
    }
}
