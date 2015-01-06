package com.weiwend.fooldelivery;

import java.io.File;

import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MD5Util;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.SpUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class UserLoginActivity extends BaseActivity implements OnClickListener,onDataLoaderListener{
	
	//“忘记密码?”
	private TextView mForgetPswTv;
	
	//“立即登录”
	private Button mLoginBtn;
	
	//“注册”
	private TextView mRegisterTv;
	
	//用户名、密码输入框
	private EditText mUsernameEt,mPswEt;
	
	//“自动登录”选择框
	private CheckBox mAutoLoginCb;
	
	//“自动登录”显示
	private TextView mAutoLoginTv;
	
	//调用登录接口后的各种状态信息
	private final int USER_LOGINNING=0;
	private final int USER_LOGIN_SUCCESS=1;
	private final int USER_LOGIN_FAILED=2;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case USER_LOGINNING:      //登录接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","loginning......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_loginning, UserLoginActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case USER_LOGIN_SUCCESS:      //登录成功
				
				Log.e("zyf","login success......");
				
				MyToast.makeText(UserLoginActivity.this, getString(R.string.login_success), MyToast.LENGTH_SHORT).show();
				
				if(mAutoLoginCb.isChecked()){    //如果用户选择了自动登录，则下次启动时自动登录
					Log.e("zyf","login automatically next time......");
					SpUtils.saveIsAutoLogin(UserLoginActivity.this, true);
					SpUtils.saveUsername(UserLoginActivity.this, mUsernameEt.getText().toString());
					SpUtils.savePsw(UserLoginActivity.this, mPswEt.getText().toString());
				}else{
					SpUtils.saveIsAutoLogin(UserLoginActivity.this, false);
				}
				
				MyApplicaition.mUserName=mUsernameEt.getText().toString();
				
				Intent intent=new Intent();
				intent.putExtra("loginStatus", true);
				setResult(ActivityResultCode.CODE_LOGIN,intent);
				
				finish();
				
				mProgressDialogUtils.dismissDialog();
            	
				break;
			case USER_LOGIN_FAILED:      //登录失败
				
				Log.e("zyf","login failed......");
				
				MyToast.makeText(UserLoginActivity.this, getString(R.string.login_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//用于登录的异步加载类
	private AsyncTaskDataLoader mLoginAsyncTaskDataLoader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_user_login);
		
		initViews();
	}
	
	//用来更新“立即登录”按钮的状态
	private void updateSummitBtnStatus(){
		
		if(mUsernameEt.getText().toString().length()>0&&mPswEt.getText().toString().length()>=6){
			mLoginBtn.setEnabled(true);
		}else{
			mLoginBtn.setEnabled(false);
		}
	}
	
	//初始化ui
	private void initViews(){
		mForgetPswTv=(TextView)findViewById(R.id.mForgetPswTv);
		mForgetPswTv.setOnClickListener(this);
		
		mRegisterTv=(TextView)findViewById(R.id.mRegisterTv);
		mRegisterTv.setOnClickListener(this);
		
		mLoginBtn=(Button)findViewById(R.id.mLoginBtn);
		mLoginBtn.setOnClickListener(this);
		
		mUsernameEt=(EditText)findViewById(R.id.mUsernameEt);
		mPswEt=(EditText)findViewById(R.id.mPswEt);
		
		mAutoLoginCb=(CheckBox)findViewById(R.id.mAutoLoginCb);
		mAutoLoginTv=(TextView)findViewById(R.id.mAutoLoginTv);
		
		mAutoLoginTv.setOnClickListener(this);
		
		mUsernameEt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				
				updateSummitBtnStatus();
			}
		});
		
		mPswEt.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable arg0) {
				
				updateSummitBtnStatus();
			}
		});
	}

	//初始化页面的标题栏信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		Button rightBtn=(Button)findViewById(R.id.rightBtn);
		rightBtn.setVisibility(View.VISIBLE);
		rightBtn.setText(getString(R.string.register));
		rightBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_user_login));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:      //标题栏的返回功能
			finish();
			break;
		case R.id.rightBtn:      //标题栏的右侧“注册”
		case R.id.mRegisterTv:
			Intent intent=new Intent(this,UserRegisterActivity.class);
			startActivityForResult(intent, ActivityResultCode.CODE_USER_REGISTER);
			break;
		case R.id.mForgetPswTv:      //“忘记密码”
			Intent intent2=new Intent(this,PswForgetActivity.class);
			startActivity(intent2);
			break;
		case R.id.mLoginBtn:      //“立即登录”
			
			if(mUsernameEt.getText().toString().length()==0
			   ||mPswEt.getText().toString().length()==0){
				MyToast.makeText(UserLoginActivity.this, getString(R.string.prompt_login), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			String acnt=mUsernameEt.getText().toString();
			String pswd_md5=MD5Util.MD5(mPswEt.getText().toString());
			
			if(mLoginAsyncTaskDataLoader!=null){
				mLoginAsyncTaskDataLoader.canceled();
			}
			
			//String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_LOGIN_URL+"?acnt="+acnt+"&pswd_md5="+pswd_md5;
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_LOGIN_URL;
			
			mLoginAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_USER_LOGIN, url, null, formatLoginContent(acnt, pswd_md5));
			mLoginAsyncTaskDataLoader.setOnDataLoaderListener(this);
			mLoginAsyncTaskDataLoader.execute();
			break;
		case R.id.mAutoLoginTv:      //“自动登录”
			if(mAutoLoginCb.isChecked()){
				mAutoLoginCb.setChecked(false);
			}else{
				mAutoLoginCb.setChecked(true);
			}
			break;
		default:
			break;
		}
	}
	
	//格式化登录接口所要上传的json数据
    private String formatLoginContent(String acnt,String pswd_md5){
		
		String str="{\"acnt\":"+"\""+acnt+"\""+","
				   +"\"pswd_md5\":"+"\""+pswd_md5+"\""+"}";
		
		Log.e("zyf","login content: "+str);
		
		return str;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	super.onActivityResult(requestCode, resultCode, data);
		
	    if(requestCode==ActivityResultCode.CODE_USER_REGISTER){  //用户注册成功后返回到本页面，实现自动登录
			if (data!=null) {
				
				String username=data.getStringExtra("username");
				String pswd_md5=MD5Util.MD5(data.getStringExtra("psw"));
				
				//String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_LOGIN_URL+"?acnt="+username+"&pswd_md5="+pswd_md5;
				String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_LOGIN_URL;
				
				Log.e("zyf","登录url: "+url);
				
				if(mLoginAsyncTaskDataLoader!=null){
					mLoginAsyncTaskDataLoader.canceled();
				}
				
				mLoginAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_USER_LOGIN, url, null, formatLoginContent(username, pswd_md5));
				mLoginAsyncTaskDataLoader.setOnDataLoaderListener(this);
				mLoginAsyncTaskDataLoader.execute();
			}
        }
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_USER_LOGIN){      //正在登录
			
			Utils.sendMessage(mHandler,USER_LOGINNING);
			
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_USER_LOGIN){      //获取用户登录返回结果
			
			Log.e("zyf","login result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){      //用户登录成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					
					MyApplicaition.sesn=dataJsonObject.getString("sesn");
					
					MyApplicaition.bindedMobileNumberWithNoHide=dataJsonObject.getString("mobi");
					
					MyApplicaition.bindedMobileNumber=MyApplicaition.bindedMobileNumberWithNoHide.substring(0, 3)+"****"+MyApplicaition.bindedMobileNumberWithNoHide.substring(7, 11);
					
					MyApplicaition.mNickName=dataJsonObject.getString("nnam");
					
					Log.e("zyf","sesn: "+MyApplicaition.sesn);
					
					Utils.sendMessage(mHandler, USER_LOGIN_SUCCESS);
					
					return;
				}
				
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, USER_LOGIN_FAILED);      //用户登录失败
		}
	}

}
