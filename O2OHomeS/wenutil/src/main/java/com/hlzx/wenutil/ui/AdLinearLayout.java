package com.hlzx.wenutil.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.hlzx.wenutil.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alan on 2016/3/21.
 */
public class AdLinearLayout extends LinearLayout {


    private Context mContext;
    private RadioGroup mAdRg;
    private AdGallery mAdGallery;

    //广告数
    private int mAdNum;

    //广告
    private List<String> mAdUrls;
    private AdAdapter mAdAdapter;

    public AdLinearLayout(Context context) {
        this(context, null);
    }

    public AdLinearLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AdLinearLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Ad);
        mAdNum = a.getInteger(R.styleable.Ad_ad_num, 3);

        a.recycle();
        init();
    }

    private void init() {
        LayoutInflater.from(mContext).inflate(R.layout.layout_ad, this);
        mAdRg = (RadioGroup) findViewById(R.id.ad_rg);
        mAdGallery = (AdGallery) findViewById(R.id.ad_gallery);
        //initData();
    }

    public void start() {
        mAdGallery.start();
    }

    public void stop() {
        mAdGallery.stop();
    }

    /**
     * 初始化广告
     */
    private void initData() {

        if (mAdUrls!=null)
        {
           new NullPointerException("urls is null");
        }

        mAdUrls = new ArrayList<String>();
        mAdUrls.add("http://img.blog.csdn.net/20160318181428653");
        mAdUrls.add("http://cdn.duitang.com/uploads/item/201505/29/20150529200613_T2cKW.jpeg");
        mAdUrls.add("http://pic.to8to.com/attch/day_160218/20160218_d968438a2434b62ba59dH7q5KEzTS6OH.png");

        mAdAdapter = new AdAdapter();
        mAdGallery.setAdapter(mAdAdapter);
        mAdGallery.setOnItemSelectedListener(itemSelectedListener);
        addGuideIntor(mAdRg, mAdUrls.size());
    }

    //设置广告
    private void setUrls(ArrayList<String> urls) {
        this.mAdUrls = urls;
        initData();
    }

    /**
     * 设置小圆点显示，position会一直增加，如果要循环显示图片，需要对position取余，否则数组越界
     */
    private AdapterView.OnItemSelectedListener itemSelectedListener = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int position, long id) {
            // TODO Auto-generated method stub
            int pos = position % mAdUrls.size();
            mAdRg.getChildAt(pos).performClick();
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            // TODO Auto-generated method stub

        }
    };


    /**
     * 创建广告导航
     *
     * @param radioGroup
     * @param size       广告数
     */
    private void addGuideIntor(RadioGroup radioGroup, int size) {
        radioGroup.removeAllViews();
        RadioButton radioButton = null;
        for (int i = 0; i < size; i++) {
            radioButton = new RadioButton(mContext);
            radioButton.setId(i);
            radioButton.setButtonDrawable(android.R.color.transparent);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(12, 12);
            params.leftMargin = 2;
            radioButton.setLayoutParams(params);
            radioButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.circle_radiobutton));
            radioGroup.addView(radioButton);
            radioButton = null;
        }
    }


    class AdAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return Integer.MAX_VALUE;
        }

        @Override
        public Object getItem(int position) {

            if (position >= mAdUrls.size()) {
                position = position % mAdUrls.size();
            }
            return mAdUrls.get(position);
        }

        @Override
        public long getItemId(int position) {
            if (position >= mAdUrls.size()) {
                position = position % mAdUrls.size();
            }
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (mAdUrls != null && mAdUrls.size() != 0) {
                if (position >= mAdUrls.size()) {
                    position = position % mAdUrls.size();
                }
            }

            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.ad_item, null);
                holder.im = (ImageView) convertView.findViewById(R.id.playImage);
                convertView.setTag(convertView);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            Picasso.with(mContext).load(mAdUrls.get(position)).
                    placeholder(R.drawable.ic_launcher).error(R.drawable.ic_launcher).into(holder.im);

            return convertView;
        }
    }

    private static class ViewHolder {
        public ImageView im;
    }
}
