package com.weiwend.fooldelivery;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

//所有activity的基类
public abstract class BaseActivity extends FragmentActivity {

	@Override
	protected void onStart() {
		super.onStart();
		
		initTitleViews();   
	}
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		MyApplicaition.getInstance().addActivity(this);      //将自身对象加入到应用的运行activity列表中
	}

	@Override
	protected void onDestroy() {
		
		super.onDestroy();
		
		//MyToast.dismiss();
		
		MyApplicaition.getInstance().removeActivity(this);      //将自身对象从应用的运行activity列表中移除
	}

	//继承该Activity的所有Activity都需要实现该方法，主要用于初始化各自页面的标题栏信息
	public abstract void initTitleViews();
	
}
