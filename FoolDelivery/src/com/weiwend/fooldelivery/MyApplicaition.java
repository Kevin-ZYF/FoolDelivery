package com.weiwend.fooldelivery;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Application;

public class MyApplicaition extends Application {
	
	private List<Activity> activityList = new LinkedList<Activity>();  //用来缓存本应用所有正在运行的activity

	private static MyApplicaition mInstance = null;   //单例
	
	public static String mUserName="";   //保存登陆成功后的用户名
	
	public static String mNickName="";    //保存登陆成功后的用户昵称
	
	public static String sesn="";    //保存登陆成功后的sesn
	
	public static String bindedMobileNumber="";    //保存登陆成功后的绑定手机号，中间用*标识
	
	public static String bindedMobileNumberWithNoHide="";    //保存登陆成功后的绑定手机号，中间用*标识
	
	//public static ArrayList<DeliveryCompanyItem> deliveryCompanyItems=new ArrayList<DeliveryCompanyItem>();

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;

	}
	
	//将指定activity加入到缓存列表中
	public void addActivity(Activity activity){
		activityList.add(activity);
	}
	
	//从缓存列表中移除指定activity
	public void removeActivity(Activity activity){
		activityList.remove(activity);
	}
	
	//退出应用
	public void exit(){
		for(Activity activity:activityList){
			activity.finish();
		}
		System.exit(0);
	}
	
	//获取Application单例
	public static MyApplicaition getInstance(){
		if(null == mInstance){
			mInstance = new MyApplicaition();
		}
		return mInstance;
	}
	
}