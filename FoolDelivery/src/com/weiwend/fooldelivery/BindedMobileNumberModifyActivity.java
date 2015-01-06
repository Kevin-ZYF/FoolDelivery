package com.weiwend.fooldelivery;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hp.hpl.sparta.Text;
import com.weiwend.fooldelivery.configs.DataConfigs;
import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class BindedMobileNumberModifyActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,TextWatcher{
	
	//调用获取短信验证码接口后的各种状态信息
	private final int CODE_GETTING=1;
	private final int CODE_GET_SUCCESS=2;
	private final int CODE_GET_FAILED=3;
	
	//调用短信验证码验证接口后的各种状态信息
	private final int CODE_CHECKING=4;
	private final int CODE_CHECK_SUCCESS=5;
	private final int CODE_CHECK_FAILED=6; 
	
	//调用绑定新手机号码接口后的各种状态信息
	private final int MOBILE_NUMBER_BINDING=7;
	private final int MOBILE_NUMBER_BIND_SUCCESS=8;
	private final int MOBILE_NUMBER_BIND_FAILED=9;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			
            case CODE_GETTING:    //获取短信验证码的接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","code getting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_code_getting, BindedMobileNumberModifyActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case CODE_GET_SUCCESS:    //获取短信验证码成功
				
				Log.e("zyf","code get success.....");
				
				mCodeEt.setText(code);
				
				mGetCodeBtn.setEnabled(false);
				mGetCodeBtn.setText(getString(R.string.remain)+leftTime+" s");
				
				mHandler.postDelayed(mTimeCountRunnable, 1000);
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.code_get_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case CODE_GET_FAILED:    //获取短信验证码失败
				
				Log.e("zyf","code get failed.....");
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.code_get_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case CODE_CHECKING:    //短信验证码的验证接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","start check code...");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_check_code, BindedMobileNumberModifyActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case CODE_CHECK_SUCCESS:    //短信验证码验证成功
				
				Log.e("zyf","start check code success...");
				
				mSummitBtn.setEnabled(true);
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.check_code_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case CODE_CHECK_FAILED:    //短信验证码验证失败
				
				Log.e("zyf","start check code failed...");
				
				mSummitBtn.setEnabled(false);
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.check_code_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
            case MOBILE_NUMBER_BINDING:    //绑定新手机号码的接口已经调用，正在等待服务器端的回复
				
				Log.e("zyf","new moblie number binding...");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_bind_mobile_number, BindedMobileNumberModifyActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case MOBILE_NUMBER_BIND_SUCCESS:    //新手机号码绑定成功
				
				Log.e("zyf","bind new moblie number success...");
				
				String str=mNewMobileNumberEt.getText().toString();
		        MyApplicaition.bindedMobileNumber=str.substring(0, 3)+"****"+str.substring(str.length()-4, str.length());

				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.mobile_number_bind_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				finish();
				
				break;
			case MOBILE_NUMBER_BIND_FAILED:    //新手机号码绑定失败
				
				Log.e("zyf","bind new moblie number failed...");
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.mobile_number_bind_failed), MyToast.LENGTH_SHORT).show();
				
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
			}else{
				mGetCodeBtn.setEnabled(true);
				mGetCodeBtn.setText(getString(R.string.code_get));
				
				leftTime=DataConfigs.MAX_TIME_CODE_WAIT;
			}
		}
	};
	
	//保存输入的短信验证码
	private String code="";
	
	//保存获取短信验证码成功后的sesn
	private String mcodSesn="";
	
	//获取短信验证码的异步加载类
	private AsyncTaskDataLoader mGetCodeAsyncTaskDataLoader;
	
	//短信验证码验证的异步加载类
	private AsyncTaskDataLoader mCheckCodeAsyncTaskDataLoader;
	
	//绑定新手机号码的异步加载类
	private AsyncTaskDataLoader mBindMobileNumberAsyncTaskDataLoader;
	
	//旧手机号、新手机号、短信验证码输入框
	private EditText mOldMobileNumberEt,mNewMobileNumberEt,mCodeEt;
	
	//“确定”、“获取验证码”按钮
	private Button mSummitBtn,mGetCodeBtn;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_binded_mobile_number_modify);
		
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
	
	//初始化ui
	private void initViews(){
		mOldMobileNumberEt=(EditText)findViewById(R.id.mOldMobileNumberEt);
		mNewMobileNumberEt=(EditText)findViewById(R.id.mNewMobileNumberEt);
		mCodeEt=(EditText)findViewById(R.id.mCodeEt);
		
		mCodeEt.addTextChangedListener(this);
		
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mGetCodeBtn=(Button)findViewById(R.id.mGetCodeBtn);
		
		mSummitBtn.setOnClickListener(this);
		mGetCodeBtn.setOnClickListener(this);
	}

	//初始化页面的标题栏信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_binded_mobile_number_modify));
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_GET_CODE){    //正在获取短信验证码
			
			Utils.sendMessage(mHandler, CODE_GETTING);
			
		}else if(flag==Flag.FLAG_CHECK_CODE){    //正在验证短信验证码
			
			Utils.sendMessage(mHandler, CODE_CHECKING);
			
		}else if(flag==Flag.FLAG_MOBILE_NUMBER_BIND){    //正在绑定新的手机号码
			
			Utils.sendMessage(mHandler, MOBILE_NUMBER_BINDING);
			
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_GET_CODE){    //短信验证码获取返回结果
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				String msg=totalJsonObject.getString("msg");
				
				if("0".equals(rc)){    //短信验证码获取成功
					
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
			
			Utils.sendMessage(mHandler, CODE_GET_FAILED);    //短信验证码获取失败
			
		}else if(flag==Flag.FLAG_CHECK_CODE){    //获取短信验证码检查结果
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				String msg=totalJsonObject.getString("msg");
				
				if("0".equals(rc)){    //短信验证码检查通过
					
					Utils.sendMessage(mHandler, CODE_CHECK_SUCCESS);
					
					return;
				}
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, CODE_CHECK_FAILED);    //短信验证码检查不通过
			
		}else if(flag==Flag.FLAG_MOBILE_NUMBER_BIND){     //获取绑定手机号码返回结果
			
			Log.e("zyf","bind result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				String msg=totalJsonObject.getString("msg");
				
				if("0".equals(rc)){    //绑定手机号码成功
					
					Utils.sendMessage(mHandler, MOBILE_NUMBER_BIND_SUCCESS);
					
					return;
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期，重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(this, UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, MOBILE_NUMBER_BIND_FAILED);    //绑定手机号码失败
		}
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:    //n标题栏的返回功能
			
			finish();
			break;
		case R.id.mGetCodeBtn:    //获取短信验证码
			
			if(mNewMobileNumberEt.getText().toString().length()!=11){
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.prompt_phone_number_is_unlegal), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(mNewMobileNumberEt.getText().toString().equals(MyApplicaition.bindedMobileNumberWithNoHide)){
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.prompt_new_phone_number_is_equal_to_old), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(mGetCodeAsyncTaskDataLoader!=null){
				mGetCodeAsyncTaskDataLoader.canceled();
			}
			
			//调用获取短信验证码的接口
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_CODE_URL+"?mobi="+mNewMobileNumberEt.getText().toString();
			mGetCodeAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_GET_CODE, url, null ,null);
			mGetCodeAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mGetCodeAsyncTaskDataLoader.execute();
			
			break;
		case R.id.mSummitBtn:  //点击“确定”按钮
			
            if(mNewMobileNumberEt.getText().toString().length()!=11){
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.prompt_new_phone_number_is_unlegal), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(mOldMobileNumberEt.getText().toString().length()!=11){
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.prompt_old_phone_number_is_unlegal), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
            if(mCodeEt.getText().toString().length()!=4){
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.prompt_code_is_unlegal), MyToast.LENGTH_SHORT).show();
				
				return;
			}
            
            if(mNewMobileNumberEt.getText().toString().equals(mOldMobileNumberEt.getText().toString())){
				
				MyToast.makeText(BindedMobileNumberModifyActivity.this, getString(R.string.prompt_new_phone_number_is_equal_to_old), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			if(mBindMobileNumberAsyncTaskDataLoader!=null){
				mBindMobileNumberAsyncTaskDataLoader.canceled();
			}
			
			//调用更改绑定手机号的接口
			String url2=UrlConfigs.SERVER_URL+UrlConfigs.GET_BIND_MOBILE_NUMBER
					+"?sesn="+MyApplicaition.sesn
					+"&old_mobi="+mOldMobileNumberEt.getText().toString()
					+"&new_mobi="+mNewMobileNumberEt.getText().toString()
					+"&mcod="+code
					+"&mcod_sesn="+mcodSesn;
			
			mBindMobileNumberAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_MOBILE_NUMBER_BIND, url2, null, null);
			mBindMobileNumberAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mBindMobileNumberAsyncTaskDataLoader.execute();
			
			break;
		default:
			break;
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
		
        if(str.length()==DataConfigs.LENGTH_CODE){    //如果短信验证码输入框内容长度为4，则自动进行短信验证码的验证
			
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
