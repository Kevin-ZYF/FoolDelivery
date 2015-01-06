package com.weiwend.fooldelivery.customviews;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.weiwend.fooldelivery.R;

//版本更新的提示框
public class MyAppUpdatePromptDialog extends Dialog implements android.view.View.OnClickListener{
		
	//上下文对象
	private Context mContext;
	
	//“确定”、“取消”按钮
	private Button mSummitBtn,mCancelBtn;
	
	//对话框中显示内容控件
	private TextView mContentTv;
	
	//对话框中显示的内容
	private String content;
	
	//点击"确定"按钮时的监听器
	public interface MySubmmitListener{
		void summit(String phoneNumber);
	}
	
	//点击"确定"按钮时的监听器
	private MySubmmitListener mSubmmitListener;
	
	//设置点击"确定"按钮时的监听器
	public void setMySubmmitListener(MySubmmitListener mSubmmitListener){
		this.mSubmmitListener=mSubmmitListener;
	}

	public MyAppUpdatePromptDialog(Context context, int theme) {
		super(context, theme);
		
		mContext=context;
	}
	
	public MyAppUpdatePromptDialog(Context context, int theme, String content) {
		this(context, theme);
		
		this.content=content;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.custom_dialog_delete);
		
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mCancelBtn=(Button)findViewById(R.id.mCancelBtn);
		
		mSummitBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
		
		TextView mTitleTv=(TextView)findViewById(R.id.mTitleTv);
		mTitleTv.setText("版本更新");
		
		mContentTv=(TextView)findViewById(R.id.mContentTv);
		mContentTv.setText(content);
		
		//控制整个Dialog显示的大小
		WindowManager.LayoutParams  lp=getWindow().getAttributes();
		WindowManager wm=(WindowManager)mContext.getSystemService(mContext.WINDOW_SERVICE);
        int width=wm.getDefaultDisplay().getWidth();
        lp.width=width*4/5;
        getWindow().setAttributes(lp);
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		case R.id.mSummitBtn:    //“确定”
			
			if(mSubmmitListener!=null){    //回调“确定”按钮点击时的处理函数
				mSubmmitListener.summit(content);
			}
			
			dismiss();
			
			break;
		case R.id.mCancelBtn:    //“取消”
			
			dismiss();
			
			break;
		default:
			break;
		}
	}

}
