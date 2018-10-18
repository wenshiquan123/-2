package com.hlzx.wenutil.http.download;

import android.text.TextUtils;
import android.util.Log;

import com.hlzx.wenutil.http.BasicConnection;
import com.hlzx.wenutil.http.Headers;
import com.hlzx.wenutil.http.HttpHeaders;
import com.hlzx.wenutil.http.NoHttp;
import com.hlzx.wenutil.http.error.ArgumentError;
import com.hlzx.wenutil.http.error.ClientError;
import com.hlzx.wenutil.http.error.NetworkError;
import com.hlzx.wenutil.http.error.ServerError;
import com.hlzx.wenutil.http.error.StorageReadWriteError;
import com.hlzx.wenutil.http.error.StorageSpaceNotEnoughError;
import com.hlzx.wenutil.http.error.TimeoutError;
import com.hlzx.wenutil.http.error.URLError;
import com.hlzx.wenutil.http.error.UnKnownHostError;
import com.hlzx.wenutil.http.tools.HeaderParser;
import com.hlzx.wenutil.http.tools.NetUtil;
import com.hlzx.wenutil.utils.Logger;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

/**
 * Created by alan on 2016/3/16.
 */
public class DownloadConnection extends BasicConnection implements Downloader {

    public DownloadConnection() {
    }

