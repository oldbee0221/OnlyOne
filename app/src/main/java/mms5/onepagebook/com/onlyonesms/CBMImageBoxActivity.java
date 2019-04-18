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
import mms5.onepagebook.com.onlyonesms.adapter.ImgAdapter;
import mms5.onepagebook.com.onlyonesms.adapter.MsgAdapter;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.ImageBox;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;

public class CBMImageBoxActivity extends AppCompatActivity implements Constants, View.OnClickListener {
    private Context mContext;

    private SwipeRefreshLayout mSrl;
    private RecyclerView mRv;
    private LinearLayoutManager mLayoutManager;
    private ImgAdapter mAdapter;

    private List<ImageBox> mImgs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_cbm_image_box);

        mContext = getApplicationContext();

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
        if (mAdapter != null) {
            mAdapter.removeAll();
        }

        mRv.setHasFixedSize(true);
        mRv.setLayoutManager(mLayoutManager);
        mAdapter = new ImgAdapter(mContext) {
            @Override
            public void load() {
                loadData();
            }

            @Override
            public void onAdoption(final ImageBox item) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(EXTRA_IMG_PATH, item.imgPath);
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                }).start();
            }

            @Override
            public void onDel(final ImageBox item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CBMImageBoxActivity.this);
                builder.setCancelable(false);

                builder.setTitle(getString(R.string.img_delete));
                builder.setMessage(getString(R.string.q_img_delete));

                builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
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

                if (CBMImageBoxActivity.this.isFinishing() == false) {
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
                mImgs = AppDatabase
                        .getInstance(mContext)
                        .getImageBoxDao()
                        .getAll();

                mAdapter.add(mImgs);
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
                    mAdapter.remove((ImageBox) msg.obj);
                    break;
            }
        }
    };
}
