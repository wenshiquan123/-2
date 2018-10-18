package com.hlzx.ljdjsj;

import android.app.Notification;
import android.content.Intent;
import android.graphics.Color;
import android.os.*;
import android.os.Process;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlzx.ljdjsj.activity.BillActivity;
import com.hlzx.ljdjsj.activity.MessageActivity;
import com.hlzx.ljdjsj.bean.UserInfo;
import com.hlzx.ljdjsj.common.MyObservable;
import com.hlzx.ljdjsj.fragment.ChatFragment;
import com.hlzx.ljdjsj.fragment.IncomeFragment;
import com.hlzx.ljdjsj.fragment.MeFragment;
import com.hlzx.ljdjsj.fragment.OrderListFragment;
import com.hlzx.ljdjsj.fragment.ServerFragment;
import com.hlzx.ljdjsj.service.MyPushIntentService;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.umeng.message.ALIAS_TYPE;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.PushAgent;
import com.umeng.message.RegistrationReceiver;
import com.umeng.message.UmengRegistrar;
import com.umeng.message.local.UmengLocalNotification;
import com.umeng.message.local.UmengNotificationBuilder;
import com.umeng.update.UmengUpdateAgent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;


/**
 * Created by alan on 2015/12/8.
 */
public class MainActivity extends BaseFragmentActivity implements View.OnClickListener {
    @ViewInject(R.id.main_title1_ll)
    private LinearLayout main_title1_ll;

    @ViewInject(R.id.main_title2_ll)
    private LinearLayout main_title2_ll;

    @ViewInject(R.id.main_title3_ll)
    private LinearLayout main_title3_ll;

    @ViewInject(R.id.main_title4_ll)
    private LinearLayout main_title4_ll;

    @ViewInject(R.id.main_title5_ll)
    private LinearLayout main_title5_ll;

    @ViewInject(R.id.main_title1_tv)
    private TextView main_title1_tv;
    @ViewInject(R.id.main_title2_tv)
    private TextView main_title2_tv;
    @ViewInject(R.id.main_title3_tv)
    private TextView main_title3_tv;
    @ViewInject(R.id.main_title4_tv)
    private TextView main_title4_tv;
    @ViewInject(R.id.main_title5_tv)
    private TextView main_title5_tv;

    @ViewInject(R.id.main_title1_iv)
    private ImageView main_title1_iv;
    @ViewInject(R.id.main_title2_iv)
    private ImageView main_title2_iv;
    @ViewInject(R.id.main_title3_iv)
    private ImageView main_title3_iv;
    @ViewInject(R.id.main_title4_iv)
    private ImageView main_title4_iv;
    @ViewInject(R.id.main_title5_iv)
    private ImageView main_title5_iv;

    //有消息的原点
    @ViewInject(R.id.point1_iv)
    private ImageView point1_iv;
    @ViewInject(R.id.point2_iv)
    private ImageView point2_iv;
    @ViewInject(R.id.point3_iv)
    private ImageView point3_iv;
    @ViewInject(R.id.point4_iv)
    private ImageView point4_iv;
    @ViewInject(R.id.point5_iv)
    private ImageView point5_iv;
    //标题
    @ViewInject(R.id.title_tv)
    private TextView title_tv;


    //@ViewInject(R.id.bill_tv)
   // private TextView bill_tv;

    //账单
    @ViewInject(R.id.bill_ll)
    private LinearLayout bill_ll;

    //消息
    @ViewInject(R.id.msg_iv)
    private ImageView msg_iv;

    @ViewInject(R.id.msg_ll)
    private LinearLayout msg_ll;

    MeFragment mMeFragment;
    IncomeFragment mIncomeFragment;
    ServerFragment mServerFragment;
    OrderListFragment mOrderFragment;
    ChatFragment mChatFragment;

    //记录fragment的位置
    private static int mPosition = -1;

    //请求码，
    private static final int MSG_CODE=1001;
    private static final int BILL_CODE=1002;

    //判断操作
    private static final int OPERATE_EXIT=1;

    int operate=0;

    MyObservable observable=new MyObservable();
    @Override
    public void layout() {
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);

