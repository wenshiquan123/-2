package com.hlzx.ljdjsj.bean;

import com.alibaba.fastjson.JSONArray;

/**
 * Created by alan on 2015/12/11.
 * 订单信息
 */
public class OrderInfo {

    //订单id
    int order_id;
    //订单编号
    String order_code;
    //联系人
    String link_man;
    //手机号码
    String link_phone;
    //送货地址
    String address;
    //订单总额
    String total_money;
    //确认时间
    String come_time;
    //备注内容
    String remark;
    //创建时间
    String ctime;
    //订单状态 整数
    int order_status;
    //订单状态 字符串
    String order_status_str;
    //付款方式
    int pay_type;
    //付款状态
    int pay_status;
    //付款时间
    String pay_time;
    //商品详情(图片路径)
    JSONArray order_detail;


    public int getOrder_id() {
        return order_id;
    }

    public void setOrder_id(int order_id) {
        this.order_id = order_id;
    }

    public String getOrder_code() {
        return order_code;
    }

    public void setOrder_code(String order_code) {
        this.order_code = order_code;
    }

    public String getLink_man() {
        return link_man;
    }

    public void setLink_man(String link_man) {
        this.link_man = link_man;
    }

    public String getLink_phone() {
        return link_phone;
    }

    public void setLink_phone(String link_phone) {
        this.link_phone = link_phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTotal_money() {
        return total_money;
    }

    public void setTotal_money(String total_money) {
        this.total_money = total_money;
    }

    public String getCome_time() {
        return come_time;
    }

    public void setCome_time(String come_time) {
        this.come_time = come_time;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public int getOrder_status() {
        return order_status;
    }

    public void setOrder_status(int order_status) {
        this.order_status = order_status;
    }

    public String getOrder_status_str() {
        return order_status_str;
    }

    public void setOrder_status_str(String order_status_str) {
        this.order_status_str = order_status_str;
    }

    public int getPay_type() {
        return pay_type;
    }

    public void setPay_type(int pay_type) {
        this.pay_type = pay_type;
    }

    public int getPay_status() {
        return pay_status;
    }

    public void setPay_status(int pay_status) {
        this.pay_status = pay_status;
    }

    public String getPay_time() {
        return pay_time;
    }

    public void setPay_time(String pay_time) {
        this.pay_time = pay_time;
    }

    public JSONArray getOrder_detail() {
        return order_detail;
    }

    public void setOrder_detail(JSONArray order_detail) {
        this.order_detail = order_detail;
    }

}
