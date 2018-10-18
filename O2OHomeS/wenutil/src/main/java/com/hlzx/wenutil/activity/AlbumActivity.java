package com.hlzx.wenutil.activity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import com.hlzx.wenutil.dialog.AlbumDialog;
import com.hlzx.wenutil.dialog.interfaces.IOperation;
import com.hlzx.wenutil.utils.FileUtils;

import java.io.File;
import java.net.URI;

/**
 * Created by alan on 2016/3/15.
 * @author wenshiquan
 */
public abstract class AlbumActivity extends Activity implements IOperation{

   private AlbumDialog mDialog=null;

    /**
     * photo file name.
     */
    private String mPhotoFileName;

    /*
     *photo file
     */
    private File mPhotoFile;

    /**
     * request code
     */
    private final static int CAMERE_REQUESTCODE = 1001;
    private final static int PIC_REQUESTCODE = 1002;
    private final static int CROP_PICTURE = 1003;

    /**
     * photo uri
     */
    private Uri uri;

    public AlbumActivity()
    {}

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();
    }

    private void init()
    {
          mDialog=new AlbumDialog(this,this);
    }

    public void showDialog()
    {
        mDialog.show();
    }

    @Override
    public void onCanceled() {
       if(mDialog!=null)
       {
           mDialog.dismiss();
       }
    }

    @Override
    public void onOpenAlbum() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
        } else {
            intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        }
        startActivityForResult(intent, PIC_REQUESTCODE);
    }

    @Override
    public void onTakePhoto() {
        //whether or not SDCard is exist.
        if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
        {
            Toast.makeText(this,"SDCard is not exist",Toast.LENGTH_SHORT).show();
            return;
        }
        mPhotoFileName = "wen_" + System.currentTimeMillis() + ".png";
        mPhotoFile = FileUtils.getOrCreateFile(mPhotoFileName);

        startActivityForResult(new Intent(
                        MediaStore.ACTION_IMAGE_CAPTURE).putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mPhotoFile)),
                      CAMERE_REQUESTCODE);
    }


    //用系统自带程序裁剪图片
    public void startPhotoZoom(Uri uri) {
        /*
         * 至于下面这个Intent的ACTION是怎么知道的，大家可以看下自己路径下的如下网页
		 * yourself_sdk_path/docs/reference/android/content/Intent.html
		 * 直接在里面Ctrl+F搜：CROP ，之前没仔细看过，其实安卓系统早已经有自带图片裁剪功能, 是直接调本地库的
		 */
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 下面这个crop=true是设置在开启的Intent中设置显示的VIEW可裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        //把“return-data”设置为了true然后在onActivityResult中通过
        // data.getParcelableExtra("data")来获取数据，不过这样的话dp这个变量的值就不能太大了，
        // 不然你的程序就挂了。这里也就是我遇到问题的地方了，在大多数高配手机上这样用是没有问题的，
        // 不过很多低配手机就有点hold不住了，直接就异常了，包括我们的国产神机米3也没能hold住，
        // 所以我建议大家不要通过return data 大数据，
        //小数据还是没有问题的，说以我们在剪切图片的时候就尽量使用Uri这个东东来帮助我们。
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);//设置为不返回数据
        intent.putExtra("return-data", false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        startActivityForResult(intent, CROP_PICTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERE_REQUESTCODE) {
                if (mPhotoFile != null && mPhotoFile.exists()) {
                    uri = Uri.fromFile(new File(mPhotoFile.getAbsolutePath()));
                    startPhotoZoom(uri);
                }
            } else if (requestCode == PIC_REQUESTCODE) {
                if (data != null) {
                    uri = data.getData();
                    startPhotoZoom(uri);
                }
            } else if (requestCode == CROP_PICTURE) {
                if (uri != null) {
                    //保存裁剪之后的图片
                    //saveCropImg(uri);
                }
            }
        }
    }
}
