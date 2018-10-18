package com.hlzx.wenutil.http.able;

/**
 * Created by alan on 2016/3/12.
 */
public interface SignCancelAble extends CancelAble{
    /**
     * Cancel operation by contrast the sign.
     *
     * @param sign an object that can be null.
     */
    void cancelBySign(Object sign);

    /**
     * Set cancel sign.
     *
     * @param object save a object.
     */
    void setCancelSign(Object object);
}
