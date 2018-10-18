package com.hlzx.ljdjsj.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.view.NoScrollForListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2015/12/10.
 */
public class MessageActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    @ViewInject(R.id.msg_lv)
    private NoScrollForListView msg_lv;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_message);
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
        title_tv.setText("消息");

        msg_lv.setAdapter(new MyMsgAdapter(this));
        msg_lv.setOnItemClickListener(itemClickListener);
    }

    AdapterView.OnItemClickListener itemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            PublicDialog.MessageDialog(MessageActivity.this,"商品扩充","哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈哈");
        }
    };

    public void back(View view) {
        finish();
    }

    class MyMsgAdapter extends BaseAdapter
    {
        Context mContext;
        public MyMsgAdapter(Context context) {
            mContext=context;
        }

        @Override
        public int getCount() {
            return 5;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            if(convertView==null)
            {
              holder=new ViewHolder();
              convertView= LayoutInflater.from(mContext).inflate(R.layout.item_message,null);
              holder.theme_tv=(TextView)convertView.findViewById(R.id.theme_tv);
              holder.time_tv=(TextView)convertView.findViewById(R.id.time_tv);
              convertView.setTag(holder);
            }else
            {
                holder=(ViewHolder)convertView.getTag();
            }
            return convertView;
        }

        class ViewHolder
        {
            TextView theme_tv;
            TextView time_tv;
        }


    }
}
