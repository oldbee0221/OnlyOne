package mms5.onepagebook.com.onlyonesms;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import java.util.ArrayList;

import io.fabric.sdk.android.Fabric;
import mms5.onepagebook.com.onlyonesms.adapter.ViewPagerAdapter;
import mms5.onepagebook.com.onlyonesms.base.BaseFragment;
import mms5.onepagebook.com.onlyonesms.common.Constants;
import mms5.onepagebook.com.onlyonesms.common.ZoomOutPageTransformer;
import mms5.onepagebook.com.onlyonesms.fragment.CBMTab1Fragment;
import mms5.onepagebook.com.onlyonesms.fragment.CBMTab2Fragment;
import mms5.onepagebook.com.onlyonesms.fragment.CBMTab3Fragment;

public class CBMMainActivity extends AppCompatActivity implements Constants, View.OnClickListener {
    private Context mContext;

    private ArrayList<BaseFragment> mFragments;
    private TextView[] mTvTab;
    private View[] mVTab;

    private int mMainTab;

    private ViewPager mViewPager;
    private ViewPagerAdapter mViewPagerAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Answers(), new Crashlytics());
        setContentView(R.layout.activity_cbm_main);

        mContext = getApplicationContext();

        findViewById(R.id.rl_main_tab_1).setOnClickListener(this);
        findViewById(R.id.rl_main_tab_2).setOnClickListener(this);
        findViewById(R.id.rl_main_tab_3).setOnClickListener(this);

        mMainTab = NOTHING;

        mTvTab = new TextView[3];
        mTvTab[0] = findViewById(R.id.tv_main_tab_1);
        mTvTab[1] = findViewById(R.id.tv_main_tab_2);
        mTvTab[2] = findViewById(R.id.tv_main_tab_3);

        mVTab = new View[3];
        mVTab[0] = findViewById(R.id.v_main_tab_1);
        mVTab[1] = findViewById(R.id.v_main_tab_2);
        mVTab[2] = findViewById(R.id.v_main_tab_3);

        setMainTab(0);
        initView();
    }

    @Override
    public void onClick(View view) {
        int vid = view.getId();

        switch (vid) {
            case R.id.rl_main_tab_1:
                mViewPager.setCurrentItem(0);
                break;

            case R.id.rl_main_tab_2:
                mViewPager.setCurrentItem(1);
                break;

            case R.id.rl_main_tab_3:
                mViewPager.setCurrentItem(2);
                break;
        }
    }

    private void initView() {
        if (mFragments == null) {
            mFragments = new ArrayList<>();
            mFragments.add(CBMTab1Fragment.create());
            mFragments.add(CBMTab2Fragment.create());
            mFragments.add(CBMTab3Fragment.create());
        }

        if (mViewPager == null) {
            mViewPager = findViewById(R.id.view_pager);
        }

        mViewPager.setPageTransformer(true, new ZoomOutPageTransformer());
        if (mViewPagerAdapter == null) {
            mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(), mFragments);
        }

        mViewPager.setAdapter(mViewPagerAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setMainTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void setMainTab(int tab) {
        if (mMainTab == tab) return;

        switch (mMainTab) {
            case 0:
                mVTab[0].setVisibility(View.GONE);
                break;

            case 1:
                mVTab[1].setVisibility(View.GONE);
                break;

            case 2:
                mVTab[2].setVisibility(View.GONE);
                break;
        }

        switch (tab) {
            case 0:
                mVTab[0].setVisibility(View.VISIBLE);
                mVTab[0].setBackgroundColor(Color.rgb(0x29, 0x65, 0xA7));
                break;

            case 1:
                mVTab[1].setVisibility(View.VISIBLE);
                mVTab[1].setBackgroundColor(Color.rgb(0x29, 0x65, 0xA7));
                break;

            case 2:
                mVTab[2].setVisibility(View.VISIBLE);
                mVTab[2].setBackgroundColor(Color.rgb(0x29, 0x65, 0xA7));
                break;
        }

        mMainTab = tab;
    }
}
