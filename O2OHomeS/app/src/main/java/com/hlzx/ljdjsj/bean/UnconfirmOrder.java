package com.hlzx.ljdjsj.bean;

/**
 * Created by alan on 2016/1/10.
 */
public class UnconfirmOrder {

    //订单号
    String order_code;
    //订单金额
    String total_money;
    //状态：1-处理中、2-已完成
    String order_status;
    //操作时间
    String ctime;
    //更新数据
    String update_time;
    //订单状态中文显示
    String order_status_str;
    //判断是否到账
    //is_account
    int is_account;

    public String getOrder_code() {
        return order_code;
    }

    public void setOrder_code(String order_code) {
        this.order_code = order_code;
    }

    public String getTotal_money() {
        return total_money;
    }

    public void setTotal_money(String total_money) {
        this.total_money = total_money;
    }

    public String getOrder_status() {
        return order_status;
    }

    public void setOrder_status(String order_status) {
        this.order_status = order_status;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getUpdate_time() {
        return update_time;
    }

    public void setUpdate_time(String update_time) {
        this.update_time = update_time;
    }

    public String getOrder_status_str() {
        return order_status_str;
    }

    public void setOrder_status_str(String order_status_str) {
        this.order_status_str = order_status_str;
    }

    public int getIs_account() {
        return is_account;
    }

    public void setIs_account(int is_account) {
        this.is_account = is_account;
    }
}
