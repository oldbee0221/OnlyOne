package mms5.onepagebook.com.onlyonesms;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Transaction;

import org.apache.commons.text.StringEscapeUtils;

import java.io.FileNotFoundException;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.adapter.CallMsgChoiceAdapter;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.CallMsg;
import mms5.onepagebook.com.onlyonesms.dialog.ProgressDialog;
import mms5.onepagebook.com.onlyonesms.util.Settings;
import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2020/04/06.
 */
public class CBMChoiceListActivity extends AppCompatActivity implements Constants, View.OnClickListener {
    private final int HANDLER_SEND = 301;
    private final int REQ_UPDATE = 500;

    private Context mContext;

    private SwipeRefreshLayout mSrl;
    private RecyclerView mRv;
    private LinearLayoutManager mLayoutManager;
    private CallMsgChoiceAdapter mAdapter;
    private TextView mTvPhoneNumber;
    private TextView mTvName;
    private LinearLayout mLayoutPhoneNumber;
    private LinearLayout mLayoutMenu;

    private List<CallMsg> mMsgs;
    private boolean mIsFromMsg;

    private Transaction mSendTransaction;
    private Settings mSettings;
    private CallMsg mMsgForSending;
    private String mSndNumber;

    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_cbm_choice_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = getApplicationContext();

        mTvPhoneNumber = findViewById(R.id.tv_phonenum);
        mTvName = findViewById(R.id.tv_name);
        mLayoutPhoneNumber = findViewById(R.id.ll_phonenum);
        mLayoutMenu = findViewById(R.id.ll_menu);

        mSrl = findViewById(R.id.srl_base);
        mSrl.setOnRefreshListener(onRefresh);
        mRv = findViewById(R.id.rv_base);
        mLayoutManager = new LinearLayoutManager(mContext);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        findViewById(R.id.btn_write).setOnClickListener(this);
        findViewById(R.id.btn_cancel).setOnClickListener(this);
        findViewById(R.id.iv_home).setOnClickListener(this);
        findViewById(R.id.iv_menu).setOnClickListener(this);

        Intent intent = getIntent();

        if(TextUtils.isEmpty(intent.getStringExtra(EXTRA_FROM_DOOR))) {
            findViewById(R.id.btn_cancel).setVisibility(View.VISIBLE);
            mLayoutPhoneNumber.setVisibility(View.VISIBLE);
            mIsFromMsg = true;
            mSndNumber = intent.getStringExtra(EXTRA_SND_NUM);
            mTvPhoneNumber.setText(makePhonenum(mSndNumber));
            mTvName.setText(getDisplayName(mSndNumber));
            Utils.Log("CBMListActivity mSndNumber => " + mSndNumber);
        } else {
            mLayoutPhoneNumber.setVisibility(View.GONE);
            mIsFromMsg = false;
            mSndNumber = "";
        }

