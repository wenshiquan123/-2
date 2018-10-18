package com.hlzx.ljdjsj.bean;

/**
 * Created by alan on 2015/12/22.
 */
public class DepositRecord {

    //提现金额
    String balance_change;
    //操作时间
    String change_time;
    //操作状态
    int change_status;

    public String getBalance_change() {
        return balance_change;
    }

    public void setBalance_change(String balance_change) {
        this.balance_change = balance_change;
    }

    public String getChange_time() {
        return change_time;
    }

    public void setChange_time(String change_time) {
        this.change_time = change_time;
    }

    public int getChange_status() {
        return change_status;
    }

    public void setChange_status(int change_status) {
        this.change_status = change_status;
    }
}
