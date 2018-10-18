package com.hlzx.ljdjsj.fragment;

import android.support.v4.app.Fragment;
import android.widget.Toast;

/**
 * Created by alan on 2015/12/14.
 */
public abstract class BaseFragment extends Fragment {
    public void showToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }
}