    @Override
    public void download(int what, DownloadRequest downloadRequest, DownloadListener downloadListener) {
        HttpURLConnection httpConnection = null;
        InputStream inputStream = null;
        if (downloadRequest == null) {
            throw new IllegalArgumentException("downloadRequest==null");
        }
        if (downloadListener == null) {
            throw new IllegalArgumentException("downloadListener==null");
        }

        RandomAccessFile randomAccessFile = null;
        String savePathDir = downloadRequest.getFileDir();
        try {
            if (TextUtils.isEmpty(savePathDir) || TextUtils.isEmpty(downloadRequest.getFileName())) {
                throw new ArgumentError("Destination folder creation failed, please check folder parameters and storage devices");
            }
            if (!NetUtil.isNetworkAvailable(NoHttp.getContext())) {
                throw new NetworkError("Network is not available");
            }

            if (!FileUtil.createFolder(savePathDir)) {
                throw new StorageReadWriteError("Failed to create the folder " + savePathDir + ", please check storage devices");
            }

            File tempFile = new File(savePathDir, downloadRequest.getFileName() + ".nohttp");
            //根据临时文件处理断点头
            long rangeSize = 0L;//断点开始处
            if (tempFile.exists()) {
                if (downloadRequest.isRange()) {
                    rangeSize = tempFile.length();
                    //例如：从1024开始下载：Range:bytes=1024-
                    downloadRequest.setHeader("Range", "bytes=" + rangeSize + "-");
                } else {
                    tempFile.delete();
                }
            }

            //连接服务器，处理响应头
            httpConnection = getHttpConnection(downloadRequest);
            Logger.e("-----------Response Start----------");
            int responseCode = httpConnection.getResponseCode();
            Headers httpHeaders = parseResponseHeaders(new URI(downloadRequest.url()),
                    responseCode, httpConnection.getResponseMessage(), httpConnection.getHeaderFields());

            long contentLength = 0;
            //文件总大小
            if (responseCode == 206) {
                // Content-Range: bytes [文件块的开始字节]-[文件的总大小 - 1]/[文件的总大小]
                String range = httpHeaders.getValue(Headers.HEAD_KEY_CONTENT_RANGE, 0);// 事例：Content-Range:bytes 1024-2047/2048
                try {
                    contentLength = Long.parseLong(range.substring(range.indexOf('/') + 1));// 截取'/'之后的总大小
                } catch (Exception e) {
                    throw new ServerError("ResponseCode is 206, but content-Range error in Server HTTP header information: " + range);
                }
            } else if (responseCode == 200) {
                contentLength = httpHeaders.getContentLength();//直接下载
                rangeSize = 0;
            }

            File lastFile = new File(savePathDir, downloadRequest.getFileName());
            if (lastFile.exists()) {
                if (downloadRequest.isDeleteOld())
                    lastFile.delete();
                else if ((responseCode == 200 || responseCode == 206) &&
                        (lastFile.length() == contentLength || contentLength == 0)) {
                    downloadListener.onStart(what, true, lastFile.length(), new HttpHeaders(), lastFile.length());
                    downloadListener.onProgress(what, 100, lastFile.length());
                    Logger.d("-----------Download finish-------");
                    downloadListener.onFinish(what, lastFile.getAbsolutePath());
                    return;
                } else
                    lastFile.delete();

            }

            // 生成临时文件
            if (responseCode == 200 && !FileUtil.createNewFile(tempFile))
                throw new StorageReadWriteError("Failed to create the file, please check storage devices");

            if (FileUtil.getDirSize(savePathDir) < contentLength)
                throw new StorageSpaceNotEnoughError("The folder is not enough space to save the downloaded file: " + savePathDir);

            if (downloadRequest.isCanceled()) {
                Log.i("NoHttpDownloader", "Download request is canceled");
                downloadListener.onCancel(what);
                return;
            }

            try {
                inputStream = httpConnection.getInputStream();
            } catch (IOException e) {
                if (responseCode >= 500)
                    throw new ServerError(e.getMessage());
                else if (responseCode <= 400)
                    throw new ClientError(e.getMessage());
            }

            //解压文件流
            if (HeaderParser.isGzipContent(httpHeaders.getContentEncoding()))
                inputStream = new GZIPInputStream(inputStream);

            //通知开始下载
            Logger.d("-------download start--------");
            downloadListener.onStart(what, rangeSize > 0, rangeSize, httpHeaders, contentLength);

            randomAccessFile = new RandomAccessFile(tempFile, "rw");
            randomAccessFile.seek(rangeSize);

            byte[] buffer = new byte[1024];
            int len;

            int oldProgress = 0;// 旧的进度记录，防止重复通知
            long count = rangeSize;// 追加目前已经下载的进度

            while (((len = inputStream.read(buffer)) != -1)) {
                if (downloadRequest.isCanceled()) {
                    Log.i("NoHttpDownloader", "Download request is canceled");
                    downloadListener.onCancel(what);
                    break;
                } else {
                    randomAccessFile.write(buffer, 0, len);
                    count += len;
                    if (contentLength != 0) {
                        int progress = (int) (count * 100 / contentLength);
                        if ((0 == progress % 2 || 0 == progress % 3 || 0 == progress % 5 || 0 == progress % 7) && oldProgress != progress) {
                            oldProgress = progress;
                            downloadListener.onProgress(what, oldProgress, count);// 进度通知
                        }
                    }
                }
            }

            if (!downloadRequest.isCanceled() && (tempFile.length() == contentLength || contentLength == 0)) {
                tempFile.renameTo(lastFile);
                Logger.d("-------Download finish-------");
                downloadListener.onFinish(what, lastFile.getAbsolutePath());
            }

        } catch (MalformedURLException e) {
            Logger.e(e);
            downloadListener.onDownloadError(what, new URLError(e.getMessage()));
        } catch (UnknownHostException e) {
            Logger.e(e);
            downloadListener.onDownloadError(what, new UnKnownHostError(e.getMessage()));
        } catch (SocketTimeoutException e) {
            Logger.e(e);
            downloadListener.onDownloadError(what, new TimeoutError(e.getMessage()));
        } catch (SocketException e) {
            if (NetUtil.isNetworkAvailable(NoHttp.getContext()))
                downloadListener.onDownloadError(what, e);
            else {
                String message = "The network is not available ";
                Logger.e(e, message);
                downloadListener.onDownloadError(what, new NetworkError(message));
            }
        } catch (IOException e) {
            if (!FileUtil.canWrite(savePathDir))
                downloadListener.onDownloadError(what, new StorageReadWriteError("This folder cannot be written to the file: " + savePathDir));
            else if (FileUtil.getDirSize(savePathDir) < 1024)
                downloadListener.onDownloadError(what, new StorageSpaceNotEnoughError("The folder is not enough space to save the downloaded file: " + savePathDir));
            else
                downloadListener.onDownloadError(what, e);
        } catch (Exception e) {// NetworkError | ClientError | ServerError | StorageCantWriteError | StorageSpaceNotEnoughError
            Logger.e(e);
            downloadListener.onDownloadError(what, e);
        } finally {
            Logger.i("----------Response End----------");
            if (randomAccessFile != null)
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                }
            try {
                if (inputStream != null)
                    inputStream.close();
            } catch (IOException e) {
            }
            if (httpConnection != null)
                httpConnection.disconnect();
        }

    }
}
