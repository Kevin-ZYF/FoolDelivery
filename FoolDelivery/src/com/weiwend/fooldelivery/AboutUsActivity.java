package com.weiwend.fooldelivery;

import com.weiwend.fooldelivery.utils.Utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class AboutUsActivity extends BaseActivity implements OnClickListener{
	
	//显示app版本信息
	private TextView mAppInfoTv;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_about_us);
		
		mAppInfoTv=(TextView)findViewById(R.id.mAppInfoTv);
		
		mAppInfoTv.setText(Utils.getVersion(this));
	}

	//初始化当前页面的标题，以及监听标题左端的返回功能
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_about_us));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		
		case R.id.leftBtn:    //页面标题的返回功能
			
			finish();
			
			break;

		default:
			break;
		}
	}

}
