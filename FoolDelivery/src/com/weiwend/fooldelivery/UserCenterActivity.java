package com.weiwend.fooldelivery;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.provider.MediaStore.Images.Media;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyHeadImageLoaderView;
import com.weiwend.fooldelivery.customviews.MyHeadImgPostSelectorDialog;
import com.weiwend.fooldelivery.customviews.MyHeadImgPostSelectorDialog.MyOptionClickListener;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.internet.InternetHelper;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.CacheHandler;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.SpUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class UserCenterActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,MyOptionClickListener{
	
	//“登录”显示，是否登录的状态显示
	private TextView mLoginTv,mStatusTv;
	
	//头像
	private MyHeadImageLoaderView mHeadView;
	
	//登录、常用地址、我的订单、投诉建议、系统设置整体的Container
	private RelativeLayout mLoginContainer,mAddressContainer,mOrdersContainer,mFeedbackContainer,mSettingsContainer;
	
	//头像上传的异步加载类
	private AsyncTaskDataLoader mPostAsyncTaskDataLoader;
	
	//用于暂时保存通过相机拍摄的照片
    private final String IMAGE_FILE_NAME="faceImage.jpg";
    
    private final int HEAD_IMG_POSTING=0;
    private final int HEAD_IMG_POST_SUCCESS=1;
    private final int HEAD_IMG_POST_FAILED=2;
    
    //耗时操作的loading对话框
    private MyProgressDialogUtils mProgressDialogUtils;
    
    //设置头像的对话框
    private MyHeadImgPostSelectorDialog mHeadImgPostSelectorDialog;
    
    private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			
			switch (msg.what) {
			
			case HEAD_IMG_POSTING:   //头像上传的接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","head img posting...");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_post_head_img, UserCenterActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case HEAD_IMG_POST_SUCCESS:    //头像上传成功，将上传的头像保存到缓存目录
				
				Log.e("zyf","head img post success...");
				
				mHeadView.setBitmap(mBitmap);
				
				String filePath=CacheHandler.getHeadImageCacheDir()+"/"+MyApplicaition.mUserName+".png";

				File f=new File(filePath); 
				if(f.exists()){
					f.delete(); 
				} 
				try { 
					FileOutputStream out = new FileOutputStream(f); 
					mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out); 
					out.flush(); 
					out.close(); 
					Log.e("zyf", "file save success......"); 
				} catch (Exception e) {
					Log.e("zyf", "file save exception: "+e.toString());
				}
				
				MyToast.makeText(UserCenterActivity.this, getString(R.string.head_img_post_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
				
			case HEAD_IMG_POST_FAILED:    //头像上传失败
				
				Log.e("zyf","head img post failed...");
				
                MyToast.makeText(UserCenterActivity.this, getString(R.string.head_img_post_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
    };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_user_center);
		
		initViews();
		
		//注册密码修改成功的广播，用于刷新界面
		IntentFilter filter=new IntentFilter();
		filter.addAction(ActivityResultCode.ACTION_PSW_MODIFY);
		registerReceiver(mPswModifiedReceiver, filter);
		
		//注册昵称修改成功的广播，用于刷新界面
		IntentFilter filter2=new IntentFilter();
		filter2.addAction(ActivityResultCode.ACTION_NICKNAME_MODIFY);
		registerReceiver(mNicknameModifiedReceiver, filter2);
	}
	
	
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		//注销密码修改成功、昵称修改成功的广播
		unregisterReceiver(mPswModifiedReceiver);
		unregisterReceiver(mNicknameModifiedReceiver);
	}

	//初始化页面ui
	private void initViews(){
		mLoginTv=(TextView)findViewById(R.id.mLoginTv);
		
		mStatusTv=(TextView)findViewById(R.id.mStatusTv);
		
		mHeadView=(MyHeadImageLoaderView)findViewById(R.id.mHeadView);
		
		if(MyApplicaition.sesn!=null&&MyApplicaition.sesn.length()>0){
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_HEAD_IMG_URL+"?sesn="+MyApplicaition.sesn+"&lastUpdatedTime="+SpUtils.getHeadImgLastUpdatedTime(this, MyApplicaition.mUserName);
			Log.e("zyf","head url: "+url);
			
			mStatusTv.setText(MyApplicaition.mNickName);
			mLoginTv.setVisibility(View.GONE);

			mHeadView.setURL(url);
			
		}else{
			
			mHeadView.logout();
		}
		
		mHeadView.setOnClickListener(this);
		
		mLoginContainer=(RelativeLayout)findViewById(R.id.mLoginContainer);
		mLoginContainer.setOnClickListener(this);
		
		mFeedbackContainer=(RelativeLayout)findViewById(R.id.mFeedbackContainer);
		mFeedbackContainer.setOnClickListener(this);
		
		mAddressContainer=(RelativeLayout)findViewById(R.id.mAddressContainer);
		mAddressContainer.setOnClickListener(this);
		
		
		mSettingsContainer=(RelativeLayout)findViewById(R.id.mSettingsContainer);
		mSettingsContainer.setOnClickListener(this);
		
		mOrdersContainer=(RelativeLayout)findViewById(R.id.mOrdersContainer);
		mOrdersContainer.setOnClickListener(this);
	}

	//初始化标题栏信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_user_center));
	}
	
	//用于记录未登录时，用户点击的选项，当用户登录成功后，根据mode的值，直接跳转到对应的页面
	private int mode=-1;
	private final int MODE_CLICK_NORMAL=-1;
	private final int MODE_CLICK_ADDRESS=0;
	private final int MODE_CLICK_ACTIVITY=1;
	private final int MODE_CLICK_SUGGESTION=2;
	private final int MODE_CLICK_SETTING=3;
	private final int MODE_CLICK_ORDER=4;

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:    //标题栏的返回功能
			finish();
			break;
		case R.id.mLoginContainer:    //用户登录
			Intent intent=new Intent(this,UserLoginActivity.class);
			if(MyApplicaition.sesn.length()>0){
				intent=new Intent(this,UserLoginedActivity.class);
			}else{
				intent=new Intent(this,UserLoginActivity.class);
			}
			startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
			break;
		case R.id.mHeadView:     //上传头像
			
			if(MyApplicaition.sesn.length()==0){
				MyToast.makeText(UserCenterActivity.this, getString(R.string.you_have_not_login), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			mHeadImgPostSelectorDialog=new MyHeadImgPostSelectorDialog(UserCenterActivity.this, R.style.MyDialog);
			mHeadImgPostSelectorDialog.setMyOptionClickListener(this);
			mHeadImgPostSelectorDialog.show();
			
			break;
		case R.id.mAddressContainer:     //“常用地址”
			
        	if(MyApplicaition.sesn.length()==0){
				MyToast.makeText(UserCenterActivity.this, getString(R.string.you_have_not_login), MyToast.LENGTH_SHORT).show();
				
				mode=MODE_CLICK_ADDRESS;
				Intent loginIntent=new Intent(this, UserLoginActivity.class);
				startActivityForResult(loginIntent, ActivityResultCode.CODE_LOGIN);
				
				return;
			}
		
			Intent addressIntent=new Intent(this,AddressManagerActivity.class);
			startActivity(addressIntent);
        	break;
		case R.id.mFeedbackContainer:     //“投诉建议”
			
			if(MyApplicaition.sesn.length()==0){
				MyToast.makeText(UserCenterActivity.this, getString(R.string.you_have_not_login), MyToast.LENGTH_SHORT).show();
				
				mode=MODE_CLICK_SUGGESTION;
				Intent loginIntent=new Intent(this, UserLoginActivity.class);
				startActivityForResult(loginIntent, ActivityResultCode.CODE_LOGIN);
				
				return;
			}
			
			Intent feedbackIntent=new Intent(this,FeedbackActivity.class);
			startActivity(feedbackIntent);
			break;
        case R.id.mSettingsContainer:     //“系统设置”
        	
        	if(MyApplicaition.sesn.length()==0){
				MyToast.makeText(UserCenterActivity.this, getString(R.string.you_have_not_login), MyToast.LENGTH_SHORT).show();
				
				mode=MODE_CLICK_ORDER;
				Intent loginIntent=new Intent(this, UserLoginActivity.class);
				startActivityForResult(loginIntent, ActivityResultCode.CODE_LOGIN);
				
				return;
			}
        	
        	Intent settingIntent=new Intent(this,SettingActivity.class);
        	startActivity(settingIntent);
        	break;
        case R.id.mOrdersContainer:   //“我的订单”
        	
        	if(MyApplicaition.sesn.length()==0){
        		
				MyToast.makeText(UserCenterActivity.this, getString(R.string.you_have_not_login), MyToast.LENGTH_SHORT).show();
				
				mode=MODE_CLICK_ORDER;
				Intent loginIntent=new Intent(this, UserLoginActivity.class);
				startActivityForResult(loginIntent, ActivityResultCode.CODE_LOGIN);
				
				return;
			}
        	
        	Intent ordersIntent=new Intent(this,MyOrdersActivity.class);
        	startActivity(ordersIntent);
        	
        	/*new Thread(){

				@Override
				public void run() {
					String url="http://116.228.73.26:2080/User/Acnt/reg";
	            	InternetHelper mInternetHelper=new InternetHelper();
	            	String result=mInternetHelper.postContentToServer(url, formatRegisterContent());
	            	Log.e("zyf",result);
				}
        		
        		
        	}.start();*/
        	break;
		default:
			break;
		}
	}
    
	//设置头像对话框的点击回调函数
	@Override
	public void optionClick(int option) {
		
		if(option==MyHeadImgPostSelectorDialog.OPTION_CAPTURE){     //用户选择了“拍照”
			
			Intent intentFromCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intentFromCapture.putExtra(MediaStore.EXTRA_OUTPUT,Uri.fromFile(new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_NAME)));
            startActivityForResult(intentFromCapture,ActivityResultCode.CODE_CAMERA_REQUEST);
            
		}else if(option==MyHeadImgPostSelectorDialog.OPTION_GALLERY){    //用户选择了“相册”
			
		   Intent intentFromGallery = new Intent(Intent.ACTION_PICK,Media.EXTERNAL_CONTENT_URI);
           intentFromGallery.setType("image/*");
           startActivityForResult(intentFromGallery,ActivityResultCode.CODE_IMAGE_REQUEST);
		}
	}
    
    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode==RESULT_CANCELED) {
			
			return;
		}
		
		if(requestCode==ActivityResultCode.CODE_IMAGE_REQUEST){    //用户从相册中选择了照片，下一步裁剪操作
			
			startCropPic(data.getData());
			
		}else if(requestCode==ActivityResultCode.CODE_CAMERA_REQUEST){    //用户调用相机拍摄了照片，下一步裁剪操作
	
			File tempFile = new File(Environment.getExternalStorageDirectory(),IMAGE_FILE_NAME);
			startCropPic(Uri.fromFile(tempFile));
			
		}else if(requestCode==ActivityResultCode.CODE_RESULT_REQUEST){    //用户裁剪照片完成后，开始上传头像
			
			if (data!=null) {
				handleCropedPic(data);
			}
			
        }else if(requestCode==ActivityResultCode.CODE_LOGIN){   //用户重新登录或者退出登陆，返回该页面后重新刷新界面
        	
        	if(MyApplicaition.sesn.length()>0){   //用户登录成功
        		
        		String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_HEAD_IMG_URL+"?sesn="+MyApplicaition.sesn+"&lastUpdatedTime="+SpUtils.getHeadImgLastUpdatedTime(this, MyApplicaition.mUserName);
				Log.e("zyf","head url: "+url);
				
				mStatusTv.setText(MyApplicaition.mNickName);
				mLoginTv.setVisibility(View.INVISIBLE);
				
				mHeadView.setURL(url);
				
        	}else{   //用户退出登录
        		
        		mStatusTv.setText(getString(R.string.you_have_not_login));
    			mLoginTv.setVisibility(View.VISIBLE);
    			
    			mHeadView.logout();
        	}
    		
    		Intent intent=null;
    		
    		switch (mode) {

			case MODE_CLICK_ADDRESS:    //用户之前点击了“常用地址”
				
				intent=new Intent(this,AddressManagerActivity.class);
				
				break;
            case MODE_CLICK_SUGGESTION:    //用户之前点击了“投诉建议”
            	
            	intent=new Intent(this,FeedbackActivity.class);
				
				break;
            case MODE_CLICK_SETTING:    //用户之前点击了“系统设置”
            	
            	intent=new Intent(this,SettingActivity.class);

                break;
            case MODE_CLICK_ORDER:    //用户之前点击了“我的订单”
            	
            	intent=new Intent(this,MyOrdersActivity.class);
            	
                break;
			default:
				break;
			}
   
    		if(intent!=null){
    		    startActivity(intent);
    		}
    		
    		mode=MODE_CLICK_NORMAL;
        }
	
	}
    
    //格式化用户注册的json数据
    private String formatRegisterContent(){
		
		String str="{\"acnt\":"+"\""+"zhangyongfeng"+"\""+","
	               +"\"pswd\":"+"\""+"123456"+"\""+","
	               +"\"mobi\":"+"\""+"13962516913"+"\""+","
	               +"\"mcod\":"+"\""+"6904"+"\""+","
				   +"\"email\":"+"\""+"739578514@qq.com"+"\""+"}";
		
		Log.e("zyf","content: "+str);
		return str;
	}
    
    //用于用户修改密码成功后刷新界面
    BroadcastReceiver mPswModifiedReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			
			mStatusTv.setText(getString(R.string.you_have_not_login));
			mLoginTv.setVisibility(View.VISIBLE);
			
			mHeadView.logout();
			
			Intent intent1=new Intent(context,UserLoginActivity.class);
			startActivityForResult(intent1, ActivityResultCode.CODE_LOGIN);
		}
    	
    };
    
    //用于用户修改昵称成功后刷新界面
    BroadcastReceiver mNicknameModifiedReceiver=new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			
			mStatusTv.setText(MyApplicaition.mNickName);
		}
    	
    };
    
	//裁剪图片方法实现 
    public void startCropPic(Uri uri) {  
    	if(uri==null){
			Log.e("zyf", "the uri is not exist.");
		}
        Intent intent = new Intent("com.android.camera.action.CROP");  
        intent.setDataAndType(uri, "image/*");  
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, ActivityResultCode.CODE_RESULT_REQUEST);  
    }
    
    private Bitmap mBitmap;
 
    //保存裁剪之后的图片数据
    private void handleCropedPic(Intent data) {  
        Bundle extras = data.getExtras();  
        if (extras != null) {
        	
        	mBitmap= extras.getParcelable("data");	      

        	String content=formatContent(MyApplicaition.sesn, mBitmap);
        	mPostAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_POST_HEAD_IMG, UrlConfigs.SERVER_URL+UrlConfigs.GET_HEAD_IMG_SET_URL, null, content);
        	mPostAsyncTaskDataLoader.setOnDataLoaderListener(this);
        	mPostAsyncTaskDataLoader.execute();
        }  
    }
    
    //格式化头像上传的json数据
    public String formatContent(String sesn,Bitmap bitmap)
	{
    	String content="{\"sesn\":"+"\""+sesn+"\""+", image:"+"\""+Bitmap2StrByBase64(bitmap)+"\""+"}";
		return content;
	}
    
    //将bitmap转化为Base64编码格式的字符串
    public String Bitmap2StrByBase64(Bitmap bit){
	   ByteArrayOutputStream bos=new ByteArrayOutputStream();  
	   bit.compress(CompressFormat.JPEG, 50, bos);
	   byte[] bytes=bos.toByteArray();
	   return Base64.encodeToString(bytes, Base64.NO_WRAP);
	}

    //AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_POST_HEAD_IMG){    //正在上传头像
			
			Utils.sendMessage(mHandler, HEAD_IMG_POSTING);
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_POST_HEAD_IMG){  //获取头像上传的返回结果
			
			try {
				JSONObject jsonObject=new JSONObject(result);
				
				String rc=jsonObject.getString("rc");
				
				if("0".equals(rc)){    //头像上传成功
					
					Utils.sendMessage(mHandler, HEAD_IMG_POST_SUCCESS);
					
					return;
				}
			} catch (Exception e) {
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, HEAD_IMG_POST_FAILED);    //头像上传失败
		}
	}

}
