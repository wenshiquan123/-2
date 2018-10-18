package com.hlzx.ljdjsj.utils;

/**
 * Created by Administrator on 2015/6/15.
 *
 */
public interface HttpReturn {
    public void SuccessReturn(String result) throws Exception;
    public void FailReturn(String result);
}
