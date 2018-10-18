package com.hlzx.wenutil.utils;

import android.graphics.Bitmap;


import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.util.Hashtable;

/**
 * Created by alan on 2016/2/25.
 * make the QRCode
 * by wenshiquan
 */
public class MakeQRCodeUtil {

    /**
     * 根据指定内容生成自定义宽高的二维码图片
     *
     * @param content 需要生成二维码的内容
     * @param width   二维码宽度
     * @param height  二维码高度
     * @throws WriterException 生成二维码异常
     */
   public static Bitmap makeQRImage(String content, int width, int height)
            throws WriterException {

        Hashtable<EncodeHintType,String> hints=new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET,"utf-8");
        //图像数据转换，使用了矩阵转换
        BitMatrix bitMatrix=new QRCodeWriter().encode(content,
                BarcodeFormat.QR_CODE,width,height,hints);
        int[] pixels=new int[width*height];
        // 按照二维码的算法，逐个生成二维码的图片，两个for循环是图片横列扫描的结果
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitMatrix.get(x, y))//范围内为黑色的
                    pixels[y * width + x] = 0xffff0000;
                else                  //其他的地方为白色
                    pixels[y * width + x] = 0xffffffff;
            }
        }
        // 生成二维码图片的格式，使用ARGB_8888
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);

        //设置像素矩阵的范围
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
