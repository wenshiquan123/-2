package com.hlzx.ljdjsj.bean;

/**
 * Created by alan on 2016/1/26.
 * 评价
 */
public class Evaluate {

    //头像路径
    String picpath;
    //用户账号
    String username;
    //用户电话
    String user_phone;
    //日期
    String date;
    //配送速度
    int express_v;
    //服务态度
    int serve_attiude;
    //服务质量
    int serve_quality;
    //评论内容
    String content;


    public String getPicpath() {
        return picpath;
    }

    public void setPicpath(String picpath) {
        this.picpath = picpath;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUser_phone() {
        return user_phone;
    }

    public void setUser_phone(String user_phone) {
        this.user_phone = user_phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getExpress_v() {
        return express_v;
    }

    public void setExpress_v(int express_v) {
        this.express_v = express_v;
    }

    public int getServe_attiude() {
        return serve_attiude;
    }

    public void setServe_attiude(int serve_attiude) {
        this.serve_attiude = serve_attiude;
    }

    public int getServe_quality() {
        return serve_quality;
    }

    public void setServe_quality(int serve_quality) {
        this.serve_quality = serve_quality;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
