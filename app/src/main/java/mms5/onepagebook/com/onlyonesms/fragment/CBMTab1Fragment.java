package mms5.onepagebook.com.onlyonesms.fragment;

import android.content.Intent;
import android.os.Bundle;
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
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class CBMTab1Fragment extends BaseFragment implements View.OnClickListener {
    private LinearLayout ll_no_msg;
    private LinearLayout ll_msg;
    private Button btn_reg;

    public static CBMTab1Fragment create() {
        CBMTab1Fragment fragment = new CBMTab1Fragment();
        return fragment;
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch(vid) {
            case R.id.btn_reg:
                {
                    Intent intent = new Intent(getActivity(), CBMRegActivity.class);
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
                        .findByTypeOnUse("수신");

                if(msg == null) {
                    Utils.Log("msg is null!");
                    ll_msg.setVisibility(View.GONE);
                    ll_no_msg.setVisibility(View.VISIBLE);
                } else {
                    Utils.Log("msg is NOT null!");
                    ll_msg.setVisibility(View.VISIBLE);
                    ll_no_msg.setVisibility(View.GONE);
                }
            }
        }).start();
    }
}
