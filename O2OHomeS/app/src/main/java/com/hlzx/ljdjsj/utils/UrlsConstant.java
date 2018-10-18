package com.hlzx.ljdjsj.utils;

/**
 * Created by Administrator on 2015/6/8.
 * 接口常量类
 */
public class UrlsConstant {
    /**
     * 测试地址
     */
    //public static final String BASE_URL = "http://sellerapitest.lijidaojia.com/";//测试地址
    public static final String BASE_URL = "http://sellerapi.lijidaojia.com/";//正式服务器
    public static final String BASE_PIC_URL="http://cdn.lijidaojia.com";//图片基地址
    //握手接口
    public static final String SHAKE_HAND = BASE_URL + "ShakeHands";
    /**
     * 登录接口
     */
    public static final String LOGIN = BASE_URL + "Login";
    /*
     *获取验证码
     */
    public static final String GET_CODE = BASE_URL + "login/phoneVerifyCode";

    /*
     *重置密码
     */
    public static final String PASSWORD_RESET = BASE_URL + "login/modifyPasswd";

    /*
     *注销登录接口
     */
    public static final String LOGOUT = BASE_URL + "logout";

    /**
     * 订单接口
     */
    public static final String GET_ORDER = BASE_URL + "order";

    /*
     * 订单详情
     */
    public static final String ORDER_DETAIL = BASE_URL + "order/detail";

    /**
     * 订单处理接口
     */
    public static final String ORDER_ACTION = BASE_URL + "order/action";

    /**
     * 收入
     */
    public static final String INCOME = BASE_URL + "income";
    /*
    账单
    */
    public static final String INCOME_BILL = BASE_URL + "income/bills";

    /*
     *到账记录
     */
    public static final String INCOME_ACCOUNT = BASE_URL + "income/accounts";
    /*
     *提现规则
     */
    public static final String INCOME_DRAW_RULE = BASE_URL + "income/withdrawrule";

    /*
     *提现记录
     */
    public static final String INCOME_WITH_DRAW_LIST = BASE_URL + "income/withdrawlist";

    /**
     * 提现申请
     */
    public static final String INCOME_DEPOSIT=BASE_URL+"income/withdraw";//提现


    /**
     *获取店铺库存锁验证码
     */
    public static final String SHOP_VERIFY_CODE = BASE_URL + "shop/phoneVerifyCode";
    /*
     *店铺操作接口
     */
    public static final String SHOP_REPERTORY_LOCK = BASE_URL + "shop/repertorylock";
    /**
     * 暂停营业
     */
    public static final String SHOP_STOP_WORK= BASE_URL + "shop/audit";
    /**
     *公告设置
     */
    public static final String SHOP_NOTICE= BASE_URL + "shop/notice";

    /**
     * 邮费设置
     */
    public static final String SHOP_POSTAGE= BASE_URL + "shop/fare";
    /**
     * 意见反馈
     */
    public static final String ABOUT_OPINION= BASE_URL + "about/opinion";
    /**
     * 上传头像
     */
    public static final String SHOP_UPDATE_LOGO=BASE_URL + "shop/logo";
    /**
     * 我的意见
     */
    public static final String ABOUT_OPINION_LIST=BASE_URL + "about/opinionlist";

    /**
     * 营业时间
     */
    public static final String SHOP_OPENING=BASE_URL + "shop/opening";
    /**
     * 商品信息发布规则
     */
    public static final String GOODS_RELEASE_RULE=BASE_URL + "goods/releaserule";
    /**
     * 商品建议
     */
    public static final String GOODS_SUGGESTION=BASE_URL + "goods/suggestlist";
    /**
     * 店铺信息设置
     */
    public static final String SHOP_MESSAGE=BASE_URL + "shop";
    /**
     * 获取商品库商品详情
     */
    public static final String GOODS_DEPOT_INFO=BASE_URL + "goods/depotinfo";
    /**
     * 获取分类列表
     */
     public static final String GOODS_CATEGORY_LIST=BASE_URL+"goods/categorylist";
    /**
     * 获取商品库商品列表
     */
    public static final String GOODS_DEPOT_LIST=BASE_URL+"goods/depotlist";
    /**
     * 保存商品信息
     */
    public static final String GOODS_SAVE=BASE_URL+"goods/save";

    /**
     * 获取商品列表
     */
    public static final String GOODS_LIST=BASE_URL+"goods/goodslist";

    /**
     * 置顶
     */
    public static final String GOODS_TO_TOP=BASE_URL+"goods/totop";
    /**
     * 设置上下架、删除
     */
    public static final String GOODS_SET_STATUS=BASE_URL+"goods/setstatus";

    /**
     * 添加推荐
     */
    //public static final String GOODS_RECOMMEND=BASE_URL+"goods/recommend";

    /**
     *  掌柜推荐/热门/0.1元补贴
     */
    public static final String GOODS_MARKETING_TOOLS=BASE_URL+"goods/marketing_tools";

    /**
     *营销工具帮助说明
     */
    public static final String GOODS_TOOLS_HRLP=BASE_URL+"goods/toolhelp";

    /**
     * 未确认订单
     */
    //public static final String INCOME_UNCONFIRM_ORDER=BASE_URL+"income/unconfirmed";

    /**
     * 订单（含未确认/今日）列表
     */
    public static final String INCOME_ORDER_LIST=BASE_URL+"income/orderlist";

    /**
     * 商品详情
     */
    public static final String GOODS_INFO=BASE_URL+"goods/goodsinfo";

    /**
     * 用户评价
     */
    public static final String ORDER_COMMENT_LIST=BASE_URL+"order/commentlist";
    /**
     * 盈利记录
     */
    public static final String INCOME_PROFIT_LIST=BASE_URL+"income/profitlist";

    /**
     * 营销工具使用权限
     */
    public static final String SHOP_TOOLS=BASE_URL+"shop/tools";

}
