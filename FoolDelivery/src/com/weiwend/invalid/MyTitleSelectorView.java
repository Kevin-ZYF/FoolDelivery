package com.weiwend.invalid;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.weiwend.fooldelivery.R;

//******************************该类由于需求问题，暂时已经作废*******************************
public class MyTitleSelectorView extends LinearLayout {
	
	private LayoutInflater mInflater;
	private Context mContext;
	
	private int optionsBgNormal[];
	
	private int optionsBgActive[];
	
	private int OPTION_NUM=3;
	private ImageView [] optionImageViews;
	private TextView [] optionTextViews;
	private LinearLayout [] optionContainers;
	
	private int preChoice=-1;
	
	public interface MyOnTabClickLister
	{
		void OnTabClick(int choice);
	};
	
	private MyOnTabClickLister mOnTabClickLister;
	
	public void setOnTabClickListener(MyOnTabClickLister onTabClickLister)
	{
		mOnTabClickLister=onTabClickLister;
	}

	public MyTitleSelectorView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		mContext=context;
		mInflater=(LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);

	}
	
	public void setDatas(int []iconNormalIds,int []iconActiveIds){
		optionsBgNormal=iconNormalIds;
		optionsBgActive=iconActiveIds;
		
		initViews();
	}
	
	private void initViews(){
		
		View contentView=mInflater.inflate(R.layout.custom_message_center_title_seletor, this);//must write this
		
		optionImageViews=new ImageView[OPTION_NUM];
		optionContainers=new LinearLayout[OPTION_NUM];
		optionTextViews=new TextView[OPTION_NUM];
		
		optionTextViews[0]=(TextView)contentView.findViewById(R.id.mTextview1);
		optionTextViews[1]=(TextView)contentView.findViewById(R.id.mTextview2);
		optionTextViews[2]=(TextView)contentView.findViewById(R.id.mTextview3);
		
		optionContainers[0]=(LinearLayout)contentView.findViewById(R.id.mContainer0Layout);
		optionContainers[1]=(LinearLayout)contentView.findViewById(R.id.mContainer1Layout);
		optionContainers[2]=(LinearLayout)contentView.findViewById(R.id.mContainer2Layout);
		
		optionContainers[0].setOnClickListener(mOnClickListener);
		optionContainers[1].setOnClickListener(mOnClickListener);
		optionContainers[2].setOnClickListener(mOnClickListener);
		
		for(int i=0;i<OPTION_NUM;i++){
			optionContainers[i].setBackgroundColor(Color.GRAY);
		}
	}
	
    OnClickListener mOnClickListener=new OnClickListener() {
		
		@Override
		public void onClick(View view) {
			int choice=0;
			switch (view.getId()) {
			case R.id.mContainer0Layout:
				choice=0;
				break;
			case R.id.mContainer1Layout:
				choice=1;		
				break;
			case R.id.mContainer2Layout:
				choice=2;
				break;
			default:
				break;
			}
			
			if(preChoice==choice){
				Log.e("zyf","not need handle click event...");
				
				return;
			}
			
			preChoice=choice;
			
			handleClickEffect(choice);
			
			if(mOnTabClickLister!=null){
				mOnTabClickLister.OnTabClick(choice);;
			}
		}
	};
	
    private void handleClickEffect(int choice){
		
		for(int i=0;i<OPTION_NUM;i++){
			if(i==choice){
				optionContainers[i].setBackgroundColor(getResources().getColor(R.color.orange_normal));
				optionTextViews[i].setTextColor(Color.WHITE);
			}else{
				optionContainers[i].setBackgroundColor(Color.parseColor("#F4F4F4"));
				optionTextViews[i].setTextColor(Color.GRAY);
			}
		}
	}
	
	public void setCurItem(int choice){
		
		if(preChoice==choice){
			Log.e("zyf","not need handle click event...");
			
			return;
		}
		preChoice=choice;
		
		handleClickEffect(choice);
		if(mOnTabClickLister!=null){
			mOnTabClickLister.OnTabClick(choice);
		}
	}
}
