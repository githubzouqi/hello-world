package com.example.pc2.carmapproject.fragment;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.KeyListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.pc2.carmapproject.R;
import com.example.pc2.carmapproject.adapter.MenusAdapter;
import com.example.pc2.carmapproject.adapter.TipsAdapter;
import com.example.pc2.carmapproject.constant.Constants;
import com.example.pc2.carmapproject.entity.CarCurrentPathEntity;
import com.example.pc2.carmapproject.entity.ChargingPileEntity;
import com.example.pc2.carmapproject.entity.ChargingTaskEntity;
import com.example.pc2.carmapproject.entity.ErrorCharging;
import com.example.pc2.carmapproject.entity.MenuEntity;
import com.example.pc2.carmapproject.entity.NoMoveTimeoutEntity;
import com.example.pc2.carmapproject.entity.PodAddressEntity;
import com.example.pc2.carmapproject.entity.PodEntity;
import com.example.pc2.carmapproject.entity.RobotCloseConnEntity;
import com.example.pc2.carmapproject.entity.RobotEntity;
import com.example.pc2.carmapproject.entity.RobotErrorEntity;
import com.example.pc2.carmapproject.entity.RtHeartTimeoutEntity;
import com.example.pc2.carmapproject.entity.StorageEntity;
import com.example.pc2.carmapproject.interfaces.MyItemClickListener;
import com.example.pc2.carmapproject.interfaces.onPodRemoveListener;
import com.example.pc2.carmapproject.service.RcsTripService;
import com.example.pc2.carmapproject.utils.DensityUtil;
import com.example.pc2.carmapproject.utils.FileUtil;
import com.example.pc2.carmapproject.utils.LogUtil;
import com.example.pc2.carmapproject.utils.MyItemDecoration;
import com.example.pc2.carmapproject.utils.ProgressBarUtil;
import com.example.pc2.carmapproject.utils.ScreenUtil;
import com.example.pc2.carmapproject.utils.ToastUtil;
import com.example.pc2.carmapproject.view.BoxView;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * A simple {@link Fragment} subclass.
 * 格子图主界面
 */
public class BoxFragment extends BaseFragment {

    private static final int WHATMAP = 0;// 地图和pod的初始化
    private static final int WHATCAR = 1;//小车
    private static final int WHAT_CAR_ROUTE = 2;// 小车路径信息查看
    private static final int WHATSTORAGEMAP = 3;// 仓库地图初始化
    private static final int WHAT_CAR_ROUTE_SHOW = 4;// 地图显示小车路径信息
    private static final int WHAT_SHOW_POD = 5;// 显示pod状态
    private static final int WHAT_SHOW_ALL_CAR_CURRENT_PATH = 6;// 显示小车当前路径
    private static final int WHAT_CHARGING_TASK = 7;// 所有充电桩的充电任务
    private static final int WHAT_CLEAR_DATA = 8;// 重选仓库地图时清空原有的数据
    private static final int WHAT_CANCEL_CAR_ALL_CURRENT_PATH = 9;// 取消小车当前路径显示
    private static final int WHAT_CLOSE_CONNECTION = 10;// 关闭连接
    private static final int WHAT_CLEAR_CHARGE_DATA =0x10;// 清空充电任务数据
    private static final int WHAT_ROBOT_ERROR = 0x11;// 小车错误反馈
    private static final int WHAT_ROBOT_NOMOVE_TIMEOUT = 0x12;// 小车位置不改变超时
    private static final int WHAT_REFRESH_ERROR_DATA = 0x13;// 刷新错误反馈数据
    private static final int WHAT_ERROR_CLOSE_CONNECTION = 0x14;// 小车断开连接
    private static final int WHAT_ROBOT_CHARGING_ERROR = 0X15;// 小车充电故障
    private static final int WHAT_CREATE_EMPTY_ROUTE = 0x16;// 生成空车路径
    private static final long DELAY_TIME = 1500;// 延时 1.5 秒自动开启小车监控
    private static final int WHAT_CAR_DATA = 0x17;// 开始小车监控
    private static final int WHAT_RT_HEART_TIMEOUT = 0X18;// 小车心跳或实时包未收到超时
    private static final int WHAT_AUTO_DRAW_MAP = 0x22;// 延迟消息绘制地图
    private static final int WHAT_RELEASE_POD = 0X23;// 延迟消息不间断释放pod
    private static final long RELEASE_POD_TIME = 8000;// 延迟消息不间断释放pod延迟时间

    private static final String MAP_INSTRUCTIONS = "地图说明";// 地图说明
    private static final String MAP_INFORMATION = "地图信息";// 地图信息
    private static final String MAP_RESET = "地图复位";// 地图复位
    private static final String MAP_RESELECT = "地图重选";// 地图重选
    private static final String MAP_LOCK_UNLOCK = "锁格/解锁";// 锁格/解锁
    private static final String MAP_ROBOT_BATTERY = "小车电量";// 小车电量
    private static final String MAP_WCS = "WCS部分";// WCS部分
    private static final String MAP_RCS = "RCS部分";// RCS部分
    private static final String MAP_POD_TRIP_MESSAGE = "POD调度\n状态查看";

    private static final int PERMISSION_REQUEST_STORAGE = 0X20;// 动态申请存储权限请求码
    private static final int WHAT_POD_AGV_ADDRESS_HIDE = 0x21;// 隐藏agv或者pod的位置信息
    private static final int WHAT_TRIP_TASK_LISTENER = 0x24;// 调度任务时长监听标识
    private static final long TIME_TRIP_TASK_LISTENER = 300000;// 调度任务时长监听延迟间隔5min

    private ConnectionFactory factory = new ConnectionFactory();// 声明ConnectionFactory对象

    @BindView(R.id.boxView)BoxView boxView;// 自定义方格图
//    @BindView(R.id.boxView)BoxSurfaceView boxView;// 自定义方格图
    @BindView(R.id.tv_hint)TextView tv_hint;// 地图界面用户信息提示
    @BindView(R.id.btn_drawing)Button btn_drawing;// 进行地图的绘制

    @BindView(R.id.linear_zoomOutIn)LinearLayout linear_zoomOutIn;// 放大、缩小图标所在的线性布局
    @BindView(R.id.linear_map_introduction) LinearLayout linear_map_introduction;// 地图说明图标所在的线性布局
    @BindView(R.id.linear_map_info) LinearLayout linear_map_info;// 地图信息图标所在的线性布局
    @BindView(R.id.linear_map_reset) LinearLayout linear_map_reset;// 地图复位
    @BindView(R.id.linear_map_drawAgain) LinearLayout linear_map_drawAgain;// 地图重选
    @BindView(R.id.linear_map_carLockUnLock) LinearLayout linear_map_carLockUnLock;// 锁格、解锁
    @BindView(R.id.linear_map_carBatteryInfo) LinearLayout linear_map_carBatteryInfo;// 小车电量信息
    @BindView(R.id.linear_map_wcs) LinearLayout linear_map_wcs;// WCS部分
    @BindView(R.id.linear_map_rcs) LinearLayout linear_map_rcs;// RCS部分

    @BindView(R.id.linear_operate) LinearLayout linear_operate;// 地图绘制操作的线性布局
    @BindView(R.id.rl_mapView)RelativeLayout rl_mapView;
    @BindView(R.id.btn_initStorageMap) Button btn_selectStorageMap;// 选择仓库和地图
    @BindView(R.id.btn_init_data) Button btn_stratCarMonitor;// 开始小车的监控
//    @BindView(R.id.tv_showAllCarCurrentPath) TextView tv_showAllCarCurrentPath;// 显示小车锁格、未锁格路径
//    @BindView(R.id.tv_cancelAllCarCurrentPath) TextView tv_cancelAllCarCurrentPath;// 取消小车锁格、未锁格路径显示

    @BindView(R.id.fab_path_display)FloatingActionButton fab_path_display;// 显示小车锁格、未锁格路径悬浮按钮
    @BindView(R.id.fab_path_hide)FloatingActionButton fab_path_hide;// 取消小车锁格、未锁格路径显示悬浮按钮

    @BindView(R.id.view) View view_border;// 显示边线
    @BindView(R.id.linear_lock_unlock) LinearLayout linear_lock_unlock;
    @BindView(R.id.linear_error_tip) LinearLayout linear_error_tip;// 错误反馈提示
    @BindView(R.id.fab_menu)FloatingActionButton fab_menu;// FloatingActionButton控件

    @BindView(R.id.iv_error_scan_pod)ImageView iv_error_scan_pod;// 扫不到pod图标
    @BindView(R.id.iv_error_nomove_timeout)ImageView iv_error_nomove_timeout;// 小车位置不改变超时图标
    @BindView(R.id.iv_error_close_connection)ImageView iv_error_close_connection;// 小车断开连接图标
    @BindView(R.id.iv_error_chargepile)ImageView iv_error_chargepile;// 充电桩故障图标

    @BindView(R.id.tv_pod_car_number) TextView tv_pod_car_number;// 小车或者POD在地图上的位置坐标
    @BindView(R.id.linear_agv_pod_locate) LinearLayout linear_agv_pod_locate;// 定位agv或者pod的线性布局
    @BindView(R.id.rbt_agv)RadioButton rbt_agv;// agv
    @BindView(R.id.rbt_pod)RadioButton rbt_pod;// pod
    @BindView(R.id.iv_open_close) ImageView iv_open_close;// 定位pod或者agv的图片控件
    @BindView(R.id.tv_agv_pod) TextView tv_agv_pod;// 显示输入号码的文本控件

    @BindView(R.id.linear_tips)LinearLayout linear_tips;// tips的线性布局
    @BindView(R.id.iv_tips_open_close)ImageView iv_tips_open_close;// tips的打开和关闭
    @BindView(R.id.tv_tips_select)TextView tv_tips_select;

    @BindView(R.id.recycler_view_tips)RecyclerView recycler_view_tips;// 展示提示内容用

    private String strAgvPodInput = "请键入号码 ";// 赋值agv或者pod的号码。请输入号码定位

    private int OPEN = 0;// 默认是0，即显示的是打开图标
    private int TIPS_OPEN = 0;// 默认是0

    private TextView tv_routeInfo;// 空车路径信息显示

    private int row = 0, column = 0;// 动态设置的行数和列数

    private RequestQueue requestQueue;// volley请求队列

    private ProgressDialog pDialog;

    private List<RobotEntity> carList = new ArrayList<>();// 小车信息集合
    private List<PodEntity> podList = new ArrayList<>();// pod信息集合
    private List<Long> unWalkedList = new ArrayList<>();// 不可走区域坐标集合
    private List<Integer> workStackList = new ArrayList<>();// 停止点集合，可以用来标识工作栈
    private List<List<Long>> rotateList = new ArrayList<>();// 旋转区坐标集
    private List<Long> storageList = new ArrayList<>();// 存储区坐标集
    private Map<String, String> map_work_site_uuid =  new HashMap<>();
    private List<StorageEntity> storageEntityList = new ArrayList<>();// 创建集合保存所有仓库所有的信息
    private List<ChargingPileEntity> chargingPileList = new ArrayList<>();// 声明集合保存充电桩的数据
    private List<CarCurrentPathEntity> carCurrentPathEntityList = new ArrayList<>();// 小车当前路径数据（锁格和未锁格）
    private List<Object> allLockedAreaList = new ArrayList<>();// 地图上所有的锁格区域地标
    private List<ChargingTaskEntity> chargingTaskEntityList = new ArrayList<>();// 充电桩的充电任务数据
    private List<RobotErrorEntity> robotErrorEntityList = new ArrayList<>();// 小车错误信息
    private List<NoMoveTimeoutEntity> noMoveTimeoutEntityList = new ArrayList<>();// 小车位置不改变超时
    private List<RobotCloseConnEntity> robotCloseConnEntityList = new ArrayList<>();// 小车断开连接
    private List<ErrorCharging> errorChargings = new ArrayList<>();// 充电桩故障
    private List<RtHeartTimeoutEntity> rtHeartTimeoutEntityList = new ArrayList<>();// 心跳或实时包未收到超时实体对象数据集

    private List<Long> car_route_list;// 某辆小车的路径
    private Map<Long, List<Long>> carRouteMap = new HashMap<>();;// 小车的路径map集

    private boolean bl_initData = false;// 是否初始化，false表示未进行过初始化
    private boolean bl_initStorageMap = false;// 仓库和地图是否初始化，false表示未进行初始化
    private boolean bl_isShowCarPath = false;// 小车当前路径是否显示，false表示没有显示
    private boolean bl_isSelectLockUnLock = false;// false表示没有选择解锁或者锁格

    // 点击小车弹框
    private View pop_view_carAll;
    private PopupWindow window_carAll;
    // 操作小车
    private View pop_view_car;
    private PopupWindow window_car;
    // pod
    private View pop_view_pod;
    private PopupWindow window_pod;
    // 地图格子(没有小车和pod)
    private View pop_view_box;
    private PopupWindow window_box;
    // 地图说明
    private View pop_view_mapIntroduction;
    private PopupWindow window_mapIntroduction;
    // 地图信息
    private View pop_view_mapInfo;
    private PopupWindow window_mapInfo;
    // 仓库初始化
    private View pop_view_storageInit;
    private PopupWindow window_storageInit;
    // 所有可操作项
    private View pop_view_menu;
    private PopupWindow window_menu;

    private RecyclerView recycler_view_menu;// 地图主界面的RecyclerView控件
//    private ImageView iv_menu_close;

    private View view_options = null;// 点击主页面地图上的格子，弹出的对话框的view
    private TextView tv_options_lock_circle, tv_options_unlock_circle, tv_options_resendOrder, tv_options_other;
    private AlertDialog dialog_options = null;// 点击主页面地图上的格子，弹出的对话框
    private  View view_options_resendOrder;

    private Thread threadMapData = null;// 地图数据消费线程
    private Thread publishThread = null;// 发布消息消费线程
    private Thread subscribeThread_storageMap = null;// 仓库地图消费者线程
    private Thread threadShowAllCarCurrentPath = null;// 所有小车当前路径消费线程
    private Thread subscribeThread = null;// 小车实时数据消费者线程
    private Thread threadChargingTask = null;// 充电桩充电任务消费线程
    private Thread threadProblemFeedback = null;// 小车错误反馈消费线程
    private Thread threadNoMoveTimeout = null;// 小车位置不改变超时线程
    private Thread threadErrorCloseConnection = null;// 小车断开连接线程
    private Thread threadChargingError = null;// 小车充电故障线程
    private Thread threadRtHeartTimeout = null;// 小车心跳或实时包未收到超时监听线程

    private Connection connection_map;// 初始化地图数据的连接
    private Connection connection_car;// 初始化小车数据的连接
    private Connection connection_storageMap;// 初始化仓库地图的连接（消费消息，获取数据）
    private Connection connection_showAllCarCurrentPath;// 小车当前路径的连接
    private Connection connection_chargingTask;// 充电桩充电任务的连接
    private Connection connection_problemFeedback;//小车错误反馈的连接
    private Connection connection_noMoveTimeout;// 小车位置不改变超时
    private Connection connection_errorCloseConnection;// 小车断开连接
    private Connection connection_chargingError;// 小车充电故障
    private Connection connection_RtHeartTimeout;// 小车心跳或实时包未收到超时

    private boolean bl_isStartTripTaskListener = true;// true表示开启了调度任务时长的监听
    private int boxSizeChange;// 放大或者缩小地图时需要的参数

    private TextView tv_carPath;// 小车当前路径信息
    private String str_carPath;// 小车的路径信息
    private ScreenUtil screenUtil;
    private int pop_height = 0;

    private RelativeLayout rl_storage;// 仓库选择布局
    private RelativeLayout rl_section_map;// 地图选择布局
    private TextView tv_storageName;// 仓库名称
    private TextView tv_mapName;// 地图名称

    private EditText et_pod_trip_message;// pod调度状态查看时输入的pod号码
    private TextView tv_pod_trip_message;// pod调度状态查看成功后的提示内容

    private long sectionRcsId = -1;// 绘制仓库下某个地图时需要用的变量

    private int carRoutePos = 0;
    private List<RobotEntity> carRouteList = null;

//    private int lockArea = 0;// 锁格坐标
//    private int unLockArea = 0;// 解锁坐标

    private CharSequence items[] = {"锁格", "解锁"};

    private CharSequence item_tips[] = {"货架碰撞处理提示","重车在通道上，小车故障处理提示","小车空扫pod处理提示"};
    private List<String> listTips = new ArrayList<>();// 每个选项的所有操作提示数据集
    private TipsAdapter tipsAdapter = null;

    private String strErrorContent = "";// 错误提示内容
    private TextView tv_error_content;// 错误提示内容设置控件

    private String strPodError = "", strNoMoveTimeout = "", strCloseConn = "", strChargingError = "";
    private TextView tv_error_scan_pod, tv_error_nomove_timeout, tv_error_close_connection, tv_error_chargepile;

    private Timer timer_clear_charge_data;// 声明Timer对象，用来执行定时任务
    private Timer timer_refresh_error_data;
    private TimerTask task_clear_charge_data = null;// 定时清空充电任务数据的任务
    private TimerTask task_refresh_error_data = null;// 定时刷新错误反馈数据

    private Vibrator vibrator = null;// 震动对象

    private List<Long> emptyRouteList = new ArrayList<>();// 空车路径信息
    private boolean bl_isDriveEmpty = false;// 默认空车路径未生成

    private List<MenuEntity> list_menu;// 选项集合
    private MenusAdapter menusAdapter;// 选项适配器

    // 主界面FloatingActionButton是否点击显示选项的开关。false表示点击无效，true表示点击有效
    private boolean bl_menuEnabled = false;

    private List<Integer> nine_lock_unlock = new ArrayList<>();// 九宫格的地址点位集合

    private List<PodAddressEntity> list_pod_address = new ArrayList<>();// 货架和点位对应的实体类集合
    private int removePodId = 0;// 声明变量表示移除货架的id
    private int removePodIdTemp = 0;// 临时变量，暂存移除的货架的id

    private SimpleDateFormat format;// 声明SimpleDateFormat对象，将时间解析成一定格式

    private boolean STOP_RELEASE_POD = true;// 中断 不间断释放pod功能的开关，true表示中断
    private String strWorkSiteUUID = "";// 点击地图上对应工作站的时候，保存该工作站的UUID，释放pod需要使用该参数

    // 处理handler发送的消息，然后进行操作（在主线程）
    @SuppressLint("HandlerLeak")
    private Handler inComingMessageHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case WHATMAP:// 初始化地图数据
                    // 地图绘制完成后，中断地图消费线程
                    interruptThread(threadMapData);

                    byte[] bodyMap = (byte[]) msg.obj;
                    Map<String, Object> mapMap = (Map<String, Object>) toObject(bodyMap);

                    try {
                        FileUtil.createFileWithByte(mapMap.toString().getBytes("utf-8"), "地图返回数据.doc");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if(mapMap != null && mapMap.size() != 0){
                        row = Integer.parseInt(mapMap.get("row").toString());// 设置行数
                        column = Integer.parseInt(mapMap.get("column").toString());// 设置列数

                        // 将所选地图的行数和列数保存到常量中，方便其他类的获取和使用
                        Constants.MAP_ROWS = row;
                        Constants.MAP_COLUMNS = column;

                        if(mapMap.containsKey("sectionUUID")){// 如果map中存在该key，那么获取该key所对应的value
                            sectionId = String.valueOf(mapMap.get("sectionUUID"));
                            Constants.SECTIONID = sectionId;
                            LogUtil.e("SECTIONID",""+sectionId);
                        }
                        setPodData(mapMap);// 设置pod数据
                        setUnWalkedCellData(mapMap);// 设置不可走区域的坐标数据
                        setStorageData(mapMap);// 设置存储区的坐标数据
                        setWorkStackData(mapMap);// 设置工作栈相关数据，工作栈在停止点的上方，或者下方，或者左方，或者右方
                        setWorkSiteUUID(mapMap);// 设置工作站的uuid。key对应的是停止点的坐标，值就是uuid
                        setChargerData(mapMap);// 设置充电桩的数据
                    }

                    if((row != 0) && (column != 0)){
                        tv_hint.setVisibility(View.GONE);
                        boxView.setVisibility(View.VISIBLE);

//                        visibile(linear_map_introduction);// 地图绘制后显示地图说明图标
//                        visibile(linear_map_info);// 地图绘制后显示地图信息图标
                        visibile(linear_zoomOutIn);// 地图绘制后显示放大和缩小图标
//                        visibile(linear_map_reset);
//                        visibile(linear_map_drawAgain);
//                        visibile(linear_map_carLockUnLock);
//                        visibile(linear_map_carBatteryInfo);
//                        visibile(linear_map_wcs);
//                        visibile(linear_map_rcs);

                        // 绘制地图
                        boxView.setRowAndColumn(row, column, boxSizeChange);
                        ToastUtil.showToast(getContext(),"地图绘制完成");// 提示地图绘制完成
//                        disMissDialog();// 绘制完成，消失对话框
                        btn_drawing.setTextColor(Color.GREEN);
                        bl_initData = true;

                        // 地图上绘制货架、不可走区域、工作栈、旋转区、存储区和充电桩
                        boxView.setPodData(podList, unWalkedList, workStackList,
                                rotateList, storageList, map_work_site_uuid, chargingPileList);

                        inComingMessageHandler.sendEmptyMessageDelayed(WHAT_CAR_DATA, DELAY_TIME);// 发送延时消息，自动开启小车监控
                    }

                    // 实时获取锁格和未锁格消息
//                    setUpConnectionFactory();
//                    subscribeShowAllCarCurrentPath(inComingMessageHandler);
                    break;
                case WHAT_CAR_DATA:// 初始化小车数据

                    initCarData();

                    // 地图绘制且开始地图监控后将bl_menuEnabled置为true，点击后实现弹出选项界面
                    bl_menuEnabled = true;
                    break;
                case WHATCAR:// 初始化小车数据
                    byte[] body = (byte[]) msg.obj;
                    Map<String, Object> mapCar = (Map<String, Object>) toObject(body);
//                    try {
//                        FileUtil.createFileWithByte(mapCar.toString().getBytes("utf-8"),"Mushiny小车数据文件.doc");
//                        ToastUtil.showToast(getContext(), "文件生成成功");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
                    if(mapCar != null && mapCar.size() != 0){
                        setCarAndPodData(mapCar);// 设置小车的数据
                    }
                    if(carList != null && carList.size() != 0){
                        boxView.setCarAndPodData(carList, podList);// 小车开始绘制并实时更新小车的路径信息
                    }
                    break;
                case WHAT_CAR_ROUTE:// 小车路径信息查看
                    if(!TextUtils.isEmpty(str_carPath)){
                        tv_carPath.setText(getResources().getString(R.string.box_car_path) + ":" + str_carPath);
                    }

                    break;
                case WHATSTORAGEMAP:// 初始化仓库地图
                    byte[] bodyStorageMap = (byte[]) msg.obj;
                    Map<String, Object> mapStorageMap = (Map<String, Object>) toObject(bodyStorageMap);
                    JSONObject objectStorageMap = new JSONObject(mapStorageMap);// 将map转为JsonObject结构数据
                    LogUtil.e("TAG_init", objectStorageMap.toString());
                    if(objectStorageMap != null){
                        parseStorageMapData(objectStorageMap);// 解析仓库地图数据
                    }
                    // 仓库初始化完成，中断消费线程
                    if(subscribeThread_storageMap != null){
                        subscribeThread_storageMap.interrupt();
                    }

//                    disMissDialog();
                    ProgressBarUtil.dissmissProgressBar();

                    new AlertDialog.Builder(getContext())
                            .setIcon(R.mipmap.app_icon)
                            .setTitle("提示")
                            .setMessage("RabbitMQ连接成功！")
                            .setCancelable(false)
                            .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    showPopAboutInitStorage();// 弹框进行仓库地图的初始化
                                }
                            }).create().show();

