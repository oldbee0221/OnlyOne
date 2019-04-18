package mms5.onepagebook.com.onlyonesms.base;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import mms5.onepagebook.com.onlyonesms.OnlyOneApplication;
import mms5.onepagebook.com.onlyonesms.common.Constants;

public abstract class BaseFragment extends Fragment implements Constants {
    public Context mContext;
    public OnlyOneApplication mApplication;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getContext();
        mApplication = (OnlyOneApplication) getActivity().getApplication();
    }


}
