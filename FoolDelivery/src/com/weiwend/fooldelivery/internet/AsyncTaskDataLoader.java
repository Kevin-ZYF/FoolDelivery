package com.weiwend.fooldelivery.internet;

import java.util.HashMap;

import android.os.AsyncTask;
import android.util.Log;

public class AsyncTaskDataLoader extends AsyncTask<Object, Object, Object> {
	
	//异步加载类的监听器
	public interface onDataLoaderListener{
		void start(int flag);
		void completed(int flag,String result);
	}
	
	private onDataLoaderListener mOnDataLoaderListener;
	
	//设置异步加载类的监听器
	public void setOnDataLoaderListener(onDataLoaderListener mOnDataLoaderListener){
		this.mOnDataLoaderListener=mOnDataLoaderListener;
	}
	
	//标识调用的是get方法还是post方法
	private boolean isGetMethod; 
	
	//区别不同的异步加载类
	private int flag;
	
	//服务器端的地址
	private String url;
	
	//post方式上传的参数(键值对形式)
	private HashMap<Object, Object> params;
	
	//post方式上传的Json数据
	private String content;
	
	//网络操作类
	private InternetHelper mInternetHelper;

	public AsyncTaskDataLoader(boolean isGetMethod,int flag,String url, HashMap<Object, Object> params ,String content) {
		super();
		this.isGetMethod=isGetMethod;
		this.flag=flag;
		this.url = url;
		this.params = params;
		this.content=content;
		
		mInternetHelper=new InternetHelper();
	}

	@Override
	protected Object doInBackground(Object... arg0) {
		
		//回调函数
		if(mOnDataLoaderListener!=null){
			mOnDataLoaderListener.start(flag);
		}
		
		String result=null;
		
		if(isGetMethod){   //调用get方法
			
			result=mInternetHelper.getContentFromServer(url);
			
		}else{   //调用post方法
			
			if(params!=null){   //通过键值对的形式上传参数
				
				result=mInternetHelper.postContentToServer(url, params);
				
			}else{    //上传Json数据
				
				result=mInternetHelper.postContentToServer(url, content);
				
			}
		}
		
		//回调函数
		if(mOnDataLoaderListener!=null){
			mOnDataLoaderListener.completed(flag,result);
		}
		
		return null;
	}

	//断开与服务器端的连接
	public void canceled(){
		if(isGetMethod){
			mInternetHelper.closeGetConnection();
		}else{
			mInternetHelper.closePostConnection();
		}
	}

}
