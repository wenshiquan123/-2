package com.hlzx.yicheng.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.widget.RadioGroup;

import com.hlzx.yicheng.R;
import com.hlzx.yicheng.utils.Logger;

import java.util.List;

/**
 * Created by alan on 2016/3/28.
 */
public class FragmentTabAdapter implements RadioGroup.OnCheckedChangeListener {

    FragmentActivity mFragmentActivity;
    List<Fragment> mFragments;
    int mContentID;
    RadioGroup mRadioGroup;

    private int currentTab;

    public FragmentTabAdapter(FragmentActivity fragmentActivity, List<Fragment> fragments, int contentID, RadioGroup radioGroup) {
        this.mFragmentActivity = fragmentActivity;
        this.mFragments = fragments;
        this.mContentID = contentID;
        this.mRadioGroup = radioGroup;

        fragmentActivity.getSupportFragmentManager().beginTransaction().
                add(contentID, fragments.get(0)).
                add(contentID, fragments.get(1)).
                add(contentID, fragments.get(2)).
                add(contentID, fragments.get(3)).
                hide(fragments.get(1)).
                hide(fragments.get(2)).
                hide(fragments.get(3)).
                show(fragments.get(0)).
                commit();
        radioGroup.setOnCheckedChangeListener(this);

    }

    /**
     * 根据选择的fragment确定动画
     *
     * @param paramInt
     * @return
     */
    private FragmentTransaction obtainFragmentTransaction(int paramInt) {
        FragmentTransaction localFragmentTransaction = mFragmentActivity.getSupportFragmentManager().beginTransaction();
        if (paramInt > this.currentTab) {
            localFragmentTransaction.setCustomAnimations(R.anim.slide_in_right,R.anim.slide_out_left);
            return localFragmentTransaction;
        }
        localFragmentTransaction.setCustomAnimations(R.anim.slide_in_left, R.anim.slide_out_right);
        return localFragmentTransaction;
    }

    private void showTab(int paramInt) {
        FragmentTransaction localFragmentTransaction = obtainFragmentTransaction(paramInt);
        for(int i=0;i<mFragments.size();i++)
        {
            if(paramInt!=i) {
                localFragmentTransaction.hide(mFragments.get(i));
            }
        }
        localFragmentTransaction.show(mFragments.get(paramInt));
        localFragmentTransaction.commit();
    }

    public Fragment getCurrentFragment() {
        return (Fragment) mFragments.get(this.currentTab);
    }

    public int getCurrentTab() {
        return this.currentTab;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int i = 0;
        switch (checkedId) {
            case R.id.tab_one:
                i = 0;
                break;
            case R.id.tab_two:
                i = 1;
                break;
            case R.id.tab_three:
                i = 2;
                break;
            case R.id.tab_four:
                i = 3;
                break;
        }
        showTab(i);
        currentTab=i;
        Logger.e("i="+i);
    }
}
