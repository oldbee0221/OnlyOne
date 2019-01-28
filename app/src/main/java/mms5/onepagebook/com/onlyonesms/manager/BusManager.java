package mms5.onepagebook.com.onlyonesms.manager;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

public class BusManager {
  private static final MainThreadBus mBus = new MainThreadBus();

  private BusManager() {
    // No instances.
  }

  public static MainThreadBus getInstance() {
    return mBus;
  }

  public static class MainThreadBus extends Bus {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    public void post(final Object event) {
      if (Looper.myLooper() == Looper.getMainLooper()) {
        super.post(event);
      }
      else {
        handler.post(new Runnable() {
          @Override
          public void run() {
            MainThreadBus.super.post(event);
          }
        });
      }
    }
  }
}
