package com.weiwend.fooldelivery.customviews;

import android.app.Dialog;
import android.content.Context;
import android.widget.TextView;

import com.weiwend.fooldelivery.R;

//自定义ProgressDialog
public class MyProgressDialog extends Dialog {
	
	//ProgressDialog中的显示内容
	private TextView mMsgTv;

	public MyProgressDialog(Context context, int theme) {
		super(context, theme);
		
        setContentView(R.layout.custom_progress_dialog);
        
		mMsgTv=(TextView)findViewById(R.id.mMsgTv);
	}

	
	//设置ProgressDialog中的显示内容
	public void setMessage(String msg){
		
		mMsgTv.setText(msg);
	}
}
