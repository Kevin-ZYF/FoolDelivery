package com.weiwend.fooldelivery.customviews;

import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.customviews.MyAddRemarkDialog.MySubmmitListener;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

//设置头像时的对话框
public class MyHeadImgPostSelectorDialog extends Dialog implements View.OnClickListener{

	//上下文对象
    private Context mContext;
	
    //“取消”按钮
	private Button mCancelBtn;
	
	//“相册”、“拍照”
	private TextView mSelectGalleryTv,mSelectCaptureTv;
	
	public static final int OPTION_CANCEL=0;   //用户选择了“取消”按钮
	public static final int OPTION_CAPTURE=1;   //用户选择了“相册”
	public static final int OPTION_GALLERY=2;   //用户选择了“拍照”
	
	//用户选择的监听器
	public interface MyOptionClickListener{
		void optionClick(int option);
	}
	
	//用户选择的监听器
	private MyOptionClickListener mOptionClickListener;
	
	//设置用户选择的监听器
	public void setMyOptionClickListener(MyOptionClickListener mOptionClickListener){
		this.mOptionClickListener=mOptionClickListener;
	}

	public MyHeadImgPostSelectorDialog(Context context, int theme) {
		super(context, theme);
		
		mContext=context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.custom_dialog_head_img_post_selector);
		
		mCancelBtn=(Button)findViewById(R.id.mCancelBtn);
		
		mCancelBtn.setOnClickListener(this);
		
		mSelectGalleryTv=(TextView)findViewById(R.id.mSelectGalleryTv);
		mSelectCaptureTv=(TextView)findViewById(R.id.mSelectCaptureTv);
		
		mSelectGalleryTv.setOnClickListener(this);
		mSelectCaptureTv.setOnClickListener(this);
		
		//控制整个Dialog显示的大小
		WindowManager.LayoutParams  lp=getWindow().getAttributes();
		WindowManager wm=(WindowManager)mContext.getSystemService(mContext.WINDOW_SERVICE);
        int width=wm.getDefaultDisplay().getWidth();
        lp.width=width-100;
        getWindow().setAttributes(lp);
	}

	@Override
	public void onClick(View view) {
		
		
		dismiss();
		
		switch (view.getId()) {
		
		case R.id.mCancelBtn:   //“取消”
			
			if(mOptionClickListener!=null){   //回调用户选择监听器的处理函数
				mOptionClickListener.optionClick(OPTION_CANCEL);
			}
			
			break;
		case R.id.mSelectGalleryTv:   //“相册”
			
			if(mOptionClickListener!=null){    //回调用户选择监听器的处理函数
				mOptionClickListener.optionClick(OPTION_GALLERY);
			}
					
			break;
		case R.id.mSelectCaptureTv:   //“拍照”
			
			if(mOptionClickListener!=null){    //回调用户选择监听器的处理函数
				mOptionClickListener.optionClick(OPTION_CAPTURE);
			}
			
			break;

		default:
			break;
		}
	}
}
