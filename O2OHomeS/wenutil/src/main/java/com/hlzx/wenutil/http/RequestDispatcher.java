package com.hlzx.wenutil.http;

import android.os.*;
import android.os.Process;

import com.hlzx.wenutil.utils.Logger;

import java.util.concurrent.BlockingQueue;

/**
 * Created by alan on 2016/3/14.
 */
public class RequestDispatcher extends Thread {

    /**
     * Gets the lock for Handler to prevent the request result from confusing.
     */
    private static final Object HANDLER_LOCK=new Object();

    /**
     * Poster of send request result.
     */
    private static Handler sRequestHandler;

    /**
     * Request queue.
     */
    private final BlockingQueue<HttpRequest<?>> mRequestQueue;

    /**
     * HTTP request parse interface.
     *
     */
    private final ImplRestParser mImplRestParser;

    /**
     * Whether the current request queue polling thread is out of.
     */
    private volatile boolean mRunning=true;

    /**
     * Create a request queue polling thread.
     *
     * @param requestQueue   request queue.
     * @param implRestParser network request task actuator.
     */
    public RequestDispatcher(BlockingQueue<HttpRequest<?>> requestQueue, ImplRestParser implRestParser) {
        mRequestQueue = requestQueue;
        mImplRestParser = implRestParser;
    }

    /**
     * Exit polling thread.
     */
    public void quit()
    {
        mRunning=false;
        interrupt();
    }

    /**
     * Dispatcher is running.
     * @return the status.
     */
    public boolean isRunning()
    {
        return mRunning;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);//标准后台线程
        while (mRunning)
        {
            final HttpRequest<?> request;
            try {
                request=mRequestQueue.take();//取走队列的第一个对象
            }catch (InterruptedException e)
            {
                if(!mRunning)
                    return;
                continue;
            }

            if(request.request.isCanceled())
            {
                Logger.d(request.request.url() + "is canceled");
                continue;
            }

            request.request.start(true);
            //start
            final ThreadPoster startThread=new ThreadPoster(request.what,request.responseListener);
            startThread.onStart();
            getPosterHandler().post(startThread);

            //request
            Response<?> response=mImplRestParser.parserRequest(request.request);

            //finish
            final ThreadPoster finishThread=new ThreadPoster(request.what,request.responseListener);
            finishThread.onFinished();
            getPosterHandler().post(finishThread);
            request.request.finish(true);
            request.request.queue(false);

            //response
            if(request.request.isCanceled())
            {
                Logger.d(request.request.url()+"finish,but it's cancaled");

            }else
            {
                final ThreadPoster responseThread=new ThreadPoster(request.what,request.responseListener);
                responseThread.onResponse(response);
                getPosterHandler().post(responseThread);
            }
        }

    }

    private Handler getPosterHandler()
    {
        synchronized (HANDLER_LOCK)
        {
            if(sRequestHandler==null)
            {
                sRequestHandler=new Handler(Looper.getMainLooper());
            }
        }
        return sRequestHandler;
    }

    private class ThreadPoster implements Runnable
    {
        public static final int COMMAND_START=0;
        public static final int COMMAND_RESPONSE=1;
        public static final int COMMAND_FINISH=2;

        private final int what;
        private final OnResponseListener responseListener;

        private int command;
        private Response response;

        public ThreadPoster(int what, OnResponseListener responseListener) {
            this.what = what;
            this.responseListener = responseListener;
        }

        public void onStart()
        {
            this.command=COMMAND_START;
        }

        public void onResponse(Response response)
        {
            this.command=COMMAND_RESPONSE;
            this.response=response;
        }
        public void onFinished()
        {
            this.command=COMMAND_FINISH;
        }


        @Override
        public void run() {
           if(responseListener!=null)
           {
               if(command==COMMAND_START)
               {
                    responseListener.onStart(what);

               }else if(command==COMMAND_FINISH) {
                    responseListener.onFinish(what);
               }else if(command==COMMAND_RESPONSE)
               {
                   if(response==null)
                   {
                        responseListener.onFailed(what,null,null,null,0,0);
                   }else
                   {
                       if(response.isSucceed())
                       {
                           responseListener.onSucceed(what,response);
                       }else
                       {
                           Headers headers=response.getHeaders();
                           responseListener.onFailed(what, response.url(), response.getTag(), response.getException(), headers == null ? -1 : headers.getResponseCode(), response.getNetworkMillis());
                       }
                   }
               }

           }
        }
    }
}
