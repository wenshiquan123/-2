package com.hlzx.ljdjsj.bean;

import com.hlzx.ljdjsj.utils.UrlsConstant;

/**
 * Created by alan on 2015/12/16.
 * 商品详情
 */
public class OrderDetail {

    //商品ID
    int goods_id;
    //商品名称
    String goods_name;
    //商品单价
    String goods_price;
    //商品图片路径
    String goods_path;
    //购买数量
    int purchase_num;
    //商品规格
    String format;

    public int getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(int goods_id) {
        this.goods_id = goods_id;
    }

    public String getGoods_name() {
        return goods_name;
    }

    public void setGoods_name(String goods_name) {
        this.goods_name = goods_name;
    }

    public String getGoods_price() {
        return goods_price;
    }

    public void setGoods_price(String goods_price) {
        this.goods_price = goods_price;
    }

    public String getGoods_path() {
        return goods_path;
    }

    public void setGoods_path(String goods_path) {
        this.goods_path = UrlsConstant.BASE_PIC_URL+goods_path;
    }

    public int getPurchase_num() {
        return purchase_num;
    }

    public void setPurchase_num(int purchase_num) {
        this.purchase_num = purchase_num;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
