package com.hlzx.ljdjsj.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.MyApplication;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.bean.OrderDetail;
import com.hlzx.ljdjsj.common.Constants;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.interfaces.ShakeHandCallback;
import com.hlzx.ljdjsj.utils.HttpConstant;
import com.hlzx.ljdjsj.utils.HttpUtil;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.utils.PublicUtils;
import com.hlzx.ljdjsj.utils.UrlsConstant;
import com.hlzx.ljdjsj.utils.http.ClientEncryptionPolicy;
import com.hlzx.ljdjsj.view.NoScrollForListView;
import com.hlzx.ljdjsj.view.ScoreView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;
import com.lidroid.xutils.view.annotation.ViewInject;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by alan on 2015/12/10.
 * 订单详情
 */
public class OrderDetailActivity extends BaseActivity implements android.os.Handler.Callback {

    private int order_id = 0;//订单id
    private int order_status = 0;//订单状态

    //加载数据
    @ViewInject(R.id.load_linear_data)
    private RelativeLayout load_linear_data;
    //没有数据
    @ViewInject(R.id.rela_no_data)
    private RelativeLayout rela_no_data;

    @ViewInject(R.id.order_detail_lv)
    private NoScrollForListView order_detail_lv;

    Handler handler = new Handler(this);
    private static final int UPDATE = 101;//更新UI

    //订单编号
    @ViewInject(R.id.order_code_tv)
    private TextView order_code_tv;
    //下单时间
    @ViewInject(R.id.place_order_tv)
    private TextView place_order_tv;
    //预约配送时间
    @ViewInject(R.id.come_time_tv)
    private TextView come_time_tv;
    //配送地址
    @ViewInject(R.id.address_tv)
    private TextView address_tv;
    //备注
    @ViewInject(R.id.remark_tv)
    private TextView remark_tv;
    //总额
    @ViewInject(R.id.total_money_tv)
    private TextView total_money_tv;
    //收货人
    @ViewInject(R.id.link_man_tv)
    private TextView link_man_tv;
    //联系电话
    @ViewInject(R.id.link_phone_tv)
    private TextView link_phone_tv;

    //总下单次数
    @ViewInject(R.id.order_count_tv)
    private TextView order_count_tv;
    //支付账户
    @ViewInject(R.id.pay_account_tv)
    private TextView pay_account_tv;
    //今日支付次数
    @ViewInject(R.id.pay_today_count_tv)
    private TextView pay_today_count_tv;
    //用户账户
    @ViewInject(R.id.user_name_tv)
    private TextView user_name_tv;
    //订单状态
    //@ViewInject(R.id.order_status_tv)
    //private TextView order_status_tv;
    //付款方式
    @ViewInject(R.id.pay_type_tv)
    private TextView pay_type_tv;
    //付款状态
    @ViewInject(R.id.pay_status_tv)
    private TextView pay_status_tv;
    //付款时间
    @ViewInject(R.id.pay_time_tv)
    private TextView pay_time_tv;
    //商品总价
    @ViewInject(R.id.goods_total_tv)
    private TextView goods_total_tv;

    //拒绝订单
    @ViewInject(R.id.right_tv)
    private TextView right_tv;

    //回复
    @ViewInject(R.id.response_tv)
    private TextView response_tv;

    @ViewInject(R.id.commit_bt)
    private Button commit_bt;

    //运费
    @ViewInject(R.id.fare_price_tv)
    private TextView fare_price_tv;

    //订单奖励
    @ViewInject(R.id.order_reward_tv)
    private TextView order_reward_tv;

    //最后更新时间
    @ViewInject(R.id.update_time_tv)
    private TextView update_time_tv;

    //买家评价
    @ViewInject(R.id.evaluate_ll)
    private LinearLayout evaluate_ll;

    //评论时间
    @ViewInject(R.id.evaluate_time_tv)
    private TextView evaluate_time_tv;
    //配送速度
    @ViewInject(R.id.speed_sv)
    private ScoreView speed_sv;

    //服务态度
    @ViewInject(R.id.serve_sv)
    private ScoreView serve_sv;

    //服务质量
    @ViewInject(R.id.quality_sv)
    private ScoreView quality_sv;

    //评价内容
    @ViewInject(R.id.content_tv)
    private TextView content_tv;

    Dialog loadDialog = null;


