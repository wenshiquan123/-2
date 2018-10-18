package com.hlzx.wenutil.http;

/**
 * Created by alan on 2016/3/14.
 */
public interface ImplRestParser {

    /**
     * Based on HTTP header analysis results, etc.
     *
     * @param request request.
     * @param <T>     T.
     * @return {@link Response}.
     */
    <T> Response<T> parserRequest(Request<T> request);
}
