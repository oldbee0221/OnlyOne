package mms5.onepagebook.com.onlyonesms;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.api.ApiCallback;
import mms5.onepagebook.com.onlyonesms.api.Client;
import mms5.onepagebook.com.onlyonesms.api.body.ServiceListBody;
import mms5.onepagebook.com.onlyonesms.api.body.SignInBody;
import mms5.onepagebook.com.onlyonesms.api.response.DefaultResult;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.manager.GsonManager;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;
import mms5.onepagebook.com.onlyonesms.manager.RealmManager;
import mms5.onepagebook.com.onlyonesms.manager.RetrofitManager;
import mms5.onepagebook.com.onlyonesms.model.ServiceList;
import mms5.onepagebook.com.onlyonesms.model.ServiceListData;
import mms5.onepagebook.com.onlyonesms.model.UserInfo;
import mms5.onepagebook.com.onlyonesms.service.CheckTaskService;
import mms5.onepagebook.com.onlyonesms.service.SyncContactsService;
import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * opbnew / 190117
 **/
public class LogInActivity extends AppCompatActivity implements Constants {
    private static final int REQUEST_PERMISSION = 120;

    private ProgressBar mProgress;
    private AppCompatEditText mEditId;
    private AppCompatEditText mEditPw;
    private AppCompatButton mBtnLogIn;
    private PreferenceManager mPrefManager;

    private String mRcvTelNum;
    private boolean mFlagMsgBox;
    private int mResumeCnt;

    private Spinner mSpnService;
    private ArrayList<ServiceListData> mServiceList;

    private Context mContext;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (MainActivity.HAS_TO_SHOW_LOGS) {
            getMenuInflater().inflate(R.menu.menu_scrolling, menu);
        }

