package com.weiwend.fooldelivery.configs;

public class UrlConfigs {
	
	//public static String SERVER_URL="http://store.weiwend.com:800";    //初始化服务器地址
	
	public static String SERVER_URL="http://10.0.0.28:800";
	
    public static String GALLLERY_PRE_URL="/";   //斜杠
	
	public static String GET_GALLERY_URL="/Base/Banner/get";        //获取轮播图
	
	public static String GET_PROVINCE_URL="/Base/Loca/getProvList";   //获取省份列表
	
	public static String GET_CITY_URL="/Base/Loca/getCityList";    //获取城市列表
	
	public static String GET_DISTRICT_URL="/Base/Loca/getDistList";   //获取区、县列表
	
	public static String GET_HEAD_IMG_URL="/User/Acnt/getheadimg";    //获取用户云端的头像
	
	public static String GET_HEAD_IMG_SET_URL="/User/Acnt/setheadimg";  //设置用户头像
	
	public static String GET_CAPTCHA_URL="/Base/Captcha/get";  //获取图像验证码
	
	public static String GET_REGISTER_URL="/User/Acnt/reg";    //用户注册
	
	public static String GET_LOGIN_URL="/User/Acnt/login";     //用户登录
	
	public static String GET_PSW_MODIFY_URL="/User/Acnt/chgpwd";   //修改密码
	
	public static String GET_FEEDBACK_URL="/User/Sugs/add";    //投诉建议
	
	public static String GET_ADDRESS_ADD_URL="/User/Addr/add";  //添加地址
	
	public static String GET_ADDRESS_MODIFY_URL="/User/Addr/mod";   //编辑地址
	
	public static String GET_ADDRESS_DELETE_URL="/User/Addr/del";   //删除地址
	
	public static String GET_ADDRESS_LIST_URL="/User/Addr/list";    //获取地址列表
	
	public static String GET_SEND_ADD_URL="/User/Send/add";     //新增寄件
	
	public static String GET_SEND_MODIFY_URL="/User/Send/mod";    //编辑寄件
	 
	public static String GET_SEND_CANCEL_URL="/User/Send/cancel";    //取消寄件
	
	public static String GET_SEND_LIST_URL="/User/Send/list";     //获取寄件列表
	
	public static String GET_SEND_COMMENT_URL="/User/Send/comment";   //寄件备注
	
	//public static String GET_SEND_CAL_PRICE_URL="/User/Send/calprice";   
	
	//public static String GET_SEND_DELIVERY_COMPANY_URL="/Base/Comp/list"; 
	
	public static String GET_HOT_CITY_URL="/Base/Loca/getHotCity";    //获取热门城市
	
	public static String GET_DELIVERY_QUERY_URL="/User/Look/get";     //查询快递详情，通过单号
	
	public static String GET_DELIVERY_COMPANY_URL="/Base/Comp/list";    //获取快递公司列表
	
	public static String GET_DELIVERY_COLLECT_HISTORY_URL="/User/Look/list";  //获取收藏列表
	
	public static String GET_DELIVERY_DETAIL_URL="/User/Look/detail";   //查询快递详情，通过id
	
	//public static String GET_HISTORY_DELETE_URL="/User/Look/del";
	
    public static String GET_PARITY_URL="/User/Send/compare";     //比价
	
	public static String GET_CODE_URL="/User/Acnt/sendmcod";     //获取短信验证码
	
	public static String GET_EXIT_URL="/User/Acnt/quit";     //退出登录
	
	public static String GET_CHECK_VERSION_URL="/Base/Sys/chkVersion";   //检查更新
	
	public static String GET_CHECK_CODE="/User/Acnt/chkmcode";   //验证短信验证码
	
	public static String GET_SUGGESTION_LIST="/User/Sugs/list";   //获取投诉建议列表
	
	public static String GET_IDLE_URL="/Base/Sys/getIdle";      //获取负载平衡后的服务器地址
	
	public static String GET_PSW_FORGET="/User/Acnt/lostpwd";    //忘记密码
	
	public static String GET_BIND_MOBILE_NUMBER="/User/Acnt/chgmobi";   //绑定手机号码
	
	public static String GET_QUERY_RESULT_ADD_REMARK_URL="/User/Look/comment";   //查询记录添加备注
	
	public static String GET_NICK_NAME_MODIFY_URL="/User/Acnt/chgnnam";   //修改昵称
	
	public static String GET_DELIVERY_COLLECT_URL="/User/Look/store";    //获取查询记录
	
	public static String GET_DELIVERY_CANCEL_COLLECT_URL="/User/Look/cancel";  //取消收藏
	
	public static String GET_NEARBY_URL="/Base/Station/listNear";    //获取附近网点
	
}
