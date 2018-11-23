package com.example.pc2.carmapproject.fragment;


import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pc2.carmapproject.R;
import com.example.pc2.carmapproject.constant.Constants;
import com.example.pc2.carmapproject.interfaces.onPodRemoveListener;
import com.example.pc2.carmapproject.utils.FragmentUtil;
import com.example.pc2.carmapproject.utils.LogUtil;
import com.example.pc2.carmapproject.utils.Obj2ByteUtil;
import com.example.pc2.carmapproject.utils.ProgressBarUtil;
import com.example.pc2.carmapproject.utils.ToastUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.nio.charset.MalformedInputException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * wcs调用接口来实现小车的控制。
 * 现在包含大部分查询的接口，陆续会实现所有涉及的接口
 *
 */
public class WcsCarOperateFragment extends BaseFragment{

    @BindView(R.id.iv_fragment_back)ImageView iv_fragment_back;
    @BindView(R.id.tv_fragment_title)TextView tv_fragment_title;

    private static final int WHAT_CAR_STATUS = 0x10;// 查看小车的状态
    private static final int WHAT_POD_STATUS = 0x11;// 查看pod信息
    private static final int WHAT_ADDR_STATUS = 0x12;// 查看地址状态
    private static final int WHAT_RESEND_ORDER = 0x13;// 重发任务
    private static final int WHAT_OFFLINE = 0x14;// 下线某小车
    private static final int WHAT_DRIVE_POD = 0x15;// 驱动pod去某地
    private static final int WHAT_SEND_ORDER = 0x16;// 下发指定任务
    private static final int WHAT_OFFLINE_POD = 0x17;// 下线货架

    private RequestQueue requestQueue;// volley请求队列
    private String rootAddress = "";// 请求地址根路径
    private View viewShowContent = null;

    private String sectionId = "";// 地图的sectionId
    private AlertDialog dialog_operate;// 输入信息的弹框
    private AlertDialog dialog_response;// 查看返回结果弹框
    private ProgressDialog pDialog;// 进度框
    private View viewOperate;

    private ConnectionFactory factory = new ConnectionFactory();// 声明ConnectionFactory对象
    private Thread publishThread = null;

    private onPodRemoveListener onPodRemoveListener = null;

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case WHAT_CAR_STATUS:// 查看小车的状态
                    ToastUtil.showToast(getContext(),"信息获取成功");
                    try{
                        JSONObject jbCarStatus = (JSONObject) msg.obj;
                        int robotId1 = msg.arg1;
                        showRobotStatus(jbCarStatus, robotId1);
                    }catch (Exception e){
                        e.printStackTrace();
                        ToastUtil.showToast(getContext(), "小车信息数据解析异常：" + e.getMessage());
                    }

//                    String objCarStatus = (String) msg.obj;
//                    int robotId1 = msg.arg1;
//                    alertDialogShowCarStatus(objCarStatus, robotId1);
                    break;
                case WHAT_POD_STATUS:// 查看pod信息
                    ToastUtil.showToast(getContext(),"信息获取成功");
                    String objPodInfo = (String) msg.obj;
                    int podId1 = msg.arg1;
                    alertDialogShowPodStatus(objPodInfo, podId1);
                    break;
                case WHAT_ADDR_STATUS:// 查看地址状态
                    ToastUtil.showToast(getContext(),"信息获取成功");
                    String objAddr = (String) msg.obj;
                    int addr1 = msg.arg1;
                    alertDialogShowAddrStatus(objAddr, addr1);
                    break;
                case WHAT_RESEND_ORDER:// 重发任务
                    ToastUtil.showToast(getContext(),"重发任务成功");
                    String objResend = (String) msg.obj;
                    int robotId2 = msg.arg1;
                    alertDialogShowResendOrder(objResend, robotId2);
                    break;
                case WHAT_OFFLINE:// 下线小车
                    ToastUtil.showToast(getContext(),"小车下线成功");
                    break;
                case WHAT_DRIVE_POD:// 驱动POD去某地
                    ToastUtil.showToast(getContext(),"POD驱动去目标点位成功");
                    break;
                case WHAT_SEND_ORDER:// 下发指定任务

                    // 获取小车id和任务id
                    int robotId = msg.arg1;
                    String orderId = (String) msg.obj;
                    sendOrder(robotId, orderId);

                    break;
                case WHAT_OFFLINE_POD:// 下线货架

                    final int podIndex = (int) msg.obj;
                    try {

                        setUpConnectionFactory();
                        publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CHANGING_POD_POSITION);

