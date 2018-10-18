package com.hlzx.wenutil.ui.pulltorefresh;

/**
 * Created by alan on 2016/3/17.
 */
public enum State {

    RESET(0x01),

    PULL_TO_REFRESH(0x02),

    RELEASE_TO_REFRESH(0x03),

    REFRESHING(0x04)

    ;
    private int mIntValue;
    State(int intValue)
    {
       mIntValue=intValue;
    }

    int getValue()
    {
        return mIntValue;
    }
}
