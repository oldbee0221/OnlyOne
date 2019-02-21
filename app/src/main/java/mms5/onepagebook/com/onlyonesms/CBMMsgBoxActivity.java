package mms5.onepagebook.com.onlyonesms;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.adapter.MsgAdapter;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class CBMMsgBoxActivity extends AppCompatActivity implements Constants, View.OnClickListener {
    private String mMsgType;
    private Context mContext;

    private SwipeRefreshLayout mSrl;
    private RecyclerView mRv;
    private LinearLayoutManager mLayoutManager;
    private MsgAdapter mAdapter;

    private List<Msg> mMsgs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_cbm_msg_box);

        mContext = getApplicationContext();

        mMsgType = getIntent().getStringExtra(Constants.EXTRA_CB_MSGTYPE);

        mSrl = findViewById(R.id.srl_base);
        mSrl.setOnRefreshListener(onRefresh);
        mRv = findViewById(R.id.rv_base);
        mLayoutManager = new LinearLayoutManager(mContext);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    @Override
    public void onClick(View view) {

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
        if(mAdapter != null) {
            mAdapter.removeAll();
        }

        mRv.setHasFixedSize(true);
        mRv.setLayoutManager(mLayoutManager);
        mAdapter = new MsgAdapter(mContext) {
            @Override
            public void load() {
                loadData();
            }

            @Override
            public void onAdoption(final Msg item) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        AppDatabase.getInstance(mContext).getMsgDao().updateUseYnYtoN(mMsgType);
                        AppDatabase.getInstance(mContext).getMsgDao().updateUseYnByUpdateTime(item.lastUpdateTime, "Y");

                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                }).start();
            }

            @Override
            public void onDel(final Msg item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CBMMsgBoxActivity.this);
                builder.setCancelable(false);

                builder.setTitle(getString(R.string.msg_delete));
                builder.setMessage(getString(R.string.q_msg_delete));

                builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase.getInstance(mContext).getMsgDao().delete(item);
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

                if(CBMMsgBoxActivity.this.isFinishing() == false) {
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
                        .getMsgDao()
                        .findByType(mMsgType);

                mAdapter.add(mMsgs);
            }
        }).start();

        if (mSrl.isRefreshing()) {
            mSrl.setRefreshing(false);
        }
    }

    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 100:
                    mAdapter.remove((Msg)msg.obj);
                    break;
            }
        }
    };
}
