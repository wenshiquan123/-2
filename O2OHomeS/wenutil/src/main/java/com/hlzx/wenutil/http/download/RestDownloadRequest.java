package com.hlzx.wenutil.http.download;

import com.hlzx.wenutil.http.Headers;
import com.hlzx.wenutil.http.RequestMethod;
import com.hlzx.wenutil.http.RestRequest;

import java.io.File;

/**
 * Created by alan on 2016/3/16.
 */
public class RestDownloadRequest extends RestRequest<Void> implements DownloadRequest {
    /**
     * File the target folder.
     */
    private final String mFileDir;
    /**
     * The file target name.
     */
    private final String mFileName;
    /**
     * If is to download a file, whether the breakpoint continuing.
     */
    private final boolean isRange;
    /**
     * If there is a old files, whether to delete the old files.
     */
    private final boolean isDeleteOld;

    public RestDownloadRequest(String url, String fileFolder, String filename, boolean isRange, boolean isDeleteOld) {
        this(url, RequestMethod.GET, fileFolder, filename, isRange, isDeleteOld);
    }

    public RestDownloadRequest(String url, RequestMethod requestMethod, String fileFolder, String filename, boolean isRange, boolean isDeleteOld) {
        super(url, requestMethod);
        this.mFileDir = fileFolder;
        this.mFileName = filename;
        this.isRange = isRange;
        this.isDeleteOld = isDeleteOld;
    }

    @Override
    public String getFileDir() {
        return this.mFileDir;
    }

    @Override
    public String getFileName() {
        return this.mFileName;
    }

    @Override
    public boolean isRange() {
        return this.isRange;
    }

    @Override
    public boolean isDeleteOld() {
        return this.isDeleteOld;
    }

    @Override
    public int checkBeforeStatus() {
        if(this.isRange)
        {
            try {
                File lastFile=new File(mFileDir,mFileName);
                if(lastFile.exists() && !isDeleteOld)
                {
                    return STATUS_FINISH;
                }

                File tempFile=new File(mFileDir,mFileName+".nohttp");
                if(tempFile.exists())
                    return STATUS_RESUME;

            }catch (Exception e)
            {}
        }
        return STATUS_RESTART;
    }

    @Override
    public Void parseResponse(String url, Headers responseHeaders, byte[] responseBody) {
        return null;
    }

    @Override
    public String getAccept() {
        return "*/*";
    }
}