//                    showPopAboutInitStorage();// 弹框进行仓库地图的初始化

                    break;
                case WHAT_CAR_ROUTE_SHOW:// 小车全路径显示（一辆或者多辆）
                    carRouteMap.put(carRouteList.get(carRoutePos).getRobotID(), car_route_list);// 保存小车的路径信息
                    boxView.setCarRouteData(carRouteMap);// 重绘，显示小车的路径

                    // 将该小车的路径显示标志置为true，表示小车的路径已经显示
                    RobotEntity entity = new RobotEntity();
                    entity.setRobotID(carRouteList.get(carRoutePos).getRobotID());
                    entity.setAddressCodeID(carRouteList.get(carRoutePos).getAddressCodeID());
                    entity.setCarRouteIsShow(true);
                    carList.remove(carRoutePos);// 移除对应位置的小车
                    carList.add(carRoutePos, entity);// 在对应位置添加新的小车实体
                    break;
                case WHAT_SHOW_POD:// 显示pod状态
                    if(TextUtils.isEmpty(podName)){
                        ToastUtil.showToast(getContext(),"当前工作站没有pod");
                    }else{
                        call_releasePod();// 释放pod
                    }
                    break;
                case WHAT_SHOW_ALL_CAR_CURRENT_PATH:// 小车当前路径（实时显示）
                    disMissDialog();
                    bl_isShowCarPath = true;
                    byte[] bodyShowAllCarCurrentPath = (byte[]) msg.obj;
                    Map<String, Object> mapShow = (Map<String, Object>) toObject(bodyShowAllCarCurrentPath);
                    try {
                        FileUtil.createFileWithByte(mapShow.toString().getBytes("utf-8"), "小车所有路径数据.doc");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if(mapShow != null && mapShow.size() != 0){
                        setCarCurrentPath(mapShow);// 设置小车当前路径数据
                        boxView.setCarCurrentPath(carCurrentPathEntityList, allLockedAreaList, manualLockList);// 绘制小车的当前路径区域（锁格和未锁格）
                    }
                    break;
                case WHAT_CANCEL_CAR_ALL_CURRENT_PATH:// 取消小车当前路径显示
                    bl_isShowCarPath = false;
                    interruptThread(t_cancel_car_all_current_path);
                    carCurrentPathEntityList.clear();
                    boxView.setCarCurrentPath(null, null, null);
                    ToastUtil.showToast(getContext(), "小车当前路径已经取消");
                    break;
                case WHAT_CHARGING_TASK:// 充电桩的充电任务
                    byte[] bodyChargingTask = (byte[]) msg.obj;
                    Map<String, Object> mapCharge = (Map<String, Object>) toObject(bodyChargingTask);
//                    try {
//                        FileUtil.createFileWithByte(mapCharge.toString().getBytes("utf-8"), "充电任务数据.doc");
//                    } catch (UnsupportedEncodingException e) {
//                        e.printStackTrace();
//                    }
                    if(mapCharge != null && mapCharge.size() != 0){
                        setChargingTaskData(mapCharge);// 设置充电任务数据
                        boxView.setChargeData(chargingTaskEntityList, sectionId);

                    }
                    break;
                case WHAT_CLEAR_DATA:// 仓库地图重选，清空所有的数据
                    interruptThread(subscribeThread);
                    interruptThread(t_clear_all_data);
                    interruptThread(threadChargingTask);
                    interruptThread(threadProblemFeedback);
                    interruptThread(threadNoMoveTimeout);
                    interruptThread(threadErrorCloseConnection);
                    interruptThread(threadChargingError);
                    interruptThread(threadRtHeartTimeout);
                    interruptThread(threadShowAllCarCurrentPath);
                    clearData();
                    publishToAMPQ(Constants.MQ_EXCHANGE_STORAGEMAP, Constants.MQ_ROUTINGKEY_STORAGEMAP_REQUEST);// publish消息给请求队列
                    selectStorageMap();
                    break;
                case WHAT_CLOSE_CONNECTION:// 关闭连接
                    interruptThread(threadCloseConnection);
                    carCurrentPathEntityList.clear();
                    allLockedAreaList.clear();
                    boxView.setCarCurrentPath(null, null, null);
                    break;
                case WHAT_CLEAR_CHARGE_DATA:// 定时清空充电任务数据
                    boxView.setChargeData(null, sectionId);
                    break;
                case WHAT_ROBOT_ERROR:// AGV扫不到pod
                    byte[] bodyProblemFeedback = (byte[]) msg.obj;
                    Map<String, Object> mapProblemFeedback = (Map<String, Object>) toObject(bodyProblemFeedback);
                    try {
                        FileUtil.createFileWithByte(mapProblemFeedback.toString().getBytes("utf-8"),"AGV扫不到pod.doc");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                    if (robotErrorEntityList.size() == 0){
                        if (vibrator != null){
                            vibrator.vibrate(new long[]{1000,2000}, 0);// 震动
                        }
                    }
                    setRobotErrorData(mapProblemFeedback);
                    visibile(linear_error_tip);
                    visibile(iv_error_scan_pod);
                    break;
                case WHAT_ROBOT_NOMOVE_TIMEOUT:// 小车位置不改变超时
                    byte[] bodyNoMoveTimeout = (byte[]) msg.obj;
                    Map<String, Object> mapNoMoveTimeout = (Map<String, Object>) toObject(bodyNoMoveTimeout);
                    try {
                        FileUtil.createFileWithByte(mapNoMoveTimeout.toString().getBytes("utf-8"),"位置不改变超时.doc");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (noMoveTimeoutEntityList.size() == 0){
                        if (vibrator != null){
                            vibrator.vibrate(new long[]{1000,2000}, 0);// 震动
                        }
                    }
                    LogUtil.e("errorShow", "WHAT_ROBOT_NOMOVE_TIMEOUT");
                    setTimeoutData(mapNoMoveTimeout);
                    visibile(linear_error_tip);
                    visibile(iv_error_nomove_timeout);
                    break;

                case WHAT_RT_HEART_TIMEOUT:// 小车心跳或实时包未收到超时
                    byte[] bodyRtHeartTimeout = (byte[]) msg.obj;
                    Map<String, Object> mapRtHeartTimeout = (Map<String, Object>) toObject(bodyRtHeartTimeout);
                    try{
                        FileUtil.createFileWithByte(mapRtHeartTimeout.toString().getBytes("utf-8"),"小车心跳或实时包未收到超时.doc");
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    if (rtHeartTimeoutEntityList.size() == 0){// 第一次收到数据就震动提示工程师
                        if (vibrator != null){
                            vibrator.vibrate(new long[]{1000, 2000}, 0);// 震动
                        }
                    }
                    setRtHeartTimeoutData(mapRtHeartTimeout);
                    visibile(linear_error_tip);
                    break;
                case WHAT_REFRESH_ERROR_DATA:// 刷新错误数据显示
                    clearErrorContent();
                    getAndSetErrorContent();
                    break;
                case WHAT_ERROR_CLOSE_CONNECTION:// 小车断开连接
                    byte[] bodyErrorCloseConn = (byte[]) msg.obj;
                    Map<String, Object> mapErrorCloseConn = (Map<String, Object>) toObject(bodyErrorCloseConn);
                    if (robotCloseConnEntityList.size() == 0){
                        if (vibrator != null){
                            vibrator.vibrate(new long[]{1000,2000}, 0);// 震动
                        }
                    }
                    setErrorCloseConn(mapErrorCloseConn);
                    visibile(linear_error_tip);
                    visibile(iv_error_close_connection);
                    break;

                case WHAT_ROBOT_CHARGING_ERROR:// 充电桩故障（这里获取的是所有充电桩的信息，每次都会收到一条消息）
                    byte[] bodyChargingError = (byte[]) msg.obj;
//                    JSONObject jbChargingError = (JSONObject) toObject(bodyChargingError);
                    Map<String, Object> mapChargingError = (Map<String, Object>) toObject(bodyChargingError);
                    // 生成文件
                    try {
                        FileUtil.createFileWithByte(mapChargingError.toString().getBytes("utf-8"),"小车充电状态返回数据文件.doc");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (errorChargings.size() == 0 && bl_isChargingError){// 第一次就震动提示工程师
                        if (vibrator != null){
                            vibrator.vibrate(new long[]{1000,2000}, 0);// 震动
                        }
                        visibile(linear_error_tip);// 故障提示
                        visibile(iv_error_chargepile);
                    }
                    if (mapChargingError.toString() != null){
                        setChargingErrorData(mapChargingError);// 设置充电故障数据
                    }
                    break;

                case WHAT_CREATE_EMPTY_ROUTE:// 生成空车路径成功

                    String strRouteInfo = (String) msg.obj;
                    if (!TextUtils.isEmpty(strRouteInfo)){
                        tv_routeInfo.setText(strRouteInfo);// 设置空车路径信息
                        route2List(strRouteInfo);// 将路径信息转为list保存
                        bl_isDriveEmpty = true;
                    }
                    break;

                case WHAT_POD_AGV_ADDRESS_HIDE:// 隐藏位置信息
                    gone(tv_pod_car_number);
                    break;

                case WHAT_AUTO_DRAW_MAP:// 延迟消息绘制地图

                    try {
                        Map<String, Object> message = new HashMap<>();
                        message.put("name", Constants.MQ_ROUTINGKEY_MAP_REQUEST);
                        message.put("requestTime", System.currentTimeMillis());// 系统当前时间
                        message.put("sectionID", sectionRcsId);// 根据该值来确定绘制仓库下的哪个地图
                        queue.putLast(message);// 发送消息到MQ
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    break;
                case WHAT_RELEASE_POD:// 不间断释放pod

                    LogUtil.e("release", "延迟释放执行");
                    releasePodForever();// 不间断释放pod

                    break;
                case WHAT_TRIP_TASK_LISTENER:// 调度任务时长监听

                    tripTaskListen();

                    break;
            }
        }
    };

    /**
     * 调度任务时长监听
     */
    private void tripTaskListen() {

        if (bl_isStartTripTaskListener){
            call_tripTaskListen();
            // 延迟轮循，执行调度任务时长监听
            inComingMessageHandler.sendEmptyMessageDelayed(WHAT_TRIP_TASK_LISTENER, TIME_TRIP_TASK_LISTENER);
        }

    }

    private AlertDialog dialog_trip_task_listen = null;// 调度任务时间过长的弹框提示

    /**
     * 调接口获取是否存在调度任务时长过长的调度任务
     */
    private void call_tripTaskListen() {

//        LogUtil.e("triptask","时长监听");
        String url = getResources().getString(R.string.url_tripTaskListen);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try{
                            String msg = String.valueOf(response.opt("msg"));
                            if (!TextUtils.isEmpty(msg)){
                                // 1表示存在时间过长的调度任务
                                if ("1".equals(msg)){
                                    if (dialog_trip_task_listen == null){
                                        dialog_trip_task_listen = new AlertDialog.Builder(getContext())
                                                .setIcon(R.mipmap.app_icon)
                                                .setTitle("提示")
                                                .setMessage("存在时间过长的调度任务，请登录WMS前端页面：在车辆管理的调度单模块进行查看。请及时进行处理，确保调度任务的正常运行。")
                                                .setCancelable(false)// 触摸弹框外侧区域不会取消弹框提示
                                                .setPositiveButton("我知道了", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                }).create();
                                        dialog_trip_task_listen.show();
                                    }else {
                                        dialog_trip_task_listen.show();
                                    }
                                }
                            }

                        }catch (Exception e){
                            ToastUtil.showToast(getContext(), "调度任务时长数据解析异常：" + e.getMessage());
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ToastUtil.showToast(getContext(), "调度任务时长异常：" + error.getMessage());
                    }
                }
        );

        requestQueue.add(request);

    }

    /**
     * 不间断释放pod
     */
    private void releasePodForever() {

        if (!STOP_RELEASE_POD){
            call_showPod(strWorkSiteUUID);
            inComingMessageHandler.sendEmptyMessageDelayed(WHAT_RELEASE_POD, RELEASE_POD_TIME);
        }

    }

    /**
     * 设置小车心跳或实时包未收到超时
     * @param mapRtHeartTimeout
     */
    private void setRtHeartTimeoutData(Map<String, Object> mapRtHeartTimeout) {

        try{

            // 取值
            int robotId = Integer.parseInt(String.valueOf(mapRtHeartTimeout.get("robotID")));
//            int type = Integer.parseInt(String.valueOf(mapRtHeartTimeout.get("type")));
            String time = format.format(new Date(Long.parseLong(String.valueOf(mapRtHeartTimeout.get("time")))));
            // 实体对象设值
            RtHeartTimeoutEntity entity = new RtHeartTimeoutEntity();
            entity.setRobotId(robotId);
//            entity.setType(type);
            entity.setTime(time);

            // list集合中保存实体对象
            if (rtHeartTimeoutEntityList.size() == 0){
                rtHeartTimeoutEntityList.add(entity);
            }else{
                boolean bl_isAdd = false;
                int index = -1;

                for (int i = 0;i < rtHeartTimeoutEntityList.size();i++){
                    if (robotId == rtHeartTimeoutEntityList.get(i).getRobotId()){
                        bl_isAdd = true;
                        index = i;
                    }
                }

                // 集合中已经添加过该实体对象了
                if (bl_isAdd){
                    rtHeartTimeoutEntityList.remove(index);
                    rtHeartTimeoutEntityList.add(index, entity);
                }else{
                    rtHeartTimeoutEntityList.add(entity);
                }
            }


        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(), "心跳或实时包未收到超时数据解析异常：" + e.getMessage());

        }

    }

    /**
     * 将路径信息保存到list中
     * @param strRouteInfo
     */
    private void route2List(String strRouteInfo) {
        try{
            JSONArray arrayRoute = new JSONArray(strRouteInfo);
            for(int i = 0;i < arrayRoute.length(); i++){
                emptyRouteList.add(Long.parseLong(String.valueOf(arrayRoute.opt(i))));
            }
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(), "空车路径信息解析异常");
        }
    }

    private boolean bl_isChargingError = false;// false 表示充电桩无故障
    /**
     * 解析充电桩故障数据
     * @param chargingError
     */
    private void setChargingErrorData(Map<String, Object> chargingError) {
        try{

            /**
             * ｛number=2， sectionID=1, time=1527090817342, type=1, statusName=空闲, statusIndex=2｝
             */

            // 取值
            int number = Integer.parseInt(String.valueOf(chargingError.get("number")));
            int statusIndex = Integer.parseInt(String.valueOf(chargingError.get("statusIndex")));
            String statusName = String.valueOf(chargingError.get("statusName"));// 故障:4 ,空闲:2 ,充电:1
            String type = String.valueOf(chargingError.get("type"));// 充电桩类型
            long time = Long.parseLong(String.valueOf(chargingError.get("time")));

            if (4 == statusIndex){
                // 实体对象设值
                ErrorCharging entity = new ErrorCharging();
                entity.setNumber(number);
                entity.setStatusIndex(statusIndex);
                entity.setStatusName(statusName);
                entity.setType(type);
                entity.setTime(time);

                // list集合中保存实体对象
                if (errorChargings.size() == 0){
                    errorChargings.add(entity);
                    bl_isChargingError = true;
                }else{
                    bl_isChargingError = false;
                    boolean bl_isAdd = false;
                    int index = -1;

                    for (int i = 0;i < errorChargings.size();i++){
                        if (number == errorChargings.get(i).getNumber()){
                            bl_isAdd = true;
                            index = i;
                        }
                    }

                    // 集合中已经添加过该实体对象了
                    if (bl_isAdd){
                        errorChargings.remove(index);
                        errorChargings.add(index, entity);
                    }else{
                        errorChargings.add(entity);
                    }
                }
            }


        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(),"故障数据解析异常");
        }
    }

    /**
     * 设置小车断开连接数据
     * @param mapErrorCloseConn
     */
    private void setErrorCloseConn(Map<String, Object> mapErrorCloseConn) {
        try {
            // 取值
            int robotID = Integer.parseInt(String.valueOf(mapErrorCloseConn.get("robotID")));

            int port = -1;
            if (mapErrorCloseConn.containsKey("port")){
                port = Integer.parseInt(String.valueOf(mapErrorCloseConn.get("port")));
            }

            String ip = "";
            if (mapErrorCloseConn.containsKey("ip")){
                ip = String.valueOf(mapErrorCloseConn.get("ip"));
            }

            long time = Long.parseLong(String.valueOf(mapErrorCloseConn.get("time")));

            // 实体对象设值
            RobotCloseConnEntity entity = new RobotCloseConnEntity();
            entity.setRobotID(robotID);
            entity.setPort(port);
            entity.setIp(ip);
            entity.setTime(time);

            // list集合中保存实体对象
            if (robotCloseConnEntityList.size() == 0){
                robotCloseConnEntityList.add(entity);
            }else{
                boolean bl_isAdd = false;
                int index = -1;

                for (int i = 0;i < robotCloseConnEntityList.size();i++){
                    if (robotID == robotCloseConnEntityList.get(i).getRobotID()){
                        bl_isAdd = true;
                        index = i;
                    }
                }

                // 集合中已经添加过该实体对象了
                if (bl_isAdd && index != -1){
                    robotCloseConnEntityList.remove(index);
                    robotCloseConnEntityList.add(index, entity);
                }else{
                    robotCloseConnEntityList.add(entity);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(),"小车断开连接数据解析异常");
        }
    }

    /**
     * 设置小车位置不改变超时数据
     * @param mapNoMoveTimeout
     */
    private void setTimeoutData(Map<String, Object> mapNoMoveTimeout) {

        try {
            // 取值
            int robotID = Integer.parseInt(String.valueOf(mapNoMoveTimeout.get("robotID")));
            int port = Integer.parseInt(String.valueOf(mapNoMoveTimeout.get("port")));
            String ip = String.valueOf(mapNoMoveTimeout.get("ip"));
            long currentAddress = Long.parseLong(String.valueOf(mapNoMoveTimeout.get("currentAddress")));

            // 实体对象设值
            NoMoveTimeoutEntity entity = new NoMoveTimeoutEntity();
            entity.setRobotID(robotID);
            entity.setPort(port);
            entity.setIp(ip);
            entity.setCurrentAddress(currentAddress);

            // list集合中保存实体对象
            if (noMoveTimeoutEntityList.size() == 0){
                noMoveTimeoutEntityList.add(entity);
            }else{
                boolean bl_isAdd = false;
                int index = -1;

                for (int i = 0;i < noMoveTimeoutEntityList.size();i++){
                    if (robotID == noMoveTimeoutEntityList.get(i).getRobotID()){
                        bl_isAdd = true;
                        index = i;
                    }
                }

                // 集合中已经添加过该实体对象了
                if (bl_isAdd && index != -1){
                    noMoveTimeoutEntityList.remove(index);
                    noMoveTimeoutEntityList.add(index, entity);
                }else{
                    noMoveTimeoutEntityList.add(entity);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(),"小车位置不改变超时数据解析异常");
        }

    }

    /**
     * 设置小车错误数据
     * @param mapRobotError
     */
    private void setRobotErrorData(Map<String, Object> mapRobotError) {
        try {
            // 取值
            int robotID = Integer.parseInt(String.valueOf(mapRobotError.get("robotID")));
            long errorTime = Long.parseLong(String.valueOf(mapRobotError.get("errorTime")));
            String errorID = String.valueOf(mapRobotError.get("errorID"));
            String errorStatus = String.valueOf(mapRobotError.get("errorStatus"));
            int podCodeID = Integer.parseInt(String.valueOf(mapRobotError.get("podCodeID")));
            int curPodID = Integer.parseInt(String.valueOf(mapRobotError.get("curPodID")));
            String sectionID = String.valueOf(mapRobotError.get("sectionID"));

            // 实体对象设置值
            RobotErrorEntity entity = new RobotErrorEntity();
            entity.setRobotID(robotID);
            entity.setErrorTime(errorTime);
            entity.setErrorID(errorID);
            entity.setErrorStatus(errorStatus);
            entity.setPodCodeID(podCodeID);
            entity.setCurPodID(curPodID);
            entity.setSectionID(sectionID);

            // list集合中保存实体对象
            if (robotErrorEntityList.size() == 0){
                robotErrorEntityList.add(entity);
            }else{
                boolean bl_isAdd = false;
                int index = -1;

                for (int i = 0;i < robotErrorEntityList.size();i++){
                    if (robotID == robotErrorEntityList.get(i).getRobotID()){
                        bl_isAdd = true;
                        index = i;
                    }
                }

                // 集合中已经添加过该实体对象了
                if (bl_isAdd && index != -1){
                    robotErrorEntityList.remove(index);
                    robotErrorEntityList.add(index, entity);
                }else{
                    robotErrorEntityList.add(entity);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(),e.getMessage());
            ToastUtil.showToast(getContext(),"小车错误反馈数据解析异常");
        }
    }

    private Thread t_cancel_car_all_current_path;// 取消小车路径显示
    private Thread t_clear_all_data;// 重选地图清空所有数据
    private Thread threadCloseConnection;
    private List<Object> carLockList = new ArrayList<>();// 保存所有小车的路径坐标（不重复）
    private List<Object> manualLockList = new ArrayList<>();// 保存手动锁格的区域坐标，不包含小车的锁格坐标
    private String rootAddress = "";

    /**
     * 设置小车当前路径数据
     * @param mapShow
     */
    private void setCarCurrentPath(Map<String, Object> mapShow) {
        try {

            list_pod_address.clear();
            // 获取货架和对应点位，存入集合
            List<Map<String, Object>> map_pod_address = (List<Map<String, Object>>) mapShow.get("podList");
            if (map_pod_address != null && map_pod_address.size() != 0){
                for (int i = 0;i < map_pod_address.size();i++){
                    Map<String, Object> map = map_pod_address.get(i);
                    // 取值
                    int podCodeID = Integer.parseInt(String.valueOf(map.get("podCodeID")));
                    int addressCodeID = Integer.parseInt(String.valueOf(map.get("addressCodeID")));
                    // 赋值
                    PodAddressEntity entity = new PodAddressEntity();
                    entity.setPodCodeID(podCodeID);
                    entity.setAddressCodeID(addressCodeID);
                    // 保存实体对象
                    list_pod_address.add(entity);
                }
            }

            // 判断货架是否从地图上移除了
            if (removePodId != 0){
                for (int i = 0;i < list_pod_address.size();i++){
                    int podCodeID = list_pod_address.get(i).getPodCodeID();
                    int addressCodeID = list_pod_address.get(i).getAddressCodeID();
                    if (removePodId == podCodeID && addressCodeID == 0){
                        ToastUtil.showToast(getContext(), "亲爱的工程师！已经成功将货架"
                                + removePodId + "从地图上面移除");
                        // 重新赋值为0
                        removePodId = 0;
                    }
                }
            }

            carCurrentPathEntityList.clear();// 清空集合中所有的数据，之后集合变为一个空的集合
            List<Map<String, Object>> list = (List<Map<String, Object>>) mapShow.get("agvList");
            if(list != null && list.size() != 0){
                for (int i = 0;i < list.size();i++){
                    Map<String, Object> map = list.get(i);
                    // 取值
//                    long robotID = (long) map.get("robotID");
                    long robotID = Long.parseLong(String.valueOf(map.get("robotID")));

                    List<Long> longs_lock = (List<Long>) map.get("currentSeriesPath");// 锁格区域
                    List<Long> longs_all = (List<Long>) map.get("currentGlobalSeriesPath");// 锁格和未锁格区域
                    // 实体对象设值
                    CarCurrentPathEntity entity = new CarCurrentPathEntity();
                    entity.setRobotID(robotID);
                    entity.setLockPath(longs_lock);
                    entity.setAllPath(longs_all);
                    // 添加实体对象，保存到集合
                    carCurrentPathEntityList.add(entity);
                }

                /*carLockList.clear();
                for (int i = 0;i < list.size();i++){
                    Map<String, Object> map = list.get(i);
                    List<Long> longs_lock = (List<Long>) map.get("currentSeriesPath");// 锁格区域
                    if(carLockList == null || carLockList.size() == 0){
                        if(longs_lock != null && longs_lock.size() != 0){
                            for (long l : longs_lock){
                                carLockList.add(l);
                            }
                        }
                    }else {
                        if(longs_lock != null && longs_lock.size() != 0){
                            for (int k = 0;k < longs_lock.size();k++){
                                Object carLockObject = longs_lock.get(k);
                                if(!carLockList.contains(carLockObject)){
                                    carLockList.add(carLockObject);// 添加集合中不存在的锁格坐标，确保锁格坐标不重复
                                }
                            }
                        }

                    }
                }*/

            }

            allLockedAreaList = (List<Object>) mapShow.get("lockedList");// 所有锁格区域地标

//            manualLockList.clear();
//            if(allLockedAreaList != null && allLockedAreaList.size() != 0){
//                for (int i = 0;i < allLockedAreaList.size(); i++){
//                    Object object = allLockedAreaList.get(i);
//                    long long_object = Long.parseLong(object.toString());
//                    if(!carLockList.contains(long_object)){
//                        manualLockList.add(object);
//                    }
//                }
//            }

            // 锁格状态判断，现在的版本是可以选择多个点锁格的，下面已经不适用
//            if(lockArea != 0){
//                if(allLockedAreaList.contains(lockArea)){
//                    ToastUtil.showToast(getContext(), "锁格成功");
//                    lockArea = 0;
//                }
//            }
//
//            if(unLockArea != 0){
//                if(!allLockedAreaList.contains(unLockArea)){
//                    ToastUtil.showToast(getContext(), "解锁成功");
//                    unLockArea = 0;
//                }
//            }

            // 不显示小车当前路径的情况下，显示手动锁格的区域；如果显示小车当前路径，那么显示所有的锁格区域
//            if(bl_isManualLock){
//                allLockedAreaList.clear();
//                carCurrentPathEntityList.clear();
//            }else {
//                manualLockList.clear();
//            }

        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(),"小车当前路径信息数据解析异常");
        }
    }

    /**
     * 设置充电任务数据
     * @param mapCharge
     */
    private void setChargingTaskData(Map<String, Object> mapCharge) {
        try {
            chargingTaskEntityList.clear();// 每次清空集合中的数据
            List<Map<String, Object>> list = (List<Map<String, Object>>) mapCharge.get("chargeList");
            if(list == null || list.size() == 0){
                boxView.setChargeData(null, sectionId);
                return;
            }
            int len = list.size();
            for(int i = 0;i < len;i++){
                Map<String, Object> map = list.get(i);
                ChargingTaskEntity entity = new ChargingTaskEntity();
                // 取值
//                String warehouseId = (String) map.get("warehouseId");
                String warehouseId = String.valueOf(map.get("warehouseId"));
                String sectionUUID = "";
                if(map.containsKey("sectionUUID")){
//                    sectionUUID = (String) map.get("sectionUUID");
                    sectionUUID = String.valueOf(map.get("sectionUUID"));
                }else if(map.containsKey("sectionId")){
//                    sectionUUID = (String) map.get("sectionId");
                    sectionUUID = String.valueOf(map.get("sectionId"));
                }

//                String orderId = (String) map.get("orderId");
                String orderId = String.valueOf(map.get("orderId"));
//                String tripState = (String) map.get("tripState");
                String tripState = String.valueOf(map.get("tripState"));
//                String driveId = (String) map.get("driveId");
                String driveId = String.valueOf(map.get("driveId"));

                String chargeUUID = "";
                if(map.containsKey("chargeUUID")){
//                    chargeUUID = (String) map.get("chargeUUID");
                    chargeUUID = String.valueOf(map.get("chargeUUID"));
                }else if(map.containsKey("chargeId")){
//                    chargeUUID = (String) map.get("chargeId");
                    chargeUUID = String.valueOf(map.get("chargeId"));
                }

                String robotAddressCodeId = "";
                if (map.containsKey("robotAddressCodeId")){
                    robotAddressCodeId = String.valueOf(map.get("robotAddressCodeId"));
                }
                // 实体对象设值
                entity.setWarehouseId(warehouseId);
                entity.setSectionUUID(sectionUUID);
                entity.setOrderId(orderId);
                entity.setTripState(tripState);
                entity.setDriveId(driveId);
                entity.setChargeUUID(chargeUUID);
                entity.setRobotAddressCodeId(robotAddressCodeId);
                // 往集合中添加实体对象
                chargingTaskEntityList.add(entity);
            }
            LogUtil.e("chargingTaskEntityList",chargingTaskEntityList.toString());
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(),"充电任务数据解析异常");
        }
    }

    private String strStorageMapName = "";// 声明一个变量保存仓库和地图的名称

    /**
     * popupwindow进行仓库地图的初始化选择
     */
    private void showPopAboutInitStorage() {
        // 构建popupwindow的布局
        pop_view_storageInit = getLayoutInflater().inflate(R.layout.popupwindow_storage_init, null);
        int popWidth = screenUtil.getScreenSize(ScreenUtil.WIDTH) - DensityUtil.dp2px(getContext(),40);
        int popHeight = DensityUtil.dp2px(getContext(), 180);// 高度是180dp
        // 构建PopupWindow对象
        window_storageInit = new PopupWindow(pop_view_storageInit, popWidth, popHeight);

        rl_storage = pop_view_storageInit.findViewById(R.id.rl_storage);
        rl_section_map = (RelativeLayout) pop_view_storageInit.findViewById(R.id.rl_section_map);

        tv_storageName = pop_view_storageInit.findViewById(R.id.tv_storageName);
        tv_mapName = pop_view_storageInit.findViewById(R.id.tv_mapName);

        // 设置弹框的动画
        window_storageInit.setAnimationStyle(R.style.pop_anim);
        // 设置背景透明
        window_storageInit.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00000000")));
        // 设置获取焦点
        window_storageInit.setFocusable(true);
        // 设置触摸区域外可消失
        window_storageInit.setOutsideTouchable(true);
        // 实时更新状态
        window_storageInit.update();
        // 根据偏移量确定在parent view中的显示位置
        window_storageInit.showAtLocation(rl_mapView, Gravity.CENTER, 0, 0);

        // 背景透明度改变，优化用户体验
        bgAlpha(0.618f);
        window_storageInit.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                bgAlpha(1.0f);
            }
        });

        Button btn_storage_init = pop_view_storageInit.findViewById(R.id.btn_storage_init);// 确认按钮
        btn_storage_init.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {// 确认按钮监听
                if(getResources().getString(R.string.str_please_select).equals(tv_storageName.getText().toString())){
                    ToastUtil.showToast(getContext(),"请选择仓库");
                    return;
                }else{
                    if(getResources().getString(R.string.str_please_select).equals(tv_mapName.getText().toString())){
                        ToastUtil.showToast(getContext(),"请选择地图");
                        return;
                    }else {
                        if(sectionRcsId != -1){// 仓库下的某张地图存在时
                            bl_initStorageMap = true;// 表示仓库和地图的选择完成，将bl_initStorageMap置为true
                            window_storageInit.dismiss();
                            ToastUtil.showToast(getContext(),"初始化，仓库和地图选择完成");
                            btn_selectStorageMap.setTextColor(Color.GREEN);

                            LogUtil.e("step1", "auto draw");

                            ProgressBarUtil.showProgressBar(getContext(), "正在开启监控...", getResources().getColor(R.color.colorAccent));
                            setUpConnectionFactory();// 连接设置
                            subscribeMapData(inComingMessageHandler);// (这里需要先绑定队列，防止队列接收不到消息)发送地图数据请求到MQ，开始获取地图数据
                            // 发送消息到MQ 从MQ上拿到地图的行和列数据，然后绘制地图
                            publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_MAP_REQUEST);
                            // 这时候发送延迟消息代替手动点击按钮执行绘制地图操作
                            inComingMessageHandler.sendEmptyMessageDelayed(WHAT_AUTO_DRAW_MAP, DELAY_TIME);
                        }
                    }
                }
            }
        });

        chooseStorageMap();// 选择仓库地图
    }

    private int checkedItemStorage = -1;
    private int checkedItemSectionMap = -1;
    private List<StorageEntity.SectionEntity> section_map = null;// 声明集合，表示某个仓库下的地图集
    private String str_storage_map_name = "";// 声明变量保存仓库和地图选择信息
    /**
     * 选择仓库
     */
    private void chooseStorageMap() {
        final List<List<StorageEntity.SectionEntity>> sectionMapList = new ArrayList<>();// 用来保存所有仓库下的地图集
        // 仓库选择监听
        rl_storage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(storageEntityList.size() == 0){
                    ToastUtil.showToast(getContext(), "仓库数据为空");
                }else {

                    final CharSequence[] items_storage = new CharSequence[storageEntityList.size()];// 保存仓库名称
                    final CharSequence[] items_houseId = new CharSequence[storageEntityList.size()];// 保存仓库ID
                    for(int i = 0;i < storageEntityList.size();i++){
                        String storageName = storageEntityList.get(i).getWarehouseName();// 仓库名称
                        items_storage[i] = storageName;// 赋值仓库名称

                        // 获取warehouseId
                        String warehouseId = storageEntityList.get(i).getWarehouseId();
                        items_houseId[i] = warehouseId;

                        List<StorageEntity.SectionEntity> sectionMap = storageEntityList.get(i).getSectionMap();
                        sectionMapList.add(sectionMap);// 添加某个仓库下的地图数据
                    }

                    new AlertDialog.Builder(getContext()).setSingleChoiceItems(items_storage, checkedItemStorage, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkedItemStorage = which;// 赋值选中项的位置
                            tv_storageName.setText(items_storage[which]);
                            section_map = sectionMapList.get(which);// 赋值仓库下的地图数据
                            str_storage_map_name = "当前仓库名称：" + items_storage[which];
                            // 赋值所选仓库的id
                            Constants.WAREHOUSEID = String.valueOf(items_houseId[which]);
                            LogUtil.e("WAREHOUSEID","" + Constants.WAREHOUSEID);
                            dialog.dismiss();
                        }
                    }).create().show();
                }

            }
        });

        // 仓库下地图的选择监听
        rl_section_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(getResources().getString(R.string.str_please_select).equals(tv_storageName.getText().toString())){
                    ToastUtil.showToast(getContext(),"请先选择仓库");
                }else {
                    final CharSequence[] items_sectionMap = new CharSequence[section_map.size()];// 保存地图名称
                    for (int i = 0;i< section_map.size();i++){
                        String mapName = section_map.get(i).getSectionName();// 某个地图名称
                        items_sectionMap[i] = mapName;// 赋值地图名称
                    }

                    new AlertDialog.Builder(getContext()).setSingleChoiceItems(items_sectionMap, checkedItemSectionMap, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            checkedItemSectionMap = which;
                            tv_mapName.setText(items_sectionMap[which]);// 显示地图名称
                            sectionRcsId = section_map.get(which).getSectionRcsId();// 赋值地图sectionRcsId字段的值
                            Constants.EXCHANGE = Constants.exchange_begin + sectionRcsId;// 设置交换机名称
                            Constants.SECTION_RCS_ID = String.valueOf(sectionRcsId);// 设置地图的具体id值（例如：1、2）
                            LogUtil.e("TAG", sectionRcsId + "");
                            strStorageMapName = str_storage_map_name + "\n当前地图名称：" + items_sectionMap[which];
                            dialog.dismiss();
                        }
                    }).create().show();
                }
            }
        });

    }

    public BoxFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_box, container, false);
        ButterKnife.bind(this, view);// butterknife的绑定
        init();// 初始化数据

        requestPermission();// 申请权限

        setListener();// 设置监听
