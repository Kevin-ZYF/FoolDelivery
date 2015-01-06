package com.weiwend.fooldelivery;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyNicknameModifyDialog;
import com.weiwend.fooldelivery.customviews.MyNicknameModifyDialog.MySubmmitListener;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.items.ParityCompanyItem;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.SpUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class UserLoginedActivity extends BaseActivity implements OnClickListener,onDataLoaderListener,MySubmmitListener{
	
	//显示用户绑定的手机号码
	private TextView mBindedMobileNumberTv;
	
	//绑定的手机号码、修改账号密码、修改昵称的Container
	private RelativeLayout mBindedMobileNumberContainer,mPswModifyContainer,mNicknameModifyContainer;
	
	//“退出登录”按钮
	private Button mExitLoginBtn;
	
	//修改昵称对话框
	private MyNicknameModifyDialog mNicknameModifyDialog;
	
	//调用退出登录接口后的各种状态信息
	private final int EXITING=0;
	private final int EXIT_SUCCESS=1;
	private final int EXIT_FAILED=2;
	
	//调用修改昵称接口后的各种状态信息
	private final int NICKNAME_MODIFYING=3;
	private final int NICKNAME_MODIFY_SUCCESS=4;
	private final int NICKNAME_MODIFY_FAILED=5;
	
	//耗时操作的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case EXITING:    //退出登录的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","exiting......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_exit, UserLoginedActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
				
			case EXIT_SUCCESS:    //退出登录成功
				
				Log.e("zyf","exit success......");
				
				MyApplicaition.sesn="";
				MyApplicaition.mUserName="";
				
				//取消自动登录
				SpUtils.saveIsAutoLogin(UserLoginedActivity.this, false);
				
				MyToast.makeText(UserLoginedActivity.this, getString(R.string.exit_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				Intent intent=new Intent();
				intent.putExtra("loginStatus", true);
				setResult(ActivityResultCode.CODE_LOGIN,intent);
				finish();
            	
				break;
			case EXIT_FAILED:    //退出登录失败
				
				Log.e("zyf","exit failed......");
				
				MyToast.makeText(UserLoginedActivity.this, getString(R.string.exit_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			case NICKNAME_MODIFYING:    //修改昵称的接口已经调用，正在等待服务器端的回复
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_nickname_modify, UserLoginedActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case NICKNAME_MODIFY_SUCCESS:    //修改昵称成功
				
				MyApplicaition.mNickName=nickname;
				
				mNicknameModifyDialog.dismiss();
				
                MyToast.makeText(UserLoginedActivity.this, getString(R.string.nickname_modify_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				Intent intent2=new Intent();
				intent2.setAction(ActivityResultCode.ACTION_NICKNAME_MODIFY);
				sendBroadcast(intent2);
				
				finish();
							
				break;
			case NICKNAME_MODIFY_FAILED:    //修改昵称失败
				
                MyToast.makeText(UserLoginedActivity.this, getString(R.string.nickname_modify_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//退出登录的异步加载类
	private AsyncTaskDataLoader mExitAsyncTaskDataLoader;
	
	//昵称修改的异步加载类
	private AsyncTaskDataLoader mNicknameModifyAsyncTaskDataLoader;
	
	//保存用户输入的新的昵称
	private String nickname="";

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_user_logined);
		
		initViews();
	}

	//刷新用户绑定的手机号
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		
		mBindedMobileNumberTv.setText(getString(R.string.binded)+MyApplicaition.bindedMobileNumber);
	}

	//初始化ui
	private void initViews(){
		mBindedMobileNumberTv=(TextView)findViewById(R.id.mBindedMobileNumberTv);
		
		mBindedMobileNumberTv.setText(getString(R.string.binded)+MyApplicaition.bindedMobileNumber);
		
		mBindedMobileNumberContainer=(RelativeLayout)findViewById(R.id.mBindedMobileNumberContainer);
		mPswModifyContainer=(RelativeLayout)findViewById(R.id.mPswModifyContainer);
		mNicknameModifyContainer=(RelativeLayout)findViewById(R.id.mNicknameModifyContainer);
		
		mExitLoginBtn=(Button)findViewById(R.id.mExitLoginBtn);
		
		mBindedMobileNumberContainer.setOnClickListener(this);
		mPswModifyContainer.setOnClickListener(this);
		mNicknameModifyContainer.setOnClickListener(this);
		mExitLoginBtn.setOnClickListener(this);
	}

	//初始化标题栏信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_user_login));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:   //标题栏的返回功能
			finish();
			break;
		case R.id.mBindedMobileNumberContainer:    //修改绑定的手机号码
			
			Intent intent2=new Intent(this,BindedMobileNumberModifyActivity.class);
			startActivity(intent2);
			
			break;
		case R.id.mPswModifyContainer:    //修改账号密码
			
			Intent intent=new Intent(this,PswModifyActivity.class);
			startActivityForResult(intent, ActivityResultCode.CODE_PSW_MODIFY);
			
			break;
		case R.id.mExitLoginBtn:    //退出登录
			
			if(mExitAsyncTaskDataLoader!=null){
				mExitAsyncTaskDataLoader.canceled();
			}
			
			//调用退出登录的接口
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_EXIT_URL+"?sesn="+MyApplicaition.sesn;
			mExitAsyncTaskDataLoader=new AsyncTaskDataLoader(true, Flag.FLAG_EXIT, url, null, null);
			mExitAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mExitAsyncTaskDataLoader.execute();
			break;
		case R.id.mNicknameModifyContainer:    //修改昵称
			
			mNicknameModifyDialog=new MyNicknameModifyDialog(UserLoginedActivity.this, R.style.MyDialog,MyApplicaition.mNickName);
			mNicknameModifyDialog.setMySubmmitListener(this);
			mNicknameModifyDialog.show();
			
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_PSW_MODIFY){ //密码修改成功，退出本页面，提示用户重新登录
			
			if(data!=null){
				
				if(data.getBooleanExtra("PswModifySuccess", false)){
					Log.e("zyf","back,psw modify success......");
					
	                SpUtils.saveIsAutoLogin(UserLoginedActivity.this, false);
					
					MyToast.makeText(UserLoginedActivity.this, getString(R.string.psw_modify_success), MyToast.LENGTH_SHORT).show();
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent();
					intent.setAction(ActivityResultCode.ACTION_PSW_MODIFY);
					sendBroadcast(intent);
					
					finish();
				}
			}
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_EXIT){    //正在退出登录
			
			Utils.sendMessage(mHandler, EXITING);
			
		}else if(flag==Flag.FLAG_NICKNAME_MODIFY){   //正在修改昵称
			
			Utils.sendMessage(mHandler, NICKNAME_MODIFYING);
			
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_EXIT){    //获取退出登录的返回结果
			
			Log.e("zyf","exit result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){   //退出登录成功
					
					Utils.sendMessage(mHandler, EXIT_SUCCESS);
					
					return;
				}
				
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, EXIT_FAILED);    //退出登录失败
			
		}else if(flag==Flag.FLAG_NICKNAME_MODIFY){    //获取昵称修改的返回结果
			
			Log.e("zyf","nickname modify result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){    //昵称修改成功
					
					Utils.sendMessage(mHandler, NICKNAME_MODIFY_SUCCESS);
					
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
			
			Utils.sendMessage(mHandler, NICKNAME_MODIFY_FAILED);    //昵称修改失败
		}
	}

	//昵称修改对话框的“确定”按钮的点击回调函数
	@Override
	public void summit(String content) {
		
		Log.e("zyf", "nickname: "+content);
		
		if(content.length()>0){
			
			if(content.equals(MyApplicaition.mNickName)){
				
				mNicknameModifyDialog.dismiss();
				return;
			}
			
			nickname=content;
			
			//调用昵称修改的接口
			String url=UrlConfigs.SERVER_URL+UrlConfigs.GET_NICK_NAME_MODIFY_URL;
			mNicknameModifyAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_NICKNAME_MODIFY, url, null, formatContent(content));
			mNicknameModifyAsyncTaskDataLoader.setOnDataLoaderListener(this);
			
			mNicknameModifyAsyncTaskDataLoader.execute();
		}
	}
	
	//格式化昵称修改接口所要上传的json数据
    private String formatContent(String nickname){
		
		String str="{\"sesn\":"+"\""+MyApplicaition.sesn+"\""+","
				   +"\"nnam\":"+"\""+nickname+"\""+"}";
		return str;
	}

}
