package com.hlzx.wenutil.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlzx.wenutil.R;
import com.hlzx.wenutil.dialog.interfaces.IOperation;

/**
 * Created by alan on 2016/3/15.
 * take photo like IOS;
 *
 * @author wenshiquan
 */
public class AlbumDialog extends Dialog implements View.OnClickListener {

    private Context mContext;
    private IOperation mOperation;

    private TextView takePhoto;
    private TextView getPhoto;
    private TextView txtCancel;


    public AlbumDialog(Context context, IOperation operation) {
        super(context, R.style.ActionSheet);
        this.mContext = context;
        this.mOperation = operation;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    public void init() {

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.layout_action_foot, null);
        final int cFullFillWidth = 10000;
        layout.setMinimumWidth(cFullFillWidth);

        takePhoto = (TextView) layout.findViewById(R.id.txt_take_photo);
        getPhoto = (TextView) layout.findViewById(R.id.txt_get_photo);
        txtCancel = (TextView) layout.findViewById(R.id.cancel);

        takePhoto.setOnClickListener(this);
        getPhoto.setOnClickListener(this);
        txtCancel.setOnClickListener(this);

        Window w = getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        final int cMakeBottom = -1000;
        lp.y = cMakeBottom;
        lp.gravity = Gravity.BOTTOM;
        onWindowAttributesChanged(lp);
        setCanceledOnTouchOutside(false);
        setContentView(layout);

    }

    @Override
    public void onClick(View v) {
        if (v == takePhoto) {
            mOperation.onTakePhoto();
            this.dismiss();
        } else if (v == getPhoto) {
            mOperation.onOpenAlbum();
            this.dismiss();

        } else if (v == txtCancel) {
            mOperation.onCanceled();
            this.dismiss();
        }
    }
}
