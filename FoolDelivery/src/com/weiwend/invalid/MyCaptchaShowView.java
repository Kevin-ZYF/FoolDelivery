package com.weiwend.invalid;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.weiwend.fooldelivery.R;

//*****************************该界面由于需求问题，暂时已经作废*************************
public class MyCaptchaShowView extends FrameLayout {
	
	private LayoutInflater mInflater;
	private Context mContext;
	
	private ImageView mImageView;
	private ProgressBar mProgressBar;

	public MyCaptchaShowView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext=context;
		mInflater=(LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		
		init();
	}
	
	private void init(){
		
		View contentView=mInflater.inflate(R.layout.custom_my_captcha_show_view, this);//must write this
		
		mImageView=(ImageView)contentView.findViewById(R.id.mImageView);
		mImageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		mProgressBar=(ProgressBar)contentView.findViewById(R.id.mProgressBar);
		
	}
	
	public void setRefreshing(boolean refreshing){
		if(refreshing){
			mProgressBar.setVisibility(View.VISIBLE);
		}else{
			mProgressBar.setVisibility(View.GONE);
		}
	}
	
	public void setImageBitmap(Bitmap bitmap){
		mImageView.setImageBitmap(bitmap);
		mProgressBar.setVisibility(View.GONE);
	}
}
