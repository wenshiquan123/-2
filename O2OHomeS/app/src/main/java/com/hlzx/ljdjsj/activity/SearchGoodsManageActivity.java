package com.hlzx.ljdjsj.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

import com.hlzx.ljdjsj.BaseActivity;
import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.utils.LogUtil;
import com.hlzx.ljdjsj.view.ClearEditText;
import com.hlzx.ljdjsj.view.XCFlowLayout;
import com.lidroid.xutils.ViewUtils;
import com.lidroid.xutils.view.annotation.ViewInject;

/**
 * Created by alan on 2015/12/10.
 */
public class SearchGoodsManageActivity extends BaseActivity implements View.OnClickListener {

    @ViewInject(R.id.search_et)
    private ClearEditText search_et;

    @ViewInject(R.id.delete_bt)
    private Button delete_bt;
    private String mNames[] = {};

    @ViewInject(R.id.flowlayout)
    private XCFlowLayout mFlowLayout;
    SharedPreferences.Editor editor = null;

    @Override
    public void setLayout() {
        setContentView(R.layout.activity_search_goods);
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
        editor = getSharedPreferences("search_history", Context.MODE_PRIVATE).edit();
        search_et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    // 先隐藏键盘
                    ((InputMethodManager) search_et.getContext().getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(
                                    SearchGoodsManageActivity.this.getCurrentFocus().getWindowToken(),
                                    InputMethodManager.HIDE_NOT_ALWAYS);
                    search();
                    return true;
                }
                return false;
            }
        });

        mNames = getSharedPreferences("search_history", 0).getString("his", "").split(",");
        LogUtil.e("ME", "name=" + mNames[0]);
        if (!mNames[0].equals("")) {
            mFlowLayout = (XCFlowLayout) findViewById(R.id.flowlayout);
            ViewGroup.MarginLayoutParams lp = new ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            lp.leftMargin = 5;
            lp.rightMargin = 5;
            lp.topMargin = 5;
            lp.bottomMargin = 5;
            for (int i = 0; i < mNames.length; i++) {
                TextView view = new TextView(this);
                view.setText(mNames[i]);
                view.setTextColor(Color.WHITE);
                view.setOnClickListener(this);
                view.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape_flow_text_bg));
                mFlowLayout.addView(view, lp);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mFlowLayout.removeAllViews();
                initView();
            }
        });
    }

    public void delete(View view)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                clearHis();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v instanceof TextView) {
                String strLable = ((TextView) v).getText().toString();
                Intent intent = new Intent(this, GoodsManageSearchResultActivity.class);
                intent.putExtra("keyword", strLable);
                startActivity(intent);

        }
    }

    /**
     * 清除历史
     */
    private void clearHis() {
        StringBuilder sb = new StringBuilder();
        sb.append("");
        editor.putString("his", sb.toString()).commit();

        mFlowLayout.removeAllViews();

    }

    private void saveHis(String searchStr) {
        //存储搜索历史
        String historyStr = getSharedPreferences("search_history", 0).getString("his", "");
        String[] arrayHis = historyStr.split(",");
        //如果存在则不保存
        for (int i = 0; i < arrayHis.length; i++) {
            if (arrayHis[i].equals(searchStr)) {
                return;
            }
        }
        StringBuilder sb = new StringBuilder(historyStr);
        sb.append(searchStr + ",");
        LogUtil.e("ME", "保存=" + historyStr + ";" + sb.toString());
        editor.putString("his", sb.toString()).commit();

    }

    public void search() {

        String searchStr = search_et.getText().toString().trim();
        if (TextUtils.isEmpty(searchStr)) {
            showToast("搜索不能为空！");
            return;
        }
        saveHis(searchStr);
        Intent intent = new Intent(this, GoodsManageSearchResultActivity.class);
        intent.putExtra("keyword", searchStr);
        startActivity(intent);
    }

    public void back(View view) {
        finish();
    }
}
