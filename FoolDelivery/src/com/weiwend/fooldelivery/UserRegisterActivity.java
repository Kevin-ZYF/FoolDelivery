package com.weiwend.fooldelivery;


import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
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

import com.weiwend.fooldelivery.configs.DataConfigs;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MD5Util;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class UserRegisterActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,TextWatcher{
	
	//“立即注册”按钮、“获取验证码”按钮
	private Button mRegisterBtn,mGetCodeBtn;
		
	//用户名、昵称、密码、重复密码，短信验证码、绑定的手机号码输入框
	private EditText mUsernameEt,mNicknameEt,mPswEt,mPswRepeatEt,mCaptchaEt,mMobileNumberEt;
	
	//“同意”选择框
	private CheckBox mAgreeCb;
	
    //“<<注册协议>>”
	private TextView mRegisterAgreementTv;
	
	//调用注册接口后的各种状态信息
	private final int USER_REGISTERING=1;
	private final int USER_REGISTER_SUCCESS=2;
	private final int USER_REGISTER_FAILED=3;
	
	//调用获取短信验证码接口后的各种状态信息
	private final int CODE_GETTING=4;
	private final int CODE_GET_SUCCESS=5;
	private final int CODE_GET_FAILED=6;
	
	//调用短信验证码验证接口后的各种状态信息
	private final int CODE_CHECKING=7;
	private final int CODE_CHECK_SUCCESS=8;
	private final int CODE_CHECK_FAILED=9;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	//短信验证码
	private String code="";
	
	//绑定的手机号码
	private String phoneNumber="";
	
	//保存获取短信验证码时的sesn值
	private String mcodSesn="";
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
           
            case USER_REGISTERING:      //注册接口已经调用，正在等待服务器端的回复
            	
            	mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_registering, UserRegisterActivity.this);
				mProgressDialogUtils.showDialog();
				
				Log.e("zyf","registering......");
				
				break;
			case USER_REGISTER_SUCCESS:      //注册成功，返回之前的页面，并实现自动登录
				
				Log.e("zyf","register success.....");
				
				mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(UserRegisterActivity.this, getString(R.string.register_success), MyToast.LENGTH_SHORT).show();
				
				Intent intent=new Intent();
				intent.putExtra("username", mUsernameEt.getText().toString());
				intent.putExtra("psw", mPswEt.getText().toString());
				
				setResult(ActivityResultCode.CODE_USER_REGISTER,intent);
				
				finish();
				
				break;
			case USER_REGISTER_FAILED:      //注册失败
				
				Log.e("zyf","register failed.....");
				
				mProgressDialogUtils.dismissDialog();
				
				MyToast.makeText(UserRegisterActivity.this, getString(R.string.register_failed), MyToast.LENGTH_SHORT).show();
				
				break;
            case CODE_GETTING:      //获取短信验证码的接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","code getting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_code_getting, UserRegisterActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case CODE_GET_SUCCESS:     //获取短信验证码成功
				
				Log.e("zyf","code get success.....");
				
				mGetCodeBtn.setEnabled(false);
				mGetCodeBtn.setText(getString(R.string.remain)+leftTime+" s");
				
				mCaptchaEt.setText(code);
				
				phoneNumber=mMobileNumberEt.getText().toString();
				
				mHandler.postDelayed(mTimeCountRunnable, 1000);      //开启60s的倒计时时间，在倒计时结束前，用户不可以重复点击“获取验证码”
				
                MyToast.makeText(UserRegisterActivity.this, getString(R.string.code_get_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case CODE_GET_FAILED:      //获取短信验证码失败
				
				Log.e("zyf","code get failed.....");
				
				MyToast.makeText(UserRegisterActivity.this, getString(R.string.code_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case CODE_CHECKING:      //短信验证码的验证接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","start check code...");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_check_code, UserRegisterActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case CODE_CHECK_SUCCESS:      //短信验证码验证成功
				
				Log.e("zyf","start check code success...");
				
				mRegisterBtn.setEnabled(true);
				
				MyToast.makeText(UserRegisterActivity.this, getString(R.string.check_code_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case CODE_CHECK_FAILED:      //短信验证码验证失败
				
				Log.e("zyf","start check code failed...");
				
				mRegisterBtn.setEnabled(false);
				
				MyToast.makeText(UserRegisterActivity.this, getString(R.string.check_code_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//验证码可重新获取的剩余倒计时时间
	private int leftTime;
	
	//用于每隔1秒定时刷新“获取验证码”按钮的显示信息
	Runnable mTimeCountRunnable=new Runnable() {
		
		@Override
		public void run() {
			
			leftTime--;
			mGetCodeBtn.setText(getString(R.string.remain)+leftTime+" s");
			
			if(leftTime>0){
				
				mHandler.postDelayed(this, 1000);
				
			}else{      //倒计时时间结束
				
				mGetCodeBtn.setEnabled(true);
				mGetCodeBtn.setText(getString(R.string.code_get));
				
				leftTime=DataConfigs.MAX_TIME_CODE_WAIT;
			}
		}
	};
	
	//用户注册的异步加载类
	private AsyncTaskDataLoader mRegisterAsyncTaskDataLoader;
	
	//获取短信验证码的异步加载类
	private AsyncTaskDataLoader mGetCodeAsyncTaskDataLoader;
	
	//短信验证码验证的异步加载类
	private AsyncTaskDataLoader mCheckCodeAsyncTaskDataLoader;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_user_register);
		
		initViews();
		
		leftTime=DataConfigs.MAX_TIME_CODE_WAIT;
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		//页面销毁时，取消倒计时
		mHandler.removeCallbacks(mTimeCountRunnable);
	}
	
	//初始化页面的ui
	private void initViews(){
		
		//“<<注册协议>>”添加下划线
		mRegisterAgreementTv=(TextView)findViewById(R.id.mRegisterAgreementTv);
		mRegisterAgreementTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		
		mRegisterBtn=(Button)findViewById(R.id.mRegisterBtn);
		mRegisterBtn.setOnClickListener(this);
		
		mGetCodeBtn=(Button)findViewById(R.id.mGetCodeBtn);
		mGetCodeBtn.setOnClickListener(this);
		
		mUsernameEt=(EditText)findViewById(R.id.mUsernameEt);
		mNicknameEt=(EditText)findViewById(R.id.mNicknameEt);
		mPswEt=(EditText)findViewById(R.id.mPswEt);
		mPswRepeatEt=(EditText)findViewById(R.id.mPswRepeatEt);
		mCaptchaEt=(EditText)findViewById(R.id.mCaptchaEt);
		mMobileNumberEt=(EditText)findViewById(R.id.mMobileNumberEt);
		
		mCaptchaEt.addTextChangedListener(this);
		
		mAgreeCb=(CheckBox)findViewById(R.id.mAgreeCb);
	}

	//初始化页面的标题信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_user_register));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:      //标题栏的返回功能
			finish();
			break;
		case R.id.mRegisterBtn:      //注册
			
			if(mUsernameEt.getText().toString().length()==0
			   ||mNicknameEt.getText().toString().length()==0
			   ||mPswEt.getText().toString().length()==0
			   ||mPswRepeatEt.getText().toString().length()==0
			   ||mCaptchaEt.getText().toString().length()==0){
			   MyToast.makeText(UserRegisterActivity.this, getString(R.string.please_input_info_completely), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(!mPswRepeatEt.getText().toString().equals(mPswEt.getText().toString())){
				MyToast.makeText(UserRegisterActivity.this, getString(R.string.prompt_psws_is_different), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(code.length()==0){
				MyToast.makeText(UserRegisterActivity.this, getString(R.string.prompt_get_code_firstly), MyToast.LENGTH_SHORT).show();
			}
			
			if(!mAgreeCb.isChecked()){
				MyToast.makeText(UserRegisterActivity.this, getString(R.string.prompt_agree_firstly), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			String content=formatContent();
			
			if(mRegisterAsyncTaskDataLoader!=null){
				mRegisterAsyncTaskDataLoader.canceled();
			}
			mRegisterAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_USER_REGISTER, UrlConfigs.SERVER_URL+UrlConfigs.GET_REGISTER_URL, null ,content);
			mRegisterAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mRegisterAsyncTaskDataLoader.execute();
			
			break;
		case R.id.mGetCodeBtn:      //获取短信验证码
			
			if(mMobileNumberEt.getText().toString().length()==0){
				MyToast.makeText(UserRegisterActivity.this, getString(R.string.please_input_tel_number), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(mGetCodeAsyncTaskDataLoader!=null){
				mGetCodeAsyncTaskDataLoader.canceled();
			}
			
			String url2=UrlConfigs.SERVER_URL+UrlConfigs.GET_CODE_URL+"?mobi="+mMobileNumberEt.getText().toString();
			mGetCodeAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_CODE, url2, null ,null);
			mGetCodeAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mGetCodeAsyncTaskDataLoader.execute();
			break;
		default:
			break;
		}
	}
	
	//格式化注册接口所要上传的json数据
    private String formatContent(){
		
		String str="{\"user\":"+"\""+mUsernameEt.getText().toString()+"\""+","
				   +"\"nnam\":"+"\""+mNicknameEt.getText().toString()+"\""+","
	               +"\"pswd_md5\":"+"\""+MD5Util.MD5(mPswEt.getText().toString())+"\""+","
	               +"\"mobi\":"+"\""+phoneNumber+"\""+","
	               +"\"mcod\":"+"\""+code+"\""+","
				   +"\"mcod_sesn\":"+"\""+mcodSesn+"\""+"}";
		return str;
	}

    //AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_USER_REGISTER){      //正在提交注册信息
			
			Utils.sendMessage(mHandler, USER_REGISTERING);
			
		}else if(flag==Flag.FLAG_GET_CODE){      //正在获取短信验证码
			
			Utils.sendMessage(mHandler, CODE_GETTING);
			
		}else if(flag==Flag.FLAG_CHECK_CODE){      //正在检查短信验证码
			
			Utils.sendMessage(mHandler, CODE_CHECKING);
			
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_USER_REGISTER){      //获取用户注册返回结果
			
			try {
				
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				String msg=totalJsonObject.getString("msg");
				
				Log.e("zyf","rc: "+rc+" msg: "+msg);
				
				if("0".equals(rc)){      //用户注册成功
					
					Utils.sendMessage(mHandler, USER_REGISTER_SUCCESS);
					
					return;
				}
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, USER_REGISTER_FAILED);      //用户注册失败
			
		}else if(flag==Flag.FLAG_GET_CODE){      //短信验证码获取返回结果
			
			try {
				
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				String msg=totalJsonObject.getString("msg");
				
				if("0".equals(rc)){      //短信验证码获取成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					mcodSesn=dataJsonObject.getString("mcod_sesn");
					
					Log.e("zyf"," msg: "+msg);
					
					code=msg;
					
					Utils.sendMessage(mHandler, CODE_GET_SUCCESS);
					
					return;
				}
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, CODE_GET_FAILED);      //短信验证码获取失败
			
		}else if(flag==Flag.FLAG_CHECK_CODE){      //获取短信验证码检查结果
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				String msg=totalJsonObject.getString("msg");
				
				if("0".equals(rc)){      //短信验证码检查通过
					
					Utils.sendMessage(mHandler, CODE_CHECK_SUCCESS);
					
					return;
				}
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, CODE_CHECK_FAILED);      //短信验证码检查不通过
		}
	}

	@Override
	public void afterTextChanged(Editable arg0) {
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
	}

	@Override
	public void onTextChanged(CharSequence str, int arg1, int arg2, int arg3) {
		
		if(str.length()==DataConfigs.LENGTH_CODE){      //如果短信验证码输入框内容长度为4，则自动进行短信验证码的验证
			
			code=str.toString();
			
			if(mCheckCodeAsyncTaskDataLoader!=null){
				mCheckCodeAsyncTaskDataLoader.canceled();
			}
			
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CHECK_CODE+"?mcod_sesn="+mcodSesn+"&mcod="+str;
			
			Log.e("zyf","code check url: "+url);
			
			mCheckCodeAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_CHECK_CODE, url, null, null);
			mCheckCodeAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mCheckCodeAsyncTaskDataLoader.execute();
		}
	}

}
