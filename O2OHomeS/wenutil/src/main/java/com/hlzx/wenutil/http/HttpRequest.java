package com.hlzx.wenutil.http;

/**
 * Created by alan on 2016/3/14.
 */
public class HttpRequest<E> {

    public final int what;
    public final Request<E> request;
    public final OnResponseListener<E> responseListener;

    public HttpRequest(int what, Request<E> request, OnResponseListener<E> responseListener) {
        super();
        this.what = what;
        this.request = request;
        this.responseListener = responseListener;
    }
}