        initView();
    }

    private void initView()
    {

        main_title1_ll.setOnClickListener(this);
        main_title2_ll.setOnClickListener(this);
        main_title3_ll.setOnClickListener(this);
        main_title4_ll.setOnClickListener(this);
        main_title5_ll.setOnClickListener(this);
        main_title1_ll.performClick();
        hideAllRedPoint();
        //showMessagePoint(1);

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        main_title1_ll.performClick();
        hideAllRedPoint();
        //需要打开的订单状态
        observable.setOrder_status(1);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //super.onSaveInstanceState(outState);//将这一行注释掉，阻止activity保存fragment的状态
        //记录当前的position
        outState.putInt("position", mPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        mPosition = savedInstanceState.getInt("position");
        if (mPosition > 0 && mPosition < 6) {
            if (mPosition == 1) {
                main_title1_ll.performClick();
            } else if (mPosition == 2) {

                main_title2_ll.performClick();
            } else if (mPosition == 3) {

                main_title3_ll.performClick();
            } else if (mPosition == 4) {

                main_title4_ll.performClick();
            } else if (mPosition == 5) {

                main_title5_ll.performClick();
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (v == main_title1_ll) {
            if (mOrderFragment == null || !mOrderFragment.isAdded()) {
                mOrderFragment = new OrderListFragment();
                observable.addObserver(mOrderFragment);//添加观察者
                transaction.add(R.id.content_ll, mOrderFragment);
            }
            textAndImageHigh(transaction, 1);
            transaction.show(mOrderFragment);
            bill_ll.setVisibility(View.INVISIBLE);

        } else if (v == main_title2_ll) {
            if (mServerFragment == null || !mServerFragment.isAdded()) {
                mServerFragment = new ServerFragment();
                transaction.add(R.id.content_ll, mServerFragment);
            }
            textAndImageHigh(transaction, 2);
            transaction.show(mServerFragment);
            bill_ll.setVisibility(View.INVISIBLE);

        } else if (v == main_title3_ll) {
            if (mChatFragment == null || !mChatFragment.isAdded()) {
                mChatFragment = new ChatFragment();
                transaction.add(R.id.content_ll, mChatFragment);
            }
            textAndImageHigh(transaction, 3);
            transaction.show(mChatFragment);
            bill_ll.setVisibility(View.INVISIBLE);

        } else if (v == main_title4_ll) {
            if (mIncomeFragment == null || !mIncomeFragment.isAdded()) {
                mIncomeFragment = new IncomeFragment();
                transaction.add(R.id.content_ll, mIncomeFragment);
            }
            textAndImageHigh(transaction, 4);
            transaction.show(mIncomeFragment);


        } else if (v == main_title5_ll) {

            if (mMeFragment == null || !mMeFragment.isAdded()) {
                mMeFragment = new MeFragment();
                transaction.add(R.id.content_ll, mMeFragment);
            }
            textAndImageHigh(transaction, 5);
            transaction.show(mMeFragment);
            bill_ll.setVisibility(View.INVISIBLE);
        }
        transaction.commitAllowingStateLoss();


    }

    /**
     * 某个高亮
     *
     * @param position
     */
    public void textAndImageHigh(FragmentTransaction transaction, int position) {
        mPosition = position;
        allTextColorGrey();
        allImageBeGrey();
        hideAllFragment(transaction);
        if (position == 1) {
            bill_ll.setVisibility(View.INVISIBLE);
            msg_ll.setVisibility(View.INVISIBLE);
            main_title1_tv.setTextColor(Color.parseColor("#FFD600"));
            main_title1_iv.setImageResource(R.mipmap.tab_list_selected);
            //设置店名
            UserInfo userInfo=MyApplication.getInstance().getUserInfo();
            if(userInfo!=null)
            {
                title_tv.setText(userInfo.getShop_name());
                //LogUtil.e("ME","店铺名称="+userInfo.getShop_name());
            }

        } else if (position == 2) {
            msg_ll.setVisibility(View.INVISIBLE);
            bill_ll.setVisibility(View.INVISIBLE);
            main_title2_tv.setTextColor(Color.parseColor("#FFD600"));
            main_title2_iv.setImageResource(R.mipmap.tab_serves_selected);
            title_tv.setText("服务");
        } else if (position == 3) {
            bill_ll.setVisibility(View.INVISIBLE);
            msg_ll.setVisibility(View.INVISIBLE);
            main_title3_tv.setTextColor(Color.parseColor("#FE7749"));
            main_title3_iv.setImageResource(R.mipmap.tab_chat_selected);
            title_tv.setText("聊天");
        } else if (position == 4) {
            bill_ll.setVisibility(View.VISIBLE);
            msg_ll.setVisibility(View.INVISIBLE);
            main_title4_tv.setTextColor(Color.parseColor("#FFD600"));
            main_title4_iv.setImageResource(R.mipmap.tab_income_selected);
            title_tv.setText("收入");
        } else if (position == 5) {
            bill_ll.setVisibility(View.INVISIBLE);
            msg_ll.setVisibility(View.INVISIBLE);
            main_title5_tv.setTextColor(Color.parseColor("#FFD600"));
            main_title5_iv.setImageResource(R.mipmap.tab_me_selected);
            title_tv.setText("我");
        }
    }
    /**
     * 隐藏所有的fragment
     *
     * @param transaction
     */
    public void hideAllFragment(FragmentTransaction transaction) {
        if (mOrderFragment != null) {
            transaction.hide(mOrderFragment);
        }
        if (mServerFragment != null) {
            transaction.hide(mServerFragment);
        }
        if (mChatFragment != null) {
            transaction.hide(mChatFragment);
        }
        if (mIncomeFragment != null) {
            transaction.hide(mIncomeFragment);
        }
        if (mMeFragment != null) {
            transaction.hide(mMeFragment);
        }
    }

    //隐藏所有消息点
    public void hideAllRedPoint() {
        point1_iv.setVisibility(View.INVISIBLE);
        point2_iv.setVisibility(View.INVISIBLE);
        point3_iv.setVisibility(View.INVISIBLE);
        point4_iv.setVisibility(View.INVISIBLE);
        point5_iv.setVisibility(View.INVISIBLE);
    }

    //显示某个红点
    public void showMessagePoint(int position) {
        if (position > 5 || position < 0) {
            throw new IllegalStateException("position is out");
        }
        hideAllRedPoint();
        if (position == 1) {
            point1_iv.setVisibility(View.VISIBLE);
        } else if (position == 2) {
            point2_iv.setVisibility(View.VISIBLE);
        } else if (position == 3) {
            point3_iv.setVisibility(View.VISIBLE);
        } else if (position == 4) {
            point4_iv.setVisibility(View.VISIBLE);
        } else if (position == 5) {
            point5_iv.setVisibility(View.VISIBLE);
        }
    }


    /**
     * 字体暗色
     */
    public void allTextColorGrey() {
        main_title1_tv.setTextColor(Color.parseColor("#282828"));
        main_title2_tv.setTextColor(Color.parseColor("#282828"));
        main_title3_tv.setTextColor(Color.parseColor("#282828"));
        main_title4_tv.setTextColor(Color.parseColor("#282828"));
        main_title5_tv.setTextColor(Color.parseColor("#282828"));
    }

    /**
     * 图片暗色
     */
    public void allImageBeGrey() {
        main_title1_iv.setImageResource(R.mipmap.tab_list);
        main_title2_iv.setImageResource(R.mipmap.tab_serves);
        main_title3_iv.setImageResource(R.mipmap.tab_chat);
        main_title4_iv.setImageResource(R.mipmap.tab_income);
        main_title5_iv.setImageResource(R.mipmap.tab_me);
    }

    public void bill(View view) {
        startActivityForResult(new Intent(this, BillActivity.class), BILL_CODE);
    }

    private long lastTime = 0;

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 2000) {
            showToast("再按一次返回键退出应用程序");
            lastTime = currentTime;
        } else {

            exit();
        }
    }

    private void exit()
    {
        //operate=OPERATE_EXIT;
        //清空返回栈
        finish();

    }

    public void onMsg(View view)
    {
       startActivityForResult(new Intent(this, MessageActivity.class), MSG_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==MSG_CODE)
        {
            if(mOrderFragment!=null)
                mOrderFragment.onActivityResult(requestCode,resultCode,data);
        }else if(requestCode==BILL_CODE)
        {
            if(mIncomeFragment!=null)
                mIncomeFragment.onActivityResult(requestCode,resultCode,data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mOrderFragment=null;
        mIncomeFragment=null;
        mMeFragment=null;
        //关闭推送
        /*if(PushAgent.getInstance(this).isEnabled()) {
            PushAgent.getInstance(this).disable();
        }*/

        if(operate==OPERATE_EXIT)
        {
            android.os.Process.killProcess(Process.myPid());//退出应用，建议用这个
        }

    }

}
