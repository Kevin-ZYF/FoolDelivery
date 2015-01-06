package com.weiwend.fooldelivery.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SpUtils {
	
	//用来保存最近更新头像的时间，服务器端根据这个时间，判断是否返回给客户端新的头像
	private static String ITEM_LAST_UPDATED_TIME="lastUpdatedTime";
	
	//用户登录的设置
	private static String ITEM_BASIC="basicItem";
	
	//基本的设置
	private static String ITEM_SETTING="setting";
	
	//保存头像更新的时间
	public static void saveHeadImgLastUpdatedTime(Context context, String userName, String lastUpdatedTime){
		SharedPreferences settings = context.getSharedPreferences(ITEM_LAST_UPDATED_TIME, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putString(userName, lastUpdatedTime);
		editor.commit();
	}
	
	//获取头像更新的时间
	public static String getHeadImgLastUpdatedTime(Context context,String userName){
		SharedPreferences settings = context.getSharedPreferences(ITEM_LAST_UPDATED_TIME, Context.MODE_PRIVATE);
		String userId= settings.getString(userName, "");
		
		return userId;
	}
	
	//保存用户是否开启了"自动登录"功能
	public static void saveIsAutoLogin(Context context, boolean isAutoLogin){
		SharedPreferences settings = context.getSharedPreferences(ITEM_BASIC, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putBoolean("isAutoLogin", isAutoLogin);
		editor.commit();
	}
	
	//获取用户是否开启了"自动登录"功能
	public static boolean getIsAutoLogin(Context context){
		SharedPreferences settings = context.getSharedPreferences(ITEM_BASIC, Context.MODE_PRIVATE);
		boolean isAutoLogin= settings.getBoolean("isAutoLogin", false);
		
		return isAutoLogin;
	}
	
	//保存"自动登录"时的用户名
	public static void saveUsername(Context context, String username){
		SharedPreferences settings = context.getSharedPreferences(ITEM_BASIC, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putString("username", username);
		editor.commit();
	}
	
	//获取"自动登录"时的用户名
	public static String getUsername(Context context){
		SharedPreferences settings = context.getSharedPreferences(ITEM_BASIC, Context.MODE_PRIVATE);
		String username= settings.getString("username","");
		
		return username;
	}
	
	//保存"自动登录"时的密码
	public static void savePsw(Context context, String psw){
		SharedPreferences settings = context.getSharedPreferences(ITEM_BASIC, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putString("psw", psw);
		editor.commit();
	}
	
	//获取"自动登录"时的密码
	public static String getPsw(Context context){
		SharedPreferences settings = context.getSharedPreferences(ITEM_BASIC, Context.MODE_PRIVATE);
		String psw= settings.getString("psw","");
		
		return psw;
	}
	
	//声音设置(开启、关闭)
	public static void enableVoice(Context context, boolean enabled){
		SharedPreferences settings = context.getSharedPreferences(ITEM_SETTING, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putBoolean("VoiceEnabled", enabled);
		editor.commit();
	}
	
	//获取声音的设置值(开启、关闭)
	public static boolean getVoiceStatus(Context context){
		SharedPreferences settings = context.getSharedPreferences(ITEM_SETTING, Context.MODE_PRIVATE);
		boolean enabled= settings.getBoolean("VoiceEnabled", false);
		
		return enabled;
	}
	
	//震动设置(开启、关闭)
	public static void enableShark(Context context, boolean enabled){
		SharedPreferences settings = context.getSharedPreferences(ITEM_SETTING, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putBoolean("SharkEnabled", enabled);
		editor.commit();
	}
	
	//获取震动的设置值(开启、关闭)
	public static boolean getSharkStatus(Context context){
		SharedPreferences settings = context.getSharedPreferences(ITEM_SETTING, Context.MODE_PRIVATE);
		boolean enabled= settings.getBoolean("SharkEnabled", false);
		
		return enabled;
	}
	
	//免打扰设置
	public static void saveMessageReceiveTime(Context context, int index){
		SharedPreferences settings = context.getSharedPreferences(ITEM_SETTING, Context.MODE_PRIVATE);
		Editor editor = settings.edit();
		editor.putInt("MessageReceiveTime", index);
		editor.commit();
	}
	
	//获取免打扰的设置值
	public static int getMessageReceiveTime(Context context){
		SharedPreferences settings = context.getSharedPreferences(ITEM_SETTING, Context.MODE_PRIVATE);
		int time= settings.getInt("MessageReceiveTime", 0);
		
		return time;
	}

}
