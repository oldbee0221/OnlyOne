package mms5.onepagebook.com.onlyonesms.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import mms5.onepagebook.com.onlyonesms.CBMRegActivity;
import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.base.BaseFragment;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class CBMTab2Fragment extends BaseFragment implements View.OnClickListener {
    private LinearLayout ll_no_msg;
    private LinearLayout ll_msg;
    private Button btn_reg;

    public static CBMTab2Fragment create() {
        CBMTab2Fragment fragment = new CBMTab2Fragment();
        return fragment;
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch(vid) {
            case R.id.btn_reg:
            {
                Intent intent = new Intent(getActivity(), CBMRegActivity.class);
                intent.putExtra(Constants.EXTRA_CB_MSGTYPE, "부재중");
                startActivity(intent);
            }
            break;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cbm_tab, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ll_msg = view.findViewById(R.id.ll_msg);
        ll_no_msg = view.findViewById(R.id.ll_no_msg);

        btn_reg = view.findViewById(R.id.btn_reg);
        btn_reg.setOnClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Msg msg = AppDatabase
                        .getInstance(mContext)
                        .getMsgDao()
                        .findByTypeOnUse("부재중");

                if(msg == null) {
                    Utils.Log("msg is null!");
                    Message m = handler.obtainMessage();
                    m.what = 100;
                    handler.sendMessage(m);
                } else {
                    Utils.Log("msg is NOT null!");
                    Message m = handler.obtainMessage();
                    m.what = 101;
                    handler.sendMessage(m);
                }
            }
        }).start();
    }


    final Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case 100:
                    ll_msg.setVisibility(View.GONE);
                    ll_no_msg.setVisibility(View.VISIBLE);
                    break;

                case 101:
                    ll_msg.setVisibility(View.VISIBLE);
                    ll_no_msg.setVisibility(View.GONE);
                    break;
            }
        }
    };
}