                        new AlertDialog.Builder(getContext())
                                .setTitle("提示")
                                .setCancelable(false)// 不可取消
                                .setIcon(R.mipmap.mushiny_icon)
                                .setMessage("亲爱的工程师！请点击发布选项，将发布移除货架指令！")
                                .setPositiveButton("发布", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        try {
                                            Map<String, Object> message = new HashMap<>();
                                            message.put("podCodeID", podIndex);
                                            message.put("addressCodeID", 0);
                                            queue.putLast(message);// 发送消息到MQ
                                            ToastUtil.showToast(getContext(), "货架" + podIndex + "移除指令已发布");
                                        }catch (Exception e){
                                            e.printStackTrace();
                                            ToastUtil.showToast(getContext(), "货架移除指令发布异常");
                                        }
                                        dialog.dismiss();
                                        // 退出当前界面，放回地图监控主界面
                                        if (onPodRemoveListener != null){
                                            onPodRemoveListener.removePod(podIndex);
                                        }
                                        getActivity().getSupportFragmentManager().popBackStack();

                                    }
                                }).create().show();




                    }catch (Exception e){
                        e.printStackTrace();
                        ToastUtil.showToast(getContext(), "下线货架异常：" + e.getMessage());
                        return;
                    }

                    break;
            }
        }
    };

    /**
     * 显示小车状态信息
     * @param jbCarStatus
     * @param robotId1
     */
    private void showRobotStatus(JSONObject jbCarStatus, final int robotId1) {

        String strRobotStatus = "";// 小车状态

        try {
            JSONObject jbReInFo = jbCarStatus.getJSONObject("reInfo");
//            strRobotStatus += getLineData("robotId", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("robotAddress", "小车地址", jbReInFo) + "\n";
            strRobotStatus += getLineData("robotAvailable", "小车是否可用（false=不可用，true=可用）", jbReInFo) + "\n";
            strRobotStatus += getLineData("robotStatus", "小车状态（1=空闲，2=执行任务中，3=充电中，[-1|-9]=已下线）", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotLoginTime", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotBatteryNumber", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotLaveBattery", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotVoltage", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotFullChargeFlag", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotChargeNum", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotFullChargeTime", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotSectionId", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotIsChargingRetry", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotChargingRetryNum", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotStartLaveBattery", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("robotChargingTime", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("podName", "货架号码", jbReInFo) + "\n";
//            strRobotStatus += getLineData("podRcsId", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("podLockedBy", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("podDirect", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("podAddressId", "货架地址", jbReInFo) + "\n";
            strRobotStatus += getLineData("podAddressStatus", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderId", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderType", "任务类型", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderPath", "路径", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderUpAddr", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderDownAddr", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("orderSrcAddr", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("orderEndAddr", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderRotateTheta", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderIndex", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderUseFace", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderWorkStation", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderPodName", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("orderPodAddr", "", jbReInFo) + "\n";
//            strRobotStatus += getLineData("order2RcsTime", "", jbReInFo) + "\n";
            JSONObject jbRobotMessage = jbReInFo.getJSONObject("robotMessage");
            strRobotStatus += "robotMessage >> " + getLineData("wcsTime", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("podCodeInfoX", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("podCodeInfoY", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("sectionID", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("robotID", "小车ID", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("podCodeInfoTheta", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("speed", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("podCodeID", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("addressCodeInfoTheta", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("addressCodeInfoY", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("addressCodeID", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("addressCodeInfoX", "", jbRobotMessage) + "\n";
            strRobotStatus += "robotMessage >> " + getLineData("rtTime", "", jbRobotMessage) + "\n";
            strRobotStatus += getLineData("lastOrderId", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("lastOrderError", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("lastOrderPod", "", jbReInFo) + "\n";
            strRobotStatus += getLineData("lastOrderPath", "", jbReInFo) + "\n";

            viewShowContent = getLayoutInflater().from(getContext()).inflate(R.layout.show_view_content, null);
            TextView tv_showContent = viewShowContent.findViewById(R.id.tv_showContent);
            if (!TextUtils.isEmpty(strRobotStatus)){
                tv_showContent.setText(strRobotStatus);
            }else {
                ToastUtil.showToast(getContext(), "小车信息为空，惊了！");
            }

            viewShowContent.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    methodCheckCarStatus(robotId1);
                }
            });

            showResponseDialog("查看某小车状态");

        } catch (Exception e) {
            e.printStackTrace();
            ToastUtil.showToast(getContext(), "小车状态数据行获取解析异常：" + e.getMessage());
        }

    }

    /**
     * 获取jsonObject中某个字段的值，并和字段拼接起来
     * @param key 键
     * @param keyDescribe   键的描述
     * @param object    JsonObject对象
     * @return
     */
    private String getLineData(String key, String keyDescribe, JSONObject object){

        String strLineData ="" + key;

        // 键描述不为空，就加上描述字段的内容
        if (!TextUtils.isEmpty(keyDescribe)){
            strLineData += " <" + keyDescribe + ">";
        }
        // 获取key对应的值并和key拼接在一起
        if (object.isNull(key)){
            strLineData += ":" + "";
        }else {
            strLineData += ":" + String.valueOf(object.opt(key));
        }
        return strLineData;
    }

    /**
     * 小车重发任务
     * @param objResend
     * @param robotId
     */
    private void alertDialogShowResendOrder(String objResend, int robotId) {
        viewShowContent = getLayoutInflater().from(getContext()).inflate(R.layout.show_view_content, null);
        TextView tv_showContent = viewShowContent.findViewById(R.id.tv_showContent);

        if (!TextUtils.isEmpty(objResend)){
            tv_showContent.setText(objResend);
        }

        viewShowContent.findViewById(R.id.btn_refresh).setVisibility(View.GONE);
        showResponseDialog("重发任务返回数据");
    }

    /**
     * 查看地址状态
     * @param objAddr
     * @param addr
     */
    private void alertDialogShowAddrStatus(String objAddr, final int addr) {
        viewShowContent = getLayoutInflater().from(getContext()).inflate(R.layout.show_view_content, null);

        TextView tv_showContent = viewShowContent.findViewById(R.id.tv_showContent);
        if (!TextUtils.isEmpty(objAddr)){
            tv_showContent.setText(objAddr);
        }else {
            ToastUtil.showToast(getContext(), "地址不存在");
        }

        viewShowContent.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodCheckAddrStatus(addr);
            }
        });

        showResponseDialog("查看地址状态");
    }

    /**
     * 显示pod信息
     * @param objPodInfo
     * @param podId1
     */
    private void alertDialogShowPodStatus(String objPodInfo, final int podId1) {
        viewShowContent = getLayoutInflater().from(getContext()).inflate(R.layout.show_view_content, null);

        TextView tv_showContent = viewShowContent.findViewById(R.id.tv_showContent);
        if (!TextUtils.isEmpty(objPodInfo)){
            tv_showContent.setText(objPodInfo);
        }else {
            ToastUtil.showToast(getContext(), "POD信息为空，惊了！");
        }

        viewShowContent.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodCheckPodStatus(podId1);
            }
        });

        showResponseDialog("查看POD信息");
    }

    /**
     * 显示小车的状态
     * @param objCarStatus
     * @param robotId1
     */
    private void alertDialogShowCarStatus(String objCarStatus, final int robotId1) {

        viewShowContent = getLayoutInflater().from(getContext()).inflate(R.layout.show_view_content, null);

        TextView tv_showContent = viewShowContent.findViewById(R.id.tv_showContent);
        if (!TextUtils.isEmpty(objCarStatus)){
            tv_showContent.setText(objCarStatus);
        }else {
            ToastUtil.showToast(getContext(), "小车信息为空，惊了！");
        }

        viewShowContent.findViewById(R.id.btn_refresh).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                methodCheckCarStatus(robotId1);
            }
        });

        showResponseDialog("查看某小车状态");

    }

    public WcsCarOperateFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wcs_car_operate, container, false);
        ButterKnife.bind(this, view);// 控件绑定
        init();// 初始化
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);// 光标出现的时候，隐藏软键盘
        setListeners();
        setTitleBar();
        return view;
    }

    /**
     * 设置标题栏样式
     */
    private void setTitleBar() {
        tv_fragment_title.setText("WCS部分");
    }

    /**
     * 初始化数据
     */
    private void init() {
        requestQueue  = Volley.newRequestQueue(getContext());// 创建RequestQueue对象

        rootAddress = Constants.HTTP + Constants.ROOT_ADDRESS;// 请求地址根路径赋值

        sectionId = Constants.SECTIONID;// 地图的sectionId

        pDialog = new ProgressDialog(getContext());
        pDialog.setCanceledOnTouchOutside(true);
    }

    /**
     * 显示加载进度框
     * @param s 描述内容
     */
    private void showDialog(String s) {
        pDialog.setMessage(s);
        pDialog.show();
    }

    /**
     * 消失对话框，将其从屏幕上移除
     */
    private void disMissDialog(){
        pDialog.dismiss();
    }

    /**
     * 展示返回结果弹框
     * @param message
     */
    private void showResponseDialog(String message){
        if (dialog_response != null && dialog_response.isShowing()){
            dialog_response.dismiss();
        }

        dialog_response = new AlertDialog.Builder(getContext())
                .setMessage(message)
                .setView(viewShowContent)
                .create();

        dialog_response.show();
    }

    /**
     * 设置监听
     */
    private void setListeners() {


    }

    private TextView tv_wcs_tip;
    /**
     * 设置dialog展示的view（用户选择操作弹框）
     */
    private void setDialogView(String tip){
        viewOperate = getLayoutInflater().from(getContext()).inflate(R.layout.dialog_wcs_car, null);
        tv_wcs_tip = viewOperate.findViewById(R.id.tv_wcs_tip);
        tv_wcs_tip.setText(tip);

        dialog_operate = new AlertDialog.Builder(getContext())
                .setView(viewOperate)
                .create();

        dialog_operate.show();
    }

    /**
     * 单击事件监听
     * @param view
     */
    @OnClick({R.id.tv_checkCarStatus, R.id.tv_checkPodStatus, R.id.tv_checkAddrStatus, R.id.tv_resendOrder
    , R.id.btn_robotAct, R.id.btn_robotOffline, R.id.btn_drivePod, R.id.btn_updatePodStatus
    ,R.id.btn_releasePodStatus, R.id.btn_updateAddrState, R.id.btn_robot2Charge, R.id.btn_autoDrivePod
            , R.id.btn_driveRobotCarryPod, R.id.iv_fragment_back, R.id.btn_driveRobot
    ,R.id.btn_updateRobotStatus, R.id.btn_send_order, R.id.btn_offline_pod, R.id.btn_force_drive_car
    ,R.id.btn_finish_emptyRun})
    public void doClick(View view){

        switch (view.getId()){
            case R.id.iv_fragment_back:// 返回上一界面
                getActivity().getSupportFragmentManager().popBackStack();

                break;

            case R.id.tv_checkCarStatus:// 查看某小车的状态

                setDialogView("查看某小车的状态");

                final EditText et_carIdInput = viewOperate.findViewById(R.id.et_carIdInput);
                et_carIdInput.setVisibility(View.VISIBLE);
                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strCarId = et_carIdInput.getText().toString().trim();// 取小车id
                        if (!TextUtils.isEmpty(strCarId)){

                            methodCheckCarStatus(Integer.parseInt(strCarId));

                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车的id");
                        }
                    }
                });

                break;

            case R.id.tv_checkPodStatus:// 查看pod信息

                setDialogView("查看pod信息");

                final EditText et_podIdInput = viewOperate.findViewById(R.id.et_podIdInput);
                et_podIdInput.setVisibility(View.VISIBLE);
                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strPodId = et_podIdInput.getText().toString().trim();// 取pod的id
                        if (!TextUtils.isEmpty(strPodId)){
                            methodCheckPodStatus(Integer.parseInt(strPodId));
                        }else {
                            ToastUtil.showToast(getContext(), "请输入POD的id");
                        }
                    }
                });


                break;

            case R.id.tv_checkAddrStatus:// 查看地址状态

                setDialogView("查看地址状态");

                final EditText et_addrInput = viewOperate.findViewById(R.id.et_addrInput);
                et_addrInput.setVisibility(View.VISIBLE);
                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strAddr = et_addrInput.getText().toString().trim();// 取地址
                        if (!TextUtils.isEmpty(strAddr)){
                            methodCheckAddrStatus(Integer.parseInt(strAddr));
                        }else {
                            ToastUtil.showToast(getContext(), "请输入地址");
                        }
                    }
                });


                break;

            case R.id.tv_resendOrder:// 重发任务

                setDialogView("重发任务");

                final EditText et_resendOrder = viewOperate.findViewById(R.id.et_carIdInput);
                et_resendOrder.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strCarId = et_resendOrder.getText().toString().trim();// 取小车id
                        if (!TextUtils.isEmpty(strCarId)){

                            new AlertDialog.Builder(getContext())
                                    .setTitle("提示")
                                    .setIcon(R.mipmap.mushiny_icon)
                                    .setMessage("亲爱的工程师！确定给 " + strCarId + " 号小车重发任务？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            methodResendOrder(Integer.parseInt(strCarId));
                                            dialog.dismiss();
                                        }
                                    })
                                    .create().show();

                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车的id");
                        }
                    }
                });

                break;

            case R.id.btn_robotAct:// 控制小车上下左右移动一格

                setDialogView("控制小车上下左右移动一格");

                EditText et_robotAct = viewOperate.findViewById(R.id.et_carIdInput);
                Spinner sp_robotAct = viewOperate.findViewById(R.id.sp_robotAct);

                et_robotAct.setVisibility(View.VISIBLE);
                sp_robotAct.setVisibility(View.VISIBLE);

                break;

            case R.id.btn_robotOffline:// 下线某小车

                setDialogView("下线某小车");

                final EditText et_robotOffline = viewOperate.findViewById(R.id.et_carIdInput);
                et_robotOffline.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strCarId = et_robotOffline.getText().toString().trim();// 取小车id
                        if (!TextUtils.isEmpty(strCarId)){

                            new AlertDialog.Builder(getContext())
                                    .setTitle("提示")
                                    .setIcon(R.mipmap.mushiny_icon)
                                    .setMessage("亲爱的工程师！确定要下线 " + strCarId + " 号小车吗？（请谨慎操作！！！）")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            methodOffline(Integer.parseInt(strCarId));
                                            dialog.dismiss();
                                        }
                                    })
                                    .create().show();

                        }else {
                            ToastUtil.showToast(getContext(), "请输入小车的id");
                        }
                    }
                });

                break;

            case R.id.btn_drivePod:// 驱动pod去某地
                setDialogView("驱动pod去某地");

                final EditText et_podIdInput_drivePod = viewOperate.findViewById(R.id.et_podIdInput);
                final EditText et_addrInput_drivePod = viewOperate.findViewById(R.id.et_addrInput);

                et_podIdInput_drivePod.setVisibility(View.VISIBLE);// pod的id
                et_addrInput_drivePod.setVisibility(View.VISIBLE);// 目标点位

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strPodId = et_podIdInput_drivePod.getText().toString().trim();// 取pod的id
                        final String strAddr = et_addrInput_drivePod.getText().toString().trim();// 取目标点位

                        if (!TextUtils.isEmpty(strPodId) && !TextUtils.isEmpty(strAddr)){
                            new AlertDialog.Builder(getContext())
                                    .setTitle("提示")
                                    .setIcon(R.mipmap.mushiny_icon)
                                    .setMessage("亲爱的工程师！确定驱动 " + strPodId + " 号POD去目标点位 " + strAddr + "？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            methodDrivePod(strPodId, strAddr);
                                            dialog.dismiss();
                                        }
                                    })
                                    .create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "pod不能为空 或 目标点位不能为空");
                        }
                    }
                });

                break;

            case R.id.btn_updatePodStatus:// 更新pod地址
                setDialogView("更新pod地址");

                final EditText et_podIdInput_updatePod = viewOperate.findViewById(R.id.et_podIdInput);
                final EditText et_addrInput_updatePod = viewOperate.findViewById(R.id.et_addrInput);

                et_podIdInput_updatePod.setVisibility(View.VISIBLE);// pod的id
                et_addrInput_updatePod.setVisibility(View.VISIBLE);// 更新地址
                
                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strPodId = et_podIdInput_updatePod.getText().toString().trim();// 取pod的id
                        final String strAddr = et_addrInput_updatePod.getText().toString().trim();// 取更新地址

                        if (!TextUtils.isEmpty(strPodId) && !TextUtils.isEmpty(strAddr)){
                            new AlertDialog.Builder(getContext())
                                    .setTitle("提示")
                                    .setIcon(R.mipmap.mushiny_icon)
                                    .setMessage("亲爱的工程师！确定更新 " + strPodId + " 号POD的地址为 " + strAddr + " 吗？（请谨慎操作！！！）")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            methodUpdatePodStatus(strPodId, strAddr);
                                            dialog.dismiss();
                                        }
                                    })
                                    .create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "pod不能为空 或 更新地址不能为空");
                        }
                    }
                });

                break;

            case R.id.btn_releasePodStatus:// 释放pod状态

                setDialogView("释放pod状态");

                final EditText et_podIdInput_releasePod = viewOperate.findViewById(R.id.et_podIdInput);
                et_podIdInput_releasePod.setVisibility(View.VISIBLE);// pod的id

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strPodId = et_podIdInput_releasePod.getText().toString().trim();// 取pod的id

                        if (!TextUtils.isEmpty(strPodId)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("亲爱的工程师！确定释放 " + strPodId + " 号POD状态？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodReleasePodStatus(strPodId);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "pod不能为空");
                        }
                    }
                });
                break;

            case R.id.btn_updateAddrState:// 更新地址状态
                setDialogView("更新地址状态");

                final EditText et_podIdInput_updateAddr = viewOperate.findViewById(R.id.et_addrInput);
                et_podIdInput_updateAddr.setVisibility(View.VISIBLE);// 地址坐标

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strAddrId = et_podIdInput_updateAddr.getText().toString().trim();// 取地址

                        if (!TextUtils.isEmpty(strAddrId)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("亲爱的工程师！是否更新地址 " + strAddrId + " 状态： Status 为 Available 以及 LockedBy 为 0？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodUpdateAddrState(strAddrId);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "地址输入不能为空");
                        }
                    }
                });
                break;

            case R.id.btn_robot2Charge:// 下发充电任务
                setDialogView("下发充电任务");

                final EditText et_robot2Charge = viewOperate.findViewById(R.id.et_carIdInput);
                et_robot2Charge.setVisibility(View.VISIBLE);// 小车id

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String strRobotId = et_robot2Charge.getText().toString().trim();// 取小车id
                        final int robotId = Integer.parseInt(strRobotId.trim());// 转为整数

                        if (!TextUtils.isEmpty(strRobotId)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("给" + robotId + "号小车下发充电任务？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            if (robotId == 0){
                                                ToastUtil.showToast(getContext(), "小车id不能为0");
                                                return;
                                            }
                                            method2Charge(robotId);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "地址输入不能为空");
                        }
                    }
                });
                break;

            case R.id.btn_autoDrivePod:// 自动分配POD回存储区

                setDialogView("自动分配pod回存储区");

                final EditText et_podIdInput_autoDrive = viewOperate.findViewById(R.id.et_podIdInput);
                et_podIdInput_autoDrive.setVisibility(View.VISIBLE);// pod的id

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final String strPodId = et_podIdInput_autoDrive.getText().toString().trim();// 取pod的id

                        if (!TextUtils.isEmpty(strPodId)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("自动分配POD：" + strPodId + "回存储区？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodAutoDrivePod(strPodId);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(), "pod不能为空");
                        }
                    }
                });

                break;

            case R.id.btn_driveRobotCarryPod:// 驱动小车驮pod去某地
                setDialogView("驱动小车驮pod去某地");

                final EditText et_robotId = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                final EditText et_podId = viewOperate.findViewById(R.id.et_podIdInput);// pod的id
                final EditText et_addrId = viewOperate.findViewById(R.id.et_addrInput);// 地址

                et_robotId.setVisibility(View.VISIBLE);
                et_podId.setVisibility(View.VISIBLE);
                et_addrId.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String strRobot = et_robotId.getText().toString().trim();
                        final String strPod = et_podId.getText().toString().trim();
                        final String strAddr = et_addrId.getText().toString().trim();

                        if (!TextUtils.isEmpty(strRobot) &&
                                !TextUtils.isEmpty(strPod) &&
                                !TextUtils.isEmpty(strAddr)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("驱动小车 " + strRobot + "驮pod " + strPod + "去地址 " + strAddr + "?")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodDriveRobotCarryPod(strRobot, strPod, strAddr);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(),"请输入小车id、pod或者地址");
                        }

                    }
                });

                break;

            case R.id.btn_driveRobot:// 驱动小车去某地

                setDialogView("驱动小车去某地");

                final EditText et_robotId_driveRobot = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                final EditText et_addrId_driveRobot = viewOperate.findViewById(R.id.et_addrInput);// 地址

                et_robotId_driveRobot.setVisibility(View.VISIBLE);
                et_addrId_driveRobot.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String strRobot = et_robotId_driveRobot.getText().toString().trim();
                        final String strAddr = et_addrId_driveRobot.getText().toString().trim();

                        if (!TextUtils.isEmpty(strRobot) &&
                                !TextUtils.isEmpty(strAddr)){
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("亲爱的工程师！确定驱动小车 " + strRobot + " 去地址 " + strAddr + "?")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodDriveRobot(strRobot, strAddr);
                                        }
                                    }).create().show();
                        }else {
                            ToastUtil.showToast(getContext(),"请输入小车id或者地址");
                        }
                    }
                });

                break;

            case R.id.btn_updateRobotStatus:// 更新小车（status为1、available为true、cancelOrder为true）

                setDialogView("更新小车");

                final EditText et_robotId_updateStatus = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                
                et_robotId_updateStatus.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String strRobot = et_robotId_updateStatus.getText().toString().trim();
                        
                        if (!TextUtils.isEmpty(strRobot)){

                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("亲爱的工程师！确定更新小车 " + strRobot + " 可用（Status 为 1、Available 为 true 以及 CancelOrder 为 true）？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            methodUpdateRobotStatus(strRobot);
                                        }
                                    }).create().show();
                            
                        }else {
                            
                            ToastUtil.showToast(getContext(), "请输入小车 ID");
                            
                        }

                        
                    }
                });

                break;

            case R.id.btn_send_order:// 下发指定任务

                setDialogView("下发指定任务");

                final EditText et_robotId_sendOrder = viewOperate.findViewById(R.id.et_carIdInput);// 小车id

                et_robotId_sendOrder.setVisibility(View.VISIBLE);
                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String strRobot = et_robotId_sendOrder.getText().toString().trim();

                        if (!TextUtils.isEmpty(strRobot)){

                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("亲爱的工程师！确定给 " + strRobot + " 号小车下发指定任务？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            getTaskId(strRobot);// 获取小车的任务id
                                        }
                                    }).create().show();

                        }else {

                            ToastUtil.showToast(getContext(), "请输入小车 ID");

                        }


                    }
                });

                break;
            case R.id.btn_offline_pod:// 下线货架

                setDialogView("下线货架");

                final EditText et_podId_offline = viewOperate.findViewById(R.id.et_podIdInput);// 货架id
                et_podId_offline.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String strPod = et_podId_offline.getText().toString().trim();

                        if (!TextUtils.isEmpty(strPod)){

                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("亲爱的工程师！确定下线 " + strPod + " 号货架？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            offlinePod(strPod);
                                        }
                                    }).create().show();

                        }else {

                            ToastUtil.showToast(getContext(), "请输入货架 ID");

                        }


                    }
                });
                break;

            case R.id.btn_force_drive_car:// 强制分车

                setDialogView("强制分车");

                final EditText et_force_drive_carId = viewOperate.findViewById(R.id.et_carIdInput);// 小车id
                final EditText et_force_drive_podId = viewOperate.findViewById(R.id.et_podIdInput);// 货架id

                et_force_drive_carId.setVisibility(View.VISIBLE);
                et_force_drive_podId.setVisibility(View.VISIBLE);

                viewOperate.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String robotId = et_force_drive_carId.getText().toString().trim();
                        final String podIndex = et_force_drive_podId.getText().toString().trim();

                        if (!TextUtils.isEmpty(robotId) || !TextUtils.isEmpty(podIndex)){

                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage("亲爱的工程师！请确定是否要执行强制分车操作！请您谨慎操作：确保货架所在的调度单是 New 状态，并且小车状态可用（需要是空车空闲状态），status为1。")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            forceDrivePod(podIndex, robotId);// 强制分车
                                        }
                                    }).create().show();

                        }else {

                            ToastUtil.showToast(getContext(), "小车或者货架输入不能为空");

                        }

                    }
                });

                break;

            case R.id.btn_finish_emptyRun:// 结束状态为New未分车的EmptyRun调度任务

                new AlertDialog.Builder(getContext())
                        .setIcon(R.mipmap.app_icon)
                        .setTitle("提示")
                        .setMessage("结束New状态未分车的EmptyRun调度任务？")
                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                finishEmptyRun();
                            }
                        }).create().show();

                break;
        }

    }

    /**
     * 结束状态为New未分车的EmptyRun调度任务
     */
    private void finishEmptyRun() {

        ProgressBarUtil.showProgressBar(getContext(), "正在结束...",
                getResources().getColor(R.color.colorAccent));

        String url = getResources().getString(R.string.url_finishEmptyRun);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ProgressBarUtil.dissmissProgressBar();
                try{

                    String number = String.valueOf(response.opt("number"));
                    String msg = String.valueOf(response.opt("msg"));

                    if (!TextUtils.isEmpty(number)){
                        ToastUtil.showToast(getContext(), msg);
                    }


                }catch (Exception e){
                    e.printStackTrace();
                    ToastUtil.showToast(getContext(), "解析finish调度任务EmptyRun数据异常");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ProgressBarUtil.dissmissProgressBar();
                error.printStackTrace();
                ToastUtil.showToast(getContext(), "finish调度任务EmptyRun失败！");
            }
        });

        requestQueue.add(request);// 将请求加入请求队列中

    }

    /**
     *  强制分车
     * @param podIndex  pod号
     * @param driver    小车id
     */
    private void forceDrivePod(final String podIndex, final String driver) {

        ProgressBarUtil.showProgressBar(getContext(), "强制分车...",
                getResources().getColor(R.color.colorPrimaryDark));

        String url = getResources().getString(R.string.url_force_drive_pod)
                + "podIndex=" + podIndex + "&driver=" + driver;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ProgressBarUtil.dissmissProgressBar();
                try{

                    int result = response.optInt("result");// 1表示分车成功、0表示分车失败
                    if (result == 1){
                        ToastUtil.showToast(getContext(), "您好棒啊工程师！"
                        + podIndex + "号 POD 成功分配小车 " + driver);
                    }else if(result == 0){
                        ToastUtil.showToast(getContext(), "强制分车失败！");
                    }

                }catch (Exception e){
                    e.printStackTrace();
                    ToastUtil.showToast(getContext(), "强制分车返回数据异常");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ProgressBarUtil.dissmissProgressBar();
                error.printStackTrace();
                ToastUtil.showToast(getContext(), "强制分车失败！");
            }
        });

        requestQueue.add(request);// 将请求加入请求队列中

    }

    /**
     * 下线货架
     * @param podIndex 货架的下标，例如20
     */
    private void offlinePod(final String podIndex) {

        ProgressBarUtil.showProgressBar(getContext(), "货架下线...",
                getResources().getColor(R.color.colorPrimaryDark));
        String url = getResources().getString(R.string.url_offlinePod)
                + "podIndex=" + podIndex;

//        LogUtil.e("TAG", "下线货架 URL = " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                ProgressBarUtil.dissmissProgressBar();
                if (response != null){


                    try {
                        String code = String.valueOf(response.opt("code"));
                        if (code.equals("200")){

                            // 下线成功
                            ToastUtil.showToast(getContext(), "" + response.opt("msg"));
                            // 发消息，通知mq移除货架
                            Message message = handler.obtainMessage();
                            message.obj = podIndex;
                            message.what = WHAT_OFFLINE_POD;
                            handler.sendMessage(message);

                        }else {
                            ToastUtil.showToast(getContext(), "下线货架失败：" + String.valueOf(response.opt("msg")));
                            return;
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        ToastUtil.showToast(getContext(), "下线货架异常捕获");
                    }

                }else {
                    ToastUtil.showToast(getContext(), "下线货架返回为空");
                    return;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                ProgressBarUtil.dissmissProgressBar();
                ToastUtil.showToast(getContext(), "下线货架失败");
                return;

            }
        });

        requestQueue.add(request);

    }

    /**
     * 根据用户输入的小车id，获取当前小车执行任务的id
     * @param robotId
     */
    private void getTaskId(final String robotId) {

        showDialog("指定任务下发中...");
        String url = getResources().getString(R.string.url_getTaskId) + "robotId=" + robotId;
//        LogUtil.e("TAG", "获取任务id的URL = " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {

                if (response != null){

                    try {
                        String code = String.valueOf(response.opt("number"));
                        if (code.equals("200")){

                            // 任务id获取成功后。执行最后的重发指定任务操作
                            Message message = handler.obtainMessage();
                            message.what = WHAT_SEND_ORDER;
                            message.obj = String.valueOf(response.opt("id"));
                            message.arg1 = Integer.parseInt(robotId);
                            handler.sendMessage(message);

                        }else {
                            disMissDialog();
                            ToastUtil.showToast(getContext(), "任务获取失败：" + String.valueOf(response.opt("msg")));
                            return;
                        }
                    }catch (Exception e){
                        disMissDialog();
                        e.printStackTrace();
                        ToastUtil.showToast(getContext(), "任务获取异常捕获");
                    }

                }else {
                    disMissDialog();
                    ToastUtil.showToast(getContext(), "任务获取为空");
                    return;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                disMissDialog();
                ToastUtil.showToast(getContext(), "任务ID获取失败");
                return;

            }
        });

        requestQueue.add(request);

    }

    /**
     * 调用接口下发指定任务
     * @param robotId
     * @param orderId
     */
    private void sendOrder(int robotId, String orderId) {

        String url = rootAddress + getResources().getString(R.string.url_sendOrder)
                + "sectionId=" + sectionId + "&robotId=" + robotId + "&orderId=" + orderId;

//        LogUtil.e("URL", "下发指定任务url = " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                disMissDialog();
                if (!TextUtils.isEmpty(response.toString())){

                    ToastUtil.showToast(getContext(), "下发指定任务成功");

                }else {
                    ToastUtil.showToast(getContext(), "下发指定任务失败");
                    return;
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                disMissDialog();
                ToastUtil.showToast(getContext(), "下发指定任务over");
                return;
            }
        });

        requestQueue.add(request);

    }

    /**
     * 更新小车（status为1、available为true、cancelOrder为true）
     * @param robotId
     */
    private void methodUpdateRobotStatus(final String robotId) {

        showDialog("加载中...");

        String status = "1";
        boolean available = true;
        boolean cancelOrder = true;

        String url = rootAddress + getResources().getString(R.string.url_updateRobotStatus)
                + "sectionId=" + sectionId + "&robotId=" + robotId
                + "&status=" + status + "&available=" + available
                + "&cancelOrder=" + cancelOrder;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (!TextUtils.isEmpty(response)){

                            if (response.contains("小车不在同一个section")){
                                ToastUtil.showToast(getContext(), "小车不在同一个地图，请输入正确的小车 ID");
                                return;
                            }

                            ToastUtil.showToast(getContext(), "更新小车" + robotId + "成功");

                        }else {

                            ToastUtil.showToast(getContext(), "请检查输入的小车 ID 是否存在");
                            return;

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "更新小车over");
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 驱动小车去某地
     * @param robotId
     * @param addrCodeId
     */
    private void methodDriveRobot(String robotId, String addrCodeId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_driveRobot)
                + "sectionId=" + sectionId + "&addrCodeId=" + addrCodeId
                + "&robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查小车和地址输入是否正确");
                            return;
                        }else if ("ok".equals(response)){
                            ToastUtil.showToast(getContext(),"小车驱动成功");
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "驱动小车over");
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 驱动小车驮pod去某地
     * @param robotId
     * @param podId
     * @param addrCodeId
     */
    private void methodDriveRobotCarryPod(String robotId, String podId, String addrCodeId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_driveRobotCarryPod)
                + "sectionId=" + sectionId + "&podId=" + podId + "&addrCodeId=" + addrCodeId
                + "&robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查pod、小车和地址输入是否正确");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"操作成功");
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "驱动小车驮pod_over");
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 自动分配pod回存储区
     * @param podId
     */
    private void methodAutoDrivePod(String podId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_autoAssignAdnDrivePod)
                + "sectionId=" + sectionId + "&podId=" + podId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查pod是否存在");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"PodRun调度任务已生成");
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "自动分配pod回存储区over_e");
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 小车下发充电任务
     * @param robotId
     */
    private void method2Charge(int robotId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_robot2Charge)
                + "robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查小车是否存在");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"下发充电任务成功");
                            ToastUtil.showToast(getContext(),response);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "下发充电任务over_e");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 更新地址状态
     * @param addrCodeId
     */
    private void methodUpdateAddrState(String addrCodeId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_updateAddrState)
                + "sectionId=" + sectionId + "&addrCodeId=" + addrCodeId
                + "&status=" + "Available" + "&lockedby=" + 0;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查地址是否存在");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"更新地址状态成功");
                            ToastUtil.showToast(getContext(),response);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "更新地址状态over_e");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 释放pod状态
     * @param podId
     */
    private void methodReleasePodStatus(String podId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_releasePodStatus)
                + "sectionId=" + sectionId + "&podId=" + podId + "&lockedBy=" + 0;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "请检查pod是否存在");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),"POD状态释放成功");
                            ToastUtil.showToast(getContext(),response);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "释放pod状态over_e");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 更新pod地址
     * @param podId
     * @param addrCodeId
     */
    private void methodUpdatePodStatus(String podId, String addrCodeId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_updatePodPos)
                + "sectionId=" + sectionId + "&podId=" + podId + "&addrCodeId=" + addrCodeId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "POD或更新地址填写不正确");
                            return;
                        }else {
                            ToastUtil.showToast(getContext(),response);
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "更新POD地址异常");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 驱动pod去某地
     * @param podId
     * @param addrCodeId
     */
    private void methodDrivePod(String podId, String addrCodeId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_drivePod)
                + "sectionId=" + sectionId + "&podId=" + podId + "&addrCodeId=" + addrCodeId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response)){
                            ToastUtil.showToast(getContext(), "返回结果为空");
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_DRIVE_POD;
                        message.obj = response.toString();
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "驱动pod异常");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 下线某小车
     * @param robotId
     */
    private void methodOffline(final int robotId) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_robotOffline)
                + "robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (response.toString().contains("没有这辆小车")){
                            ToastUtil.showToast(getContext(), response);
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_OFFLINE;
                        message.obj = response.toString();
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "下线小车over_e");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 重发任务
     * @param robotId
     */
    private void methodResendOrder(final int robotId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_resendOrder)
                + "sectionId=" + sectionId + "&robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();

                        ToastUtil.showToast(getContext(), "重发任务over_s");
                        if (!TextUtils.isEmpty(response.toString())){

                            String strRes = response.toString();
                            if (strRes.contains("未注册")){
                                ToastUtil.showToast(getContext(), response.toString());
                                return;
                            }else {
                                Message message = handler.obtainMessage();
                                message.what = WHAT_RESEND_ORDER;
                                message.obj = response.toString();
                                message.arg1 = robotId;
                                handler.sendMessage(message);
                            }

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "重发任务over_e");
                    }
                });

        requestQueue.add(request);


    }

    /**
     * 查看地址状态
     * @param addr
     */
    private void methodCheckAddrStatus(final int addr) {
        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_checkAddrStatus)
                + "sectionId=" + sectionId + "&addrCodeId=" + addr;

        LogUtil.e("url","url = " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response.toString())){
                            ToastUtil.showToast(getContext(), "地址不存在");
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_ADDR_STATUS;
                        message.obj = response.toString();
                        message.arg1 = addr;
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "查看地址状态error");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 查看pod信息
     * @param podId
     */
    private void methodCheckPodStatus(final int podId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_checkPodStatus)
                + "sectionId=" + sectionId + "&podId=" + podId;

        LogUtil.e("url","url = " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response.toString())){
                            ToastUtil.showToast(getContext(), "pod信息为空");
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_POD_STATUS;
                        message.obj = response.toString();
                        message.arg1 = podId;
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "查看POD信息error");
                    }
                });

        requestQueue.add(request);

    }

    /**
     * 查看某小车的状态
     * @param robotId 小车的id
     */
    private void methodCheckCarStatus(final int robotId) {

        showDialog("加载中...");

        String url = rootAddress + getResources().getString(R.string.url_checkCarState)
                + "sectionId=" + sectionId + "&robotId=" + robotId;

        LogUtil.e("url","url = " + url);

        /*
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response.toString())){
                            ToastUtil.showToast(getContext(), "查看小车状态返回数据为空");
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_CAR_STATUS;
                        message.obj = response.toString();
                        message.arg1 = robotId;
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "查看小车状态error");
                    }
        });
        */

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        if (TextUtils.isEmpty(response.toString())){
                            ToastUtil.showToast(getContext(), "查看小车状态返回数据为空");
                            return;
                        }
                        Message message = handler.obtainMessage();
                        message.what = WHAT_CAR_STATUS;
                        message.obj = response;
                        message.arg1 = robotId;
                        handler.sendMessage(message);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        dialog_operate.dismiss();
                        ToastUtil.showToast(getContext(), "查看小车状态error");
                    }
        });

        requestQueue.add(request);

    }

    /**
     * RabbitMQ连接设置
     */
    private void setUpConnectionFactory() {
        factory.setHost(Constants.MQ_HOST);//主机地址
        factory.setPort(Constants.MQ_PORT);// 端口号
        factory.setUsername(Constants.MQ_USERNAME);// 用户名
        factory.setPassword(Constants.MQ_PASSWORD);// 密码
        factory.setAutomaticRecoveryEnabled(false);
    }

    // 创建BlockingDeque对象
    private BlockingDeque<Map<String, Object>> queue = new LinkedBlockingDeque<>();
    private void publishToAMPQ(final String exchange, final String routingKey) {
        publishThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Connection connection = factory.newConnection();
                        Channel ch = connection.createChannel();
                        ch.confirmSelect();
                        while (true) {
                            Map<String, Object> message = queue.takeFirst();
                            try {
                                ch.basicPublish(exchange, routingKey, null, Obj2ByteUtil.serialize((Serializable)message));
                                ch.waitForConfirmsOrDie();

                            } catch (Exception e) {
                                queue.putFirst(message);
                                throw e;
                            }

                        }
                    } catch (InterruptedException e) {
                        break;
                    } catch (Exception e) {
                        LogUtil.d("TAG_Publish", "Connection broken: " + e.getClass().getName());
                        try {
                            Thread.sleep(5000); //sleep and then try again
                        } catch (InterruptedException e1) {
                            break;

                        }

                    }

                }

            }

        });

        publishThread.start();
    }

    /**
     * 注册监听器
     */
    public void setOnRemovePodListener(onPodRemoveListener onRemovePodListener){

        this.onPodRemoveListener = onPodRemoveListener;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (publishThread != null){// 发消息线程 中断并置为null
            publishThread.interrupt();
            publishThread = null;
        }
        // 移除所有的回调和消息，防止Handler泄露
        handler.removeCallbacksAndMessages(null);
        if(requestQueue != null){
            requestQueue.stop();// 停止缓存和网络调度程序
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden){
            setTitleBar();
        }
    }
}
