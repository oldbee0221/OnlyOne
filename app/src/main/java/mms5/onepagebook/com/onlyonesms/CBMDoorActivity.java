package mms5.onepagebook.com.onlyonesms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.manager.PreferenceManager;

public class CBMDoorActivity extends AppCompatActivity implements Constants, View.OnClickListener {
    private Context mContext;
    private RadioGroup mRgUse;
    private RadioButton mRbUse;
    private RadioButton mRbNotUse;

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
        setContentView(R.layout.activity_cbm_door);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mContext = getApplicationContext();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mRgUse = findViewById(R.id.rg_use);
        mRgUse.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
                switch(checkedId) {
                    case R.id.rb_not_use:
                        PreferenceManager.getInstance(mContext).setIsUseCBMsg(false);
                        break;

                    case R.id.rb_use:
                        PreferenceManager.getInstance(mContext).setIsUseCBMsg(true);
                        break;
                }
            }
        });

        mRbNotUse = findViewById(R.id.rb_not_use);
        mRbUse = findViewById(R.id.rb_use);

        if(PreferenceManager.getInstance(mContext).getIsUseCBMsg()) {
            mRbNotUse.setChecked(false);
            mRbUse.setChecked(true);
        } else {
            mRbNotUse.setChecked(true);
            mRbUse.setChecked(false);
        }

        findViewById(R.id.btn_cb_addr).setOnClickListener(this);
        findViewById(R.id.btn_cb_reg).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch(vid) {
            case R.id.btn_cb_addr:
            {
                Intent i = new Intent(CBMDoorActivity.this, CBMMgrNumActivity.class);
                startActivity(i);
            }
                break;

            case R.id.btn_cb_reg:
            {
                Intent i = new Intent(CBMDoorActivity.this, CBMMainActivity.class);
                startActivity(i);
            }
                break;
        }
    }
}