//        LogUtil.e("Enjoy","onCreateView");

        return view;
    }

    @SuppressLint("NewApi")
    private void requestPermission() {
        if(getContext().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            // 进行授权
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_REQUEST_STORAGE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    ToastUtil.showToast(getContext(), "亲爱的工程师！已授权应用读写文件权限");
                }else {
                    ToastUtil.showToast(getContext(), "读写文件权限未开启");
                    // 进行授权
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_STORAGE);
                }
                break;
        }
    }

    private List<Integer> lock_unlock_pos = new ArrayList<>();
    private boolean bl_isCarExist = false;// true表示地址格中存在小车-标志
    private boolean bl_isPodExist = false;// true表示地址格中存在pod-标志
    private boolean bl_isUnWalked = false;// true表示地址格是不可走区域-标志
    // 初始化小车和货架在集合中的位置
    private int carPosition = 0;
    private int podPosition = 0;

    /**
     * 设置监听
     */
    private void setListener() {

        // 监听boxView的地址格单击事件
        boxView.setOnClickListener(new BoxView.OnClickListener() {
            @Override
            public void doClick(final int boxNo,
                                final List<RobotEntity> car_List,
                                final List<PodEntity> pod_List,
                                List<Long> unWalked_List) {

                // 每次点击都要重置初始值
                bl_isCarExist = false;
                bl_isPodExist = false;
                bl_isUnWalked = false;
                carPosition = 0;
                podPosition = 0;

                // 点击界面，取消震动
                if (vibrator != null){
                    vibrator.cancel();
                }


                if(unWalked_List != null){
                    // 遍历判断是否是不可走区域
                    for (int i = 0;i < unWalked_List.size();i++){
                        if(boxNo == unWalked_List.get(i)){
                            bl_isUnWalked = true;
                        }
                    }
                }

                if(car_List != null){
                    // 遍历判断格子中是否有小车
                    for(int i = 0;i < car_List.size();i++){
                        if(boxNo == car_List.get(i).getAddressCodeID()){
                            bl_isCarExist = true;// 格子中有小车
                            carPosition = i;
                        }
                    }
                }

                if(pod_List != null){
                    // 遍历判断格子中是否有pod
                    for(int i = 0;i < pod_List.size();i++){
                        if(boxNo == pod_List.get(i).getPodPos()){
                            bl_isPodExist = true;// 格子中有pod
                            podPosition = i;
                        }
                    }
                }

                if(bl_isUnWalked){// 点击了不可走区域
                    ToastUtil.showToast(getContext(), "当前区域是不可走区域");
                }else {

                    if (!bl_isSelectLockUnLock){

                        // 构建view
                        view_options = getLayoutInflater().from(getContext())
                                .inflate(R.layout.dialog_view_box_options, null);

                        // 创建AlertDialog对象
                        dialog_options = new AlertDialog.Builder(getContext())
                                .setView(view_options)
                                .create();
                        // 获取控件
                        tv_options_lock_circle = view_options.findViewById(R.id.tv_options_lock_circle);// 锁周边
                        tv_options_unlock_circle = view_options.findViewById(R.id.tv_options_unlock_circle);// 解锁周边
                        view_options_resendOrder = view_options.findViewById(R.id.view_options_resendOrder);
                        tv_options_resendOrder = view_options.findViewById(R.id.tv_options_resendOrder);// 重发任务
                        tv_options_other = view_options.findViewById(R.id.tv_options_other);// 其他

                        // 点击了小车，弹出的对话框中显示重发任务项按钮
                        if (bl_isCarExist){
                            visibile(tv_options_resendOrder);
                            visibile(view_options_resendOrder);
                        }

                        // 设置dialog所在的窗口的背景为透明，很关键
                        dialog_options.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        dialog_options.setCancelable(true);
                        dialog_options.show();

                        // 锁周边
                        tv_options_lock_circle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("提示")
                                        .setIcon(R.mipmap.app_icon)
                                        .setMessage("亲爱的工程师！谨慎操作！确定将地址格："
                                                + boxNo + "周围一圈的地址点位集锁格？")
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
                                                dialog_options.dismiss();
                                                ProgressBarUtil.showProgressBar(getContext(), "锁周边...",
                                                        getResources().getColor(R.color.colorPrimaryDark));
                                                // 给RabbitMQ发送消息
                                                publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_LOCK_UNLOCK);
                                                nineLock(boxNo);
                                            }
                                        }).create().show();
                            }
                        });

                        // 解锁周边
                        tv_options_unlock_circle.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                new AlertDialog.Builder(getContext())
                                        .setTitle("提示")
                                        .setIcon(R.mipmap.app_icon)
                                        .setMessage("亲爱的工程师！谨慎操作！确定将地址格："
                                                + boxNo + "周围一圈的地址点位集解锁？")
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
                                                dialog_options.dismiss();
                                                ProgressBarUtil.showProgressBar(getContext(), "解锁周边...",
                                                        getResources().getColor(R.color.colorPrimaryDark));
                                                // 给RabbitMQ发送消息
                                                publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_LOCK_UNLOCK);
                                                nineUnlock(boxNo);
                                            }
                                        }).create().show();
                            }
                        });

                        // 其他
                        tv_options_other.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {

                                dialog_options.dismiss();

                                if(bl_isCarExist && !bl_isPodExist){// 只有小车在的情况
                                    showDialogAboutCarAll(car_List, carPosition, pod_List, podPosition, bl_isCarExist, bl_isPodExist);// 所有可对小车进行操作的选项
                                }

                                if(bl_isPodExist && !bl_isCarExist){// 只有pod存在的情况
                                    showDialogAboutPod(pod_List, podPosition);
                                }

                                if(bl_isCarExist && bl_isPodExist){// 小车和pod都存在的情况
                                    showDialogAboutCarAll(car_List, carPosition, pod_List, podPosition, bl_isCarExist, bl_isPodExist);// 所有可对小车进行操作的选项
                                }

                                if(!bl_isCarExist && !bl_isPodExist){// 地图上的坐标，没小车，也没有pod
                                    showDialogAboutBox(boxNo);
                                }
                            }
                        });


                        // 小车重发任务
                        tv_options_resendOrder.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final int robotID = Integer.parseInt(String.valueOf(car_List.get(carPosition).getRobotID()));
