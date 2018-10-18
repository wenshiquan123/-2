package com.hlzx.ljdjsj.activity;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.common.ScreenManager;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

import java.util.ArrayList;

/**
 * Created by alan on 2015/12/10.
 */
public class EditMarketingGoodActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;
    @ViewInject(R.id.tab_rl)
    private RelativeLayout tab_rl;

    String[] allStr = {
            "取消特价", "下架", "删除"
    };
    MyPopAdapter adapterAll;
    ArrayList<String> allList = new ArrayList<String>();

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_edit_marketing_good);
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
        title_tv.setText("编辑商品");

        for (String str : allStr) {
            allList.add(str);
        }
        adapterAll = new MyPopAdapter(allList);


    }

    public void more(View view) {
        PopupWindow popupWindow = showPopAll(adapterAll, null,tab_rl,ScreenManager.getScreenWidth(this)-310,0);
    }

    public PopupWindow showPopAll(MyPopAdapter adapter, AbsListView.OnItemClickListener itemClickListener, View dropView, int offX, int offY) {
        View view = LayoutInflater.from(this).inflate(R.layout.pop_all, null);
        ListView listView = (ListView) view.findViewById(R.id.pop_lv);
        listView.setAdapter(adapter);
        if (itemClickListener != null)
            listView.setOnItemClickListener(itemClickListener);
        PopupWindow mPopAllCategory = new PopupWindow(view, 300, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        mPopAllCategory.setBackgroundDrawable(new BitmapDrawable());
        mPopAllCategory.setFocusable(true);
        mPopAllCategory.setOutsideTouchable(true);
        mPopAllCategory.showAsDropDown(dropView, offX, offY);
        mPopAllCategory.update();
        return mPopAllCategory;
    }

    public void back(View view) {
        finish();
    }


    /***
     * pop列表适配器
     */
    public class MyPopAdapter extends BaseAdapter {
        ArrayList<String> mData;

        public MyPopAdapter(ArrayList<String> data) {
            this.mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public Object getItem(int position) {
            return mData.get(position);
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
                convertView = LayoutInflater.from(EditMarketingGoodActivity.this).inflate(R.layout.item_pop, null);
                holder.cagetory_tv = (TextView) convertView.findViewById(R.id.category_tv);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.cagetory_tv.setText(mData.get(position));
            return convertView;
        }

        class ViewHolder {
            TextView cagetory_tv;
        }
    }

}
