package com.hlzx.ljdjsj.activity;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.common.PublicDialog;
import com.hlzx.ljdjsj.interfaces.DialogOnClickListener;
import com.hlzx.ljdjsj.view.NoScrollForListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.List;

/**
 * Created by alan on 2015/12/10.
 */
public class ExpressServeActivity extends BaseActivity {

    @ViewInject(R.id.express_lv)
    private NoScrollForListView express_lv;

    private Spinner spinner;
    private List<String> data_list;
    private ArrayAdapter<String> arr_adapter;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_express_serve);
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
        express_lv.setAdapter(new MyExpressAdapter());
    }

    public void back(View view) {
        finish();
    }
    public void add(View view)
    {
        AlertDialog dialog= PublicDialog.expressOrderDialog(this, new DialogOnClickListener() {
            @Override
            public void onCancel() {

            }

            @Override
            public void onConfirm() {

            }
        });
        Window w=dialog.getWindow();
        //设置样式


    }

    public class MyExpressAdapter extends BaseAdapter
    {
        public MyExpressAdapter() {
        }

        @Override
        public int getCount() {
            return 6;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder=null;
            if(convertView==null){
                holder=new ViewHolder();
                convertView= LayoutInflater.from(ExpressServeActivity.this).inflate(R.layout.item_express,null);
                holder.tv=(TextView)convertView.findViewById(R.id.order_id_tv);
                convertView.setTag(holder);
            }else
            {
                holder=(ViewHolder)convertView.getTag();
            }

            return convertView;
        }
        class ViewHolder
        {
           TextView tv;
        }
    }
}
