package com.example.pc2.carmapproject.activity;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telecom.Call;
import android.view.KeyEvent;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.pc2.carmapproject.R;
import com.example.pc2.carmapproject.utils.LogUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.BreakIterator;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 启动页面
 */
public class SplashActivity extends Activity {


    @BindView(R.id.linear_page1_webiew)LinearLayout linear_page1_webiew;
    @BindView(R.id.tv_mushiny)TextView tv_mushiny;
    private static final int GO_HOME = 0x11;// 跳转到主页面标识
    private static final int DELAY = 2000;// 延时时间


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_splash);// 设置该activity使用的布局文件
        ButterKnife.bind(this);

        // 设置启动页面显示的标语内容
        int year = Calendar.getInstance().get(Calendar.YEAR);
//        2017-2018 MUSHINY
        String logo = getResources().getString(R.string.str_copyright)
                + "2017-" + year + " MUSHINY";
        tv_mushiny.setText(logo);

        handler.sendEmptyMessageDelayed(GO_HOME, DELAY);// 实现界面延时
    }

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case GO_HOME:// 跳转到主界面
                    goHome();
                    break;
            }
        }
    };

    /**
     * 主界面跳转
     */
    public void goHome(){
        startActivity(new Intent(SplashActivity.this, MainActivity.class));// 界面跳转
        finish();// 销毁当前activity
    }

    /**
     * 按键事件
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK){// 点击了返回按键
            // 屏蔽返回按键
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
