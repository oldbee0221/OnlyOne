package mms5.onepagebook.com.onlyonesms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;

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
        setContentView(R.layout.activity_cbm_door2);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = getApplicationContext();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        findViewById(R.id.btn_cb_reg).setOnClickListener(this);

        mCheckBox1 = findViewById(R.id.check1);
        mCheckBox2 = findViewById(R.id.check2);
        mCheckBox3 = findViewById(R.id.check3);

        mCheckBox1.setOnCheckedChangeListener(this);
        mCheckBox2.setOnCheckedChangeListener(this);
        mCheckBox3.setOnCheckedChangeListener(this);

        mCheckBox1.setChecked(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK1));
        mCheckBox2.setChecked(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK2));
        mCheckBox3.setChecked(Utils.GetBooleanSharedPreference(mContext, PREF_CHECK3));
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch (vid) {
            case R.id.btn_cb_reg: {
                Intent i = new Intent(CBMDoor2Activity.this, CBMListActvitity.class);
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
                break;

            case R.id.check3:
                Utils.PutSharedPreference(mContext, PREF_CHECK3, isChecked);
                break;
        }
    }
}

