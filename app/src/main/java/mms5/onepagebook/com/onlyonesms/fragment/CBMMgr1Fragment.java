package mms5.onepagebook.com.onlyonesms.fragment;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.adapter.TelNumBlockAdapter;
import mms5.onepagebook.com.onlyonesms.base.BaseFragment;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.TelNumBlock;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class CBMMgr1Fragment extends BaseFragment implements View.OnClickListener {
    private SwipeRefreshLayout mSrl;
    private RecyclerView mRv;
    private LinearLayoutManager mLayoutManager;
    private TelNumBlockAdapter mAdapter;

    private List<TelNumBlock> mNums;

    public static CBMMgr1Fragment create() {
        CBMMgr1Fragment fragment = new CBMMgr1Fragment();
        return fragment;
    }

    @Override
    public void onClick(View view) {

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cbm_mgr, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSrl = view.findViewById(R.id.srl_base);
        mSrl.setOnRefreshListener(onRefresh);
        mRv = view.findViewById(R.id.rv_base);
        mLayoutManager = new LinearLayoutManager(mContext);
    }

    @Override
    public void onResume() {
        super.onResume();
        init();
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
        mAdapter = new TelNumBlockAdapter(mContext) {
            @Override
            public void load() {
                loadData();
            }

            @Override
            public void onDel(final TelNumBlock item) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setCancelable(false);

                builder.setTitle(getString(R.string.telnum_delete));
                builder.setMessage(getString(R.string.q_telnum_delete));

                builder.setPositiveButton(getString(R.string.delete), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int arg1) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                AppDatabase.getInstance(mContext).getTelNumBlockDao().delete(item);
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

                if (getActivity().isFinishing() == false) {
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
                mNums = AppDatabase
                        .getInstance(mContext)
                        .getTelNumBlockDao()
                        .getAll();

                mAdapter.add(mNums);
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
                    mAdapter.remove((TelNumBlock) msg.obj);
                    break;
            }
        }
    };
}
