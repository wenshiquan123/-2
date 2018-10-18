package com.hlzx.ljdjsj.bean;

import com.hlzx.ljdjsj.utils.UrlsConstant;

import java.io.Serializable;

/**
 * Created by alan on 2016/1/5.
 * 商品bean
 */
public class Goods implements Serializable{
    //商品库商品ID
    String goods_depot_id;
    //商品名称
    String name;
    //商品图片
    String picpath;
    //商品规格
    String format;
    //商品一级分类名称
    String category_name1;
    //商品二级分类名称
    String category_name2;
    //是否已经添加到商品中,0不存在，1已经存在
    int is_exist;
    //商品ID
    int goods_id;
    //商品特价
    String initial_price;

    //商品进价
    String dealer_price;

    //商品库存
    int inventory;
    //商品售价
    String price;
    //分类id
    int goods_category1_id;
    int goods_category2_id;
    //热卖商品，0-全部，1-只显示热卖商品，2-非热卖，默认为0
    int hot;
    //推荐商品，0-全部，1-只显示推荐，2-非推荐，默认为0
    int recommend;
    //状态：0-全部，1-在架上，2-已下架，3-已售罄
    int status;

    int sales_num;


    public String getGoods_depot_id() {
        return goods_depot_id;
    }

    public void setGoods_depot_id(String goods_depot_id) {
        this.goods_depot_id = goods_depot_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicpath() {
        return picpath;
    }

    public void setPicpath(String picpath) {
        this.picpath = UrlsConstant.BASE_PIC_URL+picpath;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getCategory_name1() {
        return category_name1;
    }

    public void setCategory_name1(String category_name1) {
        this.category_name1 = category_name1;
    }

    public String getCategory_name2() {
        return category_name2;
    }

    public void setCategory_name2(String category_name2) {
        this.category_name2 = category_name2;
    }

    public int getIs_exist() {
        return is_exist;
    }

    public void setIs_exist(int is_exist) {
        this.is_exist = is_exist;
    }

    public int getGoods_id() {
        return goods_id;
    }

    public void setGoods_id(int goods_id) {
        this.goods_id = goods_id;
    }

    public String getInitial_price() {
        return initial_price;
    }

    public void setInitial_price(String initial_price) {
        this.initial_price = initial_price;
    }

    public String getDealer_price() {
        return dealer_price;
    }

    public void setDealer_price(String dealer_price) {
        this.dealer_price = dealer_price;
    }

    public int getInventory() {
        return inventory;
    }

    public void setInventory(int inventory) {
        this.inventory = inventory;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getGoods_category1_id() {
        return goods_category1_id;
    }

    public void setGoods_category1_id(int goods_category_id) {
        this.goods_category1_id = goods_category_id;
    }

    public int getGoods_category2_id() {
        return goods_category2_id;
    }

    public void setGoods_category2_id(int goods_category2_id) {
        this.goods_category2_id = goods_category2_id;
    }

    public int getHot() {
        return hot;
    }

    public void setHot(int hot) {
        this.hot = hot;
    }

    public int getRecommend() {
        return recommend;
    }

    public void setRecommend(int recommend) {
        this.recommend = recommend;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSales_num() {
        return sales_num;
    }

    public void setSales_num(int sales_num) {
        this.sales_num = sales_num;
    }
}
