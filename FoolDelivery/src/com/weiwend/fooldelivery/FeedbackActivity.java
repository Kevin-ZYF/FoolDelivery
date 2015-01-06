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

import com.weiwend.fooldelivery.configs.UrlConfigs;
import com.weiwend.fooldelivery.customviews.MyToast;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader;
import com.weiwend.fooldelivery.internet.AsyncTaskDataLoader.onDataLoaderListener;
import com.weiwend.fooldelivery.utils.ActivityResultCode;
import com.weiwend.fooldelivery.utils.Flag;
import com.weiwend.fooldelivery.utils.MyProgressDialogUtils;
import com.weiwend.fooldelivery.utils.Utils;

public class FeedbackActivity extends BaseActivity implements OnClickListener,onDataLoaderListener{
	
	//反馈意见、快递单号、姓名、联系方式
	private EditText mSuggestionsEt,mSnumEt,mNameEt,mTelEt;
	
	//"确定"按钮、"从查询历史中选择"按钮
	private Button mSummitBtn,mSelectFromHistoryBtn;
	
	//调用意见反馈接口后的各种状态信息
	private final int FEEDBACKING=0;
	private final int FEEDBACK_SUCCESS=1;
	private final int FEEDBACK_FAILED=2;
	
	//耗时操作时的loading对话框
	private MyProgressDialogUtils mProgressDialogUtils;
	
	//根据各种状态信息更新ui等
	private Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
            case FEEDBACKING:       //意见反馈的接口已经调用，正在等待服务器端的回复
            	
				Log.e("zyf","feedbacking......");
				
				mProgressDialogUtils=new MyProgressDialogUtils(R.string.dialog_feedback, FeedbackActivity.this);
				mProgressDialogUtils.showDialog();
				
				break;
			case FEEDBACK_SUCCESS:      //意见反馈成功
				
				Log.e("zyf","feedback success......");
				
