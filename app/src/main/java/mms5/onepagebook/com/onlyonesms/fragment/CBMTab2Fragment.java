package mms5.onepagebook.com.onlyonesms.fragment;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import mms5.onepagebook.com.onlyonesms.CBMRegActivity;
import mms5.onepagebook.com.onlyonesms.R;
import mms5.onepagebook.com.onlyonesms.base.BaseFragment;
import mms5.onepagebook.com.onlyonesms.base.GlideApp;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.db.AppDatabase;
import mms5.onepagebook.com.onlyonesms.db.entity.Msg;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class CBMTab2Fragment extends BaseFragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
    private LinearLayout ll_no_msg;
    private LinearLayout ll_msg;
    private Button btn_reg;

    private ImageView iv_icon, iv_photo;
    private TextView tv_msg_type, tv_week_day, tv_time, tv_type_settings, tv_no_image, tv_msg1, tv_msg2;
    private Bitmap mBmPhoto;
    private Switch swt_msg_use;

    private Msg dMsg;

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

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if(b) {

        } else {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AppDatabase.getInstance(mContext).getMsgDao().updateUseYnByUpdateTime(dMsg.lastUpdateTime, "N");

                    dMsg = AppDatabase
                            .getInstance(mContext)
                            .getMsgDao()
                            .findByTypeOnUse("부재중");

                    if(dMsg == null) {
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

        iv_icon = view.findViewById(R.id.iv_icon);
        iv_photo = view.findViewById(R.id.iv_photo);

        tv_msg_type = view.findViewById(R.id.tv_msg_type);
        tv_week_day = view.findViewById(R.id.tv_week_day);
        tv_time = view.findViewById(R.id.tv_time);
        tv_type_settings = view.findViewById(R.id.tv_type_settings);
        tv_no_image = view.findViewById(R.id.tv_no_image);
        tv_msg1 = view.findViewById(R.id.tv_msg1);
        tv_msg2 = view.findViewById(R.id.tv_msg2);

        swt_msg_use = view.findViewById(R.id.swt_msg_use);
        swt_msg_use.setChecked(true);
        swt_msg_use.setOnCheckedChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        new Thread(new Runnable() {
            @Override
            public void run() {
                dMsg = AppDatabase
                        .getInstance(mContext)
                        .getMsgDao()
                        .findByTypeOnUse("부재중");

                if(dMsg == null) {
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

                    tv_msg1.setText(dMsg.message1);
                    tv_msg2.setText(dMsg.message2);

                    if(dMsg.allDayYn.equalsIgnoreCase("Y")) {
                        tv_time.setText(getString(R.string.all_day));
                    } else {
                        StringBuffer sb = new StringBuffer();
                        sb.append(dMsg.startTime).append(" ~ ").append(dMsg.endTime);
                        tv_time.setText(sb.toString());
                    }

                    tv_week_day.setText(dMsg.dayOfWeek);

                    if(Utils.IsEmpty(dMsg.imgPath)) {
                        tv_no_image.setVisibility(View.VISIBLE);
                    } else {
                        tv_no_image.setVisibility(View.GONE);

                        iv_photo.setVisibility(View.VISIBLE);
                        mBmPhoto = BitmapFactory.decodeFile(dMsg.imgPath);
                        GlideApp.with(getContext())
                                .load(mBmPhoto)
                                .into(iv_photo);
                    }
                    break;
            }
        }
    };
}
