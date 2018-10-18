package com.hlzx.ljdjsj.common;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.hlzx.ljdjsj.MainActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.activity.LoginActivity;
import com.hlzx.ljdjsj.bean.UserInfo;
import com.hlzx.ljdjsj.utils.LogUtil;

/**
 * Created by alan on 2016/1/14.
 * 通知接收器
 */
public class NotifyBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int id = intent.getIntExtra("id",-1);
        if (action.equals(Constants.NOTI_ACTION)) {

            LogUtil.e("ME", "收到通知广播咯id="+id);
            //判断是否登录，如果登录就跳到订单页
            //否则重新启动应用
            if(MyApplication.getInstance().getUserInfo()==null)
            {
                Intent restartIntent = context.getPackageManager().getLaunchIntentForPackage(
                        context.getPackageName());
                context.startActivity(restartIntent);
                return;
            }
            if (MyApplication.getInstance().getUserInfo().getToken().equals(""))//没有登录
            {
                 //到登录
                //Intent restartIntent = context.getPackageManager().getLaunchIntentForPackage(
                        //context.getPackageName());

                Intent loginIntent=new Intent(context,LoginActivity.class);
                loginIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(loginIntent);

            } else {
                //跳到主页
                Intent resultIntent = new Intent(context, MainActivity.class);
                resultIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(resultIntent);
            }
        }
    }
}
