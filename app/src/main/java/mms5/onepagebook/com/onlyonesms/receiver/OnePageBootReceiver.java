package mms5.onepagebook.com.onlyonesms.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import mms5.onepagebook.com.onlyonesms.service.CheckTaskService;
import mms5.onepagebook.com.onlyonesms.service.SyncContactsService;

public class OnePageBootReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    String act = intent.getAction();

    if (Intent.ACTION_BOOT_COMPLETED.equals(act) ||
      Intent.ACTION_POWER_CONNECTED.equals(act)) {
      CheckTaskService.enqueue(context);
      SyncContactsService.enqueue(context);
    }
  }
}
