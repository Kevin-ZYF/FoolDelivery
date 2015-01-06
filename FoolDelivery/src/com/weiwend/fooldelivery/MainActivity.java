package com.weiwend.fooldelivery;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.weiwend.fooldelivery.configs.DataConfigs;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyCycleGallery;
import com.weiwend.fooldelivery.customviews.MyCycleGallery.MyOnGalleryItemClickListener;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.GalleryItem;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MD5Util;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.SpUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class MainActivity extends Activity implements MyOnGalleryItemClickListener,onDataLoaderListener,OnTouchListener{

	//轮播图控件
	private MyCycleGallery mCycleGallery;
	
	//6个功能按钮
	private LinearLayout mTitleImgContainer,mSentContainer,mUserCenterContainer,mQueryContainer,mNoticeCenterContainer,mParityContainer,mNearbyContainer;
	
	//获取轮播图数据时的各种状态
	private final int GALLERY_INFO_GETTING=0;
	private final int GALLERY_INFO_GET_SUCCESS=1;
	private final int GALLERY_INFO_GET_FAILED=2;
	
	//获取轮播图数据时的各种状态
	private final int IDLE_GETTING=3;
	private final int IDLE_GET_SUCCESS=4;
	private final int IDLE_GET_FAILED=5;
	
	private final int DELIVERY_COMPANY_GETTING=6;
	private final int DELIVERY_COMPANY_GET_SUCCESS=7;
	private final int DELIVERY_COMPANY_GET_FAILED=8;
	
	//轮播图当前显示图片的序号
    //private int curSelectedItem=0;
	
    //用于轮播图列表是否获取成功
    private boolean isGalleryEnable=false;
	
	//耗时操作对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	//获取轮播图时的各种异步操作
	private Handler mHandler=new Handler(){

		@SuppressLint("NewApi") 
		@Override
		public void handleMessage(Message msg) {
			
			
			switch (msg.what) {
			
			case GALLERY_INFO_GETTING:    //获取轮播图的接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","gallery getting......");
				
				break;
				
			case GALLERY_INFO_GET_SUCCESS:    //获取轮播图成功
				
				Log.e("zyf","gallery get success......");
				
				/*mGallery.setItemDatas(mGalleryItems);
				mHandler.postDelayed(mRunnable, DataConfigs.CYCLE_TIME_GALLERY);*/
				
				mCycleGallery.setItemDatas(mGalleryItems);
				
				isGalleryEnable=true;
				
				break;
				
			case GALLERY_INFO_GET_FAILED:    //获取轮播图失败
				
				Log.e("zyf","gallery get failed......");
				
				break;
            case IDLE_GETTING:   //获取负载均衡后的服务器地址接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","idle getting......");
				
				/*mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_app_int, MainActivity.this);
				mProgressDialogUtils.showDialog();*/
				
				break;
				
           case IDLE_GET_FAILED:    //获取负载均衡后的服务器地址失败
				
				Log.e("zyf","idle get failed......");
				
				/*mProgressDialogUtils.dismissDialog();
				MyToast.makeText(MainActivity.this, "应用信息初始化失败...", MyToast.LENGTH_SHORT).show();*/
				
				break;
				
			case IDLE_GET_SUCCESS:    //获取负载均衡后的服务器地址成功
				
				Log.e("zyf","idle get success......");
				
				//UrlConfigs.SERVER_URL="http://"+"10.0.0.28"+":"+"800";
				
				//判断启动应用时是否需要自动登录
		        if(SpUtils.getIsAutoLogin(MainActivity.this)){
		        	Log.e("zyf","need login automatically......");
		        	
		        	String acnt=SpUtils.getUsername(MainActivity.this);
					String pswd_md5=MD5Util.MD5(SpUtils.getPsw(MainActivity.this));
					
					//String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_LOGIN_URL+"?acnt="+acnt+"&pswd_md5="+pswd_md5;
					String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_LOGIN_URL;
					
					AsyncTaskDataLoader mLoginAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_USER_LOGIN, url, null, formatLoginContent(acnt, pswd_md5));
					mLoginAsyncTaskDataLoader.setOnDataLoaderListener(MainActivity.this);
					mLoginAsyncTaskDataLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		        }else{
		        	Log.e("zyf","not not not need login automatically......");
		        }
		        
		        //开始获取轮播图
		        mGalleryAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_GALLERY, UrlConfigs.SERVER_URL+UrlConfigs.GET_GALLERY_URL, null, null);
		        mGalleryAsyncTaskDataLoader.setOnDataLoaderListener(MainActivity.this);
		        mGalleryAsyncTaskDataLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		        
		        //获取快递公司信息
		        /*mGetAllCompanyAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_DELIVERY_COMPANY, UrlConfigs.SERVER_URL+UrlConfigs.GET_DELIVERY_COMPANY_URL, null, null);
				mGetAllCompanyAsyncTaskDataLoader.setOnDataLoaderListener(MainActivity.this);
				mGetAllCompanyAsyncTaskDataLoader.execute(AsyncTask.THREAD_POOL_EXECUTOR);*/
				
				break;
				
           case DELIVERY_COMPANY_GETTING:   //获取快递公司列表的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","delivery company list getting......");
				
				break;
			case DELIVERY_COMPANY_GET_SUCCESS:    //获取快递公司列表成功
				
				Log.e("zyf","delivery company list get success......");
				
				MyToast.makeText(MainActivity.this, getString(R.string.app_init_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case DELIVERY_COMPANY_GET_FAILED:    //获取快递公司列表失败
				
				Log.e("zyf","delivery company list get failed......");
				
                MyToast.makeText(MainActivity.this, getString(R.string.app_init_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
				
			default:
				break;
			}
		}
		
	};
	
	//获取轮播图的异步加载类
	private AsyncTaskDataLoader mGalleryAsyncTaskDataLoader,mIdleAsyncTaskDataLoader;
	
	//获取快递公司列表的异步加载类
	private AsyncTaskDataLoader mGetAllCompanyAsyncTaskDataLoader;
	
	//获取的轮播图列表
	private ArrayList<GalleryItem> mGalleryItems=new ArrayList<GalleryItem>();
	
	@SuppressLint("NewApi") 
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initTitleImg();
        
        mCycleGallery=(MyCycleGallery)findViewById(R.id.mCycleGallery);
        mCycleGallery.setMyGalleryOnItemClickListener(this);
        
        mSentContainer=(LinearLayout)findViewById(R.id.mSentContainer);
        mUserCenterContainer=(LinearLayout)findViewById(R.id.mUserCenterContainer);
        mQueryContainer=(LinearLayout)findViewById(R.id.mQueryContainer);
        mNoticeCenterContainer=(LinearLayout)findViewById(R.id.mNoticeCenterContainer);
        mParityContainer=(LinearLayout)findViewById(R.id.mParityContainer);
        mNearbyContainer=(LinearLayout)findViewById(R.id.mNearbyContainer);
        
        mSentContainer.setOnTouchListener(this);
        mUserCenterContainer.setOnTouchListener(this);
        mQueryContainer.setOnTouchListener(this);
        mNoticeCenterContainer.setOnTouchListener(this);
        mParityContainer.setOnTouchListener(this);
        mNearbyContainer.setOnTouchListener(this);
        
        //获取负载均衡的ip:port
        mIdleAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_IDLE, UrlConfigs.SERVER_URL+UrlConfigs.GET_IDLE_URL, null, null);
        mIdleAsyncTaskDataLoader.setOnDataLoaderListener(this);
        mIdleAsyncTaskDataLoader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        
    }
	
	//格式化登录接口所要上传的json数据
    private String formatLoginContent(String acnt,String pswd_md5){
		
		String str="{\"acnt\":"+"\""+acnt+"\""+","
				   +"\"pswd_md5\":"+"\""+pswd_md5+"\""+"}";
		
		Log.e("zyf","login content: "+str);
		return str;
	}
	
	//页面暂停时，轮播图挺停止轮播
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		
		mCycleGallery.stopCycle();

	}

	//重新返回到该页面时，判断是否需要轮播轮播图
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		if(isGalleryEnable){
			
			mCycleGallery.startCycle();
			
		}else{
			Log.e("zyf","Gallery is not not not enable......");
		}
	}

	//初始化顶部的logo,主要为了适配不同分辨率的设备
	private void initTitleImg(){
		mTitleImgContainer=(LinearLayout)findViewById(R.id.mTitleImgContainer);
		ImageView titleImageView=new ImageView(this);
		titleImageView.setBackgroundResource(R.drawable.home_title);
		WindowManager wm=(WindowManager)getSystemService(Context.WINDOW_SERVICE);
        int width=wm.getDefaultDisplay().getWidth();
        int height=(int) (width*DataConfigs.RATIO_HOME_TITILE);
		LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(width, height);
		mTitleImgContainer.addView(titleImageView, lp);
	}

	//AsyncTaskDataLoader的回调函数，主要处理调用服务器端接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_USER_LOGIN){
			Log.e("zyf","loginning.....");
		}else if(flag==Flag.FLAG_GET_GALLERY){
			Utils.sendMessage(mHandler, GALLERY_INFO_GETTING);
		}else if(flag==Flag.FLAG_GET_IDLE){
			//Utils.sendMessage(mHandler, IDLE_GETTING);
		}
	}

	//AsyncTaskDataLoader的回调函数，获取调用服务器端接口后的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_USER_LOGIN){   //正在等待登录反馈结果
			
			Log.e("zyf","login result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){      //登录成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					MyApplicaition.sesn=dataJsonObject.getString("sesn");
					
                    MyApplicaition.bindedMobileNumberWithNoHide=dataJsonObject.getString("mobi");
					
					MyApplicaition.bindedMobileNumber=MyApplicaition.bindedMobileNumberWithNoHide.substring(0, 3)+"****"+MyApplicaition.bindedMobileNumberWithNoHide.substring(7, 11);
					
					MyApplicaition.mNickName=dataJsonObject.getString("nnam");
					
					MyApplicaition.mUserName=SpUtils.getUsername(this);
					
					Log.e("zyf","login success...sesn: "+MyApplicaition.sesn);
					
					return;
				}
				
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Log.e("zyf","login failed......");
			
	    }else if(flag==Flag.FLAG_GET_GALLERY){    //获取轮播图列表
	    	
            Log.e("zyf","gallery get result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //获取轮播图列表成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					JSONArray itemsJsonArray=dataJsonObject.getJSONArray("items");
					
					JSONObject jsonObject;
					GalleryItem galleryItem;
					
					mGalleryItems.clear();
					
					for(int i=0;i<itemsJsonArray.length();i++){
						
						jsonObject=itemsJsonArray.getJSONObject(i);
						
						galleryItem=new GalleryItem();
						
						galleryItem.setIdx(jsonObject.getInt("idx"));
						galleryItem.setPath(UrlConfigs.SERVER_URL+jsonObject.getString("path"));
						galleryItem.setTime(jsonObject.getInt("time"));
						galleryItem.setKact(jsonObject.getString("kact"));
						
						mGalleryItems.add(galleryItem);
					}
					
					//根据每张轮播图的idx进行升序
					GalleryItem tempItem;
					for(int i=0;i<mGalleryItems.size();i++){
						for(int j=i+1;j<mGalleryItems.size();j++){
							if(mGalleryItems.get(i).getIdx()>=mGalleryItems.get(j).getIdx()){
								tempItem=mGalleryItems.get(i);
								mGalleryItems.set(i, mGalleryItems.get(j));
								mGalleryItems.set(j, tempItem);
							}
						}
					}
					
					Utils.sendMessage(mHandler, GALLERY_INFO_GET_SUCCESS);
					
					return;
				}
				
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, GALLERY_INFO_GET_FAILED);    //获取轮播图列表失败
			 
	    }else if(flag==Flag.FLAG_GET_IDLE){    //获取负载均衡后的服务器地址
	    	
	    	Log.e("zyf","idle get result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //获取负载均衡后的服务器地址成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					String host=dataJsonObject.getString("host");
					String port=dataJsonObject.getString("port");
					
					UrlConfigs.SERVER_URL="http://"+host+":"+port;
					
					Log.e("zyf","new url: "+UrlConfigs.SERVER_URL);
					
					Utils.sendMessage(mHandler, IDLE_GET_SUCCESS);
					
					return;
				}
				
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, IDLE_GET_FAILED);    //获取负载均衡后的服务器地址失败
			
		}else if(flag==Flag.FLAG_GET_DELIVERY_COMPANY){
			
			Log.e("zyf","company list: "+result);
			
			/*try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				if("0".equals(rc)){
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					JSONArray deliveryCompJsonArray=dataJsonObject.getJSONArray("items");
					
					JSONObject jsonObject;
					
					DeliveryCompanyItem deliveryCompanyItem;
					MyApplicaition.deliveryCompanyItems.clear();
				    for(int i=0;i<deliveryCompJsonArray.length();i++){
				    	jsonObject=deliveryCompJsonArray.getJSONObject(i);
				    	
				    	deliveryCompanyItem=new DeliveryCompanyItem();
				    	deliveryCompanyItem.setId(jsonObject.getString("id"));
				    	deliveryCompanyItem.setName(jsonObject.getString("name"));
				    	deliveryCompanyItem.setTelp(jsonObject.getString("telp"));
				    	deliveryCompanyItem.setDesp(jsonObject.getString("desp"));
				    	deliveryCompanyItem.setLogo(jsonObject.getString("logo"));
				    	
				    	MyApplicaition.deliveryCompanyItems.add(deliveryCompanyItem);
				    }
				    
				    Utils.sendMessage(mHandler, DELIVERY_COMPANY_GET_SUCCESS);
				    
				    return;
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, DELIVERY_COMPANY_GET_FAILED);*/
		}
    }
	
	

	//监听touch事件，主要为了实现点击button时的缩放效果
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		
		//设置Animation   
        Animation animDwon = AnimationUtils.loadAnimation(this, R.anim.show_down);  
        Animation animUp = AnimationUtils.loadAnimation(this, R.anim.show_up);  
          
        LinearLayout layout = (LinearLayout)view;  
        switch (event.getAction()) {
        
        case MotionEvent.ACTION_DOWN:
        	
            layout.startAnimation(animDwon);  
            animDwon.setFillAfter(true); 
            
            break;  
  
        case MotionEvent.ACTION_UP:
        	
            layout.startAnimation(animUp);  
            animUp.setFillAfter(true);
            
            Intent intent=new Intent();
    		
    		switch (view.getId()) {
    		
    		case R.id.mSentContainer:    //进入"发件"页面
    			
    			intent.setClass(this, SenderActivity.class);
    			
    			break;
    		case R.id.mUserCenterContainer:    //进入"用户中心"页面
    			
    			intent.setClass(this, UserCenterActivity.class);
    			
    			break;
    		case R.id.mQueryContainer:      //进入"查询"页面
    			
    			intent.setClass(this, QueryActivity.class);
    			
    			break;
    		case R.id.mNoticeCenterContainer:       //进入"消息中心"页面
    			
    			intent.setClass(this, MessageCenterActivity.class);

    			break;
    		case R.id.mParityContainer:   //进入比价界面
    			
    			intent.setClass(this, ParityActivity.class);

    			break;
    		case R.id.mNearbyContainer:  //进入附近界面
    			
    			intent.setClass(this, NearbyActivity.class);
    			
    			break;

    		default:
    			break;
    		}
    		
    		startActivity(intent);
    		
            break;  
        }  

		return true;
	}
	
	//以下代码均为实现"连续2次点击返回键退出应用"的功能
	private static Boolean isExit = false;
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			exitByTwoClick();
		}
		return false;
	}
	
	private void exitByTwoClick(){
		Timer tExit = null;
		if(isExit == false){
			isExit = true;
			MyToast.makeText(this, true, getString(R.string.prompt_exit), MyToast.LENGTH_SHORT).show();
			tExit = new Timer();
			tExit.schedule(new TimerTask() {

				@Override
				public void run() {
					isExit = false;
				}
			},DataConfigs.EXIT_DELAY);				
		}else{
			MyApplicaition.getInstance().exit();
		}				   
	}

	//轮播图控件的回调函数，主要处理轮播图的点击事件
	@Override
	public void onGalleryItemClick(int position) {
		
		Log.e("zyf","gallery click position: "+position);
	}

}
