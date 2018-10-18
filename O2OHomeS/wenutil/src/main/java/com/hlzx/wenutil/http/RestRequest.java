package com.hlzx.wenutil.http;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by alan on 2016/3/12.
 */
public abstract class RestRequest<T> extends BasicRequest<T> {

    /**
     * Param collection.
     */
    protected Map<String, Object> mParamMap = null;

    /**
     * Create a request, RequestMethod is {@link RequestMethod#GET}.
     *
     * @param url request adress, like: http://www.google.com.
     */
    public RestRequest(String url) {
        this(url, RequestMethod.GET);
    }

    /**
     * Create a request
     *
     * @param url           request adress, like: http://www.google.com.
     * @param requestMethod request method, like {@link RequestMethod#GET}, {@link RequestMethod#POST}.
     */
    public RestRequest(String url, RequestMethod requestMethod) {
        super(url, requestMethod);
        this.mParamMap = new LinkedHashMap<String, Object>();
    }


    @Override
    public void add(String key, String value) {
        mParamMap.put(key, value == null ? "" : value);
    }

    @Override
    public void add(String key, int value) {
        mParamMap.put(key, Integer.toString(value));
    }

    @Override
    public void add(String key, long value) {
        mParamMap.put(key, Long.toString(value));
    }

    @Override
    public void add(String key, boolean value) {
        mParamMap.put(key, String.valueOf(value));
    }

    @Override
    public void add(String key, char value) {
        mParamMap.put(key, String.valueOf(value));
    }

    @Override
    public void add(String key, double value) {
        mParamMap.put(key, Double.toString(value));
    }

    @Override
    public void add(String key, float value) {
        mParamMap.put(key, Float.toString(value));
    }

    @Override
    public void add(String key, short value) {
        mParamMap.put(key, Integer.toString(value));
    }

    @Override
    public void add(String key, byte value) {
        mParamMap.put(key, Integer.toString(value));
    }

    @Override
    public void add(String key, Binary binary) {
        mParamMap.put(key, binary);
    }

    @Override
    public void add(Map<String, String> params) {
        if (params != null)
            this.mParamMap.putAll(params);
    }

    @Override
    public void set(Map<String, String> params) {
        if (params != null) {
            this.mParamMap.clear();
            this.mParamMap.putAll(params);
        }
    }

    @Override
    public Object remove(String key) {
        return mParamMap.remove(key);
    }

    @Override
    public void removeAll() {
        mParamMap.clear();
    }

    @Override
    public Set<String> keySet() {
        return mParamMap.keySet();
    }

    @Override
    public Object value(String key) {
        return mParamMap.get(key);
    }

}