//                                ToastUtil.showToast(getContext(), "" + robotID);

                                // 弹框提示是否重发任务
                                new AlertDialog.Builder(getContext())
                                        .setTitle("提示")
                                        .setIcon(R.mipmap.app_icon)
                                        .setMessage("给" + robotID + "号小车重发任务？")
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
                                                dialog_options.dismiss();
                                                methodResendOrder(robotID);
                                            }
                                        }).create().show();

                            }
                        });

                    }else{
                        if(lock_unlock_pos.size() == 0){
                            lock_unlock_pos.add(boxNo);
                        }else {

                            int index = -1;
                            boolean bl_isAdd = false;
                            for(int i = 0;i < lock_unlock_pos.size();i++){
                                int pos = lock_unlock_pos.get(i);
                                if(pos == boxNo){
                                    index = i;
                                    bl_isAdd = true;
                                }
                            }

                            if(bl_isAdd && index != -1){
                                lock_unlock_pos.remove(index);
                            }else {
                                lock_unlock_pos.add(boxNo);
                            }

                        }

                        // 根据用户点击了的所有地址格在地图上面用黑色小方块标识出来
                        boxView.setLockUnLockArea(lock_unlock_pos);
                    }

                }

            }

            // 工作站的点击事件监听
            @Override
            public void workSiteClick(boolean bl_isWorkSite, final String workSiteUUID) {
                if(bl_isWorkSite){

//                    strWorkSiteUUID = workSiteUUID;// 保存用户点击工作站的UUID
//                    CharSequence[] items_worksite = {"释放单个pod", "开启不间断释放pod", "停止不间断释放pod"};

                    new AlertDialog.Builder(getContext())
                            .setIcon(R.mipmap.app_icon)
                            .setTitle("提示")
                            .setMessage("确定释放POD？")
                            .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    dialog.dismiss();
                                }

                            })
                            .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int i) {
                                    call_showPod(workSiteUUID);// 显示pod状态
                                    dialog.dismiss();
                                }
                            }).create().show();

                    // 一个选项是释放工作站的pod、一个选项是采用发送延迟消息的方式不间断的释放pod
                    /*
                    new AlertDialog.Builder(getContext())
                            .setIcon(R.mipmap.app_icon)
                            .setTitle("提示")
                            .setSingleChoiceItems(items_worksite, -1, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    switch (which){
                                        case 0:// 释放单个pod
                                            dialog.dismiss();
                                            new AlertDialog.Builder(getContext())
                                                    .setIcon(R.mipmap.app_icon)
                                                    .setTitle("提示")
                                                    .setMessage("确定释放POD？")
                                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int i) {
                                                            dialog.dismiss();
                                                        }

                                                    })
                                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int i) {
                                                            call_showPod(workSiteUUID);// 显示pod状态
                                                            dialog.dismiss();
                                                        }
                                                    }).create().show();
                                            break;

                                        case 1:// 不间断释放pod

                                            new AlertDialog.Builder(getContext())
                                                    .setIcon(R.mipmap.app_icon)
                                                    .setTitle("提示")
                                                    .setMessage("工作站不间断释放pod？")
                                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            STOP_RELEASE_POD = true;// 不间断释放pod功能停止
                                                        }
                                                    })
                                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            STOP_RELEASE_POD = false;// 不间断释放pod功能开启
                                                            inComingMessageHandler.sendEmptyMessageDelayed(WHAT_RELEASE_POD, RELEASE_POD_TIME);
                                                        }
                                                    }).create().show();

                                            break;
                                        case 2:// 停止工作站不间断释放pod功能

                                            STOP_RELEASE_POD = true;
                                            ToastUtil.showToast(getContext(), "停止成功");

                                            break;
                                    }
                                }
                            }).create().show();
                            */

                }
            }

            // 回调设置地图格子大小
            @Override
            public void setBoxSize(int boxSizeInOut) {
                boxSizeChange = boxSizeInOut;
            }
        });

        // 监听boxView移动取消震动
        boxView.setOnCancelVibrateListener(new BoxView.CancelVibrateListener() {
            @Override
            public void cancelVibrate() {
                if (vibrator != null){
                    vibrator.cancel();
                }
            }
        });

        // 发送延迟消息来监听调度任务的时长
        if (bl_isStartTripTaskListener){
            inComingMessageHandler.sendEmptyMessageDelayed(WHAT_TRIP_TASK_LISTENER, TIME_TRIP_TASK_LISTENER);
        }

    }

    /**
     * 九宫格锁格
     * @param boxNo 工程师点击的地址格
     */
    private void nineLock(int boxNo) {

        try{
            nine_lock_unlock.clear();// 每次都需要清空点位集合数据
            // 根据点击的地址格，获取周围一圈的地址格集合
            getNineAddressList(boxNo);
            LogUtil.e("TAG", "九宫格锁格点位集为：" + nine_lock_unlock.toString());

            Map<String, Object> message = new HashMap<>();
            message.put("unAvailableAddressList", nine_lock_unlock);
            queue.putLast(message);// 发送消息到MQ
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(), "九宫格锁格异常：" + e.getMessage());
        }

        ProgressBarUtil.dissmissProgressBar();

    }

    /**
     * 九宫格解锁
     * @param boxNo 工程师点击的地址格
     */
    private void nineUnlock(int boxNo) {

        try{
            nine_lock_unlock.clear();// 每次都需要清空点位集合数据
            // 根据点击的地址格，获取周围一圈的地址格集合
            getNineAddressList(boxNo);
            LogUtil.e("TAG", "九宫格解锁点位集为：" + nine_lock_unlock.toString());

            Map<String, Object> message = new HashMap<>();
            message.put("availableAddressList", nine_lock_unlock);
            queue.putLast(message);// 发送消息到MQ
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(), "九宫格解锁异常：" + e.getMessage());
        }

        ProgressBarUtil.dissmissProgressBar();

    }

    /**
     * 获取九宫格地址点位集合
     * @param boxNo
     */
    private void getNineAddressList(int boxNo) {

        // boxNo在地图的第一行
        if (boxNo <= column){
            if (boxNo == 1){// 地图左上角
                nine_lock_unlock.add(boxNo + 1);
                nine_lock_unlock.add(boxNo + column);
                nine_lock_unlock.add(boxNo + column + 1);
            }
            if (boxNo == column){// 地图右上角
                nine_lock_unlock.add(boxNo - 1);
                nine_lock_unlock.add(boxNo + column);
                nine_lock_unlock.add(boxNo + column - 1);
            }
            if (boxNo > 1 && boxNo < column){// 地图第一行的中部
                nine_lock_unlock.add(boxNo - 1);
                nine_lock_unlock.add(boxNo + 1);
                nine_lock_unlock.add(boxNo + column);
                nine_lock_unlock.add(boxNo + column - 1);
                nine_lock_unlock.add(boxNo + column + 1);
            }
        }

        // boxNo在地图的最后一行
        else if (boxNo > (row - 1) * column){
            if (boxNo == ((row - 1) * column + 1)){// 地图左下角
                nine_lock_unlock.add((row -2) * column + 1);
                nine_lock_unlock.add((row -2) * column + 2);
                nine_lock_unlock.add((row -1) * column + 2);
            }
            if (boxNo == row * column){// 地图右下角
                nine_lock_unlock.add((row - 1) * column);
                nine_lock_unlock.add((row - 1) * column - 1);
                nine_lock_unlock.add(row * column - 1);
            }
            if (boxNo > ((row - 1) * column + 1) && boxNo < (row * column)){// 地图最后一行的中部
                nine_lock_unlock.add(boxNo - 1);
                nine_lock_unlock.add(boxNo + 1);
                nine_lock_unlock.add(boxNo - column);
                nine_lock_unlock.add(boxNo - column - 1);
                nine_lock_unlock.add(boxNo - column + 1);
            }
        }

        // boxNo在地图的第一列中部
        else if ((boxNo % column) == 1 && (boxNo > 1) && (boxNo < (row -1) * column)){
            nine_lock_unlock.add(boxNo + 1);
            nine_lock_unlock.add(boxNo - column);
            nine_lock_unlock.add(boxNo + column);
            nine_lock_unlock.add(boxNo + 1 - column);
            nine_lock_unlock.add(boxNo + 1 + column);
        }

        // boxNo在地图的最后一列中部
        else if ((boxNo % column) == 0 && (boxNo > column) && (boxNo <= (row - 1) * column)){
            nine_lock_unlock.add(boxNo - 1);
            nine_lock_unlock.add(boxNo - 1 - column);
            nine_lock_unlock.add(boxNo - 1 + column);
            nine_lock_unlock.add(boxNo - column);
            nine_lock_unlock.add(boxNo + column);
        }

        // boxNo在地图的中间部分且不在地图的最外圈
        else {
            nine_lock_unlock.add(boxNo - 1);
            nine_lock_unlock.add(boxNo + 1);
            nine_lock_unlock.add(boxNo + column);
            nine_lock_unlock.add(boxNo - column);
            nine_lock_unlock.add(boxNo + column - 1);
            nine_lock_unlock.add(boxNo + column + 1);
            nine_lock_unlock.add(boxNo - column - 1);
            nine_lock_unlock.add(boxNo - column + 1);
        }

    }

    /**
     * 点击地图上的小车，显示所有可选项
     * @param car_List
     * @param carPosition
     * @param pod_List
     * @param podPosition
     * @param bl_isCarExist
     * @param bl_isPodExist
     */
    private void showDialogAboutCarAll(final List<RobotEntity> car_List, final int carPosition,
                                    final List<PodEntity> pod_List, final int podPosition,
                                    final boolean bl_isCarExist, final boolean bl_isPodExist) {

//        new AlertDialog.Builder(getContext())
//                .setMessage("小车操作？")
//                .setPositiveButton("否", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                })
//                .setNegativeButton("是", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
                        showPopAboutCar(car_List, carPosition, pod_List, podPosition, bl_isCarExist, bl_isPodExist);
//                    }
//                })
//                .create().show();

    }

    /**
     * 点击地图上既没有小车，也没有pod的格子时候。
     * 提示当前地图格的坐标
     * @param clickBoxNumber
     */
    private void showDialogAboutBox(final int clickBoxNumber) {

        pop_view_box = getLayoutInflater().inflate(R.layout.popupwindow_map_box, null);
        TextView tv_map_box_pos = pop_view_box.findViewById(R.id.tv_map_box_pos);
        tv_map_box_pos.setText(getContext().getResources().getString(R.string.map_box_pos) + clickBoxNumber);
        // 弹出对话框显示地图信息
        new AlertDialog.Builder(getContext())
                .setView(pop_view_box)
                .create().show();

    }

    /**
     * 弹框显示pod信息
     * @param podList
     * @param pod_id
     */
    private void showDialogAboutPod(List<PodEntity> podList, int pod_id) {

        pop_view_pod = getLayoutInflater().inflate(R.layout.popupwindow_pod,null);
        showPod(podList, pod_id);
        new AlertDialog.Builder(getContext())
                .setView(pop_view_pod)
                .create().show();
    }

    /**
     * 显示pod的相关信息
     * @param podList
     * @param pod_id pod这个对象在集合中的位置
     */
    private void showPod(List<PodEntity> podList, int pod_id) {
        TextView tv_podId = pop_view_pod.findViewById(R.id.tv_podId);
        TextView tv_podPos = pop_view_pod.findViewById(R.id.tv_podPos);

        //设置pod的id
        tv_podId.setText(getContext().getResources().getString(R.string.box_pod_id) + podList.get(pod_id).getPodId());
        // 设置pod的地图坐标
        tv_podPos.setText(getContext().getResources().getString(R.string.box_pod_pos) + podList.get(pod_id).getPodPos());
    }

    private AlertDialog dialogCarView = null;// 小车弹框
    /**
     * 弹框显示car的信息和执行相应操作
     * @param carList
     * @param carPosition
     * @param podList
     * @param podPosition
     * @param bl_isCarExist
     * @param bl_isPodExist
     */
    private void showPopAboutCar(List<RobotEntity> carList, int carPosition, List<PodEntity> podList, int podPosition, boolean bl_isCarExist, boolean bl_isPodExist) {
/*
        // 构建popupwindow的布局
        pop_view_car = getLayoutInflater().inflate(R.view_error.popupwindow_car, null);
        // 构建对PopupWindow象
//        window_car = new PopupWindow(pop_view_car, ViewGroup.LayoutParams.MATCH_PARENT, pop_height);
        window_car = new PopupWindow(pop_view_car, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置弹框的动画
        window_car.setAnimationStyle(R.style.pop_anim);
        // 设置背景白色
        window_car.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        // 设置获取焦点
        window_car.setFocusable(true);
        // 设置触摸区域外可消失
        window_car.setOutsideTouchable(true);
        // 实时更新状态
        window_car.update();
        // 根据偏移量确定在parent view中的显示位置
        window_car.showAtLocation(rl_mapView, Gravity.BOTTOM, 0, 0);

        // 背景透明度改变，优化用户体验
        bgAlpha(0.618f);
        window_car.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                bgAlpha(1.0f);
            }
        });
*/

        boxView.restorePos(false);// 弹出框中有编辑框的焦点事件，需要恢复view的原来位置
        bl_isDriveEmpty = false;// 置为false，表示需要重新生成空车路径信息
        if (emptyRouteList.size() != 0){
            emptyRouteList.clear();// 每次下发都需要先清空原来的空车路径信息
        }

        pop_view_car = getLayoutInflater().inflate(R.layout.popupwindow_car, null);
        dialogCarView = new AlertDialog.Builder(getContext()).setView(pop_view_car).create();
        dialogCarView.show();

        // 显示小车的信息或则pod的信息以及可以执行的操作
        carDoSomething(carList ,carPosition, podList, podPosition, bl_isCarExist, bl_isPodExist);
    }

    /**
     * 显示小车的信息以及可以执行的操作
     * @param carList
     * @param carPosition   小车在集合中的位置
     * @param podList
     * @param podPosition   pod这个对象在集合中的位置
     * @param bl_isCarExist
     * @param bl_isPodExist
     */
    private void carDoSomething(final List<RobotEntity> carList, final int carPosition, List<PodEntity> podList, final int podPosition, boolean bl_isCarExist, boolean bl_isPodExist) {
        TextView tv_carId = pop_view_car.findViewById(R.id.tv_carId);// 小车id
        TextView tv_carPos = pop_view_car.findViewById(R.id.tv_carPos);// 小车在地图上的位置坐标
        tv_carPath = pop_view_car.findViewById(R.id.tv_carPath);// 小车的当前路径

        LinearLayout linear_pod = pop_view_car.findViewById(R.id.linear_pod);// pod信息布局
        TextView tv_podId = pop_view_car.findViewById(R.id.tv_podId);// pod的id
        TextView tv_podPos = pop_view_car.findViewById(R.id.tv_podPos);// pod在地图上的位置坐标

//        Button btn_resendPath = pop_view_car.findViewById(R.id.btn_resendPath);// 重发路径
        Button btn_up_one = pop_view_car.findViewById(R.id.btn_up_one);// 小车上移一格
        Button btn_down_one = pop_view_car.findViewById(R.id.btn_down_one);// 小车下移一格
        Button btn_left_one = pop_view_car.findViewById(R.id.btn_left_one);// 小车左移一格
        Button btn_right_one = pop_view_car.findViewById(R.id.btn_right_one);// 小车右移一格

        final Button btn_startEmptyDrive = pop_view_car.findViewById(R.id.btn_startEmptyDrive);// 开始空车调度按钮
        Button btn_createEmptyRoute = pop_view_car.findViewById(R.id.btn_createEmptyRoute);// 生成空车路径按钮
        Button btn_driveEmptyCar = pop_view_car.findViewById(R.id.btn_driveEmptyCar);// 空车调度按钮

        final LinearLayout linear_emptyDrive = pop_view_car.findViewById(R.id.linear_emptyDrive);// 空车调度操作线性布局

        final EditText et_startPos = pop_view_car.findViewById(R.id.et_startPos);// 起始点
        final EditText et_desPos = pop_view_car.findViewById(R.id.et_desPos);// 终点
        // 小车在地图上的坐标
        long carPos = carList.get(carPosition).getAddressCodeID();
        et_startPos.setText(""+carPos);// 设置小车起始点的位置

        tv_routeInfo = pop_view_car.findViewById(R.id.tv_routeInfo);// 空车路径信息

        /*
        btn_resendPath.setOnClickListener(new View.OnClickListener() {// 重发路径监听
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext()).setMessage("确定重发路径？")
                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 如果地图数据返回了sectionId值，那么执行重发路径操作
                        if(sectionId != null){
                            call_resendThePath((int) carList.get(carPosition).getRobotID());
                        }
                        // 消失popupwindow，返回地图显示界面
                        if(window_car != null){
                            window_car.dismiss();
                        }
                    }
                }).create().show();
            }
        });*/

        // 开始调度空车
        btn_startEmptyDrive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 显示输入空车信息的控件和布局，同时隐藏按钮
                et_startPos.setVisibility(View.VISIBLE);
                et_desPos.setVisibility(View.VISIBLE);
                linear_emptyDrive.setVisibility(View.VISIBLE);
                btn_startEmptyDrive.setVisibility(View.GONE);
            }
        });

        // 生成空车路径
        btn_createEmptyRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 获取起点和终点坐标
                String startPos = et_startPos.getText().toString().trim();
                String desPos = et_desPos.getText().toString().trim();
                if (!TextUtils.isEmpty(startPos) && !TextUtils.isEmpty(desPos)){
                    createEmptyRoute(startPos, desPos);// 根据输入的起始点和终点，生成空车路径信息
                }else {
                    ToastUtil.showToast(getContext(), "起始点和终点输入不能为空");
                }

            }
        });

        // 空车调度
        btn_driveEmptyCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                interruptThread(publishThread);
                if (bl_isDriveEmpty){

                    setUpConnectionFactory();
                    publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_AGV_SERIESPATH);

                    new AlertDialog.Builder(getContext())
                            .setIcon(R.mipmap.app_icon)
                            .setTitle("提示")
                            .setMessage("调度空车路径下发？")
                            .setCancelable(false)
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    interruptThread(publishThread);
                                }
                            })
                            .setPositiveButton("开始下发", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    LogUtil.e("routeList", emptyRouteList.toString());
                                    try {
                                        Map<String, Object> message = new HashMap<>();
                                        message.put("robotID", carList.get(carPosition).getRobotID());
                                        message.put("section", Constants.SECTIONID);
                                        message.put("time", System.currentTimeMillis() / 1000);
                                        message.put("podUpAddress",0);
                                        message.put("podDownAddress",0);
                                        message.put("isRotatePod",true);
                                        message.put("podCodeID",0);
                                        message.put("rotateTheta",0);
                                        message.put("seriesPath",emptyRouteList);
//                                        message.put("mac",0);
//                                        message.put("batterManufacturerNumber",0);
                                        message.put("podWeight",0);

                                        queue.putLast(message);// 发送消息到MQ
                                        ToastUtil.showToast(getContext(),"空车调度指令已下发");
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }

                                    dialog.dismiss();
                                    if (dialogCarView != null){
                                        dialogCarView.dismiss();
                                    }
                                }
                            }).create().show();
                }else {
                    ToastUtil.showToast(getContext(),"请先生成空车路径，再调度小车");
                }
            }
        });

        btn_up_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext()).setMessage("确定上移一格？")
                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        call_carMoveUp((int) carList.get(carPosition).getRobotID());// 执行上移一格操作
                        // 消失popupwindow，返回地图显示界面
                        if(window_car != null){
                            window_car.dismiss();
                        }
                    }
                }).create().show();
            }
        });

        btn_left_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext()).setMessage("确定左移一格？")
                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        call_carMoveLeft((int) carList.get(carPosition).getRobotID());// 执行左移一格操作
                        // 消失popupwindow，返回地图显示界面
                        if(window_car != null){
                            window_car.dismiss();
                        }
                    }
                }).create().show();
            }
        });

        btn_right_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext()).setMessage("确定右移一格？")
                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        call_carMoveRight((int) carList.get(carPosition).getRobotID());// 执行右移一格操作
                        // 消失popupwindow，返回地图显示界面
                        if(window_car != null){
                            window_car.dismiss();
                        }
                    }
                }).create().show();
            }
        });

        btn_down_one.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new AlertDialog.Builder(getContext()).setMessage("确定下移一格？")
                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        call_carMoveDown((int) carList.get(carPosition).getRobotID());// 执行下移一格的操作
                        // 消失popupwindow，返回地图显示界面
                        if(window_car != null){
                            window_car.dismiss();
                        }
                    }
                }).create().show();
            }
        });

        // popupwindow创建完成后发送请求获取小车路径信息
        call_checkCarState((int) carList.get(carPosition).getRobotID(), 0);

        // 设置car的id
        tv_carId.setText(getContext().getResources().getString(R.string.box_car_id) + carList.get(carPosition).getRobotID());
        // 设置car的地图坐标
        tv_carPos.setText(getContext().getResources().getString(R.string.box_car_pos) + carList.get(carPosition).getAddressCodeID());

        if(bl_isCarExist && bl_isPodExist){// 小车和货架都存在
            linear_pod.setVisibility(View.VISIBLE);// 显示pod信息
            tv_podId.setText(getResources().getString(R.string.box_pod_id) + podList.get(podPosition).getPodId());// 设置pod的id
            tv_podPos.setText(getResources().getString(R.string.box_pod_pos) + podList.get(podPosition).getPodPos());// 设置pod的位置
        }

    }

    /**
     * 生成空车路径
     * @param sourceVertex
     * @param targetVertex
     */
    private void createEmptyRoute(String sourceVertex, String targetVertex) {
        showDialog("加载中...");
        String url = getResources().getString(R.string.url_createEmptyRoute)
                + "sectionId=" + Constants.SECTIONID + "&warehouseId=" + Constants.WAREHOUSEID
                + "&sourceVertex=" + sourceVertex + "&targetVertex=" + targetVertex;

        LogUtil.e("url_routeinfo","url = " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        disMissDialog();
                        ToastUtil.showToast(getContext(), "空车路径生成成功！");
                        Message message = inComingMessageHandler.obtainMessage();
                        message.what = WHAT_CREATE_EMPTY_ROUTE;
                        message.obj = response;
                        inComingMessageHandler.sendMessage(message);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        disMissDialog();
                        ToastUtil.showToast(getContext(), "获取路径信息失败,请检查输入坐标是否符合规则");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 初始化操作
     */
    private void init() {
        vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);// 震动服务
        screenUtil = new ScreenUtil(getContext());// 创建ScreenUtil对象，获取手机屏幕宽高用
        pop_height = (int) (screenUtil.getScreenSize(ScreenUtil.HEIGHT) * 0.618f);// 黄金比例显示
        requestQueue = Volley.newRequestQueue(getContext());// 创建RequestQueue对象
        pDialog = new ProgressDialog(getContext());// 创建进度对话框
        pDialog.setCanceledOnTouchOutside(true);// 设置触摸进度框外区域可以取消进度框
        boxSizeChange = Constants.DEFAULT_BOX_SIZE;// 赋值初始化地图时格子的默认大小
        format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 创建SimpleDateFormat对象，时间格式为：yyyy-MM-dd HH:mm:ss

        rootAddress = Constants.HTTP + Constants.ROOT_ADDRESS;

        bl_isStartTripTaskListener = true;// 初始化置为true表示开启调度任务时长监听

        // 创建Timer对象
        if (timer_clear_charge_data == null){
            timer_clear_charge_data = new Timer();
        }

        if (timer_refresh_error_data == null){
            timer_refresh_error_data = new Timer();
        }



    }

    /**
     * 创建消费者线程，获取小车的数据
     * @param //handler
     */
    void subscribe(final Handler handler){

        subscribeThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connection_car != null && connection_car.isOpen()){
                        connection_car.close();
                    }
                    connection_car = factory.newConnection();
                    Channel channel = connection_car.createChannel();
                    channel.basicQos(0,1,false);
                    // 创建随机队列，可持续，自动删除
                    String queueName = System.currentTimeMillis() + "QN_CAR";
                    channel.exchangeDeclare(Constants.EXCHANGE, "direct", true);
                    AMQP.Queue.DeclareOk q = channel.queueDeclare(queueName, true, false, true, null);// 声明共享队列
                    LogUtil.e("queueName_car", q.getQueue());
                    channel.queueBind(q.getQueue(), Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CAR);
//                    channel.queueBind("1518342137798queueNameCar", Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CAR);

                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);

                            // 发消息通知UI更新
                            Message message = handler.obtainMessage();
                            message.obj = body;
                            message.what = WHATCAR;
                            handler.sendMessage(message);
                        }
                    };
                    channel.basicConsume(q.getQueue(), true, consumer);
