package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.Context;
import android.telephony.CellInfo;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import java.util.List;

import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2020/04/07.
 */
public class MyPhoneStateListener extends PhoneStateListener {
    private Context context;

    public MyPhoneStateListener(Context context) {
        this.context = context;
    }

    @Override
    public void onServiceStateChanged(ServiceState serviceState) {
        switch(serviceState.getState()) {
            case ServiceState.STATE_IN_SERVICE:
                Utils.Log("onServiceStateChanged() STATE_IN_SERVICE");
                break;
            case ServiceState.STATE_OUT_OF_SERVICE:
                Utils.Log("onServiceStateChanged() STATE_OUT_OF_SERVICE");
                break;
            case ServiceState.STATE_EMERGENCY_ONLY:
                Utils.Log("onServiceStateChanged() STATE_EMERGENCY_ONLY");
                break;
            case ServiceState.STATE_POWER_OFF:
                Utils.Log("onServiceStateChanged() STATE_POWER_OFF");
                break;
            default:
                Utils.Log("onServiceStateChanged() default");
                break;
        }
        super.onServiceStateChanged(serviceState);
    }

    @Override
    public void onMessageWaitingIndicatorChanged(boolean mwi) {
        super.onMessageWaitingIndicatorChanged(mwi);
    }

    @Override
    public void onCallForwardingIndicatorChanged(boolean cfi) {
        super.onCallForwardingIndicatorChanged(cfi);
    }

    @Override
    public void onCellLocationChanged(CellLocation location) {
        super.onCellLocationChanged(location);
    }

    @Override
    public void onCallStateChanged(int state, String phoneNumber) {
        switch(state) {
            case TelephonyManager.CALL_STATE_IDLE:
                Utils.Log("onCallStateChanged() CALL_STATE_IDLE => " + phoneNumber);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                Utils.Log("onCallStateChanged() CALL_STATE_OFFHOOK => " + phoneNumber);
                break;
            case TelephonyManager.CALL_STATE_RINGING:
                Utils.Log("onCallStateChanged() CALL_STATE_RINGING => " + phoneNumber);
                break;
            default:
                Utils.Log("onCallStateChanged() " + state + " => " + phoneNumber);
                break;
        }
        super.onCallStateChanged(state, phoneNumber);
    }

    @Override
    public void onDataConnectionStateChanged(int state) {
        super.onDataConnectionStateChanged(state);
    }

    @Override
    public void onDataConnectionStateChanged(int state, int networkType) {
        super.onDataConnectionStateChanged(state, networkType);
    }

    @Override
    public void onDataActivity(int direction) {
        super.onDataActivity(direction);
    }

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        super.onSignalStrengthsChanged(signalStrength);
    }

    @Override
    public void onCellInfoChanged(List<CellInfo> cellInfo) {
        super.onCellInfoChanged(cellInfo);
    }
}
