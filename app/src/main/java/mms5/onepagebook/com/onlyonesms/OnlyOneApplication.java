package mms5.onepagebook.com.onlyonesms;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.FirebaseApp;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.manager.PushManager;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;

public class OnlyOneApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    Fabric.with(this, new Crashlytics());

    PushManager.createChannel(this);

    RealmManager.init(this);
  }
}
