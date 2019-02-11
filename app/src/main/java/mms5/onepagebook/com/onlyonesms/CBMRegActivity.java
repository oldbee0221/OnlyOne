package mms5.onepagebook.com.onlyonesms;

import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.util.Utils;

public class CBMRegActivity extends AppCompatActivity implements Constants, View.OnClickListener,
        TimePickerDialog.OnTimeSetListener, CompoundButton.OnCheckedChangeListener {

    private final int START_TIME = 1000;
    private final int END_TIME = 1001;

    private boolean[] days_week = new boolean[7];
    private boolean mAllDay = false;
    private int mWhichTime = START_TIME;
    private int mHourS = 0, mMinS = 0, mHourE = 0, mMinE = 0;

    private TextView[] tv_days_week = new TextView[7];
    private View[] v_days_week = new View[7];
    private TextView tv_start, tv_end;
    private CheckBox cb_send_abs, cb_all_day;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_cbm_reg);

        for(int i=0; i<7; i++) {
            days_week[i] = false;
        }

        findViewById(R.id.ll_monday).setOnClickListener(this);
        findViewById(R.id.ll_tuesday).setOnClickListener(this);
        findViewById(R.id.ll_wednesday).setOnClickListener(this);
        findViewById(R.id.ll_thursday).setOnClickListener(this);
        findViewById(R.id.ll_friday).setOnClickListener(this);
        findViewById(R.id.ll_sunday).setOnClickListener(this);
        findViewById(R.id.ll_saturday).setOnClickListener(this);

        tv_days_week[0] = findViewById(R.id.tv_monday);
        tv_days_week[1] = findViewById(R.id.tv_tuesday);
        tv_days_week[2] = findViewById(R.id.tv_wednesday);
        tv_days_week[3] = findViewById(R.id.tv_thursday);
        tv_days_week[4] = findViewById(R.id.tv_friday);
        tv_days_week[5] = findViewById(R.id.tv_saturday);
        tv_days_week[6] = findViewById(R.id.tv_sunday);

        v_days_week[0] = findViewById(R.id.v_monday);
        v_days_week[1] = findViewById(R.id.v_tuesday);
        v_days_week[2] = findViewById(R.id.v_wednesday);
        v_days_week[3] = findViewById(R.id.v_thursday);
        v_days_week[4] = findViewById(R.id.v_friday);
        v_days_week[5] = findViewById(R.id.v_saturday);
        v_days_week[6] = findViewById(R.id.v_sunday);

        tv_start = findViewById(R.id.tv_start);
        tv_end = findViewById(R.id.tv_end);
        tv_start.setOnClickListener(this);
        tv_end.setOnClickListener(this);
        tv_start.setText((mHourS < 10 ? "0" : "") + mHourS + ":" + (mMinS < 10 ? "0" : "") + mMinS);
        tv_end.setText((mHourE < 10 ? "0" : "") + mHourE + ":" + (mMinE < 10 ? "0" : "") + mMinE);

        cb_send_abs = findViewById(R.id.cb_send_abs);
        cb_all_day = findViewById(R.id.cb_all_day);
        cb_send_abs.setOnCheckedChangeListener(this);
        cb_all_day.setOnCheckedChangeListener(this);
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch(vid) {
            case R.id.ll_monday:
                setDaysWeek(0);
                break;

            case R.id.ll_tuesday:
                setDaysWeek(1);
                break;

            case R.id.ll_wednesday:
                setDaysWeek(2);
                break;

            case R.id.ll_thursday:
                setDaysWeek(3);
                break;

            case R.id.ll_friday:
                setDaysWeek(4);
                break;

            case R.id.ll_saturday:
                setDaysWeek(5);
                break;

            case R.id.ll_sunday:
                setDaysWeek(6);
                break;

            case R.id.tv_start:
                if(!mAllDay) {
                    mWhichTime = START_TIME;
                    timePickerDlg();
                }
                break;

            case R.id.tv_end:
                if(!mAllDay) {
                    mWhichTime = END_TIME;
                    timePickerDlg();
                }
                break;

        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int h, int m) {
        if(mWhichTime == START_TIME) {
            mHourS = h;
            mMinS = m;
            tv_start.setText((mHourS < 10 ? "0" : "") + mHourS + ":" + (mMinS < 10 ? "0" : "") + mMinS);
        } else if(mWhichTime == END_TIME) {
            mHourE = h;
            mMinE = m;
            tv_end.setText((mHourE < 10 ? "0" : "") + mHourE + ":" + (mMinE < 10 ? "0" : "") + mMinE);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        int vid = compoundButton.getId();

        switch(vid) {
            case R.id.cb_all_day:
                mAllDay = b;

                if(mAllDay) {
                    tv_start.setTextColor(Color.parseColor("#66000000"));
                    tv_end.setTextColor(Color.parseColor("#66000000"));
                } else {
                    tv_start.setTextColor(Color.parseColor("#616161"));
                    tv_end.setTextColor(Color.parseColor("#616161"));
                }
                break;

            case R.id.cb_send_abs:
                break;
        }
    }

    private void setDaysWeek(int idx) {
        if(days_week[idx]) {
            days_week[idx] = false;
            tv_days_week[idx].setTextColor(Color.parseColor("#818181"));
            v_days_week[idx].setBackgroundColor(Color.parseColor("#818181"));
        } else {
            days_week[idx] = true;
            tv_days_week[idx].setTextColor(Color.parseColor("#eb3333"));
            v_days_week[idx].setBackgroundColor(Color.parseColor("#eb3333"));
        }
    }

    private void timePickerDlg() {
        int hour = 0;
        int min = 0;

        if(mWhichTime == START_TIME) {
            hour = mHourS;
            min = mMinS;
        } else if(mWhichTime == END_TIME) {
            hour = mHourE;
            min = mMinE;
        }

        TimePickerDialog pktmDlg = new TimePickerDialog(CBMRegActivity.this, this, hour, min, false);
        pktmDlg.show();
    }
}
