package com.hlzx.wenutil.http;

import com.hlzx.wenutil.http.able.FinishAble;
import com.hlzx.wenutil.http.able.QueueAble;
import com.hlzx.wenutil.http.able.SignCancelAble;
import com.hlzx.wenutil.http.able.StartAble;

import java.net.Proxy;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

/**
 * Created by alan on 2016/3/12.
 */
public interface ImplClientRequest extends QueueAble,FinishAble,SignCancelAble,StartAble{

    /**
     * Set proxy server.
     *
     * @param proxy Can use {@code Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("64.233.162.83", 80));}.
     */
    void setProxy(Proxy proxy);

    /**
     * If the server and {@link ImplServerRequest#needCache()} allow cache, will use this key as the only key to the.
     * cache request return data.
     *
     * @param key unique key.
     */
    void setCacheKey(String key);

    /**
     * Just read the cache, Is not enabled by default.
     *
     * @param onlyReadCache true: read, false, don't read.
     */
    void setOnlyReadCache(boolean onlyReadCache);

    /**
     * Read from the cache enabled are failing, is not enabled by default.
     *
     * @param isEnable true: enable ,false: close. isn't enable.
     */
    void setRequestFailedReadCache(boolean isEnable);

    /**
     * Sets the {@link SSLSocketFactory} for this request.
     *
     * @param socketFactory {@link SSLSocketFactory}.
     */
    void setSSLSocketFactory(SSLSocketFactory socketFactory);

    /**
     * Set the {@link HostnameVerifier}.
     *
     * @param hostnameVerifier {@link HostnameVerifier}.
     */
    void setHostnameVerifier(HostnameVerifier hostnameVerifier);

    /**
     * Sets the connection timeout time.
     *
     * @param connectTimeout timeout number, Unit is a millisecond.
     */
    void setConnectTimeout(int connectTimeout);

    /**
     * Sets the read timeout time.
     *
     * @param readTimeout timeout number, Unit is a millisecond.
     */
    void setReadTimeout(int readTimeout);

    /**
     * Sets redirect interface.
     *
     * @param redirectHandler {@link RedirectHandler}.
     */
    void setRedirectHandler(RedirectHandler redirectHandler);

    /**
     * If there is a key to delete, and then add a new key-value header.
     *
     * @param key   key.
     * @param value value.
     */
    void setHeader(String key, String value);

    /**
     * Add a new key-value header.
     *
     * @param key   key.
     * @param value value.
     */
    void addHeader(String key, String value);

    /**
     * Remove the key from the information.
     *
     * @param key key.
     */
    void removeHeader(String key);

    /**
     * Remove all header.
     */
    void removeAllHeader();

    /**
     * Settings you want to post data, if the requestBody isn't null, then other data
     * will not be sent.
     *
     * @param requestBody byte array.
     */
    void setRequestBody(byte[] requestBody);

    /**
     * @param requestBody byte array.
     * @see #setRequestBody(byte[])
     */
    void setRequestBody(String requestBody);

    /**
     * Set tag of task, At the end of the task is returned to you.
     *
     * @param tag {@link Object}.
     */
    void setTag(Object tag);


}