        return MainActivity.HAS_TO_SHOW_LOGS;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (MainActivity.HAS_TO_SHOW_LOGS) {
            if (item.getItemId() == R.id.action_logs) {
                startActivity(new Intent(this, LogActivity.class));
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_log_in);

        mContext = getApplicationContext();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Intent intent = getIntent();

        if (intent != null) {
            if (intent.getData() != null) {
                mRcvTelNum = intent.getData().getSchemeSpecificPart();
            }
        }

        mProgress = findViewById(R.id.progress);
        mProgress.setVisibility(View.GONE);
        mEditId = findViewById(R.id.edit_id);
        mEditPw = findViewById(R.id.edit_pw);
        mBtnLogIn = findViewById(R.id.login_btn);
        mSpnService = findViewById(R.id.spn_service);

        mPrefManager = PreferenceManager.getInstance(getApplicationContext());
        initListeners();

        hideKeyBoard();

        mFlagMsgBox = false;
        mResumeCnt = 0;

        Utils.PutSharedPreference(getApplicationContext(), PREF_BADGE_CNT, 0);
        Utils.removeBadge(this);

        if (!Utils.IsEmpty(mRcvTelNum)) {
            mFlagMsgBox = true;
            goToAnotherApp();
            return;
        }

        if (!Utils.hasUsim(getApplicationContext())) {
            Toast.makeText(getApplicationContext(), R.string.msg_no_usim, Toast.LENGTH_LONG).show();
            finish();
        } else if (!requestPermissions(Utils.checkPermission(this))) {
            showAgreePopupAndAutoLogin();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mResumeCnt++;
        if (mResumeCnt > 1) {
            if (mFlagMsgBox) finish();
        }
    }

    private boolean requestPermissions(String[] permissionArray) {
        if (permissionArray != null && permissionArray.length > 0) {
            ActivityCompat.requestPermissions(this, permissionArray, REQUEST_PERMISSION);
            return true;
        }
        return false;
    }

    private void initListeners() {
        findViewById(R.id.login_join).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard();

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(RetrofitManager.URL_SIGN_UP));
                startActivity(i);
            }
        });
        mBtnLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyBoard();

                if (isAllValidInput()) {
                    signIn(mEditId.getText().toString(), mEditPw.getText().toString());
                }
            }
        });
    }

    private boolean isAllValidInput() {
        if (TextUtils.isEmpty(mEditId.getText().toString())) {
            Toast.makeText(getApplicationContext(), R.string.msg_please_type_id, Toast.LENGTH_SHORT).show();
            return false;
        } else if (TextUtils.isEmpty(mEditPw.getText().toString())) {
            Toast.makeText(getApplicationContext(), R.string.msg_please_type_pw, Toast.LENGTH_SHORT).show();
            return false;
        }

        if (mSpnService.getSelectedItemPosition() == 0) {
            Toast.makeText(getApplicationContext(), R.string.msg_please_service_point, Toast.LENGTH_SHORT).show();
            return false;
        }

        RetrofitManager.cleanRetrofit();

        return true;
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

            showAgreePopupAndAutoLogin();
        }
    }

    private void showAgreePopupAndAutoLogin() {
        String currentAppVersion = Utils.getAppVersion(this);
        if (!TextUtils.isEmpty(mPrefManager.getVersion()) && !currentAppVersion.equals(mPrefManager.getVersion())) {
            mPrefManager.clear(getClass().getSimpleName());
            mPrefManager.setVersion(currentAppVersion);
        }

        if (!mPrefManager.getAgreeWithPolicy()) {
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setMessage(readPrivacyFile());
            ab.setPositiveButton(getString(R.string.term_agree), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPrefManager.setAgreeWithPolicy(true);
                }
            });
            ab.setNegativeButton(getString(R.string.term_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mPrefManager.setAgreeWithPolicy(false);
                    finish();
                }
            });
            ab.show();
        }

        // Auto login
        String userJson = mPrefManager.getUseJson();
        if (!TextUtils.isEmpty(userJson) && !TextUtils.isEmpty(mPrefManager.getToken())) {
            try {
                UserInfo userInfo = GsonManager.getGson().fromJson(userJson, UserInfo.class);
                if (!TextUtils.isEmpty(userInfo.id) && !TextUtils.isEmpty(userInfo.pw)) {
                    signIn(userInfo.id, userInfo.pw);
                } else {
                    mPrefManager.clear(getClass().getSimpleName());
                }
            } catch (Exception ignored) {
                mPrefManager.clear(getClass().getSimpleName());
            }
        } else {
            getServiceList();
        }
    }

    private void signIn(final String id, final String pw) {
        mBtnLogIn.setEnabled(false);
        mProgress.setVisibility(View.VISIBLE);

        final String phoneNumber = Utils.getPhoneNumber(this);
        final String version = Utils.getAppVersion(this);
        final String telecom = Utils.getTelecom(this);
        final String model = Utils.getDeviceModel();

        if (TextUtils.isEmpty(PreferenceManager.getInstance(this).getToken())) {
            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                return;
                            }

                            final String token = task.getResult().getToken();
                            RetrofitManager.retrofit(getApplicationContext()).create(Client.class)
                                    .signIn(new SignInBody(id, pw, phoneNumber, telecom, model, version, token))
                                    .enqueue(new ApiCallback<DefaultResult>() {
                                        @Override
                                        public void onSuccess(DefaultResult response) {
                                            mBtnLogIn.setEnabled(true);
                                            mProgress.setVisibility(View.GONE);

                                            handleLogInResult(response.result, new UserInfo(id, pw), token);
                                        }

                                        @Override
                                        public void onFail(int error, String msg) {
                                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                                            mBtnLogIn.setEnabled(true);
                                            mProgress.setVisibility(View.GONE);
                                        }
                                    });
                        }
                    });
        } else {
            final String token = PreferenceManager.getInstance(this).getToken();
            RetrofitManager.retrofit(getApplicationContext()).create(Client.class)
                    .signIn(new SignInBody(id, pw, phoneNumber, telecom, model, version, token))
                    .enqueue(new ApiCallback<DefaultResult>() {
                        @Override
                        public void onSuccess(DefaultResult response) {
                            mBtnLogIn.setEnabled(true);
                            mProgress.setVisibility(View.GONE);

                            handleLogInResult(response.result, new UserInfo(id, pw), token);
                        }

                        @Override
                        public void onFail(int error, String msg) {
                            Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
                            mBtnLogIn.setEnabled(true);
                            mProgress.setVisibility(View.GONE);
                        }
                    });
        }
    }


    private void handleLogInResult(String result, UserInfo userInfo, String token) {
        RealmManager.writeLog("Sign in result: " + result);
        RealmManager.writeLog("Firebase token: " + token);
        mPrefManager.clear(getClass().getSimpleName());

        if (DefaultResult.RESULT_0.equals(result)) {
            //로그인 성공
            Toast.makeText(getApplicationContext(), R.string.msg_success_to_log_in, Toast.LENGTH_SHORT).show();
            mPrefManager.setUserId(userInfo.id);
            mPrefManager.setUserJson(GsonManager.getGson().toJson(userInfo));
            mPrefManager.setToken(token);

            CheckTaskService.enqueue(getApplicationContext());
            SyncContactsService.enqueue(getApplicationContext());

            goToMainActivity();
        } else {
            //로그인 실패
            if (DefaultResult.RESULT_1.equals(result)) {
                Toast.makeText(getApplicationContext(), R.string.msg_fail_to_log_in_1, Toast.LENGTH_SHORT).show();
            } else if (DefaultResult.RESULT_2.equals(result)) {
                Toast.makeText(getApplicationContext(), R.string.msg_fail_to_log_in_2, Toast.LENGTH_SHORT).show();
            } else if (DefaultResult.RESULT_3.equals(result)) {
                Toast.makeText(getApplicationContext(), R.string.msg_fail_to_log_in_3, Toast.LENGTH_SHORT).show();
            } else if (DefaultResult.RESULT_4.equals(result)) {
                Toast.makeText(getApplicationContext(), R.string.msg_fail_to_log_in_4, Toast.LENGTH_SHORT).show();
            } else if (DefaultResult.RESULT_5.equals(result)) {
                Toast.makeText(getApplicationContext(), R.string.msg_fail_to_log_in_5, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getApplicationContext(), R.string.msg_fail_to_log_in, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void goToMainActivity() {
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra(EXTRA_RCV_TEL_NUM, mRcvTelNum);
        startActivity(i);
        finish();
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

    private String readPrivacyFile() {
        StringBuilder privacy = new StringBuilder();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("private_privacy_v2.txt"), "UTF-8"));
            String line;
            while ((line = reader.readLine()) != null) {
                privacy.append(line);
                privacy.append("\n");
            }
        } catch (IOException ignored) {
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
        }
        return privacy.toString();
    }

    private void hideKeyBoard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        }
    }


    private void getServiceList() {
        RetrofitManager.retrofit(getApplicationContext()).create(Client.class)
                .serviceList(new ServiceListBody())
                .enqueue(new ApiCallback<ServiceList>() {
                    @Override
                    public void onSuccess(ServiceList response) {
                        String result = response.result;

                        if (DefaultResult.RESULT_0.equals(result)) {
                            mServiceList = response.data;
                            if (mServiceList != null && mServiceList.size() > 0) {
                                int size = mServiceList.size();
                                String[] svcNm = new String[size + 1];
                                svcNm[0] = getString(R.string.choose_connect_service);

                                for (int i = 0; i < size; i++) {
                                    svcNm[i + 1] = mServiceList.get(i).service_name;
                                }

                                ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(),
                                        R.layout.custom_spinner_item,
                                        svcNm);
                                adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

                                mSpnService.setAdapter(adapter);

                                mSpnService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                        if (i > 0) {
                                            PreferenceManager.getInstance(mContext).setBaseUrl(mServiceList.get(i - 1).domain);
                                        } else {
                                            PreferenceManager.getInstance(mContext).setBaseUrl(RetrofitManager.BASE_URL);
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> adapterView) {

                                    }
                                });
                            }
                        }
                    }

                    @Override
                    public void onFail(int error, String msg) {
                        PreferenceManager.getInstance(mContext).setBaseUrl(RetrofitManager.BASE_URL2);
                        RetrofitManager.cleanRetrofit();
                        getServiceList();
                    }
                });
    }
}

