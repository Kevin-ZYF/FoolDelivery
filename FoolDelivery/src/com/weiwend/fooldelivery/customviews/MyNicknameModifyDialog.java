package com.weiwend.fooldelivery.customviews;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.weiwend.fooldelivery.R;

public class MyNicknameModifyDialog extends Dialog implements android.view.View.OnClickListener{
	
	private Context mContext;
	
	private Button mSummitBtn,mCancelBtn;
	
	private EditText mContentEt;
	
	private String defaultNickName;
	
	public interface MySubmmitListener{
		void summit(String content);
	}
	
	private MySubmmitListener mSubmmitListener;
	
	public void setMySubmmitListener(MySubmmitListener mSubmmitListener){
		this.mSubmmitListener=mSubmmitListener;
	}

	public MyNicknameModifyDialog(Context context, int theme) {
		super(context, theme);
		
		mContext=context;
	}
	
	public MyNicknameModifyDialog(Context context, int theme, String defaultNickName) {
		super(context, theme);
		
		mContext=context;
		
		this.defaultNickName=defaultNickName;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.custom_dialog_nickname_modify);
		
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mCancelBtn=(Button)findViewById(R.id.mCancelBtn);
		
		mContentEt=(EditText)findViewById(R.id.mContentEt);
		if(defaultNickName!=null&&defaultNickName.length()>0){
			mContentEt.setText(defaultNickName);
			mContentEt.setSelection(mContentEt.getText().toString().length());
		}
		
		mSummitBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
		
		WindowManager.LayoutParams  lp=getWindow().getAttributes();
		WindowManager wm=(WindowManager)mContext.getSystemService(mContext.WINDOW_SERVICE);
        int width=wm.getDefaultDisplay().getWidth();
        lp.width=width-100;
        lp.height=width*7/12;
        getWindow().setAttributes(lp);
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.mSummitBtn:
			
			String content=mContentEt.getText().toString();
			
			if(mSubmmitListener!=null){
				mSubmmitListener.summit(content);
			}
			
			break;
		case R.id.mCancelBtn:
			
			dismiss();
			
			break;

		default:
			break;
		}
	}
}

