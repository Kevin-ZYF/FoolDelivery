package com.weiwend.fooldelivery.customviews;

import com.weiwend.fooldelivery.R;
import com.weiwend.fooldelivery.customviews.MyTabView.MyOnTabClickLister;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

//自定义标题tab切换控件
public class MyTitleTabView extends LinearLayout {

	//上下文对象
	private Context mContext;
	
	//tab控件操作项的数目
	private int OPTION_NUM=2;
	
	//tab控件每个操作项的外围容器
	private LinearLayout [] optionContainers;
	
	//tab控件每个操作项的名称
	private TextView [] optionTvs;
	
	//第一个、第二个操作项的名称
	private TextView mTabName1,mTabName2;
	
	//橙色、白色颜色值
	private int orangeColor,whiteColor;
	
	//tab切换的监听器
	public interface MyOnTitleClickLister
	{
		void OnTitleClick(int index);
	};
	
	//tab切换的监听器
	private MyOnTitleClickLister mOnTitleClickLister;
	
	//设置tab切换的监听器
	public void setOnTitleClickListener(MyOnTitleClickLister mOnTitleClickLister)
	{
		this.mOnTitleClickLister=mOnTitleClickLister;
	}

	public MyTitleTabView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext=context;
		
		whiteColor=Color.WHITE;
		orangeColor=Color.parseColor("#ED6900");
		
		initViews();
	}
	
	//设置第一个，第二个操作项的名称
    public void setTabNames(String str1,String str2){
		
    	mTabName1.setText(str1);
    	mTabName2.setText(str2);
	}
	
    //初始化ui
	private void initViews(){
		
		View contentView=LayoutInflater.from(mContext).inflate(R.layout.custom_sender_title_view, this);
		
		optionContainers=new LinearLayout[OPTION_NUM];
		optionTvs=new TextView[OPTION_NUM];
		
		optionContainers[0]=(LinearLayout)contentView.findViewById(R.id.mContainer0Layout);
		optionContainers[1]=(LinearLayout)contentView.findViewById(R.id.mContainer1Layout);
		
		optionTvs[0]=(TextView)contentView.findViewById(R.id.mTabName1);
		optionTvs[1]=(TextView)contentView.findViewById(R.id.mTabName2);
		
		optionContainers[0].setOnClickListener(mOnClickListener);
		optionContainers[1].setOnClickListener(mOnClickListener);
		
		mTabName1=(TextView)contentView.findViewById(R.id.mTabName1);
		mTabName2=(TextView)contentView.findViewById(R.id.mTabName2);
	}
	
	//监听tab切换
    OnClickListener mOnClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			int choice=0;
			switch (view.getId()) {
			case R.id.mContainer0Layout:     //tab切换到第1个操作项
				choice=0;
				break;
			case R.id.mContainer1Layout:     //tab切换到第2个操作项
				choice=1;		
				break;
			default:
				break;
			}
			
			handleClickEffect(choice);    //tab切换后更新ui
			
			if(mOnTitleClickLister!=null){   //回调tab切换监听器的处理函数
				mOnTitleClickLister.OnTitleClick(choice);;
			}
		}
	};
	
	//tab切换后更新ui
    private void handleClickEffect(int choice){
		
		for(int i=0;i<OPTION_NUM;i++){
			
			if(i==choice){    //tab切换到第1个操作项
				
				optionContainers[i].setBackgroundColor(orangeColor);
				optionTvs[i].setTextColor(whiteColor);
				
			}else{    //tab切换到第2个操作项
				
				optionContainers[i].setBackgroundColor(whiteColor);
				optionTvs[i].setTextColor(orangeColor);
			}
		}
	}
	
    //设置tab默认的选择项
	public void setCurItem(int index){
		handleClickEffect(index);
		if(mOnTitleClickLister!=null){
			mOnTitleClickLister.OnTitleClick(index);
		}
	}

}
