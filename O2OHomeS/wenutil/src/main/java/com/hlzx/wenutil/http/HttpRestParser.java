package com.hlzx.wenutil.http;

import android.os.SystemClock;

/**
 * Created by alan on 2016/3/15.
 */
public class HttpRestParser implements ImplRestParser {


    private static HttpRestParser _INSTANCE;

    private final ImplRestExecutor mImplRestExecutor;

    public static HttpRestParser getInstance(ImplRestExecutor implRestExecutor) {
        if (_INSTANCE == null)
            _INSTANCE = new HttpRestParser(implRestExecutor);
        return _INSTANCE;
    }

    private HttpRestParser(ImplRestExecutor mImplRestExecutor) {
        this.mImplRestExecutor = mImplRestExecutor;
    }


    @Override
    public <T> Response<T> parserRequest(Request<T> request) {
        long startTime = SystemClock.elapsedRealtime();
        HttpResponse httpResponse = mImplRestExecutor.executeRequest(request);
        String url = request.url();
        boolean isFromCache = httpResponse.isFromCache;
        Headers responseHeaders = httpResponse.responseHeaders;
        Exception exception = httpResponse.exception;
        byte[] responseBody = httpResponse.responseBody;
        if (exception == null) {
            T result = request.parseResponse(url, responseHeaders, responseBody);
            return new RestResponse<T>(url, request.getRequestMethod(), isFromCache, responseHeaders, responseBody, request.getTag(), result, SystemClock.elapsedRealtime() - startTime, exception);
        }
        return new RestResponse<T>(url, request.getRequestMethod(), isFromCache, responseHeaders, responseBody, request.getTag(), null, SystemClock.elapsedRealtime() - startTime, exception);
    }
}
