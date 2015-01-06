package com.weiwend.fooldelivery;

import org.json.JSONObject;

import android.content.Intent;
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

public class PswModifyActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,TextWatcher{
	
	//调用密码修改后的各种状态信息
	private final int PSW_MODIFYING=0;
	private final int PSW_MODIFY_SUCCESS=1;
	private final int PSW_MODIFY_FAILED=2;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case PSW_MODIFYING:    //密码修改的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","psw modifying......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_psw_modify, PswModifyActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case PSW_MODIFY_SUCCESS:   //密码修改成功
				
				Log.e("zyf","psw modify success......");
				
				Intent intent=new Intent();
				intent.putExtra("PswModifySuccess", true);
				setResult(ActivityResultCode.CODE_PSW_MODIFY,intent);
				
				finish();
            	
				break;
			case PSW_MODIFY_FAILED:    //密码修改失败
				
				Log.e("zyf","psw modify failed......");
				
				MyToast.makeText(PswModifyActivity.this, getString(R.string.psw_modify_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//密码修改的异步加载类
	private AsyncTaskDataLoader mPswModifyAsyncTaskDataLoader;
	
	//“更改密码”按钮
	private Button mSummitBtn;
	
	//“旧密码”、“新密码”、“确认密码”输入框
	private EditText mOldPswEt,mNewPswEt,mRepeatPswEt;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_psw_modify);
		
		initViews();
	}
	
	//初始化界面ui
	private void initViews(){
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mSummitBtn.setOnClickListener(this);
		
		mOldPswEt=(EditText)findViewById(R.id.mOldPswEt);
		mNewPswEt=(EditText)findViewById(R.id.mNewPswEt);
		mRepeatPswEt=(EditText)findViewById(R.id.mRepeatPswEt);
		
		mOldPswEt.addTextChangedListener(this);
		mNewPswEt.addTextChangedListener(this);
		mRepeatPswEt.addTextChangedListener(this);
	}

	//初始化页面的标题栏信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_psw_modify));
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_PSW_MODIFY){    //正在修改密码
			
			Utils.sendMessage(mHandler, PSW_MODIFYING);
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_PSW_MODIFY){    //获取密码修改的返回结果
			
			Log.e("zyf","psw modify result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //密码修改成功
					
					Utils.sendMessage(mHandler, PSW_MODIFY_SUCCESS);
					
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
			
			Utils.sendMessage(mHandler, PSW_MODIFY_FAILED);    //密码修改失败
		}
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:    //标题栏的返回功能
			finish();
			break;
		case R.id.mSummitBtn:    //“更改密码”
			
			if(mOldPswEt.getText().toString().length()==0
			||mNewPswEt.getText().toString().length()==0
			||mRepeatPswEt.getText().toString().length()==0){
				MyToast.makeText(PswModifyActivity.this, getString(R.string.please_input_info_completely), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(!mNewPswEt.getText().toString().equals(mRepeatPswEt.getText().toString())){
				MyToast.makeText(PswModifyActivity.this, getString(R.string.prompt_psws_is_different), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(mNewPswEt.getText().toString().length()<6||mOldPswEt.getText().toString().length()<6){
				MyToast.makeText(PswModifyActivity.this, getString(R.string.prompt_psws_length_is_unlegal), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			if(mNewPswEt.getText().toString().equals(mOldPswEt.getText().toString())){
				MyToast.makeText(PswModifyActivity.this, getString(R.string.prompt_new_psw_is_equal_to_old), MyToast.LENGTH_SHORT).show();
				return;
			}
			
			String old_pswd_md5=MD5Util.MD5(mOldPswEt.getText().toString());
			String new_pswd_md5=MD5Util.MD5(mNewPswEt.getText().toString());
			
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_PSW_MODIFY_URL+"?sesn="+MyApplicaition.sesn+"&old_pswd_md5="+old_pswd_md5+"&new_pswd_md5="+new_pswd_md5;
			
			if(mPswModifyAsyncTaskDataLoader!=null){
				mPswModifyAsyncTaskDataLoader.canceled();
			}
			
			mPswModifyAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_PSW_MODIFY, url, null, null);
			mPswModifyAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mPswModifyAsyncTaskDataLoader.execute();
			
			break;
		default:
			break;
		}
	}

	@Override
	public void afterTextChanged(Editable arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
			int arg3) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub
		
		updateSummitBtnStatus();
	}
	
	//用来更新“更改密码”按钮的状态
	private void updateSummitBtnStatus(){
		
		mSummitBtn.setEnabled(false);
		
		if(mOldPswEt.getText().toString().length()==0
		    ||mNewPswEt.getText().toString().length()==0
			||mRepeatPswEt.getText().toString().length()==0){
			
			return;
		}
				
		if(!mNewPswEt.getText().toString().equals(mRepeatPswEt.getText().toString())){
			return;
		}
				
		if(mNewPswEt.getText().toString().length()<6||mOldPswEt.getText().toString().length()<6){
			return;
		}
				
		if(mNewPswEt.getText().toString().equals(mOldPswEt.getText().toString())){
			return;
		}
		
		mSummitBtn.setEnabled(true);
	}

}
