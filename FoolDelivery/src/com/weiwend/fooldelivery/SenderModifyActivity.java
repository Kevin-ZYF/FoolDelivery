package com.weiwend.fooldelivery;

import com.weiwend.fooldelivery.fragments.SenderFragment;
import com.weiwend.fooldelivery.items.AddressItem;
import com.weiwend.fooldelivery.items.SendInfoItem;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SenderModifyActivity extends BaseActivity implements OnClickListener{
	
	//套用之前的“新寄件”fragment
	private SenderFragment mSenderFragment;

	@Override
	protected void onCreate(Bundle arg0) {

		super.onCreate(arg0);
		
		setContentView(R.layout.activity_sender_modify);
		
		//获取需要编辑的订单对象
		SendInfoItem mDeliveryInfoItem=(SendInfoItem) getIntent().getSerializableExtra("DeliveryInfoItem");
		
		FragmentTransaction transaction =getSupportFragmentManager().beginTransaction();
		mSenderFragment=new SenderFragment();
		Bundle bundle=new Bundle();
		bundle.putBoolean("isDeliveryInfoModify", true);
		bundle.putSerializable("DeliveryInfoItem", mDeliveryInfoItem);    //将需要编辑的订单对象传递给SenderFragment进行处理
		mSenderFragment.setArguments(bundle);
		transaction.add(R.id.contentLayout, mSenderFragment);
		
		transaction.commit();
	}

	//初始化当前页面的标题信息
	@Override
	public void initTitleViews() {
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
	    mTitleView.setText(getString(R.string.title_send_modify));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:      //标题栏的返回功能
			
			finish();
			break;

		default:
			break;
		}
	}

}
