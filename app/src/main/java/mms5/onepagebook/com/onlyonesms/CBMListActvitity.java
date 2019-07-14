package mms5.onepagebook.com.onlyonesms;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import java.util.List;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.adapter.CallMsgAdapter;
import mms5.onepagebook.com.onlyonesms.adapter.MsgAdapter;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.CallMsg;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;

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
        mAdapter = new CallMsgAdapter(mContext) {
            @Override
            public void load() {
                loadData();
            }

            @Override
            public void onAdoption(final CallMsg item) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                    }
                }).start();
            }

            @Override
            public void onUpdate(CallMsg item) {
                Intent i = new Intent(CBMListActvitity.this, CBMUpdateActivity.class);
                i.putExtra("data", item);
                startActivity(i);
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
}
