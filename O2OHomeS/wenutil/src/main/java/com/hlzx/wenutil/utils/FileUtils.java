package com.hlzx.wenutil.utils;

import android.os.Environment;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by alan on 2016/3/15.
 */
public class FileUtils {

    public final static String SD_PATH = Environment.getExternalStorageDirectory() + File.separator;
    /**
     * cache file
     */
    public final static String TOTAl = SD_PATH + "wen";


    /**
     * get SDCard directory.
     * @return
     */
    public static String getSDCardPath() {
        String sdCardPath = "";
        if (isSDcardExist()) {
            sdCardPath = TOTAl;
        }
        return sdCardPath;
    }

    public static boolean isSDcardExist()
    {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    /**
     * get file ,if it is not exist,create it.
     * @param path
     * @return
     */
    public static File getOrCreateFile(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }

        if (path.indexOf(getSDCardPath()) != 0) {
            path = getSDCardPath() + File.separator + path;
        }
        File file = new File(path);
        if (!file.exists()) {
            //create parent file.
            File parentFile = new File(file.getParent());
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }
            try {
                file.createNewFile();
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
        return file;
    }
}
