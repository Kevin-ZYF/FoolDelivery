package com.weiwend.fooldelivery;

import com.weiwend.fooldelivery.utils.SpUtils;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SettingDisturbActivity extends BaseActivity implements OnClickListener{
	
	//免打扰设置的三个选项
	private RelativeLayout mContainer1,mContainer2,mContainer3;
	
	//用于显示当前的设置项，有且仅有一个显示
	private Button mArrow0Btn,mArrow1Btn,mArrow2Btn;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_setting_third);
		
		mContainer1=(RelativeLayout)findViewById(R.id.mContainer1);
		mContainer2=(RelativeLayout)findViewById(R.id.mContainer2);
		mContainer3=(RelativeLayout)findViewById(R.id.mContainer3);
		
		mContainer1.setOnClickListener(this);
		mContainer2.setOnClickListener(this);
		mContainer3.setOnClickListener(this);
		
		mArrow0Btn=(Button)findViewById(R.id.mArrow0Btn);
		mArrow1Btn=(Button)findViewById(R.id.mArrow1Btn);
		mArrow2Btn=(Button)findViewById(R.id.mArrow2Btn);
		
		//根据用户的设置值初始化界面，均通过SharePreferrence保存设置值
		int index=SpUtils.getMessageReceiveTime(this);
		if(index==0){
			mArrow0Btn.setVisibility(View.VISIBLE);
		}else if(index==1){
			mArrow1Btn.setVisibility(View.VISIBLE);
		}else{
			mArrow2Btn.setVisibility(View.VISIBLE);
		}
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
		case R.id.leftBtn:       //标题栏的返回功能
			
			finish();
			break;
		case R.id.mContainer1:   //"全天接收消息提醒"
			
			SpUtils.saveMessageReceiveTime(SettingDisturbActivity.this, 0);
			
			finish();
			break;
		case R.id.mContainer2:   //"只在8:00-22:00之间接收消息提醒"
			
			SpUtils.saveMessageReceiveTime(SettingDisturbActivity.this, 1);
			
			finish();
			break;
		case R.id.mContainer3:   //"不接收消息提醒"
			
			SpUtils.saveMessageReceiveTime(SettingDisturbActivity.this, 2);
			
			finish();
			break;

		default:
			break;
		}
	}

}
