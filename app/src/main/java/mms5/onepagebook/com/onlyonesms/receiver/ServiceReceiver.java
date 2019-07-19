package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;

import mms5.onepagebook.com.onlyonesms.CBMListActvitity;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.util.Utils;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by jeonghopark on 2019-07-17.
 */
public class ServiceReceiver extends BroadcastReceiver implements Constants {
    private static String mLastState;
    private static boolean mIsRinging = false;
    private static String mPhoneNumberIn;
    private static String mPhoneNumberOut;

    @Override
    public void onReceive(Context context, Intent intent) {
        Utils.Log("onReceive: 1");

        if(Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            mPhoneNumberOut = intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER);
        }

        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

        if(state.equals(mLastState)) {
            return;
        } else {
            mLastState = state;
        }

        if(TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
            mIsRinging = true;
            mPhoneNumberIn = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        } else if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
            boolean dv = Utils.GetBooleanSharedPreference(context, PREF_DEFAULT_YN);
            if(dv) {
                if (mIsRinging) {
                    mIsRinging = false;
                    boolean check2 = Utils.GetBooleanSharedPreference(context, PREF_CHECK2);
                    if (check2) {
                        Intent it = new Intent(context, CBMListActvitity.class);
                        it.putExtra(EXTRA_SND_NUM, mPhoneNumberIn);
                        it.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(it);
                    }
                } else {
                    boolean check3 = Utils.GetBooleanSharedPreference(context, PREF_CHECK3);
                    if (check3) {
                        Intent it = new Intent(context, CBMListActvitity.class);
                        it.addFlags(FLAG_ACTIVITY_NEW_TASK);
                        it.putExtra(EXTRA_SND_NUM, mPhoneNumberOut);
                        context.startActivity(it);
                    }
                }
            }
        }
    }
}
