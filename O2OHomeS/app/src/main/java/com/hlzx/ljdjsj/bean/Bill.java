package com.hlzx.ljdjsj.bean;

/**
 * Created by alan on 2015/12/18.
 */
public class Bill {

    //余额
    String after_money;
    //本次发生金额
    String change_money;
    //发生时间
    String change_time;
    //改变的类型名称
    String change_type_name_tv;

    public String getAfter_money() {
        return after_money;
    }

    public void setAfter_money(String after_money) {
        this.after_money = after_money;
    }

    public String getChange_money() {
        return change_money;
    }

    public void setChange_money(String change_money) {
        this.change_money = change_money;
    }

    public String getChange_time() {
        return change_time;
    }

    public void setChange_time(String change_time) {
        this.change_time = change_time;
    }

    public String getChange_type_name_tv() {
        return change_type_name_tv;
    }

    public void setChange_type_name_tv(String change_type_name_tv) {
        this.change_type_name_tv = change_type_name_tv;
    }
}
