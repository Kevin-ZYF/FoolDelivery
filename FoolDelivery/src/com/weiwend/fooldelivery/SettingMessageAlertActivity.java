package com.weiwend.fooldelivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.weiwend.fooldelivery.customviews.MyToggleButton;
import com.weiwend.fooldelivery.customviews.MyToggleButton.OnToggleClickListener;
import com.weiwend.fooldelivery.utils.SpUtils;

public class SettingMessageAlertActivity extends BaseActivity implements OnClickListener{
	
	//免打扰设置
	private RelativeLayout mContainer1;
	
	//声音、震动设置按钮
	private MyToggleButton mVoiceToggleButton,mSharkToggleButton;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_setting_second);
		
		initViews();
	}
	
	//初始化ui
	private void initViews(){
		
		mContainer1=(RelativeLayout)findViewById(R.id.mContainer1);
		mContainer1.setOnClickListener(this);
		
		mVoiceToggleButton=(MyToggleButton)findViewById(R.id.mVoiceToggleButton);
		mSharkToggleButton=(MyToggleButton)findViewById(R.id.mSharkToggleButton);
		
		mVoiceToggleButton.setOnToggleClickListener(new OnToggleClickListener() {
			
			@Override
			public void onClick(boolean enable) {
				
				//通过SharePrefrence保存用户设置的值
				SpUtils.enableVoice(SettingMessageAlertActivity.this, enable);
			}
		});
		
		mSharkToggleButton.setOnToggleClickListener(new OnToggleClickListener() {
			
			@Override
			public void onClick(boolean enable) {
				
				//通过SharePrefrence保存用户设置的值
				SpUtils.enableShark(SettingMessageAlertActivity.this, enable);
			}
		});
		
		//初始化已经保存的用户设置
		mVoiceToggleButton.setToggleStatus(SpUtils.getVoiceStatus(SettingMessageAlertActivity.this));
		mSharkToggleButton.setToggleStatus(SpUtils.getSharkStatus(SettingMessageAlertActivity.this));
	}

	//初始化页面的标题信息
	@Override
	public void initTitleViews() {
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.system_settings));
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()) {
		case R.id.leftBtn:   //标题栏的返回功能
			finish();
			break;
		case R.id.mContainer1:  //免打扰设置
			
			Intent intent=new Intent(this,SettingDisturbActivity.class);
			startActivity(intent);
			break;
		default:
			break;
		}
	}

}
