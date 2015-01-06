package com.weiwend.fooldelivery.customviews;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.weiwend.fooldelivery.R;

//显示省、市、区的名称按钮，该按钮右上方支持点击删除功能
public class MyDeletableBtn extends FrameLayout implements OnClickListener{
	
	private LayoutInflater mInflater;
	private Context mContext;
	
	//显示名称、删除
	private TextView mContentTv,mDeleteTv;
	
	//区别省、市、区
	private int type;
	public static int TYPE_PROVINCE=0;
	public static int TYPE_CITY=1;
	public static int TYPE_DISTRICT=2;
	
	//点击右上方删除的监听器
	public interface MyOnDeletedClickLister
	{
		void OnDeleted(int flag);
	};
	
	//点击右上方删除的监听器
	private MyOnDeletedClickLister mOnDeletedClickLister;
	
	//设置点击右上方删除的监听器
	public void setOnDeletedClickLister(MyOnDeletedClickLister mOnDeletedClickLister)
	{
		this.mOnDeletedClickLister=mOnDeletedClickLister;
	}

	public MyDeletableBtn(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext=context;
		mInflater=(LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
		
		initViews();
	}
	
	//初始化ui
	 private void initViews(){
		 View contentView=mInflater.inflate(R.layout.custom_deletable_btn, this);//must write this
		 
		 mContentTv=(TextView)contentView.findViewById(R.id.mContentTv);
		 
		 mDeleteTv=(TextView)contentView.findViewById(R.id.mDeleteTv);
		 mDeleteTv.setOnClickListener(this);
	 }
	 
	 //设置按钮的显示内容
	 public void setText(int id){
		 mContentTv.setText(getResources().getString(id));
		 
		 setVisibility(View.VISIBLE);
	 }
	 
	 //设置按钮的显示内容
	 public void setText(String str){
		 mContentTv.setText(str);
		 
		 setVisibility(View.VISIBLE);
	 }
	 
	 //设置按钮的类型(省、市、区)
	 public void setType(int type){
		this.type=type;
	 }

	@Override
	public void onClick(View view) {
		
		switch (view.getId()) {
		
		case R.id.mDeleteTv:     //用户点击删除功能
			
			setVisibility(View.INVISIBLE);
			
			if(mOnDeletedClickLister!=null){
				mOnDeletedClickLister.OnDeleted(type);
			}
			break;

		default:
			break;
		}
	}
}
