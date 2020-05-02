package mms5.onepagebook.com.onlyonesms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.util.Utils;

/**
 * Created by jeonghopark on 2019-07-11.
 */
public class CBMDoor2Activity extends AppCompatActivity implements Constants, View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private Context mContext;
    private CheckBox mCheckBox1;
    private CheckBox mCheckBox2;
    private CheckBox mCheckBox3;
    private CheckBox mCheckBox4;
    private CheckBox mCheckBox5;
    private LinearLayout mLayoutMsgChoice;
    private LinearLayout mLayoutMsgChoice2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_cbm_door2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = getApplicationContext();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        findViewById(R.id.btn_cb_reg).setOnClickListener(this);
        findViewById(R.id.iv_home).setOnClickListener(this);
        findViewById(R.id.iv_menu).setOnClickListener(this);

        mCheckBox1 = findViewById(R.id.check1);
        mCheckBox2 = findViewById(R.id.check2);
        mCheckBox3 = findViewById(R.id.check3);
        mCheckBox4 = findViewById(R.id.check4);
        mCheckBox5 = findViewById(R.id.check5);
        mLayoutMsgChoice = findViewById(R.id.ll_choice_msg);
        mLayoutMsgChoice2 = findViewById(R.id.ll_choice_msg2);

        mCheckBox1.setOnCheckedChangeListener(this);
        mCheckBox2.setOnCheckedChangeListener(this);
        mCheckBox3.setOnCheckedChangeListener(this);
        mCheckBox4.setOnCheckedChangeListener(this);
        mCheckBox5.setOnCheckedChangeListener(this);
        mLayoutMsgChoice.setOnClickListener(this);
        mLayoutMsgChoice2.setOnClickListener(this);

        mCheckBox1.setChecked(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK1));
        mCheckBox2.setChecked(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK2));
        mCheckBox3.setChecked(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK3));
        mCheckBox4.setChecked(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK4));
        mCheckBox5.setChecked(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK5));

        if(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK4)) {
            mLayoutMsgChoice.setVisibility(View.VISIBLE);
        } else {
            mLayoutMsgChoice.setVisibility(View.GONE);
        }

        if(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK5)) {
            mLayoutMsgChoice2.setVisibility(View.VISIBLE);
        } else {
            mLayoutMsgChoice2.setVisibility(View.GONE);
        }
    }

    @Override
    public void onBackPressed() {
        if(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK4)) {
            long msg = Utils.GetLongSharedPreference(mContext, PREF_CB_AUTO_MSG);
            if(msg == 0) {
                Toast.makeText(getApplicationContext(), R.string.please_choice_msg, Toast.LENGTH_LONG).show();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch (vid) {
            case R.id.btn_cb_reg:
                {
                    Intent i = new Intent(CBMDoor2Activity.this, CBMListActvitity.class);
                    i.putExtra(EXTRA_FROM_DOOR, "DOOR");
                    startActivity(i);
                }
                break;

            case R.id.iv_menu:
                startActivity(new Intent(CBMDoor2Activity.this, LogActivity.class));
                break;

            case R.id.iv_home:
                if(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK4)) {
                    long msg = Utils.GetLongSharedPreference(mContext, PREF_CB_AUTO_MSG);
                    if(msg == 0) {
                        Toast.makeText(getApplicationContext(), R.string.please_choice_msg, Toast.LENGTH_LONG).show();
                    } else {
                        finish();
                    }
                } else {
                    finish();
                }
                break;

            case R.id.ll_choice_msg:
                {
                    Intent i = new Intent(CBMDoor2Activity.this, CBMChoiceListActivity.class);
                    i.putExtra(EXTRA_FROM_DOOR, "DOOR");
                    startActivity(i);
                }
                break;

            case R.id.ll_choice_msg2:
                {
                    Intent i = new Intent(CBMDoor2Activity.this, CBMChoiceList2Activity.class);
                    i.putExtra(EXTRA_FROM_DOOR, "DOOR");
                    startActivity(i);
                }
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int vid = buttonView.getId();

        switch(vid) {
            case R.id.check1:
                Utils.PutSharedPreference(mContext, PREF_CHECK1, isChecked);
                break;

            case R.id.check2:
                Utils.PutSharedPreference(mContext, PREF_CHECK2, isChecked);
                if(isChecked) {
                    Utils.PutSharedPreference(mContext, PREF_CHECK4, false);
                    mCheckBox4.setChecked(false);
                }
                break;

            case R.id.check3:
                Utils.PutSharedPreference(mContext, PREF_CHECK3, isChecked);
                break;

            case R.id.check4:
                Utils.PutSharedPreference(mContext, PREF_CHECK4, isChecked);
                if(isChecked) {
                    mLayoutMsgChoice.setVisibility(View.VISIBLE);
                    Utils.PutSharedPreference(mContext, PREF_CHECK2, false);
                    mCheckBox2.setChecked(false);
                } else {
                    mLayoutMsgChoice.setVisibility(View.GONE);
                }
                break;

            case R.id.check5:
                Utils.PutSharedPreference(mContext, PREF_CHECK5, isChecked);
                if(isChecked) {
                    mLayoutMsgChoice2.setVisibility(View.VISIBLE);
                } else {
                    mLayoutMsgChoice2.setVisibility(View.GONE);
                }
                break;
        }
    }
}

