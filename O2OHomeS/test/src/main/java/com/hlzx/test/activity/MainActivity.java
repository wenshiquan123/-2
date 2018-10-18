package com.hlzx.test.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.hlzx.test.R;
import com.hlzx.wenutil.ui.PullZoomListView;
import com.hlzx.wenutil.ui.TwoWayProgressView;
import com.hlzx.wenutil.utils.MakeQRCodeUtil;


/**
 * Created by alan on 2016/2/15.
 */
public class MainActivity extends Activity {

    public static final String[] datas = {"好友1", "好友2", "好友3", "好友4", "好友1", "好友2",
            "好友3", "好友4", "好友1", "好友2", "好友3", "好友4",
            "好友1", "好友2", "好友3", "好友4", "好友1", "好友2", "好友3", "好友4"};
    //private HBListView mListView;
    private PullZoomListView mListview;

    private TwoWayProgressView myView;

    private ImageView myImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*mListView = (HBListView) findViewById(R.id.lv);
        myView = (TwoWayProgressView) findViewById(R.id.my);

        final View header = LayoutInflater.from(this).inflate(R.layout.view_header, null);
        mListView.addHeaderView(header);
        final MyImageView image = (MyImageView) header.findViewById(R.id.imageView);
        header.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                mListView.changeSize(image);
                header.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        mListView.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_1, datas));
        mListView.setOnSuccessListener(new HBListView.OnSuccessListener() {
            @Override
            public void onSuccess() {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("恭喜中奖！抽到了疼逊聊天气泡！").setNegativeButton("确认", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).show();
            }

            @Override
            public void onScroll(float y) {
                myView.setPregress(y);
            }
        });*/


        /*mListview = (PullZoomListView) findViewById(R.id.listview);
        String[] adapterData = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "21"};
        mListview.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, adapterData));
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        int mScreenHeight = localDisplayMetrics.heightPixels;
        int mScreenWidth = localDisplayMetrics.widthPixels;
        AbsListView.LayoutParams localObject = new AbsListView.LayoutParams(mScreenWidth, (int) (9.0F * (mScreenWidth / 16.0F)));
        mListview.setHeaderLayoutParams(localObject);*/

        /*myImage=(ImageView)findViewById(R.id.my_image);
        try {
            Bitmap bmp = MakeQRCodeUtil.makeQRImage("http://www.baidu.com", 300, 300);
            myImage.setImageBitmap(bmp);
        }catch (Exception e)
        {
            e.printStackTrace();
        }*/

    }

    public void click(View view) {

        Intent intent=new Intent(this,SecondActivity.class);
        startActivity(intent);

    }
}
