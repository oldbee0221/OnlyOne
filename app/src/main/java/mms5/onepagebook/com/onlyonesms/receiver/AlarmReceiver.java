package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mms5.onepagebook.com.onlyonesms.service.SendResultService;

/**
 * Created by jeonghopark on 2019-11-13.
 */
public class AlarmReceiver extends BroadcastReceiver {

    Context context;

    @Override
    public void onReceive(Context context, Intent intent) {

        this.context = context;
        Intent service_intent = new Intent(context, SendResultService.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){
            this.context.startForegroundService(service_intent);
        }else{
            this.context.startService(service_intent);
        }
    }
}
