package com.hlzx.ljdjsj.service;

import org.android.agoo.client.BaseConstants;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.ThemedSpinnerAdapter;

import com.hlzx.ljdjsj.MainActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.common.Constants;
import com.hlzx.ljdjsj.utils.Helper;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.umeng.common.message.Log;
import com.umeng.message.UTrack;
import com.umeng.message.UmengBaseIntentService;
import com.umeng.message.entity.UMessage;

import java.util.Random;

/**
 * Developer defined push intent service.
 * Remember to call {@link com.umeng.message.PushAgent#setPushIntentServiceClass(Class)}.
 *
 * @author lucas
 */
//完全自定义处理类
//参考文档的1.6.5
//http://dev.umeng.com/push/android/integration#1_6_5
public class MyPushIntentService extends UmengBaseIntentService {
    private static final String TAG = MyPushIntentService.class.getName();

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.e("ME", "服务");
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        // 需要调用父类的函数，否则无法统计到消息送达
        super.onMessage(context, intent);
        try {
            //可以通过MESSAGE_BODY取得消息体
            String message = intent.getStringExtra(BaseConstants.MESSAGE_BODY);
            UMessage msg = new UMessage(new JSONObject(message));
            LogUtil.e("ME", "message=" + message);    //消息体
            LogUtil.e("ME", "custom=" + msg.custom);    //自定义消息的内容
            LogUtil.e("ME", "title=" + msg.title);    //通知标题
            LogUtil.e("ME", "text=" + msg.text);    //通知内容
            //写自己的处理逻辑

            NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setSmallIcon(R.drawable.umeng_push_notification_default_small_icon);
            builder.setContentTitle(msg.title);
            builder.setContentText(msg.text);
            //builder.setNumber(12);
            Bitmap btm = BitmapFactory.decodeResource(getResources(), R.drawable.umeng_push_notification_default_large_icon);
            builder.setLargeIcon(btm);


            /**
             * 发广播
             */
            //随机生成一个10内的随机数
            Random random=new Random();
            int id=random.nextInt(1000);
            Intent actionIntent=new Intent(Constants.NOTI_ACTION);
            actionIntent.putExtra("id",id);
            LogUtil.e("ME","发广播ID="+id);
            PendingIntent resultPendingIntent = PendingIntent.getBroadcast(getApplicationContext(),
                    0,actionIntent,0);

            // 设置通知主题的意图
            builder.setContentIntent(resultPendingIntent);
            builder.setAutoCancel(true);
            builder.setDefaults(Notification.DEFAULT_SOUND|Notification.DEFAULT_VIBRATE);//设置系统默认声音
            //获取通知管理器对象
            NotificationManager mNotificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.notify(id, builder.build());

            // 对完全自定义消息的处理方式，点击或者忽略
            /*boolean isClickOrDismissed = true;
            if (isClickOrDismissed) {
                //完全自定义消息的点击统计
                UTrack.getInstance(getApplicationContext()).trackMsgClick(msg);
            } else {
                //完全自定义消息的忽略统计
                UTrack.getInstance(getApplicationContext()).trackMsgDismissed(msg);
            }

            // 使用完全自定义消息来开启应用服务进程的示例代码
            // 首先需要设置完全自定义消息处理方式
            // mPushAgent.setPushIntentServiceClass(MyPushIntentService.class);
            // code to handle to start/stop service for app process
            JSONObject json = new JSONObject(msg.custom);
            String topic = json.getString("topic");
            Log.d(TAG, "topic=" + topic);
            if (topic != null && topic.equals("appName:startService")) {
                // 在友盟portal上新建自定义消息，自定义消息文本如下
                //{"topic":"appName:startService"}
                if (Helper.isServiceRunning(context, NotificationService.class.getName()))
                    return;
                Intent intent1 = new Intent();
                intent1.setClass(context, NotificationService.class);
                context.startService(intent1);
            } else if (topic != null && topic.equals("appName:stopService")) {
                // 在友盟portal上新建自定义消息，自定义消息文本如下
                //{"topic":"appName:stopService"}
                if (!Helper.isServiceRunning(context, NotificationService.class.getName()))
                    return;
                Intent intent1 = new Intent();
                intent1.setClass(context, NotificationService.class);
                context.stopService(intent1);
            }*/
        } catch (Exception e) {
            LogUtil.e(TAG, e.toString());
        }
    }

}
