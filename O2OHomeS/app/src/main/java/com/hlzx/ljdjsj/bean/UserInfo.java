package com.hlzx.ljdjsj.bean;

import com.hlzx.ljdjsj.utils.UrlsConstant;

import java.io.Serializable;

/**
 * Created by alan on 2015/12/14.
 * 用户信息
 */
public class UserInfo implements Serializable {

    private static UserInfo instance;

    public static UserInfo getInstance() {
        if (instance == null) {
            instance = new UserInfo();
        }
        return instance;
    }

        //令牌
        String token = "";
        //登录名
        String username;
        //店铺地址
        String address;
        //状态
        String status;
        //建店时间
        String ctime;
        //上一次登录
        String last_login_ip;
        //最后登录时间
        String last_login_time;
        //营业开始时间
        String startTime;
        //打烊时间
        String endTime;
        //暂停营业说明(停业说明)
        String audit_summary;
        //开业时间
        String audit_time;
        //用户公告
        String notice;
        //运费设置开关：0-关闭、1-开启
        String fare_off;
        //运费
        String fare_point;
        //满*元免运费
        String fare_price;
        //店铺logo
        String logo;
        //联系人
        String contacts;
        //联系电话
        String telphone;
        //店铺名称
        String shop_name;

        //库存锁状态：0-关闭、1-开启
        int repertory_lock;
        //库存锁密码(不为空表示已经设置过，重新设置的时候需要验证旧的密码)
        String repertory_pw;

        //手机号码，用于找回密码跟设置库存锁
        String phone;
        //用于推送
        String seller_id;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCtime() {
        return ctime;
    }

    public void setCtime(String ctime) {
        this.ctime = ctime;
    }

    public String getLast_login_ip() {
        return last_login_ip;
    }

    public void setLast_login_ip(String last_login_ip) {
        this.last_login_ip = last_login_ip;
    }

    public String getLast_login_time() {
        return last_login_time;
    }

    public void setLast_login_time(String last_login_time) {
        this.last_login_time = last_login_time;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getAudit_summary() {
        return audit_summary;
    }

    public void setAudit_summary(String audit_summary) {
        this.audit_summary = audit_summary;
    }

    public String getAudit_time() {
        return audit_time;
    }

    public void setAudit_time(String audit_time) {
        this.audit_time = audit_time;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }

    public String getFare_off() {
        return fare_off;
    }

    public void setFare_off(String fare_off) {
        this.fare_off = fare_off;
    }

    public String getFare_point() {
        return fare_point;
    }

    public void setFare_point(String fare_point) {
        this.fare_point = fare_point;
    }

    public String getFare_price() {
        return fare_price;
    }

    public void setFare_price(String fare_price) {
        this.fare_price = fare_price;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getContacts() {
        return contacts;
    }

    public void setContacts(String contacts) {
        this.contacts = contacts;
    }

    public String getTelphone() {
        return telphone;
    }

    public void setTelphone(String telphone) {
        this.telphone = telphone;
    }

    public int getRepertory_lock() {
        return repertory_lock;
    }

    public void setRepertory_lock(int repertory_lock) {
        this.repertory_lock = repertory_lock;
    }

    public String getRepertory_pw() {
        return repertory_pw;
    }

    public void setRepertory_pw(String repertory_pw) {
        this.repertory_pw = repertory_pw;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getShop_name() {
        return shop_name;
    }

    public void setShop_name(String shop_name) {
        this.shop_name = shop_name;
    }

    public String getSeller_id() {
        return seller_id;
    }

    public void setSeller_id(String seller_id) {
        this.seller_id = seller_id;
    }
}
