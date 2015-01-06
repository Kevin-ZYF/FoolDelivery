package com.weiwend.fooldelivery.utils;

import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.customviews.MyProgressDialog;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

//耗时操作时的loading对话框
public class MyProgressDialogUtils implements OnCancelListener{
	
	//<modify by Yongfeng.zhang 2014.11.12
	//private ProgressDialog mProgressDialog;
	private MyProgressDialog mProgressDialog;
	//>end by Yongfeng.zhang 2014.11.12
	
	//对话框中显示的提示语
	private int message;
	
	private Context mContext;
	
	//用户取消对话框时的回调接口
	public interface MyProgressDialogCanceledListener{
		void canceled();
	}
	
	private MyProgressDialogCanceledListener mProgressDialogCanceledListener;
	
	//设置对话框取消的监听器
	public void setMyProgressDialogCanceledListener(MyProgressDialogCanceledListener mProgressDialogCanceledListener){
		
		this.mProgressDialogCanceledListener=mProgressDialogCanceledListener;
	}
	
	public MyProgressDialogUtils(int message, Context mContext) {
		super();
		this.message = message;
		this.mContext = mContext;
		
		//<modify by Yongfeng.zhang 2014.11.12
		//mProgressDialog=new ProgressDialog(mContext);
		mProgressDialog=new MyProgressDialog(mContext,R.style.MyProgressDialog);
		//>end by Yongfeng.zhang 2014.11.12
		
		
		mProgressDialog.setMessage(mContext.getString(message));
		mProgressDialog.setCancelable(false);
		//mProgressDialog.setCanceledOnTouchOutside(true);
		
		mProgressDialog.setOnCancelListener(this);
	}
	
	//设置用户是否可以取消对话框
	public void setProgressDialogCancelable(){
		mProgressDialog.setCancelable(true);
		mProgressDialog.setOnCancelListener(new OnCancelListener() {
			
			@Override
			public void onCancel(DialogInterface arg0) {
				
				if(mProgressDialogCanceledListener!=null){
					mProgressDialogCanceledListener.canceled();
				}
			}
		});
	}
	
	//显示对话框
	public void showDialog(){
		mProgressDialog.show();
	}
	
	//使对话框消失
	public void dismissDialog(){
		
		if(mProgressDialog!=null){
			mProgressDialog.dismiss();
		}
	}

	//取消对话框，回调取消接口
	@Override
	public void onCancel(DialogInterface arg0) {
		
		if(mProgressDialogCanceledListener!=null){
			mProgressDialogCanceledListener.canceled();
		}
	}

}
