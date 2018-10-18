package com.hlzx.wenutil.http;

import java.util.Map;

/**
 * Created by alan on 2016/3/12.
 */
public interface Request<T> extends ImplClientRequest,ImplServerRequest{

    /**
     * Add {@link CharSequence} param.
     *
     * @param key   param name.
     * @param value param value.
     */
    void add(String key, String value);

    /**
     * Add {@link Integer} param.
     *
     * @param key   param name.
     * @param value param value.
     */
    void add(String key, int value);

    /**
     * Add {@link Long} param.
     *
     * @param key   param name.
     * @param value param value.
     */
    void add(String key, long value);

    /**
     * Add {@link Boolean} param.
     *
     * @param key   param name.
     * @param value param value.
     */
    void add(String key, boolean value);

    /**
     * Add {@link char} param.
     *
     * @param key   param name.
     * @param value param value.
     */
    void add(String key, char value);

    /**
     * Add {@link Double} param.
     *
     * @param key   param name.
     * @param value param value.
     */
    void add(String key, double value);

    /**
     * Add {@link Float} param.
     *
     * @param key   param name.
     * @param value param value.
     */
    void add(String key, float value);

    /**
     * Add {@link Short} param.
     *
     * @param key   param name.
     * @param value param value.
     */
    void add(String key, short value);

    /**
     * Add {@link Byte} param.
     *
     * @param key   param name.
     * @param value param value 0 x01, for example, the result is 1.
     */
    void add(String key, byte value);

    /**
     * Add {@link java.io.File} param; NoHttp already has a default implementation: {@link FileBinary}.
     *
     * @param key    param name.
     * @param binary param value.
     */
    void add(String key, Binary binary);

    /**
     * add all param.
     *
     * @param params params map.
     */
    void add(Map<String, String> params);

    /**
     * set all param.
     *
     * @param params params map.
     */
    void set(Map<String, String> params);

    /**
     * Remove a request param by key.
     *
     * @param key key
     * @return The object is removed, if there are no returns null.
     */
    Object remove(String key);

    /**
     * Remove all request param.
     */
    void removeAll();

    /**
     * Parse response.
     *
     * @param url             url.
     * @param responseHeaders response {@link Headers} of server.
     * @param responseBody    response data of server.
     * @return {@link T}: your response result.
     */
    T parseResponse(String url, Headers responseHeaders, byte[] responseBody);
}
