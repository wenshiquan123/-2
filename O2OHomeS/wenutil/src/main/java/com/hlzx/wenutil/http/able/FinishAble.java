package com.hlzx.wenutil.http.able;

/**
 * Created by alan on 2016/3/12.
 */
public interface FinishAble {
    /**
     * Change finish state.
     *
     * @param finish true or false.
     */
    void finish(boolean finish);

    /**
     * Has it been finished ?
     *
     * @return true: finished, false: unfinished.
     */
    boolean isFinished();

    /**
     * Change the current completion status as contrary to the current status.
     */
    void toggleFinish();
}
