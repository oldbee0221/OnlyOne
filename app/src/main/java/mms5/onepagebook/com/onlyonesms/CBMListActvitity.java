package mms5.onepagebook.com.onlyonesms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.BitmapFactory;
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

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.klinker.android.send_message.ApnUtils;
import com.klinker.android.send_message.Transaction;

import org.apache.commons.text.StringEscapeUtils;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.adapter.CallMsgAdapter;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.CallMsg;
import mms5.onepagebook.com.onlyonesms.util.Settings;
import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2019-07-11.
 */
public class CBMListActvitity extends AppCompatActivity implements Constants, View.OnClickListener {
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

        if(TextUtils.isEmpty(getIntent().getStringExtra(EXTRA_FROM_DOOR))) {
            findViewById(R.id.btn_cancel).setVisibility(View.VISIBLE);
            mIsFromMsg = true;
        } else {
            mIsFromMsg = false;
        }
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
                mMsgForSending = item;
                prepareSending();
            }

            @Override
            public void onUpdate(CallMsg item) {
                mMsgForSending = item;
                prepareSending();
                /*if(mIsFromMsg) {
                    Intent i = new Intent(CBMListActvitity.this, CBMUpdate2Activity.class);
                    i.putExtra("data", item);
                    startActivity(i);
                } else {
                    Intent i = new Intent(CBMListActvitity.this, CBMUpdateActivity.class);
                    i.putExtra("data", item);
                    startActivity(i);
                }*/
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
            }
        }
    };


    private void prepareSending() {
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

                Utils.Log("SendMsgTask 1");
                Utils.Log("SendMsgTask " + mMsgForSending.imgpath);
                Utils.Log("SendMsgTask " + mMsgForSending.title);
                Utils.Log("SendMsgTask " + mMsgForSending.contents);

                com.klinker.android.send_message.Message msg = new com.klinker.android.send_message.Message();
                if (!TextUtils.isEmpty(mMsgForSending.imgpath)) {
                    msg.setImage(BitmapFactory.decodeFile(mMsgForSending.imgpath));
                }
                msg.setSubject(StringEscapeUtils.unescapeHtml4(mMsgForSending.title).replace("\\", ""));
                msg.setText(StringEscapeUtils.unescapeHtml4(mMsgForSending.contents).replace("\\", ""));
                msg.setAddress("01081433749");
                msg.setSave(false);

                mSendTransaction.sendNewMessage(msg, Transaction.NO_THREAD_ID);
                Utils.Log("SendMsgTask 2");

                //new SendMsgTask().execute();
            }
        });
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
                msg.setImage(BitmapFactory.decodeFile(mMsgForSending.imgpath));
            }
            msg.setSubject(StringEscapeUtils.unescapeHtml4(mMsgForSending.title).replace("\\", ""));
            msg.setText(StringEscapeUtils.unescapeHtml4(mMsgForSending.contents).replace("\\", ""));
            msg.setAddress("01081433749");
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
        }
    }
}
