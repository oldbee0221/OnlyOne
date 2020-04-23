package mms5.onepagebook.com.onlyonesms;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.squareup.otto.Subscribe;

import java.text.NumberFormat;

import io.fabric.sdk.android.Fabric;
import me.leolin.shortcutbadger.ShortcutBadger;
import mms5.onepagebook.com.onlyonesms.api.ApiCallback;
import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.GettingStatisticsBody;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.dialog.MessageDialog;
import mms5.onepagebook.com.onlyonesms.manager.BusManager;
import mms5.onepagebook.com.onlyonesms.manager.GsonManager;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.model.Statistics;
import mms5.onepagebook.com.onlyonesms.model.UserInfo;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class MainActivity extends AppCompatActivity implements Constants {
    public static boolean HAS_TO_SHOW_LOGS = false;
    private static final int REQUEST_DEFAULT_APP = 12;
    private static final int REQUEST_PERMISSION = 120;

    private TextView mTextTodayCount;
    private TextView mTextMonthCount;
    private TextView mTextPhoneNumber;

    private TextView mBtnShowDialog;
    private TextView mTextDefaultApp;
    private TextView mTextGoToMsgBox;
    private TextView mTextID;
    private TextView mTextVer;

    private ProgressBar mProgressBar;

    private String mRcvTelNum;
    private Context mContext;
    private PreferenceManager mPrefManager;

    private boolean isBackground = false;

    @Override
    protected void onResume() {
        super.onResume();
        BusManager.getInstance().register(this);

        if (isBackground) {
            Utils.PutSharedPreference(getApplicationContext(), PREF_BADGE_CNT, 0);
            Utils.removeBadge(this);
            isBackground = false;
        }
        mTextGoToMsgBox.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            public void run() {
                requestPermissions(Utils.checkPermissions(MainActivity.this));
            }
        }, 2000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        BusManager.getInstance().unregister(this);
        isBackground = true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 절전 방지 기능 추가
        super.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mContext = getApplicationContext();
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
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

        mTextVer = findViewById(R.id.tv_ver);
        mTextVer.setText("ver. " + getAppVer());

        mTextID = findViewById(R.id.tv_id);
        mPrefManager = PreferenceManager.getInstance(getApplicationContext());
        String userJson = mPrefManager.getUseJson();
        UserInfo userInfo = GsonManager.getGson().fromJson(userJson, UserInfo.class);
        mTextID.setText(userInfo.id);

        mProgressBar = findViewById(R.id.progress);
        mProgressBar.setVisibility(View.GONE);

        mBtnShowDialog = findViewById(R.id.btn_show_dialog);
        mTextDefaultApp = findViewById(R.id.text_default_app);

        mTextGoToMsgBox = findViewById(R.id.btn_go_messagebox);
        mTextGoToMsgBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShortcutBadger.removeCount(getApplicationContext());
                Utils.PutSharedPreference(getApplicationContext(), PREF_BADGE_CNT, 0);

                NotificationManagerCompat.from(MainActivity.this).cancel(NOTIFICATION_ID);

                boolean isWorking;
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.setComponent(new ComponentName("com.android.mms", "com.android.mms.ui.ConversationList"));
                isWorking = tryActivityIntent(mContext, intent);
                if (!isWorking) {
                    intent.setComponent(new ComponentName("com.android.mms", "com.android.mms.ui.ConversationComposer"));
                    isWorking = tryActivityIntent(mContext, intent);
                }
                if (!isWorking) {
                    intent = new Intent(Intent.ACTION_MAIN);
                    intent.setType("vnd.android-dir/mms-sms");
                    tryActivityIntent(mContext, intent);
                }
            }
        });


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
                Intent i = new Intent(MainActivity.this, CBMDoor2Activity.class);
                startActivity(i);
            }
        });

        findViewById(R.id.tv_phone_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.PutSharedPreference(mContext, PREF_AUTOLOGIN, 1);
                mPrefManager.clear(getClass().getSimpleName());
                startActivity(new Intent(MainActivity.this, LogInActivity.class));
                finish();
            }
        });

        findViewById(R.id.btn_go_iam).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                Uri u = Uri.parse("http://obmms.net/iam");
                i.setData(u);
                startActivity(i);
            }
        });

        findViewById(R.id.iv_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        findViewById(R.id.iv_menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, LogActivity.class));
            }
        });

        loadStatics();

        if (!PreferenceManager.getInstance(getApplicationContext()).getShowMaingDefault()) {
            showDefaultAppDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Please allow permissions.", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
            }
        }

        String phoneNo = Utils.getPhoneNumber(MainActivity.this);
        String sms = getString(R.string.u_can_use_this_app);

        try {
            //전송
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, sms, null, null);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private boolean requestPermissions(String[] permissionArray) {
        if (permissionArray != null && permissionArray.length > 0) {
            ActivityCompat.requestPermissions(this, permissionArray, REQUEST_PERMISSION);
            return true;
        }
        return false;
    }

    private void setViewBySetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                && !Telephony.Sms.getDefaultSmsPackage(MainActivity.this).equals(getPackageName())) {
            mBtnShowDialog.setVisibility(View.VISIBLE);
            mTextDefaultApp.setVisibility(View.GONE);
        } else {
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

        } else {
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

    private boolean tryActivityIntent(Context context, Intent activityIntent) {
        try {
            if (activityIntent.resolveActivity(context.getPackageManager()) != null) {
                startActivity(activityIntent);
                return true;
            }
        } catch (SecurityException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private String getAppVer() {
        PackageInfo pInfo = null;

        try {
            pInfo = getPackageManager().getPackageInfo(this.getPackageName(), 0);
        } catch(PackageManager.NameNotFoundException e) {
            return "";
        }

        return pInfo.versionName;
    }
}
