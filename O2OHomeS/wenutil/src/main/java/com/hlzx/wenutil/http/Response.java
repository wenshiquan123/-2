package com.hlzx.wenutil.http;

import java.net.HttpCookie;
import java.util.List;

/**
 * Created by alan on 2016/3/12.
 */
public interface Response<T> {

    /**
     * Get the requested url.
     *
     * @return URL.
     */
    String url();

    /**
     * RequestMethod.
     *
     * @return {@link RequestMethod}.
     */
    RequestMethod getRequestMethod();

    /**
     * Ask for success.
     *
     * @return True: Succeed, false: failed.
     */
    boolean isSucceed();

    /**
     * Whether from the cache.
     *
     * @return True: the data from cache, false: the data from server.
     */
    boolean isFromCache();

    /**
     * Get http response headers.
     *
     * @return {@link Headers}.
     */
    Headers getHeaders();

    /**
     * Get http response Cookie.
     *
     * @return {@code List<HttpCookie>}.
     */
    List<HttpCookie> getCookies();

    /**
     * Get raw data.
     *
     * @return {@code byte[]}.
     */
    byte[] getByteArray();

    /**
     * Get request results.
     *
     * @return {@link T}.
     */
    T get();

    /**
     * Get Error Message.
     *
     * @return The exception.
     */
    Exception getException();

    /**
     * Gets the tag of request.
     *
     * @return {@link Object}.
     */
    Object getTag();

    /**
     * Gets the millisecond of request.
     *
     * @return {@link Long}.
     */
    long getNetworkMillis();
}
