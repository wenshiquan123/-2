package com.hlzx.ljdjsj.common;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hlzx.ljdjsj.R;
import com.hlzx.ljdjsj.interfaces.DialogOnClickListener;

/**
 * Created by alan on 2015/12/11.
 */
public class PublicDialog{

    public static AlertDialog phoneDialog(Context context,String linkMsg,final DialogOnClickListener clickListener)
    {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.setCancelable(false);
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_phone);
        String[] msg=linkMsg.split(",");
        TextView phone_tv=(TextView)window.findViewById(R.id.phone_tv);
        phone_tv.setText(msg[0]);
        TextView name_tv=(TextView)window.findViewById(R.id.name_tv);
        name_tv.setText(msg[1]);
        LinearLayout cancel_ll = (LinearLayout) window.findViewById(R.id.cancel_ll);
        cancel_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onCancel();
            }
        });
        LinearLayout confirm_ll = (LinearLayout) window.findViewById(R.id.confirm_ll);
        confirm_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onConfirm();
            }
        });
        return dialog;
    }

    public static AlertDialog expressOrderDialog(Context context, final DialogOnClickListener clickListener)
    {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_add_express_order);
        LinearLayout cancel_ll = (LinearLayout) window.findViewById(R.id.cancel_ll);
        cancel_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onCancel();
            }
        });
        LinearLayout confirm_ll = (LinearLayout) window.findViewById(R.id.confirm_ll);
        confirm_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onConfirm();
            }
        });
        return dialog;
    }

    public static Dialog createLoadingDialog(Context context, String msg)
    {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.dialog_loading, null);// 得到加载view
        TextView tipTextView = (TextView) v.findViewById(R.id.msg_tv);// 提示文字
        tipTextView.setText(msg);// 设置加载信息
        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(false);// 不可以用“返回键”取消
        loadingDialog.setContentView(v, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
        return loadingDialog;
    }

    public interface OnActionSheetSelected {
        void onClick(int whichButton);
    }

    public static Dialog showSheet(Context context, final OnActionSheetSelected actionSheetSelected) {
        final Dialog dlg = new Dialog(context, R.style.ActionSheet);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.actionsheet, null);
        final int cFullFillWidth = 10000;
        layout.setMinimumWidth(cFullFillWidth);

        TextView takePhoto = (TextView) layout.findViewById(R.id.txt_take_photo);
        TextView getPhoto = (TextView) layout.findViewById(R.id.txt_get_photo);
        TextView txtCancel = (TextView) layout.findViewById(R.id.cancel);

        takePhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                actionSheetSelected.onClick(0);
                dlg.dismiss();
            }
        });

        getPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                actionSheetSelected.onClick(1);
                dlg.dismiss();
            }
        });

        txtCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                actionSheetSelected.onClick(2);
                dlg.dismiss();
            }
        });


        Window w = dlg.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        final int cMakeBottom = -1000;
        lp.y = cMakeBottom;
        lp.gravity = Gravity.BOTTOM;
        dlg.onWindowAttributesChanged(lp);
        dlg.setCanceledOnTouchOutside(false);

        dlg.setContentView(layout);
        return dlg;
    }


    //消息
    public static AlertDialog MessageDialog(Context context,String title,String msg)
    {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_message);

        TextView title_tv=(TextView)window.findViewById(R.id.title_tv);
        title_tv.setText(title);
        TextView msg_tv=(TextView)window.findViewById(R.id.msg_tv);
        msg_tv.setText(msg);
        LinearLayout cancel_ll = (LinearLayout) window.findViewById(R.id.cancel_ll);
        cancel_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        return dialog;
    }

    public static AlertDialog warningDialog(Context context,String msg,final DialogOnClickListener clickListener)
    {
        final AlertDialog dialog = new AlertDialog.Builder(context).create();
        dialog.show();
        Window window = dialog.getWindow();
        window.setContentView(R.layout.dialog_warning);
        TextView msg_tv=(TextView)window.findViewById(R.id.msg_tv);
        msg_tv.setText(msg);
        LinearLayout cancel_ll = (LinearLayout) window.findViewById(R.id.cancel_ll);
        cancel_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onCancel();
                dialog.dismiss();
            }
        });
        LinearLayout confirm_ll = (LinearLayout) window.findViewById(R.id.confirm_ll);
        confirm_ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onConfirm();
                dialog.dismiss();
            }
        });
        return dialog;
    }

}
