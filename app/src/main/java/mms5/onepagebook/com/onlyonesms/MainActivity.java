package mms5.onepagebook.com.onlyonesms;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.squareup.otto.Subscribe;

import java.text.NumberFormat;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.api.ApiCallback;
import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.GettingStatisticsBody;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.dialog.MessageDialog;
import mms5.onepagebook.com.onlyonesms.manager.BusManager;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.model.Statistics;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class MainActivity extends AppCompatActivity implements Constants {
  public static final boolean HAS_TO_SHOW_LOGS = false;
  private static final int REQUEST_DEFAULT_APP = 12;

  private TextView mTextTodayCount;
  private TextView mTextMonthCount;
  private TextView mTextPhoneNumber;

  private TextView mBtnShowDialog;
  private TextView mTextDefaultApp;

  private ProgressBar mProgressBar;

  private String mRcvTelNum;

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    if (HAS_TO_SHOW_LOGS) {
      getMenuInflater().inflate(R.menu.menu_scrolling, menu);
    }
    return HAS_TO_SHOW_LOGS;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (HAS_TO_SHOW_LOGS) {
      if (item.getItemId() == R.id.action_logs) {
        startActivity(new Intent(MainActivity.this, LogActivity.class));
        return true;
      }
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onResume() {
    super.onResume();
    BusManager.getInstance().register(this);
  }

  @Override
  protected void onPause() {
    super.onPause();
    BusManager.getInstance().unregister(this);

  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Fabric.with(this, new Answers(), new Crashlytics());
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayShowCustomEnabled(true);
      getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    mRcvTelNum = getIntent().getStringExtra(EXTRA_RCV_TEL_NUM);

    mTextTodayCount = findViewById(R.id.text_today_count);
    mTextMonthCount = findViewById(R.id.text_month_count);
    mTextPhoneNumber = findViewById(R.id.text_phone_number);

    mProgressBar = findViewById(R.id.progress);
    mProgressBar.setVisibility(View.GONE);

    mBtnShowDialog = findViewById(R.id.btn_show_dialog);
    mTextDefaultApp = findViewById(R.id.text_default_app);

    setViewBySetting();
    mBtnShowDialog.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        showDefaultAppDialog();
      }
    });
    findViewById(R.id.btn_go_to).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        goToAnotherApp();
      }
    });

    findViewById(R.id.btn_go_callback).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(MainActivity.this, CBMDoorActivity.class);
        startActivity(i);
      }
    });

    loadStatics();

    if (!PreferenceManager.getInstance(getApplicationContext()).getShowMaingDefault()) {
      showDefaultAppDialog();
    }
  }

  private void setViewBySetting() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && !Telephony.Sms.getDefaultSmsPackage(MainActivity.this)
      .equals(getPackageName())) {
      mBtnShowDialog.setVisibility(View.VISIBLE);
      mTextDefaultApp.setVisibility(View.GONE);
    }
    else {
      mBtnShowDialog.setVisibility(View.GONE);
      mTextDefaultApp.setVisibility(View.VISIBLE);
    }
  }

  private void goToAnotherApp() {
    Intent intent = new Intent(Intent.ACTION_VIEW);
    intent.setData(Uri.parse("sms:"));
    intent.setType("vnd.android-dir/mms-sms");
    intent.putExtra("address", mRcvTelNum);

    Intent chooser = Intent.createChooser(intent, getString(R.string.app_name));
    if (intent.resolveActivity(getPackageManager()) != null) {
      try {
        startActivity(chooser);
      } catch (Exception ignored) {
        Toast.makeText(getApplicationContext(), "문자를 보낼 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show();
      }

    }
    else {
      Toast.makeText(getApplicationContext(), "문자를 보낼 수 있는 앱이 없습니다.", Toast.LENGTH_SHORT).show();
    }
  }

  private void loadStatics() {
    mProgressBar.setVisibility(View.VISIBLE);
    String phoneNumber = Utils.getPhoneNumber(this);
    RetrofitManager.retrofit(this).create(Client.class)
      .getStatistics(new GettingStatisticsBody(phoneNumber))
      .enqueue(new ApiCallback<Statistics>() {
        @Override
        public void onSuccess(Statistics statistics) {
          mProgressBar.setVisibility(View.GONE);
          setStatics(statistics);
        }

        @Override
        public void onFail(int error, String msg) {
          mProgressBar.setVisibility(View.GONE);
          Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
        }
      });
  }

  private void setStatics(Statistics statics) {
    String todayCountText = NumberFormat.getInstance().format(statics.today) + getString(R.string.unit_sms);
    mTextTodayCount.setText(todayCountText);
    String monthCountText = NumberFormat.getInstance().format(statics.month) + getString(R.string.unit_sms);
    mTextMonthCount.setText(monthCountText);
    String phoneNumber = Utils.getPhoneNumber(this);
    try {
      String formattedPhoneNumber = phoneNumber.substring(0, 3) + "-";
      formattedPhoneNumber += phoneNumber.substring(3, 7) + "-";
      formattedPhoneNumber += phoneNumber.substring(7);
      mTextPhoneNumber.setText(formattedPhoneNumber);
    } catch (Exception ignored) {
      mTextPhoneNumber.setText(phoneNumber);
    }
  }

  private void showDefaultAppDialog() {
    PreferenceManager.getInstance(getApplicationContext()).setShowMakingDefault(true);
    MessageDialog.showMessage(this, new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          String myPackageName = getPackageName();
          if (!Telephony.Sms.getDefaultSmsPackage(MainActivity.this).equals(myPackageName)) {
            try {
              Intent intent = new Intent(Telephony.Sms.Intents.ACTION_CHANGE_DEFAULT);
              intent.putExtra(Telephony.Sms.Intents.EXTRA_PACKAGE_NAME, myPackageName);
              startActivityForResult(intent, REQUEST_DEFAULT_APP);
            } catch (Exception e) {
              Toast.makeText(getApplicationContext(), "기본 앱 변경을 할 수 없습니다.", Toast.LENGTH_SHORT).show();
              RealmManager.writeLog(Log.getStackTraceString(e));
            }
          }
        }
      }
    });
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (REQUEST_DEFAULT_APP == requestCode) {
      setViewBySetting();
    }
  }

  @SuppressWarnings("unused")
  @Subscribe
  public void receivedRefresh(Refresh refresh) {
    loadStatics();
  }

  @SuppressWarnings("unused")
  @Subscribe
  public void receivedFinish(Finish finish) {
  }

  public static class Refresh {
    public static Refresh newInstance() {
      return new Refresh();
    }
  }

  public static class Finish {
    public static Finish newInstance() {
      return new Finish();
    }
  }
}
