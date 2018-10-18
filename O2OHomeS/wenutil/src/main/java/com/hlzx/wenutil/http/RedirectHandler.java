package com.hlzx.wenutil.http;

/**
 * Created by alan on 2016/3/12.
 */
public interface RedirectHandler {

    /**
     * When the server's response code is 302 or 303 corresponding need to redirect is invoked.
     *
     * @param responseHeaders The service side head accordingly.
     * @return {@link Request}.
     */
    Request<?> onRedirect(Headers responseHeaders);
}
