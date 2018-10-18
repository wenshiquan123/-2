package com.hlzx.wenutil.http.able;

/**
 * Created by alan on 2016/3/12.
 */
public interface CancelAble {

    /**
     * Change cancel state.
     *
     * @param cancel true or yes.
     */
    void cancel(boolean cancel);

    /**
     * Has it been canceled ?
     *
     * @return true: canceled, false: no cancellation.
     */
    boolean isCanceled();

    /**
     * Change the current cancel status as contrary to the current status.
     */
    void toggleCancel();
}
