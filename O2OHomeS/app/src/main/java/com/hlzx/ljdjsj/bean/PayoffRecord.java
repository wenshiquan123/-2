package com.hlzx.ljdjsj.bean;

/**
 * Created by alan on 2016/1/27.
 * 盈利记录model
 */
public class PayoffRecord {

    //日期
    String date;
    //订单收益
    String orderIncome;
    //订单进价
    String orderDealerPrice;
    //订单利润
    String orderProfit;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getOrderIncome() {
        return orderIncome;
    }

    public void setOrderIncome(String orderIncome) {
        this.orderIncome = orderIncome;
    }

    public String getOrderDealerPrice() {
        return orderDealerPrice;
    }

    public void setOrderDealerPrice(String orderDealerPrice) {
        this.orderDealerPrice = orderDealerPrice;
    }

    public String getOrderProfit() {
        return orderProfit;
    }

    public void setOrderProfit(String orderProfit) {
        this.orderProfit = orderProfit;
    }
}