//                    channel.basicConsume("1518342137798queueNameCar", true, consumer);

                } catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        subscribeThread.start();// 开启线程获取RabbitMQ推送消息
    }

    /**
     * 设置小车的实时数据，存入集合中。
     * 同一辆车就进行集合中数据的更新替换，新车数据就添加到集合
     * @param mapCar
     */
    private void setCarAndPodData(Map<String, Object> mapCar) {
//        long sectionID = (long) mapCar.get("sectionID");
        try {
            RobotEntity robotEntity = new RobotEntity();
//            long robotID = (long) mapCar.get("robotID");// 小车id
            long robotID = Long.parseLong(String.valueOf(mapCar.get("robotID")));

            Object podCodeInfoTheta = mapCar.get("podCodeInfoTheta");
            float podAngle = Float.parseFloat(podCodeInfoTheta.toString());// pod的角度，0°朝上，90°朝右，依次类推180°和270°
            LogUtil.e("podAngle =", "" + podAngle);

//            long podCodeID = (long) mapCar.get("podCodeID");// 如果为0，则小车没有装载pod；如果非0，则小车装载了该pod。该值表示pod的id
            long podCodeID = Long.parseLong(String.valueOf(mapCar.get("podCodeID")));

//            long addressCodeID = (long) mapCar.get("addressCodeID");// 小车在地图上的坐标
            long addressCodeID = Long.parseLong(String.valueOf(mapCar.get("addressCodeID")));

            // 设置小车数据
            robotEntity.setRobotID(robotID);
            robotEntity.setAddressCodeID(addressCodeID);
//            robotEntity.setCarRouteIsShow(false);// 设置小车路径信息在地图上未显示
            LogUtil.e("TAG", robotEntity.toString());
            LogUtil.e("podCodeID =", "" + podCodeID);

            if (removePodIdTemp != 0){
                int removePos = -1;
                // 移除地图缓存中的pod数据
                for (int i = 0;i < podList.size();i++){
                    int podId = podList.get(i).getPodId();
                    if (podId == removePodIdTemp){
                        removePos = i;// 获取需要移除的pod实体对象在集合中的位置
                    }
                }
                if(removePos != -1){
                    podList.remove(removePos);
                    removePodIdTemp = 0;
                }

            }

            // 如果pod位置变化了，也更新pod集合的数据并更新pod的位置显示
            if(podCodeID != 0){// pod的id不为0表示小车上有pod存在
                boolean bl_isExistPod = false;// true表示小车上的pod是初始化地图的pod
                for (int i = 0;i < podList.size();i++){
                    if(podCodeID == podList.get(i).getPodId()){// 小车上的pod是初始化地图数据时存在的pod
                        bl_isExistPod = true;
                        // 获取pod新的位置
//                        int newPodId = (int) podCodeID;
                        int newPodId = Integer.parseInt(String.valueOf(podCodeID));
//                        int newPodPos = (int) addressCodeID;
                        int newPodPos = Integer.parseInt(String.valueOf(addressCodeID));
                        // 移除集合中旧的pod
                        podList.remove(i);
                        // 设置新的pod对象
                        PodEntity podEntity = new PodEntity();
                        podEntity.setPodId(newPodId);
                        podEntity.setPodPos(newPodPos);
                        podEntity.setPodAngle((int)podAngle);
                        // 往集合中添加这个新的pod
                        podList.add(i, podEntity);
                    }
                }
                if(!bl_isExistPod){// 表示小车此时载的pod是新增加的pod
//                    int newPodId = (int) podCodeID;// 新增pod的id
                    int newPodId = Integer.parseInt(String.valueOf(podCodeID));
//                    int newPodPos = (int) addressCodeID;// 新增pod的位置
                    int newPodPos = Integer.parseInt(String.valueOf(addressCodeID));
                    // 设置新的pod对象
                    PodEntity podEntity = new PodEntity();
                    podEntity.setPodId(newPodId);
                    podEntity.setPodPos(newPodPos);
                    podEntity.setPodAngle((int) podAngle);
                    // 直接添加该pod到pod集合中
                    podList.add(podEntity);
                }
            }

            boolean bl_isAddCar = false;// false表示集合中未添加过该小车
            int position = 0;
            if(carList == null || carList.size() == 0){
                carList.add(robotEntity);// 第一次添加小车数据，直接添加即可
            }else{
                // 遍历小车信息集合，根据小车的id判断是否添加过该小车
                for(int i = 0;i < carList.size();i++){
                    if(carList.get(i).getRobotID() == robotID){
                        bl_isAddCar = true;
                        position = i;// 拿到小车在集合中的位置
                    }
                }

                if(!bl_isAddCar){
                    carList.add(robotEntity);// 集合中没有添加过该小车
                }else{

//                    robotEntity.setCarRouteIsShow(carList.get(position).isCarRouteIsShow());// 已经添加过的小车设置原来的路径显示信息判断值
                    carList.remove(position);// 移除小车上一次的信息
                    carList.add(position, robotEntity);// 在原来的位置添加新的小车信息
                }

            }

        }catch (Exception e){
            ToastUtil.showToast(getContext(),"小车数据解析异常:"+e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 按钮的单击事件
     * @param view
     */
    @OnClick({R.id.btn_initStorageMap, R.id.btn_drawing, R.id.btn_init_data, R.id.iv_zoomOut, R.id.iv_zoomIn
            , R.id.linear_map_introduction, R.id.linear_map_info, R.id.linear_map_reset, R.id.linear_map_drawAgain
            , R.id.fab_path_display, R.id.fab_path_hide, R.id.linear_map_carLockUnLock
            , R.id.btn_cancel_lockunlock, R.id.btn_confirm_lockunlock, R.id.linear_map_carBatteryInfo
            , R.id.linear_map_wcs, R.id.linear_map_rcs, R.id.imgBtn_errorTip, R.id.tv_problem_solve_or_not
            , R.id.fab_menu, R.id.iv_open_close, R.id.iv_tips_open_close, R.id.rbt_agv, R.id.rbt_pod
    , R.id.tv_num0, R.id.tv_num1, R.id.tv_num2, R.id.tv_num3, R.id.tv_num4, R.id.tv_num5, R.id.tv_num6
    , R.id.tv_num7, R.id.tv_num8, R.id.tv_num9, R.id.iv_clear_agv_pod, R.id.iv_locate_agv_pod, R.id.iv_tips_select})
    public void doClick(final View view){
        switch (view.getId()){
            case R.id.btn_initStorageMap:// 初始化仓库和地图数据
                initStorageAndMap();
                break;
            case R.id.btn_drawing:// 初始化地图数据并绘制地图

                if(!bl_initStorageMap){
                    ToastUtil.showToast(getContext(),"请先选择仓库和地图");
                }else {
                    if(!bl_initData){
                        setUpConnectionFactory();// 连接设置
                        subscribeMapData(inComingMessageHandler);// (这里需要先绑定队列，防止队列接收不到消息)发送地图数据请求到MQ，开始获取地图数据
                        // 发送消息到MQ 从MQ上拿到地图的行和列数据，然后绘制地图
                        publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_MAP_REQUEST);
                        // 弹框提示用户操作
                        new AlertDialog.Builder(getContext())
                                .setIcon(R.mipmap.app_icon)
                                .setTitle("提示")
                                .setMessage(getResources().getString(R.string.str_initMapData) + "？")
                                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        interruptThread(publishThread);// 中断发布线程
                                        interruptThread(threadMapData);// 中断消费线程
                                        dialog.dismiss();
                                    }
                                }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 弹出进度框提示正在初始化
//                                showDialog("初始化...");
                                ProgressBarUtil.showProgressBar(getContext(), "初始化..."
                                        , getResources().getColor(R.color.colorPrimaryDark));
                                try {
                                    Map<String, Object> message = new HashMap<>();
                                    message.put("name", Constants.MQ_ROUTINGKEY_MAP_REQUEST);
                                    message.put("requestTime", System.currentTimeMillis());// 系统当前时间
                                    message.put("sectionID", sectionRcsId);// 根据该值来确定绘制仓库下的哪个地图
                                    queue.putLast(message);// 发送消息到MQ
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }).create().show();
                    }else {
                        ToastUtil.showToast(getContext(),"地图已经初始化");
                    }
                }

                break;
            case R.id.btn_init_data:// 初始化小车数据

                // 弹框提示用户操作
                new AlertDialog.Builder(getContext())
                        .setIcon(R.mipmap.app_icon)
                        .setTitle("提示")
                        .setMessage(getResources().getString(R.string.str_initCarData) + "？")
                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        initCarData();

                    }
                }).create().show();

                break;
//            case R.id.linear_map_reset:// 复原地图
//                if(bl_initData){
//                    boxView.reset();// 地图复原，当自定义view触摸不到后调用复原地图
//                }else {
//                    ToastUtil.showToast(getContext(), "请先绘制地图");
//                    return;
//                }
//
//                break;
//            case R.id.linear_map_drawAgain:// 重新选择仓库和地图
//                interruptThread(publishThread);
//                setUpConnectionFactory();// 连接设置
//                subscribeStorageMap(inComingMessageHandler);// 先创建队列接收仓库地图的数据
////                publishToAMPQ(Constants.MQ_EXCHANGE_STORAGEMAP, Constants.MQ_ROUTINGKEY_STORAGEMAP_REQUEST);// publish消息给请求队列
//                new AlertDialog.Builder(getContext())
//                        .setIcon(R.mipmap.app_icon)
//                        .setTitle("提示")
//                        .setMessage(getResources().getString(R.string.str_map_drawAgain) + "？")
//                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.dismiss();
//                            }
//                        })
//                        .setNegativeButton("是", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                creatThreadCloseConnectionAndClearData(connection_car, connection_chargingTask, connection_showAllCarCurrentPath);
//                            }
//                        }).create().show();
//                break;
            case R.id.iv_zoomOut:// 地图放大
                if(boxSizeChange != 0 && boxSizeChange > 60){
                    ToastUtil.showToast(getContext(),"地图已经缩放到最大");
                    return;
                }
                boxSizeChange += 2;
                boxView.zoomInOut(boxSizeChange);
                break;
            case R.id.iv_zoomIn:// 地图缩小
                if(boxSizeChange != 0 && boxSizeChange < 20){
                    ToastUtil.showToast(getContext(),"地图已经缩放到最小");
                    return;
                }
                boxSizeChange -= 2;
                boxView.zoomInOut(boxSizeChange);
                break;
//            case R.id.linear_map_introduction:// 地图说明
//                showPopAboutMapIntroduction();
//                break;
//            case R.id.linear_map_info:// 地图信息
//                showDialogAboutMapInfo();
////                setDirtyData();// 设置局部刷新区域数据
//                break;

            case R.id.fab_path_display:// 显示所有小车的当前路径（标识锁格和未锁格状态）
                showCarAllPathInfo();
                break;
            case R.id.fab_path_hide:// 取消小车当前路径显示
                new AlertDialog.Builder(getContext())
                        .setIcon(R.mipmap.app_icon)
                        .setTitle("提示")
                        .setMessage(getResources().getString(R.string.str_cancelAllCarLockUnlockPath) + "？")
                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!bl_isShowCarPath){
                                    ToastUtil.showToast(getContext(),"请先显示小车锁格、未锁格路径");
                                }else {
                                    gone(linear_lock_unlock);
//                                    visibile(linear_map_carLockUnLock);
//                                    visibile(linear_map_carBatteryInfo);
//                                    visibile(linear_map_wcs);
//                                    visibile(linear_map_rcs);
                                    visibile(linear_zoomOutIn);
//                                    visibile(linear_map_drawAgain);
//                                    visibile(linear_map_reset);
//                                    visibile(linear_map_info);
//                                    visibile(linear_map_introduction);
                                    interruptThread(threadShowAllCarCurrentPath);
                                    // 创建线程取消小车当前路径
                                    t_cancel_car_all_current_path = new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if(connection_showAllCarCurrentPath != null){
                                                connection_showAllCarCurrentPath.abort();
                                            }
                                            Message message = inComingMessageHandler.obtainMessage();
                                            message.what = WHAT_CANCEL_CAR_ALL_CURRENT_PATH;
                                            inComingMessageHandler.sendMessage(message);
                                        }
                                    });
                                    t_cancel_car_all_current_path.start();
                                }
                            }
                        }).create().show();
                break;

//            case R.id.linear_map_carBatteryInfo:// 小车电量
//                CarBatteryInfoFragment fragment = new CarBatteryInfoFragment();
//                showFragment(BoxFragment.this, fragment);
//                break;

//            case R.id.linear_map_wcs:// 跳转到wcs的小车操作界面
//                WcsCarOperateFragment wcsCarOperateFragment = new WcsCarOperateFragment();
//                showFragment(BoxFragment.this, wcsCarOperateFragment);
//                break;

//            case R.id.linear_map_rcs:// 跳转到rcs的小车操作界面
//                RcsCarOperateFragment rcsCarOperateFragment = new RcsCarOperateFragment();
//                if (errorChargings.size() != 0){
//                    String strErrorCharging = "";
//                    for (int i = 0;i < errorChargings.size();i++){
//                        strErrorCharging = strErrorCharging + "充电桩故障 [ 充电桩类型：" + errorChargings.get(i).getType()
//                                + "，充电桩ID：" + errorChargings.get(i).getNumber() + " ]\n";
//                    }
//                    Bundle bundle = new Bundle();
//                    bundle.putString("clear_charging_error", strErrorCharging);
//                    rcsCarOperateFragment.setArguments(bundle);
//                }
//                // 回调监听，清除充电桩故障信息
//                rcsCarOperateFragment.setCallBack(new RcsCarOperateFragment.CallBackListener() {
//                    @Override
//                    public void clearChargingError() {
//                        errorChargings.clear();
//                    }
//                });
//                showFragment(BoxFragment.this, rcsCarOperateFragment);
//                break;

//            case R.id.linear_map_carLockUnLock:// 锁格/解锁
//                if(!bl_isShowCarPath){
//                    ToastUtil.showToast(getContext(), "请先显示小车当前路径");
////                    return;
//                }else {
//                    publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_LOCK_UNLOCK);
//                    bl_isSelectLockUnLock = true;
//
//                    visibile(linear_lock_unlock);
////                    gone(linear_map_carLockUnLock);
////                    gone(linear_map_carBatteryInfo);
////                    gone(linear_map_wcs);
////                    gone(linear_map_rcs);
//                    gone(linear_zoomOutIn);
////                    gone(linear_map_drawAgain);
////                    gone(linear_map_reset);
////                    gone(linear_map_info);
////                    gone(linear_map_introduction);
//                }
//                break;
            case R.id.btn_cancel_lockunlock:// 取消解锁或锁格
                gone(linear_lock_unlock);

//                visibile(linear_map_carLockUnLock);
//                visibile(linear_map_carBatteryInfo);
//                visibile(linear_map_wcs);
//                visibile(linear_map_rcs);
                visibile(linear_zoomOutIn);
                visibile(iv_open_close);
//                visibile(linear_map_drawAgain);
//                visibile(linear_map_reset);
//                visibile(linear_map_info);
//                visibile(linear_map_introduction);

                bl_isSelectLockUnLock = false;
                lock_unlock_pos.clear();
                boxView.setLockUnLockArea(lock_unlock_pos);
                break;
            case R.id.btn_confirm_lockunlock:// 确定锁格或解锁
                if(lock_unlock_pos.size() == 0){
                    ToastUtil.showToast(getContext(),"请选择锁格/解锁区域");
                    return;
                }

                new AlertDialog.Builder(getContext())
                        .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LogUtil.e("sure",lock_unlock_pos.toString());
                                if(which == 0){// 点击了锁格
                                    showDialog("锁格...");
                                    try {
                                        Map<String, Object> message = new HashMap<>();
                                        message.put("unAvailableAddressList", lock_unlock_pos);
                                        queue.putLast(message);// 发送消息到MQ
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }else if(which == 1){// 点击了解锁
                                    // 弹出进度框提示正在初始化
                                    showDialog("解锁...");
                                    try {
                                        Map<String, Object> message = new HashMap<>();
                                        message.put("availableAddressList", lock_unlock_pos);
                                        queue.putLast(message);// 发送消息到MQ
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                dialog.dismiss();

                                lock_unlock_pos.clear();
                                boxView.setLockUnLockArea(null);
                                bl_isSelectLockUnLock = false;
                                gone(linear_lock_unlock);
                                visibile(iv_open_close);
//                                visibile(linear_map_carLockUnLock);
//                                visibile(linear_map_carBatteryInfo);
//                                visibile(linear_map_wcs);
//                                visibile(linear_map_rcs);
                                visibile(linear_zoomOutIn);
//                                visibile(linear_map_drawAgain);
//                                visibile(linear_map_reset);
//                                visibile(linear_map_info);
//                                visibile(linear_map_introduction);
                            }
                        }).create().show();
                break;
            case R.id.imgBtn_errorTip:// 错误反馈提示
                if (vibrator != null){
                    vibrator.cancel();// 取消震动
                }
                View view_error = getLayoutInflater().inflate(R.layout.view_error, null);
                new AlertDialog.Builder(getContext()).setView(view_error).create().show();
                tv_error_content = view_error.findViewById(R.id.tv_error_tip_content);// 错误提示内容控件
                tv_error_scan_pod = view_error.findViewById(R.id.tv_error_scan_pod);// 扫不到pod
                tv_error_nomove_timeout = view_error.findViewById(R.id.tv_error_nomove_timeout);// 位置不改变超时
                tv_error_close_connection = view_error.findViewById(R.id.tv_error_close_connection);// 小车断开连接
                tv_error_chargepile = view_error.findViewById(R.id.tv_error_chargepile);// 充电桩故障

                // 获取错误提示内容，赋值给变量
                getAndSetErrorContent();

                // 开启定时任务刷新界面数据
                if (task_refresh_error_data == null){
                    task_refresh_error_data = new TimerTask() {
                        @Override
                        public void run() {
                            Message message = inComingMessageHandler.obtainMessage();
                            message.what = WHAT_REFRESH_ERROR_DATA;
                            inComingMessageHandler.sendMessage(message);
                        }
                    };

                    if (timer_refresh_error_data == null){
                        timer_refresh_error_data = new Timer();
                    }
                    timer_refresh_error_data.schedule(task_refresh_error_data, 2000, 2000);
                }
                break;
            case R.id.tv_problem_solve_or_not:// 问题确认解决后，隐藏反馈提示界面
                new AlertDialog.Builder(getContext())
                        .setIcon(R.mipmap.app_icon)
                        .setTitle("提示")
                        .setMessage("亲爱的工程师！问题已解决，取消错误提示？")
                        .setPositiveButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 停止定时任务并清空
                                if (task_refresh_error_data != null){
                                    task_refresh_error_data.cancel();
                                    task_refresh_error_data = null;
                                }
                                // 取消震动
                                if (vibrator != null){
                                    vibrator.cancel();
                                }
                                // 清空数据并隐藏布局
                                robotErrorEntityList.clear();
                                robotCloseConnEntityList.clear();
                                noMoveTimeoutEntityList.clear();
                                errorChargings.clear();
                                bl_isChargingError = false;// 故障清空后，置为false
                                rtHeartTimeoutEntityList.clear();
                                gone(linear_error_tip);
                                gone(iv_error_scan_pod);
                                gone(iv_error_nomove_timeout);
                                gone(iv_error_close_connection);
                                gone(iv_error_chargepile);
                                dialog.dismiss();
                            }
                        }).create().show();
                break;
            case R.id.fab_menu:// 点击了FloatingActionButton

                if (!bl_isSelectLockUnLock){// 不执行 锁格/解锁 操作的时候
                    if (bl_menuEnabled){
                        showPopAboutMenu();
                    }else {
                        ToastUtil.showToast(getContext(), "亲爱的工程师！您需要先进行地图的绘制");
                    }
                }else {
                    ToastUtil.showToast(getContext(), "亲爱的工程师！您需要先将[锁格/解锁]操作执行完成");
                }

//                showPopAboutMenu();

                break;

            case R.id.iv_open_close:// 点击了点击了agv或pod定位图标
                if (!bl_isSelectLockUnLock){// 不执行 锁格/解锁 操作的时候
                    if (bl_menuEnabled){
                        if (OPEN == 0){// 点击会显示agv和pod定位的布局
                            visibile(linear_agv_pod_locate);
                            iv_open_close.setImageResource(R.mipmap.icon_close);
                            OPEN = 1;
                        }else if (OPEN == 1){// 点击将隐藏agv和pod定位的布局
                            gone(linear_agv_pod_locate);
                            iv_open_close.setImageResource(R.mipmap.icon_open);
                            OPEN = 0;
                        }

                    }else {
                        ToastUtil.showToast(getContext(), "亲爱的工程师！您需要先进行地图的绘制");
                    }
                }else {
                    ToastUtil.showToast(getContext(), "亲爱的工程师！您需要先将[锁格/解锁]操作执行完成");
                }
                break;

            case R.id.iv_tips_open_close:// 点击了提示图标
                if (!bl_isSelectLockUnLock){// 不执行 锁格/解锁 操作的时候
                    if (bl_menuEnabled){

                        // 开始创建提示adapter
                        if (tipsAdapter == null){
                            listTips.clear();
                            listTips.add("请选择一个选项查看提示");
                            tipsAdapter = new TipsAdapter(getContext(), listTips);
                            LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
                            recycler_view_tips.setLayoutManager(layoutManager);
                            recycler_view_tips.setAdapter(tipsAdapter);
                        }

                        if(TIPS_OPEN == 0){
                            visibile(linear_tips);
                            iv_tips_open_close.setImageResource(R.mipmap.icon_open);
                            TIPS_OPEN = 1;
                        }else if (TIPS_OPEN == 1){
                            gone(linear_tips);
                            TIPS_OPEN = 0;
                            iv_tips_open_close.setImageResource(R.mipmap.icon_close);

                        }

                    }else {
                        ToastUtil.showToast(getContext(), "亲爱的工程师！您需要先进行地图的绘制");
                    }
                }else {
                    ToastUtil.showToast(getContext(), "亲爱的工程师！您需要先将[锁格/解锁]操作执行完成");
                }
                break;

            case R.id.iv_tips_select:// 选择看哪个提示

                new AlertDialog.Builder(getContext()).setItems(item_tips, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        tv_tips_select.setText(item_tips[which]);// 设置提示选项内容
                        switch (which){
                            case 0:// 货架碰撞处理提示
                                listTips.clear();
                                listTips.add("1、人员操作 首先给小车拍急停");
                                listTips.add("2、pad工具 给碰撞处的pod建立安全区域（即锁格），防止发生后续碰撞；");
                                listTips.add("3、人员操作 将小车关机后重启，让小车放下货架");
                                listTips.add("4、pad工具 \n 方式1：wcs部分查看货架的位置，运维人员将货架放在该位置并摆正、\n方式2：或将货架放在最近的位置处摆正，并在wcs中更新pod地址：将货架更新到当前货架摆放处；");
                                listTips.add("关键操作 \n5、pad工具 wcs部分查看货架信息，看货架是否在当前位置，位置正确继续执行下一步操作，不正确就在wcs部分 更新pod地址 将pod更新到当前位置；");
                                listTips.add("6、将小车推到货架底下，松开小车的急停按钮，等待车灯正常闪烁后，给小车重发任务，等待小车驮起货架；");
                                listTips.add("7、解锁建立的安全区域");
                                if (tipsAdapter != null){
                                    tipsAdapter.notifyDataSetChanged();
                                }
                                break;
                            case 1:// 重车在通道上，小车故障处理提示
                                listTips.clear();
                                listTips.add("1、pad工具 wcs部分 查看pod信息 确定了pod的位置后，将pod放置在该位置并将该点位锁格；");
                                listTips.add("2、wms前端页面 问题处理模块中 finish小车数据库状态：输入小车id，点击修改即可；");
                                listTips.add("3、pad工具 wcs部分 下线小车：输入小车id， 点击确定即可；");
                                listTips.add("4、人员操作 退出pad工具地图，记得杀死后台进程重新打开pad工具；");
                                listTips.add("关键操作 \n5、pad工具 wcs部分 更新POD地址：将pod更新到当前摆放的位置；");
                                listTips.add("关键操作 \n6、pad工具 rcs部分 将通道的货架更新到地图上，防止重车撞货架");
                                listTips.add("7、pad工具 wcs部分 释放pod状态");
                                listTips.add("8、pad工具 wcs部分 驱动pod去目标点位（可用存储区），这里找一个空的存储位即可");
                                listTips.add("9、pad工具 wcs部分 查看pod信息，可以知道哪辆车要来驮该货架回存储区，记得将货架位置处解锁");
                                listTips.add("10、wms前端页面 问题处理模块 更新数据库pod状态 输入pod号码 点击修改即可");
                                listTips.add("11、wms前端页面 出库 未拣拣货单模块 重新呼叫该pod。");
                                if (tipsAdapter != null){
                                    tipsAdapter.notifyDataSetChanged();
                                }
                                break;
                            case 2:// 小车空扫pod处理提示

                                listTips.clear();
                                listTips.add("1、pad工具 会有扫不到pod警告 确定是哪个pod，记住pod号码");
                                listTips.add("2、A车在这个位置要扫pod 但是没有pod 说明实际上pod在其他位置，我们要找到这个pod");
                                listTips.add("3、先看工作站附近有没有重车驮着这个pod");
                                listTips.add("4、如果3步骤没有找到，就在空扫pod的这个车的周围找该货架");
                                listTips.add("5、找到该货架后 pad工具 wcs部分 更新pod地址：将pod更新到这个点位");
                                listTips.add("6、关键操作 \n情况1：货架在小车A的周围存储区被找到，重启小车A，然后给小车A重发任务即可。 \n情况2：货架在工作站附近被另外的小车B驮着，两台车都重启并将小车B推离货架底下，在pad工具 rcs部分 执行将通道的货架更新到地图功能后，给小车A重发任务即可。");
                                if (tipsAdapter != null){
                                    tipsAdapter.notifyDataSetChanged();
                                }
                                break;
                        }
                        dialog.dismiss();
                    }
                }).create().show();

                break;

            case R.id.rbt_agv:// agv
                rbt_agv.setChecked(true);
                rbt_pod.setChecked(false);
                break;

            case R.id.rbt_pod:// pod
                rbt_agv.setChecked(false);
                rbt_pod.setChecked(true);
                break;

            // 模拟键盘键入数字的操作
            case R.id.tv_num0:// 输入数字0
                strAgvPodInput += "0";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.tv_num1:// 输入数字1
                strAgvPodInput += "1";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.tv_num2:// 输入数字2
                strAgvPodInput += "2";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.tv_num3:// 输入数字3
                strAgvPodInput += "3";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.tv_num4:// 输入数字4
                strAgvPodInput += "4";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.tv_num5:// 输入数字5
                strAgvPodInput += "5";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.tv_num6:// 输入数字6
                strAgvPodInput += "6";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.tv_num7:// 输入数字7
                strAgvPodInput += "7";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.tv_num8:// 输入数字8
                strAgvPodInput += "8";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.tv_num9:// 输入数字9
                strAgvPodInput += "9";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.iv_clear_agv_pod:// 清空输入的数字
                strAgvPodInput = "请键入号码 ";
                tv_agv_pod.setText(strAgvPodInput);
                break;

            case R.id.iv_locate_agv_pod:// agv或者pod定位按钮
                String strAgvPod = tv_agv_pod.getText().toString().trim();
                int startIndex = 6;// 起始下标
                int endIndex = strAgvPod.length();// 结尾下标
                strAgvPod = strAgvPodInput.substring(startIndex, endIndex);
