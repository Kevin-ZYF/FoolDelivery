package com.weiwend.fooldelivery;

import com.weiwend.fooldelivery.customviews.MyPhonePromptDialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SendSuccessActivity extends BaseActivity implements OnClickListener{
	
	//"继续下单"、“历史寄件”
	private TextView mContinueSendTv,mSendHistoryTv;

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		
		setContentView(R.layout.activity_send_success);
		
		TextView mPhoneTv=(TextView)findViewById(R.id.mPhoneTv);
		mPhoneTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mPhoneTv.setOnClickListener(this);
		
		mContinueSendTv=(TextView)findViewById(R.id.mContinueSendTv);
		mContinueSendTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mContinueSendTv.setOnClickListener(this);
		
		mSendHistoryTv=(TextView)findViewById(R.id.mSendHistoryTv);
		mSendHistoryTv.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
		mSendHistoryTv.setOnClickListener(this);
	}

	//初始化页面的标题栏信息
	@Override
	public void initTitleViews() {
		
		Button leftBtn=(Button)findViewById(R.id.leftBtn);
		leftBtn.setOnClickListener(this);
		
		TextView mTitleView=(TextView)findViewById(R.id.mTitleView);
		mTitleView.setText(getString(R.string.title_send_success));
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.leftBtn:    //标题栏的返回功能
			
			finish();
			
			break;
        case R.id.mPhoneTv:    //联系客服
        	
        	MyPhonePromptDialog mDialog=new MyPhonePromptDialog(SendSuccessActivity.this, R.style.MyDialog, getResources().getString(R.string.prompt_send_success_phone));
			mDialog.setMySubmmitListener(new MyPhonePromptDialog.MySubmmitListener() {
				
				@Override
				public void summit(String phoneNumber) {
					
					Intent phoneIntent = new Intent("android.intent.action.CALL",Uri.parse("tel:"+getResources().getString(R.string.prompt_send_success_phone)));
					startActivity(phoneIntent);
				}
			});
			mDialog.show();
			
			break;
			
        case R.id.mContinueSendTv:   //回到“新寄件”页面
        	
        	Intent sendIntent=new Intent(SendSuccessActivity.this,SenderActivity.class);
        	sendIntent.putExtra("selectedIndex", 0);
        	startActivity(sendIntent);
        	
        	finish();
        	
        	break;
        case R.id.mSendHistoryTv:    //回到“历史寄件”页面
        	
        	Intent sendHistoryIntent=new Intent(SendSuccessActivity.this,SenderActivity.class);
        	sendHistoryIntent.putExtra("selectedIndex", 1);
        	startActivity(sendHistoryIntent);
        	
        	finish();
        	
        	break;

		default:
			break;
		}
	}

}
