package com.weiwend.fooldelivery.customviews;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.customviews.MyPhonePromptDialog.MySubmmitListener;

//删除操作前的提示框
public class MyDeleteDialog extends Dialog implements android.view.View.OnClickListener{
	
	//上下文对象
	private Context mContext;
	
	//“确定”、“取消”按钮
	private Button mSummitBtn,mCancelBtn;
	
	//点击"确定"按钮时的监听器
	public interface MySubmmitListener{
		void delete();
	}
	
	//点击"确定"按钮时的监听器
	private MySubmmitListener mSubmmitListener;
	
	//设置点击"确定"按钮时的监听器
	public void setMySubmmitListener(MySubmmitListener mSubmmitListener){
		this.mSubmmitListener=mSubmmitListener;
	}

	public MyDeleteDialog(Context context, int theme) {
		super(context, theme);
		
		mContext=context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.custom_dialog_delete);
		
		mSummitBtn=(Button)findViewById(R.id.mSummitBtn);
		mCancelBtn=(Button)findViewById(R.id.mCancelBtn);
		
		mSummitBtn.setOnClickListener(this);
		mCancelBtn.setOnClickListener(this);
		
		//控制整个Dialog显示的大小
		WindowManager.LayoutParams  lp=getWindow().getAttributes();
		WindowManager wm=(WindowManager)mContext.getSystemService(mContext.WINDOW_SERVICE);
        int width=wm.getDefaultDisplay().getWidth();
        lp.width=width-100;
        getWindow().setAttributes(lp);
	}

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		
		case R.id.mSummitBtn:    //“确定”
			
			if(mSubmmitListener!=null){     //回调“确定”按钮点击时的处理函数
				mSubmmitListener.delete();
			}
			
			dismiss();
			
			break;
		case R.id.mCancelBtn:   //“取消”
			
			dismiss();
			
			break;
		default:
			break;
		}
	}
}


