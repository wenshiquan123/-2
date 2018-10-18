package com.hlzx.wenutil.http;

import com.hlzx.wenutil.http.error.ClientError;
import com.hlzx.wenutil.http.error.NetworkError;
import com.hlzx.wenutil.http.error.ServerError;
import com.hlzx.wenutil.http.error.TimeoutError;
import com.hlzx.wenutil.http.error.URLError;
import com.hlzx.wenutil.http.error.UnKnownHostError;
import com.hlzx.wenutil.http.tools.HeaderParser;
import com.hlzx.wenutil.http.tools.NetUtil;
import com.hlzx.wenutil.utils.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

/**
 * Created by alan on 2016/3/14.
 */
public class HttpRestConnection extends BasicConnection implements ImplRestConnection {


    private static HttpRestConnection instance;

    public static HttpRestConnection getInstance() {
        if (instance == null) {
            instance = new HttpRestConnection();
        }
        return instance;
    }

    private HttpRestConnection() {
    }

    @Override
    public HttpResponse requestNetwork(ImplServerRequest request) {

        if (request == null) {
            throw new IllegalArgumentException("request==null");
        }

        Logger.d("--------------Request start--------------");

        Headers responseHeaders = new HttpHeaders();
        byte[] responseBody = null;
        Exception exception = null;

        HttpURLConnection httpConnection = null;
        try {
            if(!NetUtil.isNetworkAvailable(NoHttp.getContext()))
            {
                throw new NetworkError("Network error");
            }

            httpConnection=getHttpConnection(request);
            Logger.d("-------Response start-------");
            int responseCode = httpConnection.getResponseCode();
            responseHeaders=parseResponseHeaders(new URI(request.url()),
                    responseCode, httpConnection.getResponseMessage(), httpConnection.getHeaderFields());

            //handle body
            if(hasResponseBody(request.getRequestMethod(),responseCode))
            {
                InputStream inputStream=null;
                try {
                   inputStream=httpConnection.getInputStream();
                    if(HeaderParser.isGzipContent(responseHeaders.getContentEncoding()))
                    {
                        inputStream=new GZIPInputStream(inputStream);
                    }
                    responseBody=readResponseBody(inputStream);
                }catch (IOException e)
                {
                    if(responseCode >=500)
                        throw  new ServerError("Internal Server Error:"+e.getMessage());
                    else if (responseCode >= 400)
                        throw new ClientError("Internal Client Error: " + e.getMessage());
                }finally {
                    if (inputStream != null)
                        inputStream.close();
                }
            }


        } catch (MalformedURLException e) {
            Logger.e(e);
            exception = new URLError(e.getMessage());
        } catch (UnknownHostException e) {
            Logger.e(e);
            exception = new UnKnownHostError(e.getMessage());
        } catch (SocketTimeoutException e) {
            Logger.e(e);
            exception = new TimeoutError(e.getMessage());
        } catch (Exception e) {
            Logger.e(e);
            exception = e;
        } finally {
            if (httpConnection != null)
                httpConnection.disconnect();
            Logger.d("-------Response end-------");
        }
        Logger.d("--------------Request finish--------------");

        return new HttpResponse(false, responseHeaders, responseBody, exception);
    }
}
