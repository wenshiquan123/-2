package com.hlzx.ljdjsj.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.view.NoScrollForListView;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2015/12/10.
 */
public class ArmingUserActivity extends BaseActivity implements View.OnClickListener{
    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    //tab1
    @ViewInject(R.id.four_ll)
    private LinearLayout four_ll;
    @ViewInject(R.id.tab4_ll)
    private LinearLayout tab4_ll;
    @ViewInject(R.id.four_tv)
    private TextView four_tv;
    //tab2
    @ViewInject(R.id.eight_ll)
    private LinearLayout eight_ll;
    @ViewInject(R.id.tab8_ll)
    private LinearLayout tab8_ll;
    @ViewInject(R.id.eight_tv)
    private TextView eight_tv;
    //tab3
    @ViewInject(R.id.twelve_ll)
    private LinearLayout twelve_ll;
    @ViewInject(R.id.tab12_ll)
    private LinearLayout tab12_ll;
    @ViewInject(R.id.twelve_tv)
    private TextView twelve_tv;
    @ViewInject(R.id.arming_user_lv)
    private NoScrollForListView arming_user_lv;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_arming_user);
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
        title_tv.setText("待发展用户");

        four_ll.setOnClickListener(this);
        eight_ll.setOnClickListener(this);
        twelve_ll.setOnClickListener(this);
        four_ll.performClick();

        arming_user_lv.setAdapter(new MyArmingUserAdapter(this));
    }

    @Override
    public void onClick(View v) {
        if(v==four_ll)
        {
          selectTab(1);
        }else if(v==eight_ll)
        {
            selectTab(2);
        }else if(v==twelve_ll)
        {
            selectTab(3);
        }
    }
    public void selectTab(int index) {
        if (index == 1) {
            four_tv.setTextColor(getResources().getColor(R.color.orange));
            tab4_ll.setVisibility(View.VISIBLE);
            eight_tv.setTextColor(getResources().getColor(R.color.font_black));
            tab8_ll.setVisibility(View.INVISIBLE);
            twelve_tv.setTextColor(getResources().getColor(R.color.font_black));
            tab12_ll.setVisibility(View.INVISIBLE);
        } else if (index == 2) {
            four_tv.setTextColor(getResources().getColor(R.color.font_black));
            tab4_ll.setVisibility(View.INVISIBLE);
            eight_tv.setTextColor(getResources().getColor(R.color.orange));
            tab8_ll.setVisibility(View.VISIBLE);
            twelve_tv.setTextColor(getResources().getColor(R.color.font_black));
            tab12_ll.setVisibility(View.INVISIBLE);
        } else if (index == 3) {
            four_tv.setTextColor(getResources().getColor(R.color.font_black));
            tab4_ll.setVisibility(View.INVISIBLE);
            eight_tv.setTextColor(getResources().getColor(R.color.font_black));
            tab8_ll.setVisibility(View.INVISIBLE);
            twelve_tv.setTextColor(getResources().getColor(R.color.orange));
            tab12_ll.setVisibility(View.VISIBLE);
        }
    }

    public void back(View view) {
        finish();
    }

    class MyArmingUserAdapter extends BaseAdapter
    {
        Context mContext;
        public MyArmingUserAdapter(Context context) {
            mContext=context;
        }

        @Override
        public int getCount() {
            return 3;
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
            if(convertView==null)
            {
                holder=new ViewHolder();
                convertView= LayoutInflater.from(mContext).inflate(R.layout.item_arming_user,null);
                convertView.setTag(holder);
            }else
            {
                holder=(ViewHolder)convertView.getTag();
            }
            return convertView;
        }
        class ViewHolder
        {

        }
    }
}
