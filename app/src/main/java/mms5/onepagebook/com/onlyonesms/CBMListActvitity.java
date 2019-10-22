package mms5.onepagebook.com.onlyonesms;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Transaction;

import org.apache.commons.text.StringEscapeUtils;

import java.io.FileNotFoundException;
import java.util.List;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.adapter.CallMsgAdapter;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.CallMsg;
import mms5.onepagebook.com.onlyonesms.dialog.ProgressDialog;
import mms5.onepagebook.com.onlyonesms.util.Settings;
import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2019-07-11.
 */
public class CBMListActvitity extends AppCompatActivity implements Constants, View.OnClickListener {
    private final int HANDLER_SEND = 301;
    private final int REQ_UPDATE = 500;

    private Context mContext;

    private SwipeRefreshLayout mSrl;
    private RecyclerView mRv;
    private LinearLayoutManager mLayoutManager;
    private CallMsgAdapter mAdapter;

    private List<CallMsg> mMsgs;
    private boolean mIsFromMsg;

    private Transaction mSendTransaction;
    private Settings mSettings;
    private CallMsg mMsgForSending;
    private String mSndNumber;

    private ProgressDialog mProgressDialog;

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
        setContentView(R.layout.activity_cbm_list);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = getApplicationContext();

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
        findViewById(R.id.iv_back).setOnClickListener(this);
        findViewById(R.id.iv_clear).setOnClickListener(this);

        Intent intent = getIntent();

        if(TextUtils.isEmpty(intent.getStringExtra(EXTRA_FROM_DOOR))) {
            findViewById(R.id.btn_cancel).setVisibility(View.VISIBLE);
            mIsFromMsg = true;
            mSndNumber = intent.getStringExtra(EXTRA_SND_NUM);
            Utils.Log("CBMListActivity mSndNumber => " + mSndNumber);
        } else {
            mIsFromMsg = false;
            mSndNumber = "";
        }

        mProgressDialog = new ProgressDialog(CBMListActvitity.this);
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
                Intent i = new Intent(CBMListActvitity.this, CBMReg2Activity.class);
                startActivity(i);
            }
            break;

            case R.id.btn_cancel:
                finish();
                break;

            case R.id.iv_back:
                finish();
                break;

            case R.id.iv_clear:
                finishAffinity();
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

    private void init() {
        if (mAdapter != null) {
            mAdapter.removeAll();
        }

        mRv.setHasFixedSize(true);
        mRv.setLayoutManager(mLayoutManager);
        mAdapter = new CallMsgAdapter(mContext, mIsFromMsg) {
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
            public void onUpdate(CallMsg item) {
                if(mIsFromMsg) {
                    Intent i = new Intent(CBMListActvitity.this, CBMUpdate2Activity.class);
                    i.putExtra(EXTRA_SND_NUM, mSndNumber);
                    i.putExtra("data", item);
                    startActivityForResult(i, REQ_UPDATE);
                } else {
                    Intent i = new Intent(CBMListActvitity.this, CBMUpdateActivity.class);
                    i.putExtra("data", item);
                    startActivity(i);
                }
            }

            @Override
            public void onDel(final CallMsg item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CBMListActvitity.this);
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

                if (CBMListActvitity.this.isFinishing() == false) {
                    builder.show();
                }
            }
        };

        mRv.setAdapter(mAdapter);
        mAdapter.load();
    }

    private void showFinDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(CBMListActvitity.this);
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

        if (CBMListActvitity.this.isFinishing() == false) {
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
}