package com.hlzx.wenutil.http;

import com.hlzx.wenutil.http.able.CancelAble;
import com.hlzx.wenutil.http.able.FinishAble;

import java.io.OutputStream;

/**
 * Created by alan on 2016/3/12.
 */
public interface Binary extends CancelAble,FinishAble {

    /**
     * Length of byteArray.
     *
     * @return Long length.
     */
    long getLength();

    /**
     * Write your Binary data through flow out.
     *
     * @param outputStream {@link OutputStream}.
     */
    void onWriteBinary(OutputStream outputStream);

    /**
     * Return the fileName, Can be null.
     *
     * @return File name.
     */
    String getFileName();

    /**
     * Return mimeType of binary.
     *
     * @return MimeType.
     */
    String getMimeType();

}
