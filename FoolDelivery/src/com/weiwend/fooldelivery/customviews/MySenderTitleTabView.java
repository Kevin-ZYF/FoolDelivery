package com.weiwend.fooldelivery.customviews;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weiwend.fooldelivery.R;

//寄件页面顶部的切换控件
public class MySenderTitleTabView extends LinearLayout {

	//上下文对象
	private Context mContext;
	
	//左、右操作选项的容器
	private LinearLayout mContainer0Layout,mContainer1Layout;
	
	//左、右操作选项
	private TextView mTabName0Tv,mTabName1Tv;
	
	//操作选项切换的监听器
	public interface MyOnNewTitleClickLister
	{
		void OnNewTitleClick(int index);
	};
	
	//操作选项切换的监听器
	private MyOnNewTitleClickLister mOnNewTitleClickLister;
	
	//设置操作选项切换的监听器
	public void setOnTitleClickListener(MyOnNewTitleClickLister mOnNewTitleClickLister)
	{
		this.mOnNewTitleClickLister=mOnNewTitleClickLister;
	}

	public MySenderTitleTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext=context;
		
		initViews();
	}
	
	/*public void setDatas(String str1,String str2){
		
		initViews();
	}*/
	
	//初始化ui
	private void initViews(){
		
		View contentView=LayoutInflater.from(mContext).inflate(R.layout.custom_new_send_title_view, this);
		
		mContainer0Layout=(LinearLayout)contentView.findViewById(R.id.mContainer0Layout);
		mContainer1Layout=(LinearLayout)contentView.findViewById(R.id.mContainer1Layout);
		
		mTabName0Tv=(TextView)contentView.findViewById(R.id.mTabName0Tv);
		mTabName1Tv=(TextView)contentView.findViewById(R.id.mTabName1Tv);
		
		mContainer0Layout.setOnClickListener(mOnClickListener);
		mContainer1Layout.setOnClickListener(mOnClickListener);
	}
	
	//监听tab的切换
    OnClickListener mOnClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			int choice=0;
			switch (view.getId()) {
			case R.id.mContainer0Layout:   //tab切换到左边
				choice=0;
				break;
			case R.id.mContainer1Layout:   //tab切换到右边
				choice=1;		
				break;
			default:
				break;
			}
			
			handleClickEffect(choice);   //tab切换后，更新ui
			
			if(mOnNewTitleClickLister!=null){  //回调切换后的监听器处理函数
				mOnNewTitleClickLister.OnNewTitleClick(choice);;
			}
		}
	};
	
	//更新tab切换后的ui
    private void handleClickEffect(int choice){
		
		if(choice==0){
			mContainer0Layout.setBackgroundColor(Color.parseColor("#ffffff"));
			mContainer1Layout.setBackgroundColor(Color.parseColor("#8A8A8B"));
			
			mTabName0Tv.setTextColor(Color.parseColor("#000000"));
			mTabName1Tv.setTextColor(Color.parseColor("#ffffff"));
		}else{
			mContainer0Layout.setBackgroundColor(Color.parseColor("#8A8A8B"));
			mContainer1Layout.setBackgroundColor(Color.parseColor("#ffffff"));
			
			mTabName0Tv.setTextColor(Color.parseColor("#ffffff"));
			mTabName1Tv.setTextColor(Color.parseColor("#000000"));
		}
	}
	
    //设置tab默认的选择项
	public void setCurItem(int index){
		
		handleClickEffect(index);
		
		if(mOnNewTitleClickLister!=null){     //回调切换后的监听器处理函数
			
			mOnNewTitleClickLister.OnNewTitleClick(index);
		}
	}

}

