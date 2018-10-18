package com.hlzx.wenutil.http;

import android.app.Application;

import com.hlzx.wenutil.http.cache.Cache;
import com.hlzx.wenutil.http.cache.CacheEntity;
import com.hlzx.wenutil.http.cache.DiskCacheStore;
import com.hlzx.wenutil.http.tools.PRNGFixes;

import java.net.CookieHandler;
import java.net.CookieManager;

/**
 * Created by alan on 2016/3/12.
 */
public class NoHttp {

    /**
     * Default charset of request body, value is {@value}.
     */
    public static final String CHARSET_UTF8 = "utf-8";
    /**
     * Default mimeType of upload file, value is {@value}.
     */
    public static final String MIME_TYPE_FILE = "application/octet-stream";
    /**
     * Default timeout, value is {@value} ms.
     */
    public static final int TIMEOUT_8S = 8 * 1000;
    /**
     * RequestQueue default thread size, value is {@value}.
     */
    public static final int DEFAULT_THREAD_SIZE = 3;

    /**
     * Context.
     */
    private static Application sApplication;

    /**
     * Cookie.
     */
    private static CookieHandler sCookieHandler;


    /**
     * Initialization NoHttp, Should invoke on {@link Application#onCreate()}.
     *
     * @param application {@link Application}.
     */
    public static void init(Application application) {
        if (sApplication == null) {
            sApplication = application;
            PRNGFixes.apply();
            sCookieHandler=new CookieManager();

        }
    }

    /**
     * Get application of app.
     *
     * @return {@link Application}.
     */
    public static Application getContext() {
        if (sApplication == null)
            throw new ExceptionInInitializerError("Please invoke NoHttp.init(Application) on Application#onCreate()");
        return sApplication;
    }


    /**
     * Create a new request queue, using NoHttp default configuration. And number of concurrent requests is {@value #DEFAULT_THREAD_SIZE}.
     *
     * @return Returns the request queue, the queue is used to control the entry of the request.
     * @see #newRequestQueue(int)
     * @see #newRequestQueue(Cache, ImplRestConnection, int)
     * @see #newRequestQueue(ImplRestExecutor, int)
     * @see #newRequestQueue(ImplRestParser, int)
     */
    public static RequestQueue newRequestQueue() {
        return newRequestQueue(DEFAULT_THREAD_SIZE);
    }

    /**
     * Create a new request queue, using NoHttp default configuration.
     *
     * @param threadPoolSize request the number of concurrent.
     * @return Returns the request queue, the queue is used to control the entry of the request.
     * @see #newRequestQueue()
     * @see #newRequestQueue(Cache, ImplRestConnection, int)
     * @see #newRequestQueue(ImplRestExecutor, int)
     * @see #newRequestQueue(ImplRestParser, int)
     */
    public static RequestQueue newRequestQueue(int threadPoolSize) {
        return newRequestQueue(DiskCacheStore.INSTANCE, HttpRestConnection.getInstance(), threadPoolSize);
    }

    /**
     * Create a new request queue, using NoHttp default request executor {@link HttpRestExecutor} and default response parser {@link HttpRestParser}.
     *
     * @param cache              cache interface, which is used to cache the request results.
     * @param implRestConnection network operating interface, The implementation of the network layer.
     * @param threadPoolSize     request the number of concurrent.
     * @return Returns the request queue, the queue is used to control the entry of the request.
     * @see #newRequestQueue()
     * @see #newRequestQueue(int)
     * @see #newRequestQueue(ImplRestExecutor, int)
     * @see #newRequestQueue(ImplRestParser, int)
     */
    public static RequestQueue newRequestQueue(Cache<CacheEntity> cache, ImplRestConnection implRestConnection, int threadPoolSize) {
        return newRequestQueue(HttpRestExecutor.getInstance(cache, implRestConnection), threadPoolSize);
    }

    /**
     * Create a new request queue, using NoHttp default response parser {@link HttpRestParser}.
     *
     * @param implRestExecutor the executor, Interact with the network layer.
     * @param threadPoolSize   request the number of concurrent.
     * @return Returns the request queue, the queue is used to control the entry of the request.
     * @see #newRequestQueue()
     * @see #newRequestQueue(int)
     * @see #newRequestQueue(Cache, ImplRestConnection, int)
     * @see #newRequestQueue(ImplRestParser, int)
     */
    public static RequestQueue newRequestQueue(ImplRestExecutor implRestExecutor, int threadPoolSize) {
        return newRequestQueue(HttpRestParser.getInstance(implRestExecutor), threadPoolSize);
    }

    /**
     * Create a new request queue.
     *
     * @param implRestParser the response parser, The result of parsing the network layer.
     * @param threadPoolSize request the number of concurrent.
     * @return Returns the request queue, the queue is used to control the entry of the request.
     * @see #newRequestQueue()
     * @see #newRequestQueue(int)
     * @see #newRequestQueue(Cache, ImplRestConnection, int)
     * @see #newRequestQueue(ImplRestExecutor, int)
     */
    public static RequestQueue newRequestQueue(ImplRestParser implRestParser, int threadPoolSize) {
        RequestQueue requestQueue = new RequestQueue(implRestParser, threadPoolSize);
        requestQueue.start();
        return requestQueue;
    }

    /**
     * Create a String type request, the request method is {@link RequestMethod#GET}.
     *
     * @param url such as: {@code http://www.google.com}.
     * @return {@code Request<String>}.
     * @see #createStringRequest(String, RequestMethod)
     */
    public static Request<String> createStringRequest(String url) {
        return new StringRequest(url);
    }
    /**
     * Create a String type request, custom request method, method from {@link RequestMethod}.
     *
     * @param url           such as: {@code http://www.google.com}.
     * @param requestMethod {@link RequestMethod}.
     * @return {@code Request<String>}.
     * @see #createStringRequest(String)
     */
    public static Request<String> createStringRequest(String url, RequestMethod requestMethod) {
        return new StringRequest(url, requestMethod);
    }





    public static CookieHandler getDefaultCookieHandler() {
        return sCookieHandler;
    }

    /**
     * Sets the system-wide cookie handler.
     *
     * @param cookieHandler {@link CookieHandler}.
     * @see #getDefaultCookieHandler()
     */
    public static void setDefaultCookieHandler(CookieHandler cookieHandler) {
        if (cookieHandler == null)
            throw new IllegalArgumentException("CookieHandler == null");
        sCookieHandler = cookieHandler;
    }
}
