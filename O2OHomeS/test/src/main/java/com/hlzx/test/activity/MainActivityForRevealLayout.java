package com.hlzx.test.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.hlzx.test.R;
import com.hlzx.wenutil.activity.AlbumActivity;
import com.hlzx.wenutil.activity.ScanActivity;

/**
 * Created by alan on 2016/3/11.
 */
public class MainActivityForRevealLayout extends AlbumActivity{


    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_reveal);
        button=(Button)findViewById(R.id.button221);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivityForRevealLayout.this, ScanActivity.class));
            }
        });

    }



}
