package com.hlzx.wenutil.http;

/**
 * Created by alan on 2016/3/14.
 */
public interface OnResponseListener<T> {


    /**
     * When the request starts.
     *
     * @param what the credit of the incoming request is used to distinguish between multiple requests.
     */
    void onStart(int what);

    /**
     * Server correct response to callback when an HTTP request.
     *
     * @param what     the credit of the incoming request is used to distinguish between multiple requests.
     * @param response in response to the results.
     */
    void onSucceed(int what, Response<T> response);

    /**
     * When there was an error correction.
     *
     * @param what          the credit of the incoming request is used to distinguish between multiple requests.
     * @param url           url.
     * @param tag           tag of request callback.
     * @param exception     error message for request.
     * @param responseCode  server response code.
     * @param networkMillis request process consumption time.
     */
    void onFailed(int what, String url, Object tag, Exception exception, int responseCode, long networkMillis);

    /**
     * When the request finish.
     *
     * @param what the credit of the incoming request is used to distinguish between multiple requests.
     */
    void onFinish(int what);

}
