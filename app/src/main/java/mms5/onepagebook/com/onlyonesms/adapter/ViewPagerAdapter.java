package mms5.onepagebook.com.onlyonesms.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import mms5.onepagebook.com.onlyonesms.base.BaseFragment;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<BaseFragment> mFragments;

    public ViewPagerAdapter(FragmentManager fragmentManager, ArrayList<BaseFragment> fragments) {
        super(fragmentManager);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }
}
