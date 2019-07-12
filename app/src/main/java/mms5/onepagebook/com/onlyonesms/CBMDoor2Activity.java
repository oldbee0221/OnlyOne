package mms5.onepagebook.com.onlyonesms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.common.Constants;

/**
 * Created by jeonghopark on 2019-07-11.
 */
public class CBMDoor2Activity extends AppCompatActivity implements Constants, View.OnClickListener {
    private Context mContext;

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
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch (vid) {
            case R.id.btn_cb_reg: {
                Intent i = new Intent(CBMDoor2Activity.this, CBMListActvitity.class);
                startActivity(i);
            }
            break;
        }
    }
}

