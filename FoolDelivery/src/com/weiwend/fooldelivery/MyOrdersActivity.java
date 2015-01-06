package com.weiwend.fooldelivery;

import com.weiwend.fooldelivery.fragments.SenderHistoryFragment;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MyOrdersActivity extends BaseActivity implements OnClickListener{
	
	//套用之前的“寄件历史”fragment
	private SenderHistoryFragment mSenderHistoryFragment;
	
	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_my_orders);
		
		FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
		mSenderHistoryFragment=new SenderHistoryFragment();
		transaction.add(R.id.contentLayout, mSenderHistoryFragment);
		
		transaction.commit();
	}

	//初始化当前页面的标题信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_my_orders));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:    //标题栏的返回功能
			
			finish();
			break;

		default:
			break;
		}
	}

}
