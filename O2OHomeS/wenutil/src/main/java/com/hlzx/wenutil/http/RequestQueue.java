package com.hlzx.wenutil.http;

import com.hlzx.wenutil.utils.Logger;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by alan on 2016/3/14.
 */
public class RequestQueue {

    /**
     * Save request task.
     */
    private final LinkedBlockingQueue<HttpRequest<?>> mRequestQueue=new LinkedBlockingQueue<HttpRequest<?>>();

    /**
     * HTTP request actuator(执行) interface.
     */
     private final ImplRestParser mImplRestParser;

    /**
     * Request queue polling thread array.
     */
    private RequestDispatcher[] mDispatchers;

    /**
     * Create request queue manager.
     *
     * @param implRestParser download the network task execution interface, where you need to implement the download tasks that have been implemented.
     * @param threadPoolSize number of thread pool.
     */
    public RequestQueue(ImplRestParser implRestParser, int threadPoolSize) {
        mImplRestParser = implRestParser;
        mDispatchers = new RequestDispatcher[threadPoolSize];
    }

    /**
     * Start polling the request queue, a one of the implementation of the download task, if you have started to poll the download queue, then it will stop all the threads, to re create thread
     * execution.
     */
    public void start() {
        stop();
        for (int i = 0; i < mDispatchers.length; i++) {
            RequestDispatcher networkDispatcher = new RequestDispatcher(mRequestQueue, mImplRestParser);
            mDispatchers[i] = networkDispatcher;
            networkDispatcher.start();
        }
    }

    /**
     * Add a request task to download queue, waiting for execution, if there is no task in the queue or the number of tasks is less than the number of thread pool, will be executed immediately.
     *
     * @param what             the "what" will be the response is returned to you, so you can introduce multiple {@link Request} results in an A with what, please distinguish which is the result of the {@link Request}.
     * @param request          {@link Request}
     * @param responseListener {@link OnResponseListener}
     * @param <T>              {@link T}
     */
    public <T> void add(int what, Request<T> request, OnResponseListener<T> responseListener) {
        if (request.isQueue())
            Logger.w("This request has been in the queue");
        else {
            request.queue(true);
            request.start(false);
            request.cancel(false);
            request.finish(false);
            mRequestQueue.add(new HttpRequest<T>(what, request, responseListener));
        }
    }

    /**
     * Polling the queue will not be executed, and this will not be canceled.
     */
    public void stop() {
        for (int i = 0; i < mDispatchers.length; i++) {
            if (mDispatchers[i] != null) {
                mDispatchers[i].quit();
            }
        }
    }

    /**
     * All requests for the sign specified in the queue, if you are executing, will interrupt the task
     *
     * @param sign this sign will be the same as sign's Request, and if it is the same, then cancel the task.
     */
    public void cancelBySign(Object sign) {
        synchronized (mRequestQueue) {
            for (HttpRequest<?> request : mRequestQueue)
                request.request.cancelBySign(sign);
        }
    }

    /**
     * Cancel all requests, Already in the execution of the request can't use this method
     */
    public void cancelAll() {
        synchronized (mRequestQueue) {
            for (HttpRequest<?> request : mRequestQueue)
                request.request.cancel(true);
        }
    }

}
