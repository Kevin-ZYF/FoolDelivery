package com.weiwend.fooldelivery.utils;

public class ActivityResultCode {
	
	public static String INVALID_SESN="100";   //sesn过期
	
	public static String ACTION_PSW_MODIFY="com.weiwend.psw.modify";   //密码修改成功广播的action
	
	public static String ACTION_NICKNAME_MODIFY="com.weiwend.nickname.modify";   //昵称修改成功广播的action
	
	public static String ACTION_CHECK_VERSION="com.weiwend.check.version";   //版本检测成功广播的action
	
	
	//以下均为startActivityForResult函数的requestCode值
	
	public static int CODE_ADDRESS_SENDER_SELECTOR=0;   //编辑发件人
	
	public static int CODE_ADDRESS_RECIPIENT_SELECTOR=1;   //编辑收件人
	
	public static int CODE_ADDRESS_ADD=2;   //添加新地址
	
	public static int CODE_ADDRESS_SELECTOR=3;   //选择省市区
	
	public static int CODE_SCAN=4;   //扫一扫
	
	public static int CODE_SEND_INFO_MODIFY=5;   //修改订单信息
	
	public static int CODE_LOGIN=6;   //登录
	
	public static int CODE_IMAGE_REQUEST=7;   //打开本地相册
	
	public static int CODE_CAMERA_REQUEST=8;   //调用照相机
	
	public static int CODE_RESULT_REQUEST=9;    //照片裁剪完毕
	
	public static int CODE_DELIVERY_COMPANY_SELECTOR=10;   //快递公司选择
	
	//public static int CODE_QUICK_SELECTOR=11;
	
	public static int CODE_ADDRESS_MODIFY=12;    //修改地址信息
	
	public static int CODE_PARITY_SELECTOR=13;   //比价后的网点选择
	
	public static int CODE_DELIVERY_HISTORY_SELECTOR=14;  //查询历史选择
	
    public static int CODE_PARITY_CITY_SELECTOR=15;   //比价城市选择
	
	public static int CODE_USER_REGISTER=16;   //用户注册
	
	public static int CODE_PSW_MODIFY=17;    //登录密码修改
	
}
