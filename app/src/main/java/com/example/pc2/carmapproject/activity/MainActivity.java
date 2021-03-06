package com.example.pc2.carmapproject.activity;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;

import com.example.pc2.carmapproject.R;
import com.example.pc2.carmapproject.fragment.AddressConfigFragment;
import com.example.pc2.carmapproject.utils.LogUtil;
import com.example.pc2.carmapproject.utils.ToastUtil;


public class MainActivity extends FragmentActivity {

//    private BoxFragment boxFragment = null;
//    private String rootFragmentTag = "BoxFragment";
    private AddressConfigFragment addressConfigFragment = null;
    private String rootFragmentTag = "AddressConfigFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setViews();//控件初始化
//        ScreenUtil.selectScreentDirection(MainActivity.this);// 根据设备类型来确定横竖屏显示方式

//        if (Build.VERSION.SDK_INT >= 19) {
//            View decorView = getWindow().getDecorView();
//            decorView.setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            | View.SYSTEM_UI_FLAG_FULLSCREEN
//                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

//            getWindow().setStatusBarColor(Color.TRANSPARENT);
//        }
//        getSupportActionBar().hide();// 隐藏ActionBar
        // 如果通过tag没找到根fragment，那么就加载；如果找到了，不再加载，防止重影
        if(getSupportFragmentManager().findFragmentByTag(rootFragmentTag) == null){
            addFragment();// 添加主界面fragment
        }

    }

    private void setViews() {
//        boxFragment = new BoxFragment();// 新建方格图主界面
        addressConfigFragment = new AddressConfigFragment();// 配置界面
    }

    private void addFragment() {
        // 创建FragmentManager对象
        FragmentManager manager =  getSupportFragmentManager();
        // 获取FragmentTransaction对象
        FragmentTransaction transaction = manager.beginTransaction();
//        if (boxFragment != null){
//             fragment对象不为空，就添加到容器中，提交事务完成添加，并给fragment添加tag
//            transaction.add(R.id.frame_main_content, boxFragment, rootFragmentTag)
//                    .commit();
//        }
        if (addressConfigFragment != null){
            // fragment对象不为空，就添加到容器中，提交事务完成添加，并给fragment添加tag
            transaction.add(R.id.frame_main_content, addressConfigFragment, rootFragmentTag)
                    .commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private FragmentManager manager = getSupportFragmentManager();

    private long firstTime;// 记录点击返回时第一次的时间毫秒值

    /**
     * 重写该方法，判断用户按下返回按键的时候，执行退出应用方法
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){// 点击了返回按键
            if(manager.getBackStackEntryCount() != 0){
                manager.popBackStack();
            }else {
                exitApp(2000);// 退出应用
            }
            return true;// 返回true，防止该事件继续向下传播
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 退出应用
     * @param timeInterval 设置第二次点击退出的时间间隔
     */
    private void exitApp(long timeInterval) {
        // 第一次肯定会进入到if判断里面，然后把firstTime重新赋值当前的系统时间
        // 然后点击第二次的时候，当点击间隔时间小于2s，那么退出应用；反之不退出应用
        if(System.currentTimeMillis() - firstTime >= timeInterval){
            ToastUtil.showToast(this, "再按一次退出程序");
            firstTime = System.currentTimeMillis();
        }else {
            finish();// 销毁当前activity
            System.exit(0);// 完全退出应用
        }
    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        System.exit(0);// 终止当前正在运行的Java虚拟机，完全退出app，终止所有状态
    }*/
}
