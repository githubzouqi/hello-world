package com.example.pc2.carmapproject.utils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.example.pc2.carmapproject.R;

/**
 *  fragment 工具类
 */
public class FragmentUtil {

    /**
     * fragment之间的跳转
     * @param fragmentActivity
     * @param f_current 当前fragment
     * @param f_next 下一个fragment
     */
    public static void showFragment(FragmentActivity fragmentActivity, Fragment f_current, Fragment f_next) {

        FragmentTransaction transaction = fragmentActivity.getSupportFragmentManager().beginTransaction();
        if(f_next.isAdded()){
            transaction.hide(f_current)
                    .show(f_next)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_NONE)
                    .commitAllowingStateLoss();
        }else {
            transaction.hide(f_current)
                    .add(R.id.frame_main_content, f_next)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_NONE)
                    .commitAllowingStateLoss();
        }

    }

}