//                LogUtil.e("strAgvPod", "" + strAgvPod);

                if(strAgvPod.startsWith("0")){
                    ToastUtil.showToast(getContext(), "号码输入开头不能为0");
                    return;
                }

                if ("请键入号码 ".equals(strAgvPod)){
                    ToastUtil.showToast(getContext(), "请键入号码");
                }else {
                    // 选中了agv
                    if (rbt_agv.isChecked()){
                        method_locate_agv(strAgvPod);
                    }

                    // 选中了pod
                    if (rbt_pod.isChecked()){
                        method_locate_pod(strAgvPod);
                    }
                }
                break;

        }
    }

    /**
     * 定位小车位置
     * @param robotId
     */
    private void method_locate_agv(final String robotId) {
        ProgressBarUtil.showProgressBar(getContext(), "定位中...", getResources().getColor(R.color.colorAccent));
        String url = rootAddress + getResources().getString(R.string.url_checkCarState)
                + "sectionId=" + sectionId + "&robotId=" + robotId;

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        ProgressBarUtil.dissmissProgressBar();
                        if (TextUtils.isEmpty(response.toString())){
                            ToastUtil.showToast(getContext(), "查看小车状态返回数据为空");
                            return;
                        }
                        try{
                            String agv_address = response.optJSONObject("reInfo").optString("robotAddress");
                            tv_pod_car_number.setText(robotId + "号小车位于" + agv_address);
                            visibile(tv_pod_car_number);
                            Message message = inComingMessageHandler.obtainMessage();
                            message.what = WHAT_POD_AGV_ADDRESS_HIDE;
                            inComingMessageHandler.sendMessageDelayed(message, 2000);
                        }catch (Exception e){
                            e.printStackTrace();
                            ToastUtil.showToast(getContext(), "AGV位置信息解析异常");
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ProgressBarUtil.dissmissProgressBar();
                        ToastUtil.showToast(getContext(), "查看小车状态error");
                    }
                });

        requestQueue.add(request);
    }

    /**
     * 定位agv位置
     * @param podId
     */
    private void method_locate_pod(final String podId) {
        ProgressBarUtil.showProgressBar(getContext(), "定位中...", getResources().getColor(R.color.colorAccent));
        String url = rootAddress + getResources().getString(R.string.url_checkPodStatus)
                + "sectionId=" + sectionId + "&podId=" + podId;

        LogUtil.e("strAgvPod", "urlPod = " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ProgressBarUtil.dissmissProgressBar();
                if (!TextUtils.isEmpty(response)){
                    try {
                        int startIndex = response.indexOf("address") + 8;
                        int endIndex = response.indexOf(":");
//                        LogUtil.e("startIndex", "address = " + response.substring(startIndex, endIndex));
                        String pod_address = response.substring(startIndex, endIndex);
                        tv_pod_car_number.setText(podId + "号POD位于" + pod_address);
                        visibile(tv_pod_car_number);
                        Message message = inComingMessageHandler.obtainMessage();
                        message.what = WHAT_POD_AGV_ADDRESS_HIDE;
                        inComingMessageHandler.sendMessageDelayed(message, 2000);
                    }catch (Exception e){
                        e.printStackTrace();
                        ToastUtil.showToast(getContext(), "pod定位异常");
                    }
                }else {
                    ToastUtil.showToast(getContext(), "pod信息为空");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ProgressBarUtil.dissmissProgressBar();
                ToastUtil.showToast(getContext(), "POD定位失败");
            }
        });

        requestQueue.add(request);
    }

    /**
     * 弹出popupwindow显示所有可操作项
     */
    private void showPopAboutMenu() {

        // 构建popupWindow的布局
        pop_view_menu = getLayoutInflater().inflate(R.layout.popupwindow_menu, null);

        recycler_view_menu = pop_view_menu.findViewById(R.id.recycler_view_menu);
        setMenuData();
        menusAdapter = new MenusAdapter(getActivity(), list_menu);
//        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 4);
//        recycler_view_menu.setLayoutManager(new GridLayoutManager(getActivity(), list_menu.size()));
        recycler_view_menu.setLayoutManager(layoutManager);
        recycler_view_menu.setAdapter(menusAdapter);
//        recycler_view_menu.addItemDecoration(new MyItemDecoration(MyItemDecoration.LINEAR_HORIZONTAL, 10));
        recycler_view_menu.addItemDecoration(new MyItemDecoration(MyItemDecoration.GRIDLAYOUT, 5, 4));
        recycler_view_menu.setItemAnimator(new DefaultItemAnimator());// 设置系统默认的动画效果

        // 点击按钮关闭选项界面
//        iv_menu_close = pop_view_menu.findViewById(R.id.iv_menu_close);
//        iv_menu_close.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (window_menu != null){
//                    window_menu.dismiss();
//                }
//            }
//        });

        // recyclerView的item单击监听
        if (menusAdapter != null){
            menusAdapter.setOnItemClick(new MyItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    String iconShows = list_menu.get(position).getIconShows();
                    switch (iconShows){

                        case MAP_INSTRUCTIONS:// 地图说明
                            showPopAboutMapIntroduction();
                            window_menu.dismiss();
                            break;
                        case MAP_INFORMATION:// 地图信息
                            showDialogAboutMapInfo();
                            window_menu.dismiss();
                            break;
                        case MAP_RESET:// 地图复位
                            if(bl_initData){
                                boxView.reset();// 地图复原，当自定义view触摸不到后调用复原地图
                            }else {
                                ToastUtil.showToast(getContext(), "请先绘制地图");
                                return;
                            }
                            window_menu.dismiss();
                            break;
                        case MAP_RESELECT:// 地图重选
                            interruptThread(publishThread);
                            setUpConnectionFactory();// 连接设置
                            subscribeStorageMap(inComingMessageHandler);// 先创建队列接收仓库地图的数据
                            new AlertDialog.Builder(getContext())
                                    .setIcon(R.mipmap.app_icon)
                                    .setTitle("提示")
                                    .setMessage(getResources().getString(R.string.str_map_drawAgain) + "？")
                                    .setPositiveButton("否", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setNegativeButton("是", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            creatThreadCloseConnectionAndClearData(connection_car, connection_chargingTask, connection_showAllCarCurrentPath);
                                        }
                                    }).create().show();
                            window_menu.dismiss();
                            break;
                        case MAP_LOCK_UNLOCK:// 解锁/锁格
                            if(!bl_isShowCarPath){
                                ToastUtil.showToast(getContext(), "请先显示小车当前路径");
                                return;
                            }else {
                                publishToAMPQ(Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_LOCK_UNLOCK);
                                bl_isSelectLockUnLock = true;

                                visibile(linear_lock_unlock);
//                                gone(linear_map_carLockUnLock);
//                                gone(linear_map_carBatteryInfo);
//                                gone(linear_map_wcs);
//                                gone(linear_map_rcs);
                                gone(linear_zoomOutIn);
                                gone(iv_open_close);
                                // 将图标设置为打开图标
                                iv_open_close.setImageResource(R.mipmap.icon_open);
                                OPEN = 0;
                                gone(linear_agv_pod_locate);
//                                gone(linear_map_drawAgain);
//                                gone(linear_map_reset);
//                                gone(linear_map_info);
//                                gone(linear_map_introduction);
                            }
                            window_menu.dismiss();
                            break;
                        case MAP_ROBOT_BATTERY:// 小车电量
                            CarBatteryInfoFragment fragment = new CarBatteryInfoFragment();
                            showFragment(BoxFragment.this, fragment);
                            window_menu.dismiss();
                            break;
                        case MAP_WCS:// WCS部分
                            WcsCarOperateFragment wcsCarOperateFragment = new WcsCarOperateFragment();
                            // 设置回调监听，获取地图上移除的pod
                            wcsCarOperateFragment.setOnRemovePodListener(new onPodRemoveListener() {
                                @Override
                                public void removePod(int podCodeID) {
                                    // 获取移除货架id
                                    removePodId = podCodeID;
                                    removePodIdTemp = podCodeID;
                                }
                            });
                            showFragment(BoxFragment.this, wcsCarOperateFragment);
                            window_menu.dismiss();
                            break;
                        case MAP_RCS:// RCS部分
                            RcsCarOperateFragment rcsCarOperateFragment = new RcsCarOperateFragment();
                            if (errorChargings.size() != 0){
                                String strErrorCharging = "";
                                for (int i = 0;i < errorChargings.size();i++){
                                    strErrorCharging = strErrorCharging + "充电桩故障 [ 充电桩类型：" + errorChargings.get(i).getType()
                                            + "，充电桩ID：" + errorChargings.get(i).getNumber() + " ]\n";
                                }
                                Bundle bundle = new Bundle();
                                bundle.putString("clear_charging_error", strErrorCharging);
                                rcsCarOperateFragment.setArguments(bundle);
                            }
                            // 回调监听，清除充电桩故障信息
                            rcsCarOperateFragment.setCallBack(new RcsCarOperateFragment.CallBackListener() {
                                @Override
                                public void clearChargingError() {
                                    errorChargings.clear();
                                }
                            });
                            showFragment(BoxFragment.this, rcsCarOperateFragment);
                            window_menu.dismiss();
                            break;
                            /*
                        case MAP_POD_TRIP_MESSAGE:// pod调度状态查看

                            View view_pod_trip_message = LayoutInflater.from(getContext())
                                    .inflate(R.layout.dialog_view_pod_trip_message, null);

                            et_pod_trip_message = view_pod_trip_message.findViewById(R.id.et_pod_trip_message);
                            tv_pod_trip_message = view_pod_trip_message.findViewById(R.id.tv_pod_trip_message);

                            // 查看按钮点击事件
                            view_pod_trip_message.findViewById(R.id.btn_pod_trip_message)
                                    .setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            String podIndex = et_pod_trip_message.getText().toString().trim();
                                            if (!TextUtils.isEmpty(podIndex)){
                                                showPodTripMessage(podIndex);
                                            }else {
                                                ToastUtil.showToast(getContext(), "请输入 POD 号码");
                                            }

                                        }
                                    });

                            Dialog dialog_pod_trip_message = new Dialog(getContext());
                            dialog_pod_trip_message.setContentView(view_pod_trip_message);
                            dialog_pod_trip_message.setCancelable(true);
                            dialog_pod_trip_message.show();

                            break;
                            */

                    }
                }
            });
        }

        int width = screenUtil.getScreenSize(ScreenUtil.WIDTH);
        int height = screenUtil.getScreenSize(ScreenUtil.HEIGHT);
        // 构建对PopupWindow对象
        window_menu = new PopupWindow(pop_view_menu, width / 3, height / 2);
