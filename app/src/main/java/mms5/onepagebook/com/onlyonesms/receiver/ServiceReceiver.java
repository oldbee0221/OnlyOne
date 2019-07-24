package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import mms5.onepagebook.com.onlyonesms.CBMListActvitity;
import mms5.onepagebook.com.onlyonesms.MainActivity;
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
        String action = intent.getAction();

        if(action.equals(Intent.ACTION_NEW_OUTGOING_CALL)) {
            mPhoneNumberOut = intent.getExtras().getString(Intent.EXTRA_PHONE_NUMBER);
            Utils.Log("onReceive: 3 mPhoneNumberOut => " + mPhoneNumberOut);
        } else if(action.equals("android.intent.action.PHONE_STATE")) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

            //if(state.equals(mLastState)) {
            //    return;
            //} else {
            //    mLastState = state;
            //}

            if(TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                mPhoneNumberIn = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
                if(mPhoneNumberIn != null) mIsRinging = true;
            } else if(TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                        && Telephony.Sms.getDefaultSmsPackage(context).equals(context.getPackageName())) {
                    if (mIsRinging) {
                        mIsRinging = false;
                        boolean check2 = Utils.GetBooleanSharedPreference(context, PREF_CHECK2);
                        if (check2) {
                            if(!TextUtils.isEmpty(mPhoneNumberIn)) {
                                Intent it = new Intent(context, CBMListActvitity.class);
                                it.putExtra(EXTRA_SND_NUM, mPhoneNumberIn);
                                it.addFlags(FLAG_ACTIVITY_NEW_TASK);
                                context.startActivity(it);
                            }
                        }
                    } else {
                        boolean check3 = Utils.GetBooleanSharedPreference(context, PREF_CHECK3);
                        if (check3) {
                            if(!TextUtils.isEmpty(mPhoneNumberOut)) {
                                Utils.Log("onReceive: 2 mPhoneNumberOut => " + mPhoneNumberOut);
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
    }
}