        mProgressDialog = new ProgressDialog(CBMChoiceListActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch (vid) {
            case R.id.btn_write: {
                Intent i = new Intent(CBMChoiceListActivity.this, CBMReg2Activity.class);
                startActivity(i);
            }
            break;

            case R.id.btn_cancel:
                finish();
                break;

            case R.id.iv_menu:
                startActivity(new Intent(CBMChoiceListActivity.this, LogActivity.class));
                break;

            case R.id.iv_home:
                finish();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Utils.Log("requestCode ==> " + requestCode + ", resultCode ==> " + resultCode);
        if(requestCode == REQ_UPDATE) {
            if(resultCode == Activity.RESULT_OK) {
                finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if(mIsFromMsg) {
            finish();
        } else {
            super.onBackPressed();
        }
    }

    SwipeRefreshLayout.OnRefreshListener onRefresh = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            if (mSrl.isRefreshing()) {
                mSrl.setRefreshing(false);
            }
        }
    };

    private String makePhonenum(String p) {
        if(p.length() == 11) {
            return p.substring(0,3) + "-" + p.substring(3,7) + "-" + p.substring(7,11);
        } else if(p.length() == 10) {
            return p.substring(0,3) + "-" + p.substring(3,6) + "-" + p.substring(6,10);
        } else {
            return p;
        }
    }

    private void init() {
        if (mAdapter != null) {
            mAdapter.removeAll();
        }

        mRv.setHasFixedSize(true);
        mRv.setLayoutManager(mLayoutManager);
        mAdapter = new CallMsgChoiceAdapter(mContext, mIsFromMsg, Utils.GetLongSharedPreference(mContext, PREF_CB_AUTO_MSG)) {
            @Override
            public void load() {
                loadData();
            }

            @Override
            public void onSend(final CallMsg item) {
                if(!TextUtils.isEmpty(mSndNumber)) {
                    mMsgForSending = item;
                    prepareSending();
                }
            }

            @Override
            public void onUpdate(int position, long regdate) {
                mAdapter.check(position);
                Utils.PutSharedPreference(mContext, PREF_CB_AUTO_MSG, regdate);
            }

            @Override
            public void onDel(final CallMsg item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CBMChoiceListActivity.this);
                builder.setCancelable(false);

                builder.setTitle(getString(R.string.msg_delete));
                builder.setMessage(getString(R.string.q_msg_delete));

                builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase.getInstance(mContext).getCallMsgDao().delete(item);
                                Message m = handler.obtainMessage();
                                m.what = 100;
                                m.obj = item;
                                handler.sendMessage(m);
                            }
                        }).start();
                        dialog.dismiss();
                    }
                });

                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        dialog.dismiss();
                    }
                });

                if (CBMChoiceListActivity.this.isFinishing() == false) {
                    builder.show();
                }
            }
        };

        mRv.setAdapter(mAdapter);
        mAdapter.load();
    }

    private void showFinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CBMChoiceListActivity.this);
        builder.setCancelable(false);

        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(getString(R.string.sended_msg));

        builder.setPositiveButton(getString(R.string.confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.dismiss();
                finish();
            }
        });

        if (CBMChoiceListActivity.this.isFinishing() == false) {
            builder.show();
        }
    }

    private void loadData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mMsgs = AppDatabase
                        .getInstance(mContext)
                        .getCallMsgDao()
                        .getAll();

                mAdapter.add(mMsgs);
                if(mMsgs.size() == 0) {
                    mLayoutMenu.setVisibility(View.VISIBLE);
                }
            }
        }).start();

        if (mSrl.isRefreshing()) {
            mSrl.setRefreshing(false);
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    mAdapter.remove((CallMsg) msg.obj);
                    break;

                case HANDLER_SEND:
                    showFinDialog();
                    break;
            }
        }
    };


    private void prepareSending() {
        mProgressDialog.show();

        mSettings = Settings.get(getApplicationContext());
        com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
        sendSettings.setMmsc(mSettings.getMmsc());
        sendSettings.setProxy(mSettings.getMmsProxy());
        sendSettings.setPort(mSettings.getMmsPort());
        sendSettings.setUseSystemSending(true);
        mSendTransaction = new Transaction(getApplicationContext(), sendSettings);

        ApnUtils.initDefaultApns(getApplicationContext(), new ApnUtils.OnApnFinishedListener() {
            @Override
            public void onFinished() {
                mSettings = Settings.get(getApplicationContext(), true);
                com.klinker.android.send_message.Settings sendSettings = new com.klinker.android.send_message.Settings();
                sendSettings.setMmsc(mSettings.getMmsc());
                sendSettings.setProxy(mSettings.getMmsProxy());
                sendSettings.setPort(mSettings.getMmsPort());
                sendSettings.setUseSystemSending(true);

                mSendTransaction = new Transaction(getApplicationContext(), sendSettings);

                new SendMsgTask().execute();
            }
        });
    }

    private Bitmap resize(Context context, String path, int resize) {
        Bitmap resizeBitmap = null;

        Uri uri = Uri.parse("file://" + path);

        BitmapFactory.Options options = new BitmapFactory.Options();
        try {
            BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); // 1번

            int width = options.outWidth;
            int height = options.outHeight;
            int samplesize = 1;

            while (true) {//2번
                if (width / 2 < resize || height / 2 < resize)
                    break;
                width /= 2;
                height /= 2;
                samplesize *= 2;
            }

            options.inSampleSize = samplesize;
            Bitmap bitmap = BitmapFactory.decodeStream(context.getContentResolver().openInputStream(uri), null, options); //3번
            resizeBitmap=bitmap;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return resizeBitmap;
    }

    private class SendMsgTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            Utils.Log("SendMsgTask 1");
            Utils.Log("SendMsgTask " + mMsgForSending.imgpath);
            Utils.Log("SendMsgTask " + mMsgForSending.title);
            Utils.Log("SendMsgTask " + mMsgForSending.contents);

            com.klinker.android.send_message.Message msg = new com.klinker.android.send_message.Message();
            if (!TextUtils.isEmpty(mMsgForSending.imgpath)) {
                msg.setImage(resize(mContext, mMsgForSending.imgpath, 640));
            }
            msg.setSubject(StringEscapeUtils.unescapeHtml4(mMsgForSending.title).replace("\\", ""));
            msg.setText(StringEscapeUtils.unescapeHtml4(mMsgForSending.contents).replace("\\", ""));
            msg.setAddress(mSndNumber);
            msg.setSave(false);

            mSendTransaction.sendNewMessage(msg, Transaction.NO_THREAD_ID);
            Utils.Log("SendMsgTask 2");

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            mProgressDialog.dismiss();

            Message msg = new Message();
            msg.what = HANDLER_SEND;
            handler.sendMessage(msg);
        }
    }

    private String getDisplayName(String tel) {
        String name = "";

        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(tel));
        String[] projection = new String[] { ContactsContract.PhoneLookup.DISPLAY_NAME };

        Cursor cursor = getBaseContext().getContentResolver().query(uri, projection, null, null, null);
        if(cursor != null) {
            if(cursor.moveToFirst()) {
                name = cursor.getString(0);
            }
            cursor.close();
        }

        return name;
    }
}
