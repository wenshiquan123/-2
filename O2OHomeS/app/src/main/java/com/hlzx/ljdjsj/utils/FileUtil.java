package com.hlzx.ljdjsj.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by Administrator on 2015/9/17.
 */
public class FileUtil {
    public final static String SDPATH = Environment
            .getExternalStorageDirectory() + File.separator;

    /**
     * 缓存文件夹
     */
    public final static String TOTAl = SDPATH + "WXYS";
    /**
     * 获取当前应用保存文件的根目录
     *
     * @return
     */
    public static String getSaveFilesRootPath() {
        String rootPath = getSDCardPath();

        if (TextUtils.isEmpty(rootPath)) {
            return "";
        }
        rootPath += "/JYShop/";
        return rootPath;
    }

    /**
     * 获取SD卡目录
     *
     * @return
     */
    public static String getSDCardPath() {
        String sdCardPath = "";
        if (isSDcardExist()) {
            sdCardPath = TOTAl;
        }
        return sdCardPath;
    }

    /**
     * 判断SD卡是否存在
     *
     * @return
     */
    public static boolean isSDcardExist() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取文件,如果文件不存在,则尝试创建文件
     *
     * @param path
     * @return
     */
    public static File getOrCreateFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        if (path.indexOf(getSDCardPath()) != 0) {
            //如果path中开头没有包含SD卡目录,则添加SD卡目录
            path = getSDCardPath() + File.separator + path;
        }

        LogUtil.i("ME", "文件目录path:" + path);

        File file = new File(path);

        if (!file.exists()) {
            //文件不存在,创建文件,并创建文件上层目录
            File parentFile = new File(file.getParent());
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            try {
                file.createNewFile();

                LogUtil.e("ME", "创建文件成功,path:" + file.getAbsolutePath());
            } catch (Exception ex) {
                ex.printStackTrace();
                LogUtil.e("ME", "创建文件失败");
                return null;
            }

        }
        LogUtil.i("ME", "获取文件成功,path:" + file.getAbsolutePath());
        return file;
    }


}
