package com.hlzx.wenutil.http;

import com.hlzx.wenutil.http.tools.HeaderParser;
import com.hlzx.wenutil.utils.Logger;

import java.io.UnsupportedEncodingException;

/**
 * Created by alan on 2016/3/14.
 */
public class StringRequest extends RestRequest<String>{

    public StringRequest(String url) {
        this(url, RequestMethod.GET);
    }

    public StringRequest(String url, RequestMethod requestMethod) {
        super(url, requestMethod);
    }

    @Override
    public String parseResponse(String url, Headers responseHeaders, byte[] responseBody) {
        return parseResponseString(url, responseHeaders, responseBody);
    }

    @Override
    public String getAccept() {
        return "text/html,application/xhtml+xml,application/xml;*/*;q=0.9";
    }

    public static final String parseResponseString(String url, Headers responseHeaders, byte[] responseBody) {
        String result = null;
        if (responseBody != null && responseBody.length > 0) {
            try {
                String charset = HeaderParser.parseHeadValue(responseHeaders.getContentType(), "charset", "");
                result = new String(responseBody, charset);
            } catch (UnsupportedEncodingException e) {
                Logger.w("Charset error in ContentType returned by the server：" + responseHeaders.getValue(Headers.HEAD_KEY_CONTENT_TYPE, 0));
                result = new String(responseBody);
            }
        }
        return result;
    }
}
