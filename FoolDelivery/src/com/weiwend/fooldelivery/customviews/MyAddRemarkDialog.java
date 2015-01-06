package com.weiwend.fooldelivery.customviews;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.weiwend.fooldelivery.R;

//添加备注时的提示框
public class MyAddRemarkDialog extends Dialog implements android.view.View.OnClickListener{
	
	//上下文对象
	private Context mContext;
	
	//“确定”、“取消”按钮
	private Button mSummitBtn,mCancelBtn;
	
	//备注内容输入框
	private EditText mContentEt;
	
	//记录用户输入的备注信息
	private String rmrk;
	
	//点击"确定"按钮时的监听器
	public interface MySubmmitListener{
		void summit(String content);
	}
	
	//点击"确定"按钮时的监听器
	private MySubmmitListener mSubmmitListener;
	
	//设置点击"确定"按钮时的监听器
	public void setMySubmmitListener(MySubmmitListener mSubmmitListener){
		this.mSubmmitListener=mSubmmitListener;
	}

	public MyAddRemarkDialog(Context context, int theme, String rmrk) {
		super(context, theme);
		
		mContext=context;
		
		this.rmrk=rmrk;   //初始化备注信息
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.custom_dialog_query_result_add_remark);
		
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mCancelBtn=(Button)findViewById(R.id.mCancelBtn);
		mContentEt=(EditText)findViewById(R.id.mContentEt);
		
		mSummitBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
		
		if(rmrk!=null&&rmrk.length()>0){   //显示默认的备注信息
			mContentEt.setText(rmrk);
		}
		
		mContentEt.setSelection(mContentEt.getText().toString().length());
		
		//控制整个Dialog显示的大小
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
		
		case R.id.mSummitBtn:    //“确定”
			
			String content=mContentEt.getText().toString();
			
			if(mSubmmitListener!=null){    //回调“确定”按钮点击时的处理函数
				mSubmmitListener.summit(content);
			}
			
			break;
		case R.id.mCancelBtn:    //“取消”
			
			dismiss();
			
			break;

		default:
			break;
		}
	}
}
