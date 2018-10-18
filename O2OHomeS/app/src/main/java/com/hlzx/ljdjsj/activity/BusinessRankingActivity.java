package com.hlzx.ljdjsj.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.view.View;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2015/12/10.
 */
public class BusinessRankingActivity extends BaseActivity {
    @ViewInject(R.id.title_tv)
    private TextView title_tv;

    //本周销售额
    @ViewInject(R.id.money_tv)
    private TextView money_tv;
    //名次
    @ViewInject(R.id.mingci_tv)
    private TextView mingci_tv;
    //城市排名
    @ViewInject(R.id.ranking_tv)
    private TextView ranking_tv;
    //更新时间
    @ViewInject(R.id.update_time_tv)
    private TextView update_time_tv;

    SpannableString msp = null;

    //城市
    private String city = "";
    //所在城市排名
    private int city_ranking = 0;
    //击败全国排名百分比
    private String country_ranking = "0%";
    //周营业额
    private String week_sale = "0";
    //更新时间
    private String updateTime="";

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_business_ranking);
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
        title_tv.setText("营业额排行榜");

        Intent it = getIntent();
        city = it.getStringExtra("city");
        city_ranking = it.getIntExtra("city_ranking", 0);
        country_ranking = it.getStringExtra("country_ranking");
        week_sale = it.getStringExtra("week_sale");
        updateTime=it.getStringExtra("update_time");

        money_tv.setText(week_sale);
        mingci_tv.setText("第" + city_ranking + "名");
        String format = getResources().getString(R.string.ranking2);
        String strTemp = String.format(format, city, country_ranking);
        ranking_tv.setText(strTemp);
        update_time_tv.setText(updateTime);

    }

    public void back(View view) {
        finish();
    }
}
