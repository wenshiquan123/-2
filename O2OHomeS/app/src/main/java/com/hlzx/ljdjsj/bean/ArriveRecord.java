package com.hlzx.ljdjsj.bean;

/**
 * Created by alan on 2015/12/12.
 */
public class ArriveRecord {

    boolean isExpand;//是否展开
    //日期
    String date;
    //今日营业额
    String sales;
    //今日订单数
    String order_count;

    //今日确认到账金额
    String account;
    //今日到账订单数
    String account_count;
    //订单奖励
    String reward;


    public boolean isExpand() {
        return isExpand;
    }

    public void setIsExpand(boolean isExpand) {
        this.isExpand = isExpand;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSales() {
        return sales;
    }

    public void setSales(String sales) {
        this.sales = sales;
    }

    public String getOrder_count() {
        return order_count;
    }

    public void setOrder_count(String order_count) {
        this.order_count = order_count;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getAccount_count() {
        return account_count;
    }

    public void setAccount_count(String account_count) {
        this.account_count = account_count;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }
}
