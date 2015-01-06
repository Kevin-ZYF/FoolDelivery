package com.weiwend.fooldelivery;

import org.json.JSONObject;

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
import android.widget.EditText;
import android.widget.TextView;

import com.weiwend.fooldelivery.configs.DataConfigs;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MD5Util;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class PswForgetActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,TextWatcher{
	
	//绑定手机号码、短信验证码、新密码、重复新密码输入框
	private EditText mOldMobileNumberEt,mCodeEt,mPswEt,mRepeatedPswEt;
	
	//“获取验证码”、“确定”按钮
	private Button mGetCodeBtn,mSummitBtn;
	
	//调用获取短信验证码接口后的各种状态信息
	private final int CODE_GETTING=1;
	private final int CODE_GET_SUCCESS=2;
	private final int CODE_GET_FAILED=3;
	
	//调用短信验证码验证接口后的各种状态信息
	private final int CODE_CHECKING=4;
	private final int CODE_CHECK_SUCCESS=5;
	private final int CODE_CHECK_FAILED=6;
	
	//调用密码找回接口后的各种状态信息
	private final int PSW_FORGET_SUMMITTING=7;
	private final int PSW_FORGET_SUMMIT_SUCCESS=8;
	private final int PSW_FORGET_SUMMIT_FAILED=9;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
            case CODE_GETTING:      //获取短信验证码的接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","code getting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_code_getting, PswForgetActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case CODE_GET_SUCCESS:      //获取短信验证码成功
				
				Log.e("zyf","code get success.....");
				
				mCodeEt.setText(code);
				
				mobileNumber=mOldMobileNumberEt.getText().toString();
				
				mGetCodeBtn.setEnabled(false);
				mGetCodeBtn.setText(getString(R.string.remain)+leftTime+" s");
				
				mHandler.postDelayed(mTimeCountRunnable, 1000);
				
				MyToast.makeText(PswForgetActivity.this, getString(R.string.code_get_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case CODE_GET_FAILED:      //获取短信验证码失败
				
				Log.e("zyf","code get failed.....");
				
				MyToast.makeText(PswForgetActivity.this, getString(R.string.code_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case CODE_CHECKING:      //短信验证码的验证接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","start check code...");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_check_code, PswForgetActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case CODE_CHECK_SUCCESS:      //短信验证码验证成功
				
				Log.e("zyf","start check code success...");
				
				mSummitBtn.setEnabled(true);
				
				MyToast.makeText(PswForgetActivity.this, getString(R.string.check_code_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case CODE_CHECK_FAILED:      //短信验证码验证失败
				
				Log.e("zyf","start check code failed...");
				
				mSummitBtn.setEnabled(false);
				
				MyToast.makeText(PswForgetActivity.this, getString(R.string.check_code_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
            case PSW_FORGET_SUMMITTING:      //密码找回的接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","psw forget summitting...");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_psw_forget_summit, PswForgetActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case PSW_FORGET_SUMMIT_SUCCESS:      //密码找回成功
				
				Log.e("zyf","psw forget summit success...");
				
				MyToast.makeText(PswForgetActivity.this, getString(R.string.psw_forget_summit_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				finish();
				
				break;
			case PSW_FORGET_SUMMIT_FAILED:      //密码找回失败
				
				Log.e("zyf","psw forget summit failed...");
				
				MyToast.makeText(PswForgetActivity.this, getString(R.string.psw_forget_summit_failed), MyToast.LENGTH_SHORT).show();
				
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
	
	//保存短信验证码
	private String code="";
	
	//保存绑定的手机号码
	private String mobileNumber="";
	
	//获取短信验证码成功后返回的sesn
	private String mcodSesn="";
	
	//新密码、重复的新密码
	private String psw,psw2;
	
	//获取短信验证码的异步加载类
	private AsyncTaskDataLoader mGetCodeAsyncTaskDataLoader;
	
	//短信验证码验证的异步加载类
	private AsyncTaskDataLoader mCheckCodeAsyncTaskDataLoader;
	
	//密码找回的异步加载类
	private AsyncTaskDataLoader mPswFindAsyncTaskDataLoader;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_psw_forget);
		
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
	
	private void initViews(){
		mOldMobileNumberEt=(EditText)findViewById(R.id.mOldMobileNumberEt);
		mCodeEt=(EditText)findViewById(R.id.mCodeEt);
		mPswEt=(EditText)findViewById(R.id.mPswEt);
		mRepeatedPswEt=(EditText)findViewById(R.id.mRepeatedPswEt);
		
		mCodeEt.addTextChangedListener(this);
		
		mGetCodeBtn=(Button)findViewById(R.id.mGetCodeBtn);
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		
		mGetCodeBtn.setOnClickListener(this);
		mSummitBtn.setOnClickListener(this);
	}

	//初始化页面的标题栏信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_psw_forget));
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_GET_CODE){      //正在获取短信验证码
			
			Utils.sendMessage(mHandler, CODE_GETTING);
			
		}else if(flag==Flag.FLAG_CHECK_CODE){      //正在验证短信验证码
			
			Utils.sendMessage(mHandler, CODE_CHECKING);
			
		}else if(flag==Flag.FLAG_PSW_FORGET){      //正在提交新的密码
			
			Utils.sendMessage(mHandler, PSW_FORGET_SUMMITTING);
			
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_GET_CODE){      //短信验证码获取返回结果
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				String msg=totalJsonObject.getString("msg");
				
				code=msg;
				
				if("0".equals(rc)){      //短信验证码获取成功
					
					JSONObject dataJsonObject=totalJsonObject.getJSONObject("data");
					mcodSesn=dataJsonObject.getString("mcod_sesn");
					
					Log.e("zyf"," msg: "+msg);
					
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
			
		}else if(flag==Flag.FLAG_PSW_FORGET){      //获取密码修改结果
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				String msg=totalJsonObject.getString("msg");
				
				if("0".equals(rc)){      //密码修改成功
					
					Utils.sendMessage(mHandler, PSW_FORGET_SUMMIT_SUCCESS);
					
					return;
				}
				
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, PSW_FORGET_SUMMIT_FAILED);      //密码修改失败
		}
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:       //标题栏的返回功能
			
			finish();
			
			break;
		case R.id.mGetCodeBtn:      //获取短信验证码
			
			if(mOldMobileNumberEt.getText().toString().length()==0){
				MyToast.makeText(PswForgetActivity.this, getString(R.string.please_input_tel_number), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(mOldMobileNumberEt.getText().toString().length()!=11){
				MyToast.makeText(PswForgetActivity.this, getString(R.string.prompt_phone_number_is_unlegal), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(mGetCodeAsyncTaskDataLoader!=null){
				mGetCodeAsyncTaskDataLoader.canceled();
			}
			
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CODE_URL+"?mobi="+mOldMobileNumberEt.getText().toString();
			mGetCodeAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_CODE, url, null ,null);
			mGetCodeAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mGetCodeAsyncTaskDataLoader.execute();
			break;
		case R.id.mSummitBtn:       //密码找回
			
			if(mCodeEt.getText().toString().length()!=4){
				MyToast.makeText(PswForgetActivity.this, getString(R.string.prompt_code_is_unlegal), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			psw=mPswEt.getText().toString();
			psw2=mRepeatedPswEt.getText().toString();
			
			if(psw.length()==0||psw2.length()==0){
				
				MyToast.makeText(PswForgetActivity.this, getString(R.string.please_input_info_completely), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(!psw.equals(psw2)){
				MyToast.makeText(PswForgetActivity.this, getString(R.string.prompt_psws_is_different), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(mPswFindAsyncTaskDataLoader!=null){
				mPswFindAsyncTaskDataLoader.canceled();
			}
			
			String content=formatContent();
			
			mPswFindAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_PSW_FORGET, UrlConfigs.SERVER_URL+UrlConfigs.GET_PSW_FORGET, null, content);
			mPswFindAsyncTaskDataLoader.setOnDataLoaderListener(this);
			mPswFindAsyncTaskDataLoader.execute();
			
			break;
		default:
			break;
		}
	}
	
	//格式化密码找回接口所要上传的json数据
	private String formatContent(){
		
		String str="{\"mcod_sesn\":"+"\""+mcodSesn+"\""+","
	               +"\"mcod\":"+"\""+code+"\""+","
	               +"\"mobi\":"+"\""+mobileNumber+"\""+","
				   +"\"pswd_md5\":"+"\""+MD5Util.MD5(psw)+"\""+"}";
		return str;
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