    //订单商品详情
    ArrayList<OrderDetail> orderDetails = new ArrayList<OrderDetail>();
    MyOrderAdapter mOrderAdapter;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_order_detail);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewUtils.inject(this);
        initView();
    }

    @Override
    public void initView() {
        super.initView();
        order_id = getIntent().getIntExtra("order_id", 0);
        order_status = getIntent().getIntExtra("order_status", 0);

        if (order_status > 1) {
            right_tv.setVisibility(View.INVISIBLE);
        }

        if (order_status == 1) {
            commit_bt.setText("开始配送");
            evaluate_ll.setVisibility(View.GONE);

        } else if (order_status == 2) {
            commit_bt.setText("我已送达");
            evaluate_ll.setVisibility(View.GONE);


        }else if(order_status==4)
        {
            evaluate_ll.setVisibility(View.VISIBLE);
            commit_bt.setVisibility(View.GONE);

        }else {
            evaluate_ll.setVisibility(View.GONE);
            commit_bt.setVisibility(View.GONE);
        }

        LogUtil.e("ME", "订单详情:oder_id=" + order_id + ";order_status=" + order_status);
        mOrderAdapter = new MyOrderAdapter(this);
        order_detail_lv.setAdapter(mOrderAdapter);
        loadData();
    }

    public void back(View view) {
        finish();
    }

    public void chat(View view) {
        showToast("聊天");
    }

    public void refuseOrder(View view) {
        Intent intent = new Intent(this, RefuseOderActivity.class);
        intent.putExtra("order_id", order_id);
        intent.putExtra("order_status", order_status);
        startActivity(intent);
    }

    //加载数据
    private void loadData() {
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("order_id", order_id);
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);
            LogUtil.e("ME", "提交的数据=" + map.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        HttpUtil.doPostRequest(UrlsConstant.ORDER_DETAIL, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "order_detail=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    String text = jsonObject.getString("text");
                    String data = jsonObject.getString("data");
                    String iv = jsonObject.getString("iv");

                    //判断是否在别处登录
                    String serverMsg = jsonObject.getString("msg");
                    if (serverMsg.equals("20002") || serverMsg.equals("20004")) {
                        showToast(text);
                        Intent intent = new Intent(OrderDetailActivity.this, LoginActivity.class);
                        intent.putExtra("action", 0);
                        //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        startActivity(intent);
                        finish();
                        return;
                    } else if (serverMsg.equals("10012")) {    //重新握手
                        PublicUtils.handshake(OrderDetailActivity.this, new ShakeHandCallback() {
                            @Override
                            public void onSuccessed(String str) {
                                loadData();
                            }

                            @Override
                            public void onFalied(String str) {
                                showToast(str);
                            }
                        });
                        return;
                    }

                    if (status == HttpConstant.SUCCESS_CODE) {
                        if (data != null && !data.equals("")) {
                            String deData = null;
                            try {
                                deData = ClientEncryptionPolicy.getInstance().decrypt(data, iv);
                                LogUtil.e("ME", "解密数据=" + deData);
                                Message msg = handler.obtainMessage(UPDATE);
                                msg.obj = deData;
                                handler.sendMessage(msg);
                            } catch (Exception e) {
                                LogUtil.e("ME", "解密数据异常" + deData);
                                //rela_no_data.setVisibility(View.VISIBLE);
                            }
                        }
                    } else if (status == HttpConstant.FAILURE_CODE) {
                        LogUtil.e("ME", "解密数据异常");
                        //rela_no_data.setVisibility(View.VISIBLE);
                    }
                } catch (JSONException e) {
                    //rela_no_data.setVisibility(View.VISIBLE);
                }
                //load_linear_data.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(HttpException e, String s)
            {
                showToast(s);
                //rela_no_data.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (UPDATE == msg.what) {
            String deData = (String) msg.obj;
            com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(deData);
            order_code_tv.setText(jsonObject.getString("order_code"));
            place_order_tv.setText(jsonObject.getString("ctime"));
            come_time_tv.setText(jsonObject.getString("come_time"));
            address_tv.setText(jsonObject.getString("address"));
            remark_tv.setText(jsonObject.getString("remark"));
            total_money_tv.setText("￥" + jsonObject.getString("total_money"));
            link_man_tv.setText(jsonObject.getString("link_man"));
            link_phone_tv.setText(jsonObject.getString("link_phone"));
            order_count_tv.setText(jsonObject.getString("order_count"));
            pay_account_tv.setText(jsonObject.getString("pay_account"));
            pay_today_count_tv.setText(jsonObject.getString("pay_today_count"));
            user_name_tv.setText(jsonObject.getString("user_name"));
            order_reward_tv.setText("￥" + jsonObject.getString("order_reward"));
            fare_price_tv.setText("￥" + jsonObject.getString("fare_price"));
            update_time_tv.setText(jsonObject.getString("update_time"));

            //订单状态：1-待发货、2-配送中、3-已配送、4-已完成、5-已拒绝
            /*int orderStatus = 0;
            orderStatus = jsonObject.getIntValue("order_status");
            switch (orderStatus) {
                case 1:
                    order_status_tv.setText("待发货");
                    break;
                case 2:
                    order_status_tv.setText("配送中");
                    break;
                case 3:
                    order_status_tv.setText("已配送");
                    break;
                case 4:
                    order_status_tv.setText("已完成");
                    break;
                case 5:
                    order_status_tv.setText("已拒绝");
                    break;
            }*/

            //付款方式：参照支付表，1-微信支付、2-支付宝支付
            int payType = 0;
            payType = jsonObject.getIntValue("pay_type");
            switch (payType) {
                case 1:
                    pay_type_tv.setText("微信支付");
                    break;
                case 2:
                    pay_type_tv.setText("支付宝支付");
                    break;
            }
            //付款状态：1-未付款、2-已付款、3-申请退款、4-已受理、5-已退款
            int payStatus = 0;
            payStatus = jsonObject.getIntValue("pay_status");
            switch (payStatus) {
                case 1:
                    pay_status_tv.setText("未付款");
                    break;
                case 2:
                    pay_status_tv.setText("已付款");
                    break;
                case 3:
                    pay_status_tv.setText("申请退款");
                    break;
                case 4:
                    pay_status_tv.setText("已受理");
                    break;
                case 5:
                    pay_status_tv.setText("已退款");
                    break;
            }
            pay_time_tv.setText(jsonObject.getString("pay_time"));

            //评价
            try {
                JSONArray array1 = jsonObject.getJSONArray("order_comment");
                if (array1 != null) {
                    com.alibaba.fastjson.JSONObject evaluateJson = array1.getJSONObject(0);
                    //LogUtil.e("ME","评价="+evaluateJson.toString());
                    if (evaluateJson != null) {
                        evaluate_time_tv.setText(evaluateJson.getString("ctime"));
                        quality_sv.setScore(evaluateJson.getIntValue("quality"), R.mipmap.star_dark, R.mipmap.star_bright);
                        serve_sv.setScore(evaluateJson.getIntValue("service"), R.mipmap.star_dark, R.mipmap.star_bright);
                        speed_sv.setScore(evaluateJson.getIntValue("speed"), R.mipmap.star_dark, R.mipmap.star_bright);
                        evaluate_time_tv.setText(evaluateJson.getString("ctime"));
                        content_tv.setText(evaluateJson.getString("content"));
                    } else {
                        evaluate_ll.setVisibility(View.GONE);
                    }
                }
            }catch (Exception e){
                evaluate_ll.setVisibility(View.GONE);
            }


            JSONArray array = jsonObject.getJSONArray("order_detail");
            orderDetails.clear();
            OrderDetail detail = null;
            com.alibaba.fastjson.JSONObject object = null;
            float goodsTotal = 0;//商品总价
            for (int i = 0; i < array.size(); i++) {
                detail = new OrderDetail();
                object = array.getJSONObject(i);
                detail.setGoods_id(object.getIntValue("goods_id"));
                detail.setGoods_name(object.getString("goods_name"));
                detail.setGoods_path(object.getString("goods_path"));
                detail.setGoods_price(object.getString("goods_price"));
                detail.setPurchase_num(object.getIntValue("purchase_num"));
                detail.setFormat(object.getString("goods_format"));
                orderDetails.add(detail);

                float tempPrice = 0;
                int tempNum = 0;
                //LogUtil.e("ME","price="+object.getIntValue("goods_price"));
                tempPrice = Float.parseFloat(object.getString("goods_price"));
                tempNum = object.getIntValue("purchase_num");
                goodsTotal += tempPrice * tempNum;

                object = null;
                detail = null;
            }

            goods_total_tv.setText("￥" + goodsTotal);
            mOrderAdapter.notifyDataSetChanged();

        }
        return false;
    }


    public void commit(View view) {
        if (order_status == 1) {
            doCommit(1, order_id);
        } else if (order_status == 2) {
            doCommit(2, order_id);
        }

    }

    /*
    * 提交处理类型到服务器
    * @params action_type 为处理类型
    * @params order_id 订单id
    * @return void
    */
    private void doCommit(final int action_type, int order_id) {
        //封装session
        Map<String, String> map = new HashMap();
        JSONObject json = new JSONObject();
        try {
            json.put("token", MyApplication.getInstance().getUserInfo().getToken());
            json.put("order_id", order_id);
            json.put("action_type", action_type);//处理理由类型：1.开始配送、2.我已送达、3.拒绝订单
            String edata1 = URLEncoder.encode(ClientEncryptionPolicy.getInstance().encrypt(json), "utf-8");
            map.put("security_session_id", MyApplication.getSession_id());
            map.put("iv", ClientEncryptionPolicy.getInstance().getIV());
            map.put("data", edata1);

        } catch (Exception e) {
            e.printStackTrace();
        }

        loadDialog = PublicDialog.createLoadingDialog(this, "正在提交..");
        loadDialog.show();
        HttpUtil.doPostRequest(UrlsConstant.ORDER_ACTION, map, new RequestCallBack<String>() {
            @Override
            public void onSuccess(ResponseInfo<String> responseInfo) {
                String result = responseInfo.result.toString();
                LogUtil.e("ME", "订单处理=" + result);
                try {
                    com.alibaba.fastjson.JSONObject jsonObject = JSON.parseObject(result);
                    int status = jsonObject.getIntValue("status");
                    //String data=jsonObject.getString("data");
                    String text = jsonObject.getString("text");
                    if (status == HttpConstant.SUCCESS_CODE) {
                        if (loadDialog != null) {
                            loadDialog.dismiss();
                            loadDialog = null;
                        }
                        //showToast("提交成功");

                        if(action_type==1)
                        {
                            Intent intent=new Intent(Constants.ORDER_STATUS_2);
                            OrderDetailActivity.this.sendBroadcast(intent);
                        }else if(action_type==2)
                        {
                            Intent intent=new Intent(Constants.ORDER_STATUS_3);
                            OrderDetailActivity.this.sendBroadcast(intent);
                        }
                        finish();

                    } else if (status == HttpConstant.FAILURE_CODE) {
                        if (loadDialog != null) {
                            loadDialog.dismiss();
                            loadDialog = null;
                        }
                        showToast(text);
                    }
                } catch (JSONException e) {
                    showToast(e.toString());
                    if (loadDialog != null) {
                        loadDialog.dismiss();
                        loadDialog = null;
                    }
                }
            }

            @Override
            public void onFailure(HttpException e, String s) {
                loadDialog.dismiss();
                showToast(s);
            }
        });

    }

    class MyOrderAdapter extends BaseAdapter {
        Context mContext;

        public MyOrderAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return orderDetails.size();
        }

        @Override
        public Object getItem(int position) {
            return orderDetails.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.item_order_detail, null);
                holder.goods_img_iv = (ImageView) convertView.findViewById(R.id.goods_img_iv);
                holder.goods_name_tv = (TextView) convertView.findViewById(R.id.goods_name_tv);
                holder.goods_format_tv = (TextView) convertView.findViewById(R.id.goods_format_tv);
                holder.goods_price_tv = (TextView) convertView.findViewById(R.id.goods_price_tv);
                holder.goods_size_tv = (TextView) convertView.findViewById(R.id.goods_size_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (orderDetails.size() > 0) {
                Picasso.with(mContext).load(orderDetails.get(position).getGoods_path())
                        .resize(100, 100).centerCrop()
                        .placeholder(R.mipmap.tu)
                        .error(R.mipmap.tu)
                        .into(holder.goods_img_iv);
                holder.goods_name_tv.setText(orderDetails.get(position).getGoods_name());
                 holder.goods_format_tv.setText(orderDetails.get(position).getFormat());
                holder.goods_price_tv.setText("￥" + orderDetails.get(position).getGoods_price());
                holder.goods_size_tv.setText("x" + orderDetails.get(position).getPurchase_num() + "");
            }
            return convertView;
        }

        class ViewHolder {
            ImageView goods_img_iv;
            TextView goods_name_tv;
            TextView goods_format_tv;
            TextView goods_price_tv;
            TextView goods_size_tv;
        }
    }


}
