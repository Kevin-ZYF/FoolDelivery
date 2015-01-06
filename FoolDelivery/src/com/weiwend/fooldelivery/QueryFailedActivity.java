package com.weiwend.fooldelivery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class QueryFailedActivity extends BaseActivity {
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_query_failed);
	}

	//初始化页面的标题栏信息
	@Override
	public void initTitleViews() {
		// TODO Auto-generated method stub
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				finish();
			}
		});
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_delivery_query_failed));

	}

}
