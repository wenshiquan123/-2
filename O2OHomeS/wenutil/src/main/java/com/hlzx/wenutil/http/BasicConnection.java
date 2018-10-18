package com.hlzx.wenutil.http;

import android.os.Build;
import android.text.TextUtils;

import com.hlzx.wenutil.http.tools.Writer;
import com.hlzx.wenutil.utils.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by alan on 2016/3/14.
 */
public class BasicConnection {

    protected HttpURLConnection getHttpConnection(ImplServerRequest request) throws IOException
    {
        //1.pre operation notice
        request.onPreExecute();

        //2.Build URL
        String urlStr=request.url();
        Logger.i("Request address :" + urlStr);
        URL url=new URL(urlStr);
        HttpURLConnection connection;
        Proxy proxy=request.getProxy();
        if(proxy==null)
        {
            connection=(HttpURLConnection)url.openConnection();
        }else
        {
            connection=(HttpURLConnection)url.openConnection(proxy);
        }

        if(connection instanceof HttpURLConnection)
        {
            SSLSocketFactory sslSocketFactory=request.getSSLSocketFactory();
            if(sslSocketFactory!=null)
            {
                ((HttpsURLConnection)connection).setSSLSocketFactory(sslSocketFactory);
            }
            HostnameVerifier hostnameVerifier=request.getHostnameVerifier();
            if(hostnameVerifier!=null)
            {
                ((HttpsURLConnection)connection).setHostnameVerifier(hostnameVerifier);
            }
        }

        //3. Base attribute
        String requestMethod=request.getRequestMethod().toString();
        Logger.i("Request method:" + requestMethod);
        connection.setRequestMethod(requestMethod);
        connection.setDoInput(true);
        connection.setDoOutput(request.doOutPut());
        connection.setConnectTimeout(request.getConnectTimeout());
        connection.setReadTimeout(request.getReadTimeout());
        connection.setInstanceFollowRedirects(false);//是否连接遵循重定向

        //4.Set request headers
        URI uri=null;
        try {
            uri=url.toURI();
        }catch (URISyntaxException e){}



        return connection;

    }

    private void setHeaders(URI uri,HttpURLConnection connection,ImplServerRequest request)
    {
        //1.Build headers
        Headers headers=request.headers();
        //2.Base header
        //2.1 Accept-*
        String accept=request.getAccept();
        if(!TextUtils.isEmpty(accept))
        {
            headers.set(Headers.HEAD_KEY_ACCEPT,accept);
        }
        headers.set(Headers.HEAD_KEY_ACCEPT_ENCODING,Headers.HEAD_VALUE_ACCEPT_ENCODING);

        String acceptCharset=request.getAcceptCharset();
        if(!TextUtils.isEmpty(acceptCharset))
        {
            headers.set(headers.HEAD_KEY_ACCEPT_CHARSET,acceptCharset);
        }

        String acceptLanguage=request.getAcceptLanguage();
        if(!TextUtils.isEmpty(acceptLanguage))
        {
            headers.set(Headers.HEAD_KEY_ACCEPT_LANGUAGE,acceptLanguage);
        }

        //2.2 connction
        //To fix bug: accidental EOFException before API 19

        if(Build.VERSION.SDK_INT>Build.VERSION_CODES.KITKAT)
        {
            headers.set(Headers.HEAD_KEY_CONNECTION,Headers.HEAD_KEY_CONNECTION);
        }else
        {
            headers.set(Headers.HEAD_KEY_CONNECTION,Headers.HEAD_VALUE_CONNECTION_CLOSE);
        }

        //2.3 Content-*
        if(request.doOutPut())
        {
            long contentLength=request.getContentLength();
            if(contentLength<Integer.MAX_VALUE && contentLength>0)
            {
                connection.setFixedLengthStreamingMode((int)contentLength);
            }else if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT)
            {
                connection.setFixedLengthStreamingMode(contentLength);
            }else
            {
                connection.setFixedLengthStreamingMode(256*1024);
            }
            headers.set(Headers.HEAD_KEY_CONTENT_LENGTH,Long.toString(contentLength));
        }