                MyToast.makeText(FeedbackActivity.this, getString(R.string.feedback_success), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				finish();
            	
				break;
			case FEEDBACK_FAILED:      //意见反馈失败
				
				Log.e("zyf","feedback failed......");
				
                MyToast.makeText(FeedbackActivity.this, getString(R.string.feedback_failed), MyToast.LENGTH_SHORT).show();
				
				mProgressDialogUtils.dismissDialog();
				
				break;
			default:
				break;
			}
		}
		
	};
	
	//用于意见反馈的异步加载类
	private AsyncTaskDataLoader mFeedbackAsyncTaskDataLoader;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_feedback);
		
		initviews();
	}
	
	//初始化ui
	private void initviews(){
		
		mSuggestionsEt=(EditText)findViewById(R.id.mSuggestionsEt);
		mSnumEt=(EditText)findViewById(R.id.mSnumEt);
		mNameEt=(EditText)findViewById(R.id.mNameEt);
		mTelEt=(EditText)findViewById(R.id.mTelEt);
		
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mSummitBtn.setOnClickListener(this);
		
		mSelectFromHistoryBtn=(Button)findViewById(R.id.mSelectFromHistoryBtn);
		mSelectFromHistoryBtn.setOnClickListener(this);
		
		mSuggestionsEt.addTextChangedListener(new TextWatcher() {
			
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
        
		mSnumEt.addTextChangedListener(new TextWatcher() {
			
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

	//初始化页面的标题信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_feedback));
	}
	
	//用于更新“确定”按钮的状态，只有当必要信息都完整时，用户才可点击"确定"按钮
	private void updateSummitBtnStatus(){
		
		if(mSnumEt.getText().toString().length()>0&&mSuggestionsEt.getText().toString().length()>0){
			mSummitBtn.setEnabled(true);
		}else{
			mSummitBtn.setEnabled(false);
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要处理访问服务器接口前的操作
	@Override
	public void start(int flag) {
		
		if(flag==Flag.FLAG_FEEDBACK){   //开始意见反馈
			Utils.sendMessage(mHandler, FEEDBACKING);   
		}
	}

	//AsyncTaskDataLoader类的回调函数，主要获取服务器端接口的返回数据
	@Override
	public void completed(int flag, String result) {
		
		if(flag==Flag.FLAG_FEEDBACK){   //获取意见反馈返回结果
			
			Log.e("zyf", "feedback result: "+result);
			
			try {
				JSONObject totalJsonObject=new JSONObject(result);
				
				String rc=totalJsonObject.getString("rc");
				
				if("0".equals(rc)){   //意见反馈成功
					
					Utils.sendMessage(mHandler, FEEDBACK_SUCCESS);
					
					return;
				}else if(ActivityResultCode.INVALID_SESN.equals(rc)){  //sesn过期,需要重新登录
					
					MyApplicaition.sesn="";
					MyApplicaition.mUserName="";
					
					Intent intent=new Intent(this, UserLoginActivity.class);
					startActivityForResult(intent, ActivityResultCode.CODE_LOGIN);
				}
				
			}catch(Exception e){
				Log.e("zyf",e.toString());
			}
			
			Utils.sendMessage(mHandler, FEEDBACK_FAILED);  //意见反馈失败
		}
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:   //标题栏的返回功能
			finish();
			break;
		case R.id.mSummitBtn:    //调用意见反馈的接口
			
			String type="0";
			String suggestion=mSuggestionsEt.getText().toString();
			String snum=mSnumEt.getText().toString();
			
			if(suggestion.length()==0||snum.length()==0){
				MyToast.makeText(FeedbackActivity.this, getString(R.string.please_input_info_completely_feedback), MyToast.LENGTH_SHORT).show();
				
				return;
			}
			
			String content=formatContent(MyApplicaition.sesn, type, suggestion);
			
			if(mFeedbackAsyncTaskDataLoader!=null){
				mFeedbackAsyncTaskDataLoader.canceled();
			}
			
			mFeedbackAsyncTaskDataLoader=new AsyncTaskDataLoader(false, Flag.FLAG_FEEDBACK, UrlConfigs.SERVER_URL+UrlConfigs.GET_FEEDBACK_URL, null, content);
			mFeedbackAsyncTaskDataLoader.setOnDataLoaderListener(this);
			mFeedbackAsyncTaskDataLoader.execute();
			
			break;
		case R.id.mSelectFromHistoryBtn:   //从查询历史中选择单号
			
			Intent intent=new Intent(FeedbackActivity.this,DeliveryQueryHistorySelectActivity.class);
			startActivityForResult(intent, ActivityResultCode.CODE_DELIVERY_HISTORY_SELECTOR);
			break;
		default:
			break;
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		if(requestCode==ActivityResultCode.CODE_DELIVERY_HISTORY_SELECTOR){
			
			if(data!=null){   //获取从历史查询记录中的单号
				
				Log.e("zyf","snum: "+data.getStringExtra("snum"));
				
				mSnumEt.setText(data.getStringExtra("snum"));
				
				mSnumEt.setSelection(mSnumEt.getText().toString().length());
			}
		}else if(requestCode==ActivityResultCode.CODE_LOGIN){
			
			if(data!=null){
				
				if(data.getBooleanExtra("loginStatus", false)){   //用户登录成功
					Log.e("zyf","login status : true");
				}
			}
		}
	}
	
	//格式化“意见反馈”接口的json数据
	private String formatContent(String sesn,String type,String content){
		String str="{\"sesn\":"+"\""+sesn+"\""+","
	               +"\"type\":"+"\""+type+"\""+","
	               +"\"snum\":"+"\""+mSnumEt.getText().toString()+"\""+","
	               +"\"name\":"+"\""+mNameEt.getText().toString()+"\""+","
	               +"\"tel\":"+"\""+mTelEt.getText().toString()+"\""+","
				   +"\"content\":"+"\""+content+"\""+"}";
		return str;
	}

}