//        window_menu = new PopupWindow(pop_view_car, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 设置弹框的动画
        window_menu.setAnimationStyle(R.style.pop_anim);
        // 设置背景透明
        window_menu.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // 设置获取焦点
        window_menu.setFocusable(true);
        // 设置触摸区域外可消失
        window_menu.setOutsideTouchable(true);
        // 实时更新状态
        window_menu.update();
        // 根据偏移量确定在parent view中的显示位置
        window_menu.showAtLocation(fab_menu, Gravity.BOTTOM | Gravity.RIGHT, 0, 0);

        // 背景透明度改变，优化用户体验
        bgAlpha(0.618f);
        window_menu.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                bgAlpha(1.0f);
            }
        });

    }

    /**
     * 设置选项的数据（包含图标和说明）
     */
    private void setMenuData() {

        list_menu = new ArrayList<>();
        list_menu.add(new MenuEntity(R.mipmap.map_introduction, MAP_INSTRUCTIONS));
        list_menu.add(new MenuEntity(R.mipmap.map_info, MAP_INFORMATION));
        list_menu.add(new MenuEntity(R.mipmap.view_restore, MAP_RESET));
        list_menu.add(new MenuEntity(R.mipmap.draw_again, MAP_RESELECT));
        list_menu.add(new MenuEntity(R.mipmap.lock_unlock, MAP_LOCK_UNLOCK));
        list_menu.add(new MenuEntity(R.mipmap.car_battery_info, MAP_ROBOT_BATTERY));
        list_menu.add(new MenuEntity(R.mipmap.icon_wcs, MAP_WCS));
        list_menu.add(new MenuEntity(R.mipmap.icon_rcs, MAP_RCS));
//        list_menu.add(new MenuEntity(R.mipmap.icon_trip_check, MAP_POD_TRIP_MESSAGE));

    }

    /**
     * 根据pod号码查询pod当前的调度状态
     * @param podIndex  pod号码
     */
    private void showPodTripMessage(String podIndex) {

        ProgressBarUtil.showProgressBar(getContext(), "查询中...",
                getResources().getColor(R.color.colorPrimaryDark));

        String url = getResources().getString(R.string.url_tripMessage)
                + "podIndex=" + podIndex;

        LogUtil.e("url_podTripCheck", "= "+ url);

        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                ProgressBarUtil.dissmissProgressBar();
                try {

                    String message = response.optString("message");// 必带message这个字段
                    ToastUtil.showToast(getContext(), "查看成功！");
                    if (!response.isNull("detail")){// 表示返回的json数据中含有detail这个字段
                        message = message + "\n\n" + response.optString("detail");
                    }
                    tv_pod_trip_message.setText(message);

                }catch (Exception e){
                    e.printStackTrace();
                    ToastUtil.showToast(getContext(), "数据解析异常");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ProgressBarUtil.dissmissProgressBar();
                error.printStackTrace();
                ToastUtil.showToast(getContext(), "查看失败！");
            }
        });

        requestQueue.add(request);// 将请求加入请求队列

    }

    /**
     * 初始化小车数据
     */
    private void initCarData() {

        if(bl_initData){
            // 中断原来存在的消费线程
//                            interruptThread(subscribeThread);
//                            interruptThread(threadChargingTask);
//                            interruptThread(threadShowAllCarCurrentPath);
            setUpConnectionFactory();
            subscribe(inComingMessageHandler);// 开启消费者线程    获取小车实时包数据
            btn_stratCarMonitor.setTextColor(Color.GREEN);
            linear_operate.setVisibility(View.GONE);// 小车监控开始时设置绘制步骤布局不可见
            view_border.setVisibility(View.GONE);// 步骤布局的边线设置不可见
            fab_path_hide.setVisibility(View.VISIBLE);
            fab_path_display.setVisibility(View.VISIBLE);
            ToastUtil.showToast(getContext(),"开始监控小车");

            subscribeChargingTask(inComingMessageHandler);// 充电任务监听
            subscribeChargingError(inComingMessageHandler);// 充电桩故障监听
            subscribeProblemFeedback(inComingMessageHandler);// 小车错误信息反馈监听
            subscribeNoMoveTimeout(inComingMessageHandler);// 小车位置不改变超时
            subscribeErrorCloseConnection(inComingMessageHandler);// 小车断开连接
//            subscribeRtHeartTimeout(inComingMessageHandler);// 小车心跳或实时包未收到超时监听

            bl_isShowCarPath = false;
            bl_isSelectLockUnLock = false;
//                            closeConnection(connection_showAllCarCurrentPath);

            gone(linear_lock_unlock);
//            visibile(linear_map_carLockUnLock);
//            visibile(linear_map_carBatteryInfo);
//            visibile(linear_map_wcs);
//            visibile(linear_map_rcs);
            visibile(linear_zoomOutIn);
//            visibile(linear_map_drawAgain);
//            visibile(linear_map_reset);
//            visibile(linear_map_info);
//            visibile(linear_map_introduction);
//                            visibile(linear_error_tip);

            // 显示小车的的锁格和未锁格路径信息
            if(!bl_isShowCarPath){
                setUpConnectionFactory();// 连接设置
                subscribeShowAllCarCurrentPath(inComingMessageHandler);
            }else {
                ToastUtil.showToast(getContext(),"小车锁格、未锁格路径已经显示");
            }

            // 当地图绘制完成且小车监控开始后，隐藏进度提示开始小车监控
            ProgressBarUtil.dissmissProgressBar();

        }else {

            ToastUtil.showToast(getContext(), "请先绘制地图");
            return;
        }

    }

    /**
     * 小车心跳或实时包未收到超时监听
     * @param handler
     */
    private void subscribeRtHeartTimeout(final Handler handler) {

        threadRtHeartTimeout = new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    if (connection_RtHeartTimeout != null && connection_RtHeartTimeout.isOpen()){
                        connection_RtHeartTimeout.close();
                    }
                    connection_RtHeartTimeout = factory.newConnection();
                    Channel channel = connection_RtHeartTimeout.createChannel();
                    channel.basicQos(1);
                    // 声明交换机
                    channel.exchangeDeclare(Constants.EXCHANGE, "direct", true);
                    // 声明随机queue
                    String queueName = System.currentTimeMillis() + "QN_RT_HEART_TIMEOUT";
                    AMQP.Queue.DeclareOk queue = channel.queueDeclare(queueName, true, false, true, null);
                    // 交换机通过路由键绑定queue
                    channel.queueBind(queue.getQueue(), Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_HEART_RT_TIMEOUT);
                    // 创建消费者
                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);

                            Message message = handler.obtainMessage();
                            message.what = WHAT_RT_HEART_TIMEOUT;
                            message.obj = body;
                            handler.sendMessage(message);

                        }
                    };
                    // 开始消费
                    channel.basicConsume(queue.getQueue(), true, consumer);

                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    }catch (InterruptedException e1){
                        e1.printStackTrace();
                    }
                }


            }
        });

        threadRtHeartTimeout.start();

    }

    /**
     * 显示小车的锁格、未锁格路径信息
     */
    private void showCarAllPathInfo() {
        new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.app_icon)
                .setTitle("提示")
                .setCancelable(false)
                .setMessage(getResources().getString(R.string.str_showAllCarLockUnlockPath) + "？")
                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(!bl_isShowCarPath){
                            setUpConnectionFactory();// 连接设置
                            subscribeShowAllCarCurrentPath(inComingMessageHandler);
                        }else {
                            ToastUtil.showToast(getContext(),"小车锁格、未锁格路径已经显示");
                        }
                    }
                }).create().show();
    }

    /**
     * 充电桩故障监听
     * @param handler
     */
    private void subscribeChargingError(final Handler handler) {

        threadChargingError = new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    if (connection_chargingError != null && connection_chargingError.isOpen()){
                        connection_chargingError.close();
                    }
                    connection_chargingError = factory.newConnection();
                    Channel channel = connection_chargingError.createChannel();
                    channel.basicQos(1);// 一次消费一条消息，消费完再接收下一条消息

                    String queueName = System.currentTimeMillis() + "QN_CHARGE_ERROR";
                    channel.exchangeDeclare(Constants.EXCHANGE, "direct", true);
                    AMQP.Queue.DeclareOk q = channel.queueDeclare(queueName, true, false, true, null);
                    channel.queueBind(q.getQueue(), Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CHARGING_ERROR);
                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);
                            Message message = handler.obtainMessage();
                            message.what = WHAT_ROBOT_CHARGING_ERROR;
                            message.obj = body;
                            handler.sendMessage(message);
                        }
                    };

                    channel.basicConsume(q.getQueue(), true, consumer);

                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    }catch (InterruptedException e1){
                        e1.printStackTrace();
                    }
                }

            }
        });

        threadChargingError.start();

    }

    /**
     * 获取错误提示内容并赋值变量
     */
    private void getAndSetErrorContent() {

//        String date = format.format(new Date(System.currentTimeMillis()));
        // 扫不到pod错误反馈
        strPodError = "";
        if (robotErrorEntityList.size() != 0){
            for (int  i = 0;i < robotErrorEntityList.size();i++){
                strPodError = strPodError + "\n\n" + "小车id：" + robotErrorEntityList.get(i).getRobotID()
                        + "\n" + "反馈时间：" + format.format(new Date(robotErrorEntityList.get(i).getErrorTime()))
                        + "\n" + "将要扫的pod：" + robotErrorEntityList.get(i).getPodCodeID()
                        + "\n" + "当前扫到的pod：" + robotErrorEntityList.get(i).getCurPodID();
            }
        }else {
            strPodError = strPodError + "\n\n" + "无" ;
        }

        // 小车位置不改变超时
        strNoMoveTimeout = "";
        if (noMoveTimeoutEntityList.size() != 0){
            for (int i = 0;i < noMoveTimeoutEntityList.size();i++){
                strNoMoveTimeout = strNoMoveTimeout + "\n\n" + "小车id：" + noMoveTimeoutEntityList.get(i).getRobotID()
                        + "\n" + "ip地址：" + noMoveTimeoutEntityList.get(i).getIp()
                        + "\n" + "端口：" + noMoveTimeoutEntityList.get(i).getPort();
            }
        }else {
            strNoMoveTimeout = strNoMoveTimeout + "\n\n" + "无" ;
        }

        // 小车断开连接
        strCloseConn = "";
        if (robotCloseConnEntityList.size() != 0){
            for (int i = 0;i < robotCloseConnEntityList.size();i++){
                strCloseConn = strCloseConn + "\n\n" + "小车id：" + robotCloseConnEntityList.get(i).getRobotID()
                        + "\n" + "反馈时间：" + format.format(new Date(robotCloseConnEntityList.get(i).getTime()))
                        + "\n" + "ip地址：" + robotCloseConnEntityList.get(i).getIp()
                        + "\n" + "端口：" + robotCloseConnEntityList.get(i).getPort();
            }
        }else {
            strCloseConn = strCloseConn + "\n\n" + "无" ;
        }

        // 充电桩故障
        strChargingError = "";
        if (errorChargings.size() != 0){
            for (int i = 0;i < errorChargings.size();i++){
                strChargingError = strChargingError + "\n\n" + "充电桩的ID：" + errorChargings.get(i).getNumber()
                        + "\n" + "statusIndex：" + errorChargings.get(i).getStatusIndex()
                        + "\n" + "描述：" + errorChargings.get(i).getStatusName()
                        + "\n" + "时间：" + format.format(new Date(errorChargings.get(i).getTime()));
            }
        }else {
            strChargingError = strChargingError + "\n\n" + "无";
        }

        // 小车心跳或实时包未收到超时

        /*
        String strRtHeartTimeoutError = "小车心跳或实时包未收到超时";
        if (rtHeartTimeoutEntityList.size() != 0){
            for (int i = 0;i < rtHeartTimeoutEntityList.size();i++){
                strRtHeartTimeoutError = strRtHeartTimeoutError + "\n\n"
                        + "小车id：" + rtHeartTimeoutEntityList.get(i).getRobotId() + "\n"
//                        + "小车类型：" + rtHeartTimeoutEntityList.get(i).getType() + "\n"
                        + "时间：" + rtHeartTimeoutEntityList.get(i).getTime();
            }
        }else {
            strRtHeartTimeoutError = strRtHeartTimeoutError + "\n\n" + "无" + "\n\n" + "*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*";
        }
        */

        tv_error_scan_pod.setText(strPodError);// 扫不到pod
        tv_error_nomove_timeout.setText(strNoMoveTimeout);// 位置不改变超时
        tv_error_close_connection.setText(strCloseConn);// 断开连接
        tv_error_chargepile.setText(strChargingError);// 充电桩故障

//        strErrorContent = strPodError + "\n\n"
//                + strNoMoveTimeout + "\n\n"
//                + strCloseConn + "\n\n"
//                + strChargingError + "\n\n";

//        tv_error_content.setText(strErrorContent);
    }

    /**
     * 故障内容置空
     */
    private void clearErrorContent(){

        strPodError = "";
        strNoMoveTimeout = "";
        strCloseConn = "";
        strChargingError = "";
//        strErrorContent = "";

    }

    /**
     * 开启消费线程获取小车断开连接的消息
     * @param handler
     */
    private void subscribeErrorCloseConnection(final Handler handler) {

        threadErrorCloseConnection = new Thread(new Runnable() {
        @Override
        public void run() {

            try{
                if (connection_errorCloseConnection != null && connection_errorCloseConnection.isOpen()){
                    connection_errorCloseConnection.close();
                }
                connection_errorCloseConnection = factory.newConnection();
                Channel channel = connection_errorCloseConnection.createChannel();
                channel.basicQos(1);// 一次消费一条消息，消费完再接收下一条消息

                String queueName = System.currentTimeMillis() + "QN_DISCONNECT_ERROR";
                channel.exchangeDeclare(Constants.EXCHANGE, "direct", true);
                AMQP.Queue.DeclareOk q = channel.queueDeclare(queueName, true, false, true, null);
                channel.queueBind(q.getQueue(), Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CLOSE_CONNECTION);
                Consumer consumer = new DefaultConsumer(channel){
                    @Override
                    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                        super.handleDelivery(consumerTag, envelope, properties, body);
                        Message message = handler.obtainMessage();
                        message.what = WHAT_ERROR_CLOSE_CONNECTION;
                        message.obj = body;
                        handler.sendMessage(message);
                    }
                };

                channel.basicConsume(q.getQueue(), true, consumer);

            }catch (Exception e){
                e.printStackTrace();
                try {
                    Thread.sleep(5000);
                }catch (InterruptedException e1){
                    e1.printStackTrace();
                }
            }

        }
    });

        threadErrorCloseConnection.start();

}

    /**
     * 开启消费线程获取小车位置不改变超时的消息
     * @param handler
     */
    private void subscribeNoMoveTimeout(final Handler handler) {

        threadNoMoveTimeout = new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    if (connection_noMoveTimeout != null && connection_noMoveTimeout.isOpen()){
                        connection_noMoveTimeout.close();
                    }
                    connection_noMoveTimeout = factory.newConnection();
                    Channel channel = connection_noMoveTimeout.createChannel();
                    channel.basicQos(1);// 一次消费一条消息，消费完再接收下一条消息

                    String queueName = System.currentTimeMillis() + "QN_NOMOVE_TIMEOUT_ERROR";
                    channel.exchangeDeclare(Constants.EXCHANGE, "direct", true);
                    AMQP.Queue.DeclareOk q = channel.queueDeclare(queueName, true, false, true, null);
                    channel.queueBind(q.getQueue(), Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_NOMOVE_TIMEOUT);
                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);
                            Message message = handler.obtainMessage();
                            message.what = WHAT_ROBOT_NOMOVE_TIMEOUT;
                            message.obj = body;
                            handler.sendMessage(message);
                        }
                    };

                    channel.basicConsume(q.getQueue(), true, consumer);

                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    }catch (InterruptedException e1){
                        e1.printStackTrace();
                    }
                }

            }
        });

        threadNoMoveTimeout.start();

    }

    /**
     * 开启消费线程获取问题反馈的消息
     * @param handler
     */
    private void subscribeProblemFeedback(final Handler handler) {

        threadProblemFeedback = new Thread(new Runnable() {
            @Override
            public void run() {

                try{
                    if (connection_problemFeedback != null && connection_problemFeedback.isOpen()){
                        connection_problemFeedback.close();
                    }
                    connection_problemFeedback = factory.newConnection();
                    Channel channel = connection_problemFeedback.createChannel();
                    channel.basicQos(1);// 一次消费一条消息，消费完再接收下一条消息

                    String queueName = System.currentTimeMillis() + "QN_NOTSCAN_POD_ERROR";
                    channel.exchangeDeclare(Constants.EXCHANGE, "direct", true);
                    AMQP.Queue.DeclareOk q = channel.queueDeclare(queueName, true, false, true, null);
                    channel.queueBind(q.getQueue(), Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_PROBLEM_FEEDBACK);
                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);
                            Message message = handler.obtainMessage();
                            message.what = WHAT_ROBOT_ERROR;
                            message.obj = body;
                            handler.sendMessage(message);
                        }
                    };

                    channel.basicConsume(q.getQueue(), true, consumer);

                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    }catch (InterruptedException e1){
                        e1.printStackTrace();
                    }
                }

            }
        });

        threadProblemFeedback.start();

    }

    /**
     * 开启消费线程获取所有充电桩的充电任务数据
     * @param handler
     */
    private void subscribeChargingTask(final Handler handler) {
        threadChargingTask = new Thread(new Runnable() {
            @Override
            public void run() {

                if (task_clear_charge_data == null){
                    task_clear_charge_data = new TimerTask() {
                        @Override
                        public void run() {

                            Message message = inComingMessageHandler.obtainMessage();
                            message.what =  WHAT_CLEAR_CHARGE_DATA;
                            inComingMessageHandler.sendMessage(message);

                        }
                    };
                }

                if (timer_clear_charge_data == null){
                    timer_clear_charge_data = new Timer();
                }

                timer_clear_charge_data.schedule(task_clear_charge_data, 2000, 1000);// 延迟2s,并每隔3s后执行任务

                try {
                    if(connection_chargingTask != null && connection_chargingTask.isOpen()){
                        connection_chargingTask.close();
                    }
                    connection_chargingTask = factory.newConnection();
                    Channel channel = connection_chargingTask.createChannel();
                    channel.basicQos(1);

                    // 创建随机队列，可持续，自动删除
                    String queueName = System.currentTimeMillis() + "QN_CHARGE_TASK";
                    channel.exchangeDeclare(Constants.MQ_EXCHANGE_CHARGINGPILE, "direct", true);
                    AMQP.Queue.DeclareOk q = channel.queueDeclare(queueName, true, false, true, null);// 声明共享队列
                    channel.queueBind(q.getQueue(), Constants.MQ_EXCHANGE_CHARGINGPILE, Constants.MQ_ROUTINGKEY_CHARGINGPILE);

                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);

                            // 发消息通知UI更新
                            Message message = handler.obtainMessage();
                            message.obj = body;
                            message.what = WHAT_CHARGING_TASK;
                            handler.sendMessage(message);
                        }
                    };
                    channel.basicConsume(q.getQueue(), true, consumer);

                } catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        threadChargingTask.start();
    }

    /**
     * 开启消费线程获取所有小车当前的路径信息（展示锁格和未锁格状态区域）
     * @param handler
     */
    private void subscribeShowAllCarCurrentPath(final Handler handler) {

        threadShowAllCarCurrentPath = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connection_showAllCarCurrentPath != null && connection_showAllCarCurrentPath.isOpen()){
                        connection_showAllCarCurrentPath.close();
                    }
                    connection_showAllCarCurrentPath = factory.newConnection();
                    Channel channel = connection_showAllCarCurrentPath.createChannel();
                    channel.basicQos(0,1,false);

                    // 创建随机队列，可持续，自动删除
                    String queueName = System.currentTimeMillis() + "QN_SHOW_CAR_PATH";
                    channel.exchangeDeclare(Constants.EXCHANGE, "direct", true);
                    AMQP.Queue.DeclareOk q = channel.queueDeclare(queueName, true, false, true, null);// 声明共享队列
                    channel.queueBind(q.getQueue(), Constants.EXCHANGE, Constants.MQ_ROUTINGKEY_CARPATH);

                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);

                            // 发消息通知UI更新
                            Message message = handler.obtainMessage();
                            message.obj = body;
                            message.what = WHAT_SHOW_ALL_CAR_CURRENT_PATH;
                            handler.sendMessage(message);
                        }
                    };
                    channel.basicConsume(q.getQueue(), true, consumer);

                } catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });

        threadShowAllCarCurrentPath.start();
    }

    /**
     * 重选清空数据
     */
    private void clearData() {
        carList.clear();// 小车信息集合
        podList.clear();// pod信息集合
        unWalkedList.clear();// 不可走区域坐标集合
        workStackList.clear();// 停止点集合，可以用来标识工作栈
        rotateList.clear();// 旋转区坐标集
        storageList.clear();// 存储区坐标集
        map_work_site_uuid.clear();
        storageEntityList.clear();// 创建集合保存所有仓库所有的信息
        chargingPileList.clear();// 声明集合保存充电桩的数据
        carRouteMap.clear();// 小车的路径map集
        carCurrentPathEntityList.clear();// 小车路径信息
        chargingTaskEntityList.clear();// 充电桩的充电任务信息
        lock_unlock_pos.clear();

        bl_isShowCarPath = false;
        bl_menuEnabled = false;// 地图重选时候，点击floatingActionButton无效

        boxView.setPodData(null, null, null,
                null, null, null, null);
        boxView.setCarAndPodData(null, null);
        boxView.setCarRouteData(null);
        boxView.setCarCurrentPath(null, null, null);
        boxView.setChargeData(null, null);
        boxView.setLockUnLockArea(null);

        // 停止定时任务

        stopTaskClearChargeData();
        stopTaskRefreshErrorData();

        if (timer_refresh_error_data != null){
            timer_refresh_error_data.cancel();
            timer_refresh_error_data = null;
        }
        if (timer_clear_charge_data != null){
            timer_clear_charge_data.cancel();
            timer_clear_charge_data = null;
        }
    }

    /**
     * 弹出popupwindow显示仓库名称和地图名称
     */
    private void showDialogAboutMapInfo() {

        // 构建布局
        pop_view_mapInfo = getLayoutInflater().inflate(R.layout.popupwindow_map_info, null);
        // 设置数据
        TextView tv_storage_map_name = pop_view_mapInfo.findViewById(R.id.tv_storage_map_name);
        tv_storage_map_name.setText(strStorageMapName);
        // 弹出对话框显示地图信息
        new AlertDialog.Builder(getContext())
                .setView(pop_view_mapInfo)
                .create().show();

    }

    /**
     * 关闭RabbitMQ通信对应的连接
     * @param connection
     */
    private void closeConnection(final Connection connection) {

        threadCloseConnection = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connection != null && connection.isOpen()){
                        connection.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Message message = inComingMessageHandler.obtainMessage();
                message.what = WHAT_CLOSE_CONNECTION;
                inComingMessageHandler.sendMessage(message);
            }
        });

        threadCloseConnection.start();

    }

    /**
     * 初始化仓库和地图
     */
    private void initStorageAndMap() {
        interruptThread(threadShowAllCarCurrentPath);
        closeConnection(connection_showAllCarCurrentPath);
        interruptThread(publishThread);
        lock_unlock_pos.clear();
        boxView.setLockUnLockArea(null);

        setUpConnectionFactory();// 连接设置
        subscribeStorageMap(inComingMessageHandler);// 先创建队列接收仓库地图的数据
        publishToAMPQ(Constants.MQ_EXCHANGE_STORAGEMAP, Constants.MQ_ROUTINGKEY_STORAGEMAP_REQUEST);// publish消息给请求队列
        new AlertDialog.Builder(getContext())
                .setIcon(R.mipmap.app_icon)
                .setTitle("提示")
                .setMessage(getResources().getString(R.string.str_initStorageData) + "？")
                .setNegativeButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectStorageMap();
                        dialog.dismiss();
                    }
                })
                .setPositiveButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        interruptThread(publishThread);// 中断发布线程
                        interruptThread(subscribeThread_storageMap);// 中断消费线程
                        dialog.dismiss();
                    }
                }).create().show();
    }

    /**
     * 初始化，选择仓库和地图
     */
    private void selectStorageMap() {
//        interruptThread(subscribeThread);// 再次初始化仓库情况下，中断小车消费线程
//        interruptThread(threadChargingTask);
//        interruptThread(threadShowAllCarCurrentPath);
        boxView.setVisibility(View.INVISIBLE);// 设置自定义地图view不可见
        tv_hint.setVisibility(View.VISIBLE);// 设置提示内容可见
        gone(linear_lock_unlock);

//        gone(linear_map_introduction);// 仓库初始化时隐藏地图说明图标
//        gone(linear_map_info);// 仓库初始化时隐藏地图信息图标
        gone(linear_zoomOutIn);// 仓库初始化时隐藏放大和缩小图标
//        gone(linear_map_reset);
//        gone(linear_map_drawAgain);
//        gone(linear_map_carLockUnLock);
//        gone(linear_map_carBatteryInfo);
//        gone(linear_map_wcs);
//        gone(linear_map_rcs);

        gone(linear_error_tip);// 隐藏错误反馈提示
        fab_path_display.setVisibility(View.GONE);
        fab_path_hide.setVisibility(View.GONE);
        bl_initStorageMap = false;// 仓库设置状态表示还未初始化仓库地图
        bl_initData = false;// 设置状态表示地图还未绘制
        boxSizeChange = Constants.DEFAULT_BOX_SIZE;// 需要重置格子的大小

        linear_operate.setVisibility(View.VISIBLE);
        view_border.setVisibility(View.VISIBLE);
        // 将按钮的字体颜色恢复初始颜色、将按钮设置为可见
        btn_selectStorageMap.setVisibility(View.VISIBLE);
        btn_drawing.setVisibility(View.VISIBLE);
        btn_stratCarMonitor.setVisibility(View.VISIBLE);
        btn_drawing.setTextColor(Color.WHITE);
        btn_selectStorageMap.setTextColor(Color.WHITE);
        btn_stratCarMonitor.setTextColor(Color.WHITE);

//        showDialog("初始化...");
        ProgressBarUtil.showProgressBar(getContext(), "初始化...",
                getResources().getColor(R.color.colorPrimaryDark));

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("requestTime", System.currentTimeMillis());// 系统当前时间
            queue.putLast(message);// publish消息
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
     * 创建消费者线程获取仓库地图数据
     * @param handler
     */
    private void subscribeStorageMap(final Handler handler) {
        subscribeThread_storageMap = new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    if(connection_storageMap != null && connection_storageMap.isOpen()){
                        connection_storageMap.close();
                    }
                    connection_storageMap = factory.newConnection();
                    Channel channel = connection_storageMap.createChannel();
                    channel.basicQos(1);
                    String queueName = System.currentTimeMillis() + "queueNameStorageMap";
                    channel.exchangeDeclare(Constants.MQ_EXCHANGE_STORAGEMAP, "direct", true);
                    AMQP.Queue.DeclareOk q = channel.queueDeclare(queueName, true, false, true, null);
                    channel.queueBind(q.getQueue(), Constants.MQ_EXCHANGE_STORAGEMAP, Constants.MQ_ROUTINGKEY_STORAGEMAP_RESPONSE);
                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);

                            Map<String, Object> mapStorageMap = (Map<String, Object>) toObject(body);

//                            disMissDialog();
//                            ToastUtil.showToast(getContext(), "获取仓库和地图数据成功了" + mapStorageMap.toString());

                            JSONObject objectStorageMap = new JSONObject(mapStorageMap);// 将map转为JsonObject结构数据
                            if(objectStorageMap.toString() != null){
                                connection_storageMap.close();// 获取仓库数据成功了，关闭连接
                                publishThread.interrupt();// 关闭发布消息线程

                                // 发消息通知handler处理，一般用作UI更新
                                Message message = handler.obtainMessage();
                                message.obj = body;
                                message.what = WHATSTORAGEMAP;
                                handler.sendMessage(message);
                            }

                        }
                    };
                    channel.basicConsume(q.getQueue(), true, consumer);
                }catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000); //sleep and then try again
                    } catch (InterruptedException e1) {
                        LogUtil.d("TAG_STORAGEAP_DATA","InterruptedException happened!");
                        e1.printStackTrace();
                    }
                }
            }
        });
        subscribeThread_storageMap.start();

    }

    /**
     * 解析初始化仓库地图获取的数据
     * @param object 数据
     */
    private void parseStorageMapData(JSONObject object) {
        storageEntityList.clear();
        List<JSONObject> jsonList = new ArrayList<>();
        try {
            Iterator<String> iterator = object.getJSONObject("allWarehouseInfo").keys();
            while (iterator.hasNext()){
                String key = iterator.next();
                jsonList.add(object.getJSONObject("allWarehouseInfo").getJSONObject(key));
            }

//            Iterator<String> iterator = object.keys();
//            while (iterator.hasNext()){
//                String key = iterator.next();
//                if(!"requestTime".equals(key) && !"wcsTime".equals(key)){
//                    jsonList.add(object.getJSONObject(key));
//                }
//            }

            // 将仓库的信息解析存入实体类集合中
            for(int i = 0;i < jsonList.size();i++){
                JSONObject jsonObject = jsonList.get(i);
                // 创建一个仓库信息实体对象
                StorageEntity storageEntity = new StorageEntity();
                storageEntity.setWarehouseId(jsonObject.optString("warehouseId"));// 仓库id
                storageEntity.setWarehouseName(jsonObject.optString("warehouseName"));// 仓库名称

                List<StorageEntity.SectionEntity> list = new ArrayList<>();
                Iterator<String> iteratorMap = jsonObject.getJSONObject("sectionMap").keys();

                LogUtil.e("jsonMap" + i, jsonObject.getJSONObject("sectionMap").toString());

                while (iteratorMap.hasNext()){
                    String key = iteratorMap.next();
                    String sectionName = (String) jsonObject.getJSONObject("sectionMap").getJSONObject(key).opt("sectionName");
                    String sectionUUID = (String) jsonObject.getJSONObject("sectionMap").getJSONObject(key).opt("sectionUUID");
                    String sectionMapId = (String) jsonObject.getJSONObject("sectionMap").getJSONObject(key).opt("sectionMapId");
                    Long sectionRcsId = jsonObject.getJSONObject("sectionMap").getJSONObject(key).optLong("sectionRcsId");

                    StorageEntity.SectionEntity sectionEntity = new StorageEntity.SectionEntity();// 创建一个地图信息实体对象
                    // 实体类对象设置数据
                    sectionEntity.setSectionName(sectionName);
                    sectionEntity.setSectionUUID(sectionUUID);
                    sectionEntity.setSectionMapId(sectionMapId);
                    sectionEntity.setSectionRcsId(sectionRcsId);

                    list.add(sectionEntity);
                }

                storageEntity.setSectionMap(list);// 仓库实体对象设置地图集
                storageEntityList.add(storageEntity);// 往仓库实体类集合中添加一个仓库实体对象
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建popupwindow，介绍说明地图的一些显示区域所代表的内容
     */
    private void showPopAboutMapIntroduction() {
        /*
        // 构建popupwindow的布局
        pop_view_mapIntroduction = getLayoutInflater().inflate(R.view_error.popupwindow_map_introduction, null);
        // 构建PopupWindow对象
//        window_mapIntroduction = new PopupWindow(pop_view_mapIntroduction, ViewGroup.LayoutParams.MATCH_PARENT, pop_height);
        window_mapIntroduction = new PopupWindow(pop_view_mapIntroduction, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        // 设置弹框的动画
        window_mapIntroduction.setAnimationStyle(R.style.pop_anim);
        // 设置背景白色
        window_mapIntroduction.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#ffffff")));
        // 设置获取焦点
        window_mapIntroduction.setFocusable(true);
        // 设置触摸区域外可消失
        window_mapIntroduction.setOutsideTouchable(true);
        // 实时更新状态
        window_mapIntroduction.update();
        // 根据偏移量确定在parent view中的显示位置
        window_mapIntroduction.showAtLocation(rl_mapView, Gravity.BOTTOM, 0, 0);
        bgAlpha(0.618f);// 设置窗口的透明度，提高用户体验
        // 设置popupwindow消失监听
        window_mapIntroduction.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                bgAlpha(1.0f);
            }
        });
        */

        pop_view_mapIntroduction = getLayoutInflater().inflate(R.layout.popupwindow_map_introduction, null);
        new AlertDialog.Builder(getContext())
                .setView(pop_view_mapIntroduction)
                .create().show();

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
                                ch.basicPublish(exchange, routingKey, null, serialize((Serializable)message));
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
     * 将map对象转换为byte[]
     * @param obj
     * @return
     */
    private byte[] serialize(Serializable obj) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(512);
        serialize(obj, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 写对象到输出流
     * @param obj
     * @param outputStream
     */
    private void serialize(Serializable obj, OutputStream outputStream) {
        if(outputStream == null) {
            throw new IllegalArgumentException("The OutputStream must not be null");
        } else {
            ObjectOutputStream out = null;
            try {
                out = new ObjectOutputStream(outputStream);
                out.writeObject(obj);
            } catch (IOException var11) {
                var11.printStackTrace();
            } finally {
                try {
                    if(out != null) {
                        out.close();
                    }
                } catch (IOException var10) {
                    var10.printStackTrace();
                }

            }

        }
    }

    /**
     * 开启消费者线程获取地图数据
     * @param handler
     */
    private void subscribeMapData(final Handler handler) {

        threadMapData = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if(connection_map != null && connection_map.isOpen()){
                        connection_map.close();
                    }
                    connection_map = factory.newConnection();
                    Channel channel = connection_map.createChannel();
                    channel.basicQos(1);// 一次只发送一个，处理完一个再发送下一个
                    String routingKeyMap = Constants.MQ_ROUTINGKEY_MAP;// 路由键
                    String exchangeMap = Constants.EXCHANGE;// 交换机名称

                    channel.exchangeDeclare(exchangeMap, "direct", true);
                    String queueName = System.currentTimeMillis() + "queueNameMap";// 客户端随机生成队列名称
                    // 声明一个可共享队列（消息不会被该队列所独占，不会被限制在这个连接中）
                    AMQP.Queue.DeclareOk qMap = channel.queueDeclare(queueName, true, false, true, null);

                    LogUtil.e("queueName_map","" + qMap.getQueue());
                    channel.queueBind(qMap.getQueue(), exchangeMap, routingKeyMap);// 将队列绑定到交换机

                    Consumer consumer = new DefaultConsumer(channel){
                        @Override
                        public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                            super.handleDelivery(consumerTag, envelope, properties, body);
                            Map<String, Object> mapMap = (Map<String, Object>) toObject(body);
                            if(mapMap.toString() != null){
                                LogUtil.e("TAG_MAP", mapMap.toString());
                                connection_map.close();
                                publishThread.interrupt();// 获取地图数据成功了，就中断地图数据请求线程

                                Message message = handler.obtainMessage();
//                                Bundle bundle = new Bundle();
//                                bundle.putByteArray("body", body);
                                message.obj = body;
                                message.what = WHATMAP;
                                handler.sendMessage(message);
                            }
                        }
                    };
                    channel.basicConsume(qMap.getQueue(), true, consumer);
                } catch (Exception e){
                    e.printStackTrace();
                    try {
                        Thread.sleep(5000); //sleep and then try again
                    } catch (InterruptedException e1) {
                        LogUtil.d("TAG_MAP_DATA","InterruptedException happened!");
                        e1.printStackTrace();
                    }
                }
            }
        });

        threadMapData.start();
    }

    /**
     * 设置充电桩位置的数据
     * @param mapMap
     */
    private void setChargerData(Map<String, Object> mapMap) {
        try{
            List<Map<String, Object>> listCharger = (List<Map<String, Object>>) mapMap.get("chargers");// 充电桩的数据获取
            for(int i = 0;i < listCharger.size();i++){
                Map<String, Object> map = listCharger.get(i);
                ChargingPileEntity entity = new ChargingPileEntity();
                // 取值
                int chargerType = Integer.parseInt(String.valueOf(map.get("chargerType")));
                int toward = Integer.parseInt(String.valueOf(map.get("toward")));
                int chargerID = Integer.parseInt(String.valueOf(map.get("chargerID")));
                String UUID = String.valueOf(map.get("UUID"));
                String addrCodeID = String.valueOf(map.get("addrCodeID"));

                // 实体类设值
                entity.setChargerType(chargerType);
                entity.setToward(toward);
                entity.setChargerID(chargerID);
                entity.setUUID(UUID);
                entity.setAddrCodeID(addrCodeID);
                // 添加充电桩实体对象
                chargingPileList.add(entity);
            }
            LogUtil.e("Chargers = ",chargingPileList.toString());
        }catch (Exception e){
            e.printStackTrace();
            ToastUtil.showToast(getContext(),"充电桩数据解析异常");
        }
    }

    /**
     * 设置工作站的uuid数据
     * @param mapMap
     */
    private void setWorkSiteUUID(Map<String, Object> mapMap) {

        try {
            if(mapMap.containsKey("workStationMap")){
                map_work_site_uuid = (Map<String, String>) mapMap.get("workStationMap");// 如果key存在，就获取key对应的值
            }else {
                map_work_site_uuid = null;// 不存在key，这时赋值为null
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 设置存储区的坐标数据
     * @param mapMap
     */
    private void setStorageData(Map<String, Object> mapMap) {
        storageList.clear();
        if(mapMap.containsKey("storageAddrList")){
            storageList = (List<Long>) mapMap.get("storageAddrList");
        }
    }

    /**
     * 设置工作栈的数据，拿到停止点的坐标，工作栈根据该坐标来进行绘制
     * @param mapMap
     */
    private void setWorkStackData(Map<String, Object> mapMap) {

        Map<String, List<Long>> workStackMap = (Map<String, List<Long>>) mapMap.get("stationAndTurnArea");
//        Log.e("TAG_WorkStack", workStackMap.toString());
        Iterator iterator = workStackMap.keySet().iterator();// 迭代器
        List<Integer> stoplist = new ArrayList<>();
        List<List<Long>> rotateList = new ArrayList<>();
        while (iterator.hasNext()){// 遍历获取key和value
//            String key = (String) iterator.next();
            String key = String.valueOf(iterator.next());

            List<Long> list = workStackMap.get(key);
//            Log.e("key", key + "");
//            Log.e("list", list.toString());
            // 将停止点的集合存入集合中
            stoplist.add(Integer.parseInt(key));
            // 存放旋转区的坐标集合
            rotateList.add(list);
        }
        workStackList = stoplist;
        this.rotateList = rotateList;
        LogUtil.e("TAG_Stop", stoplist.toString());
        LogUtil.e("TAG_Rotate", this.rotateList.toString());
    }

    /**
     * 设置不可走区域的坐标集合
     * @param mapMap
     */
    private void setUnWalkedCellData(Map<String, Object> mapMap) {
        unWalkedList = (List<Long>) mapMap.get("unWalkedCell");// 获取不可走区域坐标集合
    }

    /**
     * 连接设置
     */
    private void setUpConnectionFactory() {
        factory.setHost(Constants.MQ_HOST);//主机地址
        factory.setPort(Constants.MQ_PORT);// 端口号
        factory.setUsername(Constants.MQ_USERNAME);// 用户名
        factory.setPassword(Constants.MQ_PASSWORD);// 密码
        factory.setAutomaticRecoveryEnabled(false);
    }


    /**
     * 将byte数组转化为Object对象
     * @return
     */
    private Object toObject(byte[] bytes){
        Object object = null;
        try {
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);// 创建ByteArrayInputStream对象
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);// 创建ObjectInputStream对象
            object = objectInputStream.readObject();// 从objectInputStream流中读取一个对象
            byteArrayInputStream.close();// 关闭输入流
            objectInputStream.close();// 关闭输入流
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return object;// 返回对象
    }

    /**
     * 设置pod数据
     * @param mapMap
     */
    public void setPodData(Map<String, Object> mapMap) {

        Map<Long, Integer> podAngleMap = new HashMap<>();
        if(mapMap.containsKey("podsDirect")){
            podAngleMap = (Map<Long, Integer>) mapMap.get("podsDirect");// 获取初始化pod的角度集合，用来确定pod面的位置
        }

        List<Map<String, Object>> list = (List<Map<String, Object>>) mapMap.get("pods");//{38=227}, {36=254}, {32=251},
        if(list.size() != 0){
            for(int i = 0;i < list.size();i++){
                Map<String, Object> map = list.get(i);
                Iterator iterator = map.keySet().iterator();// 返回该map键值集合的迭代器
                while (iterator.hasNext()){
//                    long key = (long) iterator.next();// pod的Id
                    long key = Long.parseLong(String.valueOf(iterator.next()));
                    int podId = Integer.parseInt(String.valueOf(key));// pod的Id

                    String value = String.valueOf(map.get(key));// 根据键值获取value
                    int podPos = Integer.parseInt(value);// pod的地图坐标

                    // 设置pod实体类
                    PodEntity podEntity = new PodEntity();
                    podEntity.setPodId(podId);
                    podEntity.setPodPos(podPos);

                    podList.add(podEntity);// 往pod集合中添加实体对象
                }

            }
        }

        // 往podList集合的对象中添加一个角度属性
        if(podAngleMap.size() != 0){
            Iterator iterator = podAngleMap.keySet().iterator();// 获取迭代器对象
            while (iterator.hasNext()){
//                long key = (long) iterator.next();
                long key = Long.parseLong(String.valueOf(iterator.next()));

                int podIdAngle = Integer.parseInt(String.valueOf(key));// pod的id
//                int podAngle = (int) podAngleMap.get(key);// pod的角度
                int podAngle = Integer.parseInt(String.valueOf(podAngleMap.get(key)));

                if(podList.size() != 0){
                    for(int j = 0;j < podList.size();j++){
                        if(podIdAngle == podList.get(j).getPodId()){//如果是同一个pod
                            // 创建新的pod实体并设置相应属性
                            PodEntity entity = new PodEntity();
                            entity.setPodId(podList.get(j).getPodId());
                            entity.setPodPos(podList.get(j).getPodPos());
                            entity.setPodAngle(podAngle);
                            // 移除原来实体
                            podList.remove(j);
                            // 在对应位置添加新的实体
                            podList.add(j, entity);
                        }
                    }
                }
            }
        }
    }

    // car上下左右移一格所需参数
    private String act;
    private int robotId;
    /**
     * 小车上移一格
     */
    private void call_carMoveUp(int robotID){
        robotId = robotID;
        act = "up";
        String url = rootAddress + getResources().getString(R.string.url_carMoveOneGrid) + "robotId=" + robotId + "&act=" + act;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);//将StringRequest对象添加到RequestQueue里面

    }

    /**
     * 小车下移一格
     */
    private void call_carMoveDown(int robotID){
        robotId = robotID;
        act = "down";
        String url = rootAddress + getResources().getString(R.string.url_carMoveOneGrid) + "robotId=" + robotId + "&act=" + act;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);//将StringRequest对象添加到RequestQueue里面

    }

    /**
     * 小车左移一格
     */
    private void call_carMoveLeft(int robotID){
        robotId = robotID;
        act = "left";
        String url = rootAddress + getResources().getString(R.string.url_carMoveOneGrid) + "robotId=" + robotId + "&act=" + act;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);//将StringRequest对象添加到RequestQueue里面

    }

    /**
     * 小车右移一格
     */
    private void call_carMoveRight(int robotID){
        robotId = robotID;
        act = "right";
        String url = rootAddress + getResources().getString(R.string.url_carMoveOneGrid) + "robotId=" + robotId + "&act=" + act;
        StringRequest request = new StringRequest(url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        requestQueue.add(request);//将StringRequest对象添加到RequestQueue里面

    }

    private String sectionId;// 小车重发路径参数
    /**
     * 小车重发路径
     */
    private void call_resendThePath(final int robotID){
        robotId = robotID;// 小车的id
        String url = rootAddress + getResources().getString(R.string.url_resendThePath);// 请求根路径
        // 创建一个StringRequest对象并post传参
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                ToastUtil.showToast(getContext(),robotID + "号小车重发路径失败");
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {// post传参
                Map<String, String> map = new HashMap<>();
                map.put("sectionId", sectionId);
                map.put("robotId",String.valueOf(robotId));
                return map;
            }
        };
        //将StringRequest对象添加到RequestQueue里面
        requestQueue.add(request);
    }

    private String workStationId;
    /**
     * 显示当前过来的POD和面
     */
    private void call_showPod(String str_workStationId){
        workStationId = str_workStationId;
        showDialog("获取pod信息...");
        String url = rootAddress + getResources().getString(R.string.url_showPod)
                + "sectionId=" + sectionId + "&workStationId=" + workStationId;
        LogUtil.e("url_showPod",url);
        final JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                disMissDialog();
                try {
                    podName = response.optString("pod");
                    strWorkStationId = response.optString("workstation");

                    // 发消息给handler，执行释放pod操作
                    Message message = inComingMessageHandler.obtainMessage();
                    message.what = WHAT_SHOW_POD;
                    inComingMessageHandler.sendMessage(message);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ToastUtil.showToast(getContext(),"pod信息获取失败");
                disMissDialog();
            }
        });
        //将JsonObjectRequest对象添加到RequestQueue里面
        requestQueue.add(request);

    }

    private String podName;
    private String force = "false";
    private String strWorkStationId;
    /**
     * 释放pod
     */
    private void call_releasePod(){
        ProgressBarUtil.showProgressBar(getContext(), "释放pod...", getResources().getColor(R.color.colorAccent));
        String url = rootAddress + getResources().getString(R.string.url_releasePod)
                + "sectionId=" + sectionId + "&podName=" + podName + "&force=" + force + "&workStationId=" + strWorkStationId;
        LogUtil.e("url_releasePod",url);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                ProgressBarUtil.dissmissProgressBar();
                if(!TextUtils.isEmpty(response)){
                    ToastUtil.showToast(getContext(), "工作站释放pod成功");
                }else {
                    ToastUtil.showToast(getContext(), "pod释放失败，返回为空");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                ProgressBarUtil.dissmissProgressBar();
                ToastUtil.showToast(getContext(), "pod释放失败");
                error.printStackTrace();
            }
        });
        requestQueue.add(request);// 添加这条请求
    }


    boolean bl_carRouteIsEmpty = false;// false表示小车路径信息不为空
    /**
     *  检查小车的状态（这里有小车的路径信息）
     * @param robotID 小车id
     * @param flag 一个标志 0表示list参数无用，反则表示需要用到list参数
     */
    private void call_checkCarState(int robotID, final int flag){

        bl_carRouteIsEmpty = false;// 每次获取小车路径信息的时候都需要先重置该值为false
        pDialog.setMessage("获取小车当前路径信息...");
        robotId = robotID;
        String url = rootAddress + getResources().getString(R.string.url_checkCarState)
                + "sectionId=" + sectionId + "&robotId=" + robotId;
        LogUtil.e("url_check", url);
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                pDialog.dismiss();// 消失进度框
                if(response.toString() != null){
                    try {
                        if(response.getString("isSuccess").equals("true")){
                            if(!response.getJSONObject("reInfo").isNull("orderPath")){// reInfo字段对应的jsonObject下orderPath存在
                                JSONArray carRouteArray = response.getJSONObject("reInfo").getJSONArray("orderPath");// 路径信息集合
                                List<Long> carRouteList = new ArrayList<>();
                                for(int i = 0;i < carRouteArray.length();i++){
                                    long l = carRouteArray.getLong(i);
                                    carRouteList.add(l);// 添加该路径值
                                }
                                if(flag != 0){// 赋值获取的小车路径,绘制路径要用到该集合
                                    car_route_list = carRouteList;
                                }
                                if(carRouteList.size() != 0){
                                    str_carPath = carRouteList.toString();
                                }else {
                                    bl_carRouteIsEmpty = true;// 此时小车路径信息为空
                                    str_carPath = getResources().getString(R.string.str_carRouteEmpty);
                                }
                            }else{
                                bl_carRouteIsEmpty = true;// 小车路径信息为空
                                str_carPath = getResources().getString(R.string.str_carRouteEmpty);
                            }
                        }else if(response.getString("isSuccess").equals("false")){
                            str_carPath = getResources().getString(R.string.str_carRouteEmpty);
                            bl_carRouteIsEmpty = true;// 小车路径信息为空
                        }

                        if(flag == 0){
                            // 给handler发消息,更新路径信息查看
                            Message message = inComingMessageHandler.obtainMessage();
                            message.what = WHAT_CAR_ROUTE;
                            inComingMessageHandler.sendMessage(message);
                        }else if(flag == 1){

                            if(bl_carRouteIsEmpty){
                                ToastUtil.showToast(getContext(), "当前小车路径信息为空");
                                return;// 返回，不执行下面的操作
                            }
                            // 显示地图上小车的路径信息
                            Message message = inComingMessageHandler.obtainMessage();
                            message.what = WHAT_CAR_ROUTE_SHOW;
                            inComingMessageHandler.sendMessage(message);
                        }
                    }catch (Exception e){
                        ToastUtil.showToast(getContext(),"路径信息数据解析异常");
                        e.printStackTrace();
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pDialog.dismiss();// 消失进度框
                error.printStackTrace();
            }
        });
        requestQueue.add(request);//将JsonObjectRequest对象添加到RequestQueue里面
    }

    /**
     * 重发任务
     * @param robotId
     */
    private void methodResendOrder(final int robotId) {

        ProgressBarUtil.showProgressBar(getContext(), "重发任务...", getResources().getColor(R.color.colorAccent));

        String url = rootAddress + getResources().getString(R.string.url_resendOrder)
                + "sectionId=" + sectionId + "&robotId=" + robotId;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ProgressBarUtil.dissmissProgressBar();

                        ToastUtil.showToast(getContext(), "重发任务over_s");
                        if (!TextUtils.isEmpty(response.toString())){

                            String strRes = response.toString();
                            if (strRes.contains("未注册")){
                                ToastUtil.showToast(getContext(), response.toString());
                                return;
                            }else {
                                ToastUtil.showToast(getContext(), strRes);
                            }

                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        ProgressBarUtil.dissmissProgressBar();
                        ToastUtil.showToast(getContext(), "重发任务over_e");
                    }
                });

        requestQueue.add(request);


    }

    /**
     * 设置窗口的背景透明度
     * @param f 0.0-1.0
     */
    private  void bgAlpha(float f){
        WindowManager.LayoutParams layoutParams = getActivity().getWindow().getAttributes();
        layoutParams.alpha = f;
        getActivity().getWindow().setAttributes(layoutParams);
    }

    /**
     * 碎片之间的跳转
     * @param f_current
     * @param f_next
     */
    private void showFragment(Fragment f_current, Fragment f_next){
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        if(f_next.isAdded()){
            transaction.hide(f_current)
                    .show(f_next)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commitAllowingStateLoss();
        }else {
            transaction.hide(f_current)
                    .add(R.id.frame_main_content, f_next)
                    .addToBackStack(null)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commitAllowingStateLoss();
        }
    }

    @Override
    public void onDestroy() {
        LogUtil.e("BoxFragment","onDestroy");
        ToastUtil.showToast(getContext(),"onDestory");
        STOP_RELEASE_POD = true;
        super.onDestroy();
        // fragment销毁后，也把线程中断
        interruptThread(subscribeThread);
        interruptThread(threadMapData);
        interruptThread(threadShowAllCarCurrentPath);
        interruptThread(publishThread);
        interruptThread(subscribeThread_storageMap);
        interruptThread(threadChargingTask);
        interruptThread(threadProblemFeedback);
        interruptThread(threadNoMoveTimeout);
        interruptThread(threadErrorCloseConnection);
        interruptThread(threadChargingError);
        interruptThread(threadRtHeartTimeout);
        if(requestQueue != null){
            requestQueue.stop();// 停止缓存和网络调度程序
        }
        // 移除所有的回调和消息，防止Handler泄露
        inComingMessageHandler.removeCallbacksAndMessages(null);

        // 停止定时任务
        stopTaskClearChargeData();
        stopTaskRefreshErrorData();

        if (timer_clear_charge_data != null){
            timer_clear_charge_data.cancel();
            timer_clear_charge_data = null;
        }
        if (timer_refresh_error_data != null){
            timer_refresh_error_data.cancel();
            timer_refresh_error_data = null;
        }
    }

    /**
     * 清除充电桩数据的任务取消
     */
    private void stopTaskClearChargeData(){
        if(task_clear_charge_data != null){
            task_clear_charge_data.cancel();
            task_clear_charge_data = null;
        }
    }

    /**
     * 清除监控错误故障数据的任务取消
     */
    private void stopTaskRefreshErrorData(){
        if(task_refresh_error_data != null){
            task_refresh_error_data.cancel();
            task_refresh_error_data = null;
        }
    }

    /**
     * 中断线程
     * @param thread
     */
    private void interruptThread(Thread thread) {
        if(thread != null){
            thread.interrupt();
        }
    }

    /**
     * 创建线程终止连接并且清空数据
     * @param connection_car
     * @param connection_chargingTask
     */
    private void creatThreadCloseConnectionAndClearData(final Connection connection_car,
                                                        final Connection connection_chargingTask,
                                                        final Connection connection_showAllCarCurrentPath){
        t_clear_all_data = new Thread(new Runnable() {
            @Override
            public void run() {
                if(connection_car != null){
                    connection_car.abort();// 终止连接
                }
                if(connection_chargingTask != null){
                    connection_chargingTask.abort();
                }
                if(connection_showAllCarCurrentPath != null){
                    connection_showAllCarCurrentPath.abort();
                }
                if (connection_errorCloseConnection != null){// 终止小车断开连接connection
                    connection_errorCloseConnection.abort();
                }
                if (connection_noMoveTimeout != null){// 终止小车位置不改变超时connection
                    connection_noMoveTimeout.abort();
                }
                if (connection_problemFeedback != null){
                    connection_problemFeedback.abort();// 终止小车扫不到pod connection
                }
                if (connection_chargingError != null){
                    connection_chargingError.abort();// 终止充电故障监听
                }
                // 给handler发消息清空原有数据
                Message message = inComingMessageHandler.obtainMessage();
                message.what =  WHAT_CLEAR_DATA;
                inComingMessageHandler.sendMessage(message);
            }
        });
        t_clear_all_data.start();
    }

    /**
     * view不可见
     * @param view
     */
    private void gone(View view){
        view.setVisibility(View.GONE);
    }

    /**
     * view可见
     * @param view
     */
    private void visibile(View view){
        view.setVisibility(View.VISIBLE);
    }


    /**
     * fragment的生命周期方法
     * @param hidden true表示当前fragment不可见，false表示当前fragment可见
     */

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        LogUtil.e("Enjoy","hidden = " + hidden);
        if (!hidden){
            boxView.restorePos(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        LogUtil.e("BoxFragment","onDestroyView");
    }

}