        String contentType=request.getContentType();
        if(!TextUtils.isEmpty(contentType))
        {
            headers.set(Headers.HEAD_KEY_CONTENT_TYPE,contentType);
        }

        //2.4 Cookie
        if(uri!=null)
        {
             headers.addCookie(uri,NoHttp.getDefaultCookieHandler());
        }

        //3. UserAgent
        headers.set(Headers.HEAD_KEY_USER_AGENT,request.getUserAgent());

        Map<String,String> requestHeaders= headers.toRequestHeaders();

        //4. Adds all request header to httpConnection
        for(Map.Entry<String,String> headerEntry:requestHeaders.entrySet())
        {
            String headKey = headerEntry.getKey();
            String headValue = headerEntry.getValue();
            Logger.i(headKey + ": " + headValue);
            connection.setRequestProperty(headKey, headValue);
        }

    }

    protected Headers parseResponseHeaders(URI uri,int responseCode,String responseMessage,
                                           Map<String, List<String>> responseHeaders)
    {
        //handler cookie
        try {
          NoHttp.getDefaultCookieHandler().put(uri,responseHeaders);
        }catch (IOException e){
            Logger.e(e, "Save cookie filed: " + uri.toString());
        }

        // handle headers
        Headers headers = new HttpHeaders();
        headers.set(responseHeaders);
        headers.set(Headers.HEAD_KEY_RESPONSE_MESSAGE, responseMessage);
        headers.set(Headers.HEAD_KEY_RESPONSE_CODE, Integer.toString(responseCode));
        // print
        for (String headKey : headers.keySet()) {
            List<String> headValues = headers.getValues(headKey);
            for (String headValue : headValues) {
                StringBuilder builder = new StringBuilder();
                if (!TextUtils.isEmpty(headKey))
                    builder.append(headKey).append(": ");
                if (!TextUtils.isEmpty(headValue))
                    builder.append(headValue);
                Logger.i(builder.toString());
            }
        }
        return headers;
    }


    /* ====================Wirte request body==================== */

    /**
     * Send the request body to the server.
     *
     * @param connection {@link HttpURLConnection}.
     * @param request    {@link ImplServerRequest}.
     * @throws IOException To send data when possible.
     */
    private void writeRequestBody(HttpURLConnection connection, ImplServerRequest request) throws IOException {
        if (request.doOutPut()) {
            Logger.i("-------Send request data start-------");
            BufferedOutputStream outputStream = new BufferedOutputStream(connection.getOutputStream());
            Writer writer = new Writer(outputStream, true);
            request.onWriteRequestBody(writer);
            outputStream.flush();
            outputStream.close();
            Logger.i("-------Send request data end-------");
        }
    }

	/* ====================Read response body=================== */

    /**
     * To read information from the server's response.
     *
     * @param inputStream outputStream from the service, for us is the inputStream, the data read from the inputStream.
     * @return Data from server.
     * @throws IOException To read data when possible.
     */
    protected byte[] readResponseBody(InputStream inputStream) throws IOException {
        int readBytes;
        byte[] buffer = new byte[1024];
        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
        ByteArrayOutputStream content = new ByteArrayOutputStream();
        while ((readBytes = bufferedInputStream.read(buffer)) != -1)
            content.write(buffer, 0, readBytes);
        content.flush();
        content.close();
        return content.toByteArray();
    }

    /**
     * This requestMethod and responseCode has ResponseBody ?
     *
     * @param requestMethod it's come from {@link RequestMethod}.
     * @param responseCode  responseCode from server.
     * @return True: there is data, false: no data.
     */
    public static boolean hasResponseBody(RequestMethod requestMethod, int responseCode) {
        return requestMethod != RequestMethod.HEAD && hasResponseBody(responseCode);
    }

    /**
     * According to the response code to judge whether there is data.
     *
     * @param responseCode responseCode.
     * @return True: there is data, false: no data.
     */
    public static boolean hasResponseBody(int responseCode) {
        return !(100 <= responseCode && responseCode < 200) && responseCode != 204 && responseCode != 205 && responseCode != 304;
    }
}
