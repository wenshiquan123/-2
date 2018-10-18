package com.hlzx.test.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.hlzx.test.R;
import com.hlzx.wenutil.ui.PullRefreshForListView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 2016/3/10.
 */
public class MainActivityForPull extends Activity{

    private PullRefreshForListView refreshView;

    private ListView listView;

    private List<String> dataList;

    private ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_pull);

        refreshView = (PullRefreshForListView) findViewById(R.id.refresh_hit_block);

        listView = (ListView) findViewById(R.id.list_view);

        arrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_expandable_list_item_1, createDate());

        listView.setAdapter(arrayAdapter);
        refreshView
                .setOnRefreshListener(new PullRefreshForListView.RefreshListener() {
                    @Override
                    public void onRefreshing() {
                        try {
                            // 模拟网络请求耗时动作
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        mHandler.sendEmptyMessage(0);
                    }
                });
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            dataList.add("新增内容");
            arrayAdapter.notifyDataSetChanged();
            refreshView.finishRefreshing();
            Toast.makeText(MainActivityForPull.this, "刷新成功!", Toast.LENGTH_SHORT)
                    .show();
        }
    };

    private List<String> createDate() {
        dataList = new ArrayList<String>();
        dataList.add("Item1");
        dataList.add("Item2");
        dataList.add("Item3");
        dataList.add("Item4");
        dataList.add("Item5");
        return dataList;
    }
}
