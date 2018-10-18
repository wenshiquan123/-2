package com.hlzx.wenutil.http.able;

/**
 * Created by alan on 2016/3/12.
 */
public interface StartAble {
    /**
     * Change start state.
     *
     * @param start true: start, false: un start.
     */
    void start(boolean start);

    /**
     * Has it been started ?
     *
     * @return true: has already started, false: haven't started.
     */
    boolean isStarted();

    /**
     * Change the current start status as contrary to the current status.
     */
    void toggleStart();
}
